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
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

val data = spark.read.option("inferSchema", true).option("header", true).csv("./heart_2020_cleaned.csv")
data.show
data.createOrReplaceTempView("heart_disease")

var text = spark.sql("SELECT * from heart_disease")

val sampleSize = 0.01 // use 1 percent sample size for debugging!

val categoricalCols = Array("Smoking", "AlcoholDrinking", "Sex", "Stroke", "AgeCategory", "Race", "Diabetic", "GenHealth", "Asthma", "PhysicalActivity", "KidneyDisease", "SkinCancer")
val numericalCols = Array("BMI", "PhysicalHealth", "MentalHealth", "SleepTime")

val stringIndexer = categoricalCols.map(col => new StringIndexer()
  .setInputCol(col)
  .setOutputCol(s"${col}_indexed")
  .fit(data))

// Create a string indexer for the label column
val labelIndexer = new StringIndexer()
  .setInputCol("HeartDisease")
  .setOutputCol("label")
  .fit(data)

val Array(trainData, testData) = data.randomSplit(Array(0.9, 0.1))

val assembler = new VectorAssembler()
  .setInputCols(numericalCols)
  .setOutputCol("features")

 // train the DecisionTree model
 val decisionTree = new DecisionTreeClassifier().setLabelCol("label").setFeaturesCol("features")

 val pipeline = new Pipeline().setStages(stringIndexer.toArray ++ Array(assembler, labelIndexer, decisionTree))
 val model = pipeline.fit(trainData)

 // make predictions on the test data
 val predictions = model.transform(testData)

// evaluate the model
val evaluator = new BinaryClassificationEvaluator()
  .setLabelCol("label")
  .setRawPredictionCol("rawPrediction")
  .setMetricName("areaUnderROC")

val accuracy = evaluator.evaluate(predictions)
//Test error
//Low = good
//High = bad
println("Test Error = " + (1.0 - accuracy))

// ---
// Define the hyper-parameter ranges
val paramGrid = new ParamGridBuilder()
  .addGrid(decisionTree.impurity, Array("entropy", "gini"))
  .addGrid(decisionTree.maxDepth, Array(4, 8, 12))
  .addGrid(decisionTree.maxBins, Array(10, 20, 50))
  .build()

// Define a CrossValidator with 4-fold cross-validation
val cv = new CrossValidator()
  .setEstimator(pipeline)
  .setEvaluator(evaluator)
  .setEstimatorParamMaps(paramGrid.build())
  .setNumFolds(4)

val cvModel = cv.fit(trainData)

// Evaluate the best model on the test set
val predictions = cvModel.transform(testData)
val accuracy = evaluator.evaluate(predictions)
println("Test Error = " + (1.0 - accuracy))

val bestModel = cvModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[DecisionTreeClassificationModel]

val predictions = bestModel.transform(testData)

val metrics = new BinaryClassificationMetrics(predictions.select("rawPrediction", "label").rdd.map(x => (x(0).asInstanceOf[DenseVector](1), x.getDouble(1))))

println("Accuracy = " + metrics.areaUnderROC())

bestModel.write.overwrite().save("./bestmodel")
