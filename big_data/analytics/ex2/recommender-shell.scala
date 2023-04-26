// Reduce some of the debugging output of Spark
import org.apache.log4j.Logger
import org.apache.log4j.Level

Logger.getLogger("org").setLevel(Level.ERROR)
Logger.getLogger("akka").setLevel(Level.ERROR)

// Import the basic recommender libraries from Spark's MLlib package
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation._

val sampleSize = 0.01 // again use 1 percent sample size for debugging!
val rawArtistAlias = sc.textFile("../Data/audioscrobbler/artist_alias.txt")
val rawArtistData = sc.textFile("../Data/audioscrobbler/artist_data.txt")
val rawUserArtistData = sc.textFile("../Data/audioscrobbler/user_artist_data.txt").sample(false, sampleSize)

rawUserArtistData.map(_.split(' ')(0).toDouble).stats()
rawUserArtistData.map(_.split(' ')(1).toDouble).stats()

val artistByID = rawArtistData.flatMap { 
  line => val (id, name) = line.span(_ != '\t')
  if (name.isEmpty) { 
    None 
  } else { 
    try { 
      Some((id.toInt, name.trim)) } 
    catch { case e: NumberFormatException => None } 
  } 
} 

val artistAlias = rawArtistAlias.flatMap { line => 
  val tokens = line.split('\t') 
  if (tokens(0).isEmpty) { 
    None 
  } else {
    Some((tokens(0).toInt, tokens(1).toInt)) 
  } 
}.collectAsMap()

// Some lookups
artistByID.lookup(6803336).head
artistByID.lookup(1000010).head

// Broadcast the local aliases map since it is going to be part of the closure of our training function
val bArtistAlias = sc.broadcast(artistAlias)

// Prepare and cache the training data
val trainData = rawUserArtistData.map { 
  line => val Array(userID, artistID, count) = 
    line.split(' ').map(_.toInt) 
    val finalArtistID = bArtistAlias.value.getOrElse(artistID, artistID) 
    Rating(userID, finalArtistID, count) 
}.cache()
trainData.first // show first entry

// And train the recommender model using Spark's ALS algorithm
val model = ALS.trainImplicit(trainData, 10, 5, 0.01, 1.0) 

// Inspect the model: show the first row in the X^T matrix of the resulting factorization
model.userFeatures.mapValues(_.mkString(", ")).first

// Pick the first user from the training data
val someUser = trainData.map(x => x.user).first

// Show actual artists played by some user
def actualArtistsForUser(someUser: Int): Set[Int] = {
  val rawArtistsForUser = rawUserArtistData.map(_.split(' ')).
    filter { case Array(user,_,_) => user.toInt == someUser }
  rawArtistsForUser.map(x => x(1).toInt).collect().toSet
}
actualArtistsForUser(someUser)

// Print the artist name together with the weight our model would predict for each artist this user listened to
for (artistID <- actualArtistsForUser(someUser))
  println(artistByID.lookup(bArtistAlias.value.getOrElse(artistID, artistID))(0) + ": " + model.predict(someUser, artistID))

// for (artistID <- actualArtistsForUser)
//   println(artistByID.lookup(bArtistAlias.value.getOrElse(artistID, artistID))(0) + ": " + model.predict(someUser, artistID))

// Next fetch the top 20 recommendations for this user from our model
val recommendations = model.recommendProducts(someUser, 20)

// Raw output
recommendations.foreach(println)

// And by resolving the aliases and actual artist names
for (r <- recommendations)
  println(r.user + " - " + r.product + ": " + artistByID.lookup(bArtistAlias.value.getOrElse(r.product, 
    r.product))(0) + ": " + r.rating)

// Compare the results to a baseline model which simply recommends the same most popular artists to each user
val artistsTotalCount = trainData.map(r => (r.product, r.rating)).reduceByKey(_ + _).collect().sortBy(-_._2)
def predictMostPopular(user: Int, numArtists: Int) = { 
  val topArtists = artistsTotalCount.take(numArtists)
  topArtists.map{case (artist, rating) => Rating(someUser, artist, rating)}
}

// Use the new prediction function instead of 'model.recommendProducts(someUser, 20)'
val recommendations2 = predictMostPopular(someUser, 20)

// Raw output
recommendations2.foreach(println)

// And by resolving the aliases and actual artist names
for (r <- recommendations2)
  println(r.user + ": " + artistByID.lookup(bArtistAlias.value.getOrElse(r.product, 
    r.product))(0) + ": " + r.rating)

// Batch recommendations for multiple users
val someUsers = trainData.map(x => x.user).distinct().take(10)  
val someRecommendations = someUsers.map(userID =>   
    model.recommendProducts(userID, 5)) 
  someRecommendations.map(recs => recs.head.user + " -> " +   
    recs.map(_.product).mkString(", ")).foreach(println)

// Receiver-Operator-Characteristic (ROC) & Area-Under-the-Curve (AUC) (both averaged over some users)

import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics

var AUC1 = 0.0
var AUC2 = 0.0

for (someUser <- someUsers) {
  
  val actualArtists = actualArtistsForUser(someUser)

  val recommendations1 = model.recommendProducts(someUser, 100)
  val predictionsAndLabels1 = recommendations1.map { 
    case Rating(user, artist, rating) =>
      if (actualArtists.contains(artist)) {
        (rating, 1.0)
      } else {
        (rating, 0.0)
      }
  }

  val metrics1 = new BinaryClassificationMetrics(sc.parallelize(predictionsAndLabels1))
  AUC1 += metrics1.areaUnderROC

  val recommendations2 = predictMostPopular(someUser, 100)
  val predictionsAndLabels2 = recommendations2.map { 
    case Rating(user, artist, rating) =>
      if (actualArtists.contains(artist)) {
        (rating, 1.0)
      } else {
        (rating, 0.0)
      }
  }

  val metrics2 = new BinaryClassificationMetrics(sc.parallelize(predictionsAndLabels2))
  AUC2 += metrics2.areaUnderROC
}

println("ALS-Recommender AUC: " + (AUC1/10.0))
println("Most-Popular AUC:    " + (AUC2/10.0))




