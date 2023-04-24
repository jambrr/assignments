// import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.ml.feature.HashingTF
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import scala.util.Random
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Column
import org.apache.spark.sql.Row

//Problem 1
//(a)
val dataframe = spark.read.option("inferSchema", true).option("header", true).csv("./stockerbot-export.csv")
dataframe.show
dataframe.createOrReplaceTempView("stockerbot")

var text = spark.sql("SELECT text from stockerbot")

//(bi)
//val counts1 = text.map(r => r.getString(0).replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase())
//val counts2 = counts1.filter(x => x.length > 5).filter(x => x.length < 25)

val pattern = "\\b[a-z]{5,25}\\b"
val normalize = udf((text: String) => pattern.r.findAllIn(text.toLowerCase).toList)

val tweets_normalized = dataframe.withColumn("words", normalize(col("text")))

//(bii)

// Map all email text to vectors of 100 features/dimensions  
val tf = new HashingTF().setNumFeatures(100).setInputCol("words").setOutputCol("features")

val neg_tweets = List(
    "1019010170272264200",
    "1017154360357183500",
    "1019617300683608000",
    "1018635849326579700",
    "1019603140113522700",
    "1019699298701733900",
    "1019701129930657800",
    "1019719472695787500",
    "1017844683039334400",
    "1019244541029879800",
    "1019689041053343700",
    "1019708519627489300",
    "1019721495302557700",
    "1019726630355374100",
    "1016438057229324300",
    "1016756686269251600",
    "1017048700361855000",
    "1017359034410852400",
    "1019314472564002800",
    "1019594144539467800",
    "1019615192936779800",
    "1019615446968832000",
    "1019623692266033200",
    "1019675876793966600",
    "1017782768615469000",
    "1017835110509482000",
    "1019233887371825200"
)

val pos_tweets = List(
    "1019335858652999700",
    "1019339743799267300",
    "1017822633876848600",
    "1017866466765484000",
    "1017876748241793000",
    "1018078212402942000",
    "1018138345979998200",
    "1018158737972842500",
    "1018281827558219800",
    "1018299200981864400",
    "1018318032211456000",
    "1018332085042532400",
    "1018434574144196600",
    "1019326470659993600",
    "1019394161986940900",
    "1019401947043061800",
    "1019474266071461900",
    "1019584696085336000",
    "1019664250275541000",
    "1019694240207601700",
    "1019716131991556100",
    "1019609411650707500",
    "1019251887562948600",
    "1019571763473240000",
    "1019670834338648000", 
)

val schema = StructType(
  Seq(
    StructField("id", LongType, nullable = false),
    StructField("text", StringType, nullable = false),
    StructField("timestamp", StringType, nullable = false),
    StructField("source", StringType, nullable = false),
    StructField("symbols", StringType, nullable = true),
    StructField("company_names", StringType, nullable = true),
    StructField("url", StringType, nullable = true),
    StructField("verified", StringType, nullable = false),
    StructField("words", ArrayType(StringType, containsNull = false), nullable = true),
    StructField("label", IntegerType, nullable = true)
  )
)

var negDF = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
var posDF = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)

for(id <- neg_tweets){ 
    val tweet = tweets_normalized.filter("id == "+id).withColumn("label", lit(-1))
    negDF = negDF.union(tweet)
}

for(id <- pos_tweets){
    val tweet = tweets_normalized.filter("id == "+id).withColumn("label", lit(1))
    posDF = posDF.union(tweet)
}

// pos: Create label
val data_pos_feature = tf.transform(posDF).withColumn("label", lit(1))

// neg: Create label
val data_neg_feature = tf.transform(negDF).withColumn("label", lit(-1))

// Use the union of both as training data 
val trainingData = data_pos_feature.union(data_neg_feature) 

// Run Linear Regression
val lr = new LinearRegression()
  .setLabelCol("label")
  .setFeaturesCol("features")
val model = lr.fit(trainingData)

// Test on a positive example (spam) and a negative one (normal).  
val pos_input = Seq("Your report looks good, good jo".split(" ")).toDF("words")
val posTest = tf.transform(pos_input)

val neg_input = Seq("You are being so stupid".split(" ")).toDF("words")
val negTest = tf.transform(neg_input)

val pos_predictions = model.transform(posTest)
val neg_predictions = model.transform(negTest)

// Finally show the results   
pos_predictions.show()
neg_predictions.show()

// Get the predicted value from the DataFrame
//val pos_predictions_value = pos_predictions.select("prediction").head.getDouble(0)
//val neg_predictions_value = neg_predictions.select("prediction").head.getDouble(0)
//
//// Print the predicted value
//println("Sentence: Viagra GET cheap stuff by sending money to ... \n" + "Prediction: " + pos_predictions_value)
//println("Sentence: Hi Dad, I started studying Spark the other day.\n" + "Prediction: " + neg_predictions_value)
