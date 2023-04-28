import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression._
import org.apache.spark.mllib.tree._
import org.apache.spark.mllib.tree.model._
import org.apache.spark.rdd._
import org.apache.spark.mllib.evaluation._
import org.apache.spark.ml.feature.{StringIndexer, HashingTF, IndexToString, VectorIndexer, VectorAssembler}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.ml.{Pipeline, PipelineModel}

val data = spark.read.option("inferSchema", true).option("header", true).csv("./heart_2020_cleaned.csv")
data.show
data.createOrReplaceTempView("heart_disease")

var text = spark.sql("SELECT * from heart_disease")

val sampleSize = 0.01 // use 1 percent sample size for debugging!

val categoricalColumns = Array("HeartDisease","Smoking", "AlcoholDrinking", "Stroke", "DiffWalking", "Sex", "AgeCategory", "Race", "Diabetic", "PhysicalActivity", "GenHealth", "Asthma", "KidneyDisease", "SkinCancer")
val indexers = categoricalColumns.map(col => new StringIndexer().setInputCol(col).setOutputCol(col + "_index"))
val indexed = new Pipeline().setStages(indexers).fit(data).transform(data)

// combine features into a single feature vector
 val assembler = new VectorAssembler().setInputCols(Array(Heart"BMI", "Smoking_index", "AlcoholDrinking_index", "Stroke_index", "PhysicalHealth", "MentalHealth", "DiffWalking_index", "Sex_index", "AgeCategory_index", "Race_index", "Diabetic_index", "PhysicalActivity_index", "GenHealth_index", "SleepTime", "Asthma_index", "KidneyDisease_index", "SkinCancer_index")).setOutputCol("features")
 val assembledData = assembler.transform(indexed).select("features", "HeartDisease")

 // split the data into training and testing sets
 val Array(trainData, testData) = assembledData.randomSplit(Array(0.9, 0.1), seed = 1234)

 // train the DecisionTree model
 val decisionTree = new DecisionTreeClassifier().setLabelCol("HeartDisease").setFeaturesCol("features")
 val pipeline = new Pipeline().setStages(Array(decisionTree))
 val model = pipeline.fit(trainData)

 // make predictions on the test data
 val predictions = model.transform(testData)

// evaluate the model
 val evaluator = new org.apache.spark.ml.evaluation.BinaryClassificationEvaluator().setLabelCol("HeartDisease")
 val accuracy = evaluator.evaluate(predictions)
 println("Accuracy: " + accuracy)

val model = pipline.fit(trainData)
model.show()
// --- train a first model ---

val model = DecisionTree.trainClassifier(trainData, 2, Map[Int,Int](), "gini", 4, 100)

println(model.toDebugString)

def getMetrics(model: DecisionTreeModel, data: RDD[LabeledPoint]):
  MulticlassMetrics = {
    val predictionsAndLabels = data.map(example =>
      (model.predict(example.features), example.label)
    )
    new MulticlassMetrics(predictionsAndLabels)
  }

val metrics = getMetrics(model, valData)

(0 until 7).map(
  label => (metrics.precision(label), metrics.recall(label))
).foreach(println)

def classProbabilities(data: RDD[LabeledPoint]): Array[Double] = {
  val countsByCategory = data.map(_.label).countByValue()
  val counts = countsByCategory.toArray.sortBy(_._1).map(_._2)
  counts.map(_.toDouble / counts.sum)
}

// --- compare the model to a naive prediction based on prior class probabilites ---

val trainPriorProbabilities = classProbabilities(trainData)
val valPriorProbabilities = classProbabilities(valData)
trainPriorProbabilities.zip(valPriorProbabilities).map {
  case (trainProb, valProb) => trainProb * valProb
}.sum

//// --- optimize the hyperparameters of the model --- 
//
//val evaluations =
//  for (impurity <- Array("gini", "entropy");
//    depth <- Array(10, 20, 30);
//    bins <- Array(50, 100, 300))
//  yield {
//    val model = DecisionTree.trainClassifier(
//      trainData, 7, Map[Int,Int](), impurity, depth, bins)
//    val predictionsAndLabels = valData.map(example =>
//      (model.predict(example.features), example.label)
//    )  
//    val accuracy =
//      new MulticlassMetrics(predictionsAndLabels).accuracy
//    ((impurity, depth, bins), accuracy) }
//
//evaluations.sortBy(_._2).reverse.foreach(println)
//
//// --- improve the feature vectors by replacing the "1-hot" encoding --- 
//
//val data2 = rawData.map { line =>
//  val values = line.split(',').map(_.toDouble)
//  val wilderness = values.slice(10, 14).indexOf(1.0).toDouble
//  val soil = values.slice(14, 54).indexOf(1.0).toDouble
//  val featureVector =
//    Vectors.dense(values.slice(0, 10) :+ wilderness :+ soil)
//  val label = values.last - 1
//  LabeledPoint(label, featureVector)
//}
//  
//val Array(trainData2, valData2, testData2) =
//  data2.randomSplit(Array(0.8, 0.1, 0.1))
//
//val evaluations2 =
//  for (impurity <- Array("gini", "entropy");
//    depth <- Array(20, 30);
//    bins <- Array(200, 300))
//  yield {
//    val model2 = DecisionTree.trainClassifier(
//      trainData2, 7, Map(10 -> 4, 11 -> 40),
//      impurity, depth, bins)
//    val trainAccuracy = getMetrics(model2, trainData2).accuracy
//    val valAccuracy = getMetrics(model2, valData2).accuracy
//    ((impurity, depth, bins), (trainAccuracy, valAccuracy)) }
//  
//evaluations2.sortBy(_._2).reverse.foreach(println)
//
//val model2 = DecisionTree.trainClassifier(
//      trainData2, 7, Map(10 -> 4, 11 -> 40),
//      "gini", 30, 300)
//
//val testAccuracy = getMetrics(model2, testData2).accuracy
//
//// --- finally train a random forest based on the improved vectors --- 
//
//val forest = RandomForest.trainClassifier(
//  trainData2.union(valData2), 7, Map(10 -> 4, 11 -> 40), 20,
//    "auto", "entropy", 30, 300)
//    
//val input = "2709,125,28,67,23,3224,253,207,61,6094,0,29"
//val vector = Vectors.dense(input.split(',').map(_.toDouble))
//
//forest.predict(vector)
