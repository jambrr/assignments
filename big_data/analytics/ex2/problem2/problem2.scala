import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression._
import org.apache.spark.mllib.tree._
import org.apache.spark.mllib.tree.model._
import org.apache.spark.rdd._
import org.apache.spark.mllib.evaluation._
import org.apache.spark.ml.feature.{StringIndexer, HashingTF, IndexToString, VectorIndexer, VectorAssembler}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

// (a)
val data = spark.read.option("inferSchema", true).option("header", true).csv("./heart_2020_cleaned.csv")
data.show
data.createOrReplaceTempView("heart_disease")

// (b)
// (i)
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

//Split the data
val Array(trainData, testData) = data.randomSplit(Array(0.9, 0.1), seed=1234)

// (ii)
val assembler = new VectorAssembler()
  .setInputCols(numericalCols)
  .setOutputCol("features")

 val decisionTree = new DecisionTreeClassifier().setLabelCol("label").setFeaturesCol("features")

 val pipeline = new Pipeline().setStages(stringIndexer.toArray ++ Array(assembler, labelIndexer, decisionTree))
 val model = pipeline.fit(trainData)

 // make predictions on the test data
 val predictions = model.transform(testData)

// (iii)
// evaluate the model
val evaluator = new BinaryClassificationEvaluator()
  .setLabelCol("label")
  .setRawPredictionCol("prediction")
  .setMetricName("areaUnderROC")

//Train the model using the training data
val trainedModel = pipeline.fit(trainData)

val accuracy = evaluator.evaluate(predictions)
//Test error
//Low = good
//High = bad
println("Test Error = " + (1.0 - accuracy))

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
  .setParallelism(3)

//Fit the training data to the cvModel
val cvModel = cv.fit(trainData)

// Evaluate the best model on the test set
val predictions = cvModel.transform(testData)
val accuracy = evaluator.evaluate(predictions)
println("Test Error = " + (1.0 - accuracy))

//Get the estimator to fit the training data
val estimator = cvModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[DecisionTreeClassificationModel]
val bestPipeline = new Pipeline().setStages(stringIndexer.toArray ++ Array(assembler, labelIndexer, estimator))
val bestModel = bestPipeline.fit(trainData)

//Store the best model in a directory called "bestmodel"
bestModel.write.overwrite().save("./bestmodel")

// (iv)
// Make predictions on the test set
val newPredictions = bestModel.transform(testData)

// Compute the evaluation metrics
val evaluator = new BinaryClassificationEvaluator().setLabelCol("label").setRawPredictionCol("rawPrediction")
val cvAccuracy = evaluator.evaluate(newPredictions)
val bcMetrics = new BinaryClassificationMetrics(newPredictions.select("prediction", "label").rdd.map(row => (row.getDouble(0), row.getDouble(1))))

// Compute precision and recall
val cvPrecision = bcMetrics.precisionByThreshold.collect().head._2
val cvRecall = bcMetrics.recallByThreshold.collect().head._2

// (c)
// (i)
val categoricalCols2 = Array("Smoking", "AlcoholDrinking", "Sex", "Stroke", "AgeCategory", "Race", "Diabetic", "GenHealth", "Asthma", "PhysicalActivity", "KidneyDisease", "SkinCancer")
val numericalCols2 = Array("BMI", "PhysicalHealth", "MentalHealth", "SleepTime")

val stringIndexer2 = categoricalCols2.map(col => new StringIndexer()
  .setInputCol(col)
  .setOutputCol(s"${col}_indexed")
  .fit(data))

// Create a string indexer for the label column
val labelIndexer2 = new StringIndexer()
  .setInputCol("HeartDisease")
  .setOutputCol("label")
  .fit(data)

val Array(trainData2, testData2) = data.randomSplit(Array(0.9, 0.1), seed=1234)

// (ii)
val assembler2 = new VectorAssembler()
  .setInputCols(numericalCols)
  .setOutputCol("features")

 val decisionTree2 = new DecisionTreeClassifier().setLabelCol("label").setFeaturesCol("features")

 val pipeline2 = new Pipeline().setStages(stringIndexer2.toArray ++ Array(assembler2, labelIndexer2, decisionTree2))
 val model2 = pipeline2.fit(trainData2)

 // make predictions on the test data
 val predictions2 = model2.transform(testData2)

// (iii)
// evaluate the model
val evaluator2 = new BinaryClassificationEvaluator()
  .setLabelCol("label")
  .setRawPredictionCol("prediction")
  .setMetricName("areaUnderROC")

//Train the model using the training data
val trainedModel2 = pipeline2.fit(trainData2)

val accuracy2 = evaluator2.evaluate(predictions2)
//Test error
//Low = good
//High = bad
println("Test Error = " + (1.0 - accuracy2))

// Define the hyper-parameter ranges
val paramGrid2 = new ParamGridBuilder()
  .addGrid(decisionTree.impurity, Array("entropy", "gini"))
  .addGrid(decisionTree.maxDepth, Array(4, 8, 12))
  .addGrid(decisionTree.maxBins, Array(10, 20, 50))
  .build()

// Define a CrossValidator with 4-fold cross-validation
val tvs2 = new TrainValidationSplit()
  .setEstimator(pipeline2)
  .setEvaluator(evaluator2)
  .setEstimatorParamMaps(paramGrid2.build())
  .setTrainRatio(0.8)

//Fit the training data to the cvModel
val tvsModel2 = tvs2.fit(trainData2)

// Evaluate the best model on the test set
val predictions2 = tvsModel2.transform(testData2)
val accuracy2 = evaluator2.evaluate(predictions2)
println("Test Error = " + (1.0 - accuracy2))

// (iv)
// Make predictions on the test set

// Compute the evaluation metrics
val tvsEvaluator2 = new BinaryClassificationEvaluator().setLabelCol("label").setRawPredictionCol("rawPrediction")
val tvsAccuracy2 = tvsEvaluator2.evaluate(predictions2)
val bcMetrics2 = new BinaryClassificationMetrics(predictions2.select("prediction", "label").rdd.map(row => (row.getDouble(0), row.getDouble(1))))

// Compute precision and recall
val tvsPrecision2 = bcMetrics2.precisionByThreshold.collect().head._2
val tvsRecall2 = bcMetrics2.recallByThreshold.collect().head._2

// Print the CV API results
println("CrossValidation API")
println(s"Accuracy: $cvAccuracy")
println(s"Precision: $cvPrecision")
println(s"Recall: $cvRecall")

// Print the TVS API results
println("TrainValidationSplit API")
println(s"Accuracy: $tvsAccuracy2")
println(s"Precision: $tvsPrecision2")
println(s"Recall: $tvsRecall2")

// (d)
val data = spark.read.option("inferSchema", true).option("header", true).csv("./heart_2020_cleaned.csv")
data.show
data.createOrReplaceTempView("heart_disease")

val categoricalCols = Array("Smoking", "AlcoholDrinking", "Sex", "Stroke", "Race", "Diabetic", "GenHealth", "Asthma", "PhysicalActivity", "KidneyDisease", "SkinCancer")
val numericalCols = Array("BMI", "PhysicalHealth", "MentalHealth", "SleepTime")

// Create a string indexer for the label column
val labelIndexer = new StringIndexer()
  .setInputCol("AgeCategory")
  .setOutputCol("label")
  .fit(data)

// Create a string indexer for the categorical columns
val stringIndexer = categoricalCols.map(col => new StringIndexer()
  .setInputCol(col)
  .setOutputCol(s"${col}_indexed")
  .fit(data))

val Array(trainData, testData) = data.randomSplit(Array(0.9, 0.1), seed=1234)

val assembler = new VectorAssembler()
  .setInputCols(numericalCols ++ categoricalCols.map(_ + "_indexed"))
  .setOutputCol("features")

val rf = new RandomForestClassifier()
  .setLabelCol("label")
  .setFeaturesCol("features")
  .setNumTrees(3)
  .setSeed(1234)

val pipeline = new Pipeline().setStages(stringIndexer ++ Array(labelIndexer, assembler, rf))
val model = pipeline.fit(trainData)

// Make predictions on the test data
val predictions = model.transform(testData)

// Evaluate the model
val evaluator = new MulticlassClassificationEvaluator()
  .setLabelCol("label")
  .setPredictionCol("prediction")
  .setMetricName("accuracy")

val accuracy = evaluator.evaluate(predictions)
println("Test Error = " + (1.0 - accuracy))

// Define the hyper-parameter ranges
val paramGrid = new ParamGridBuilder()
  .addGrid(rf.numTrees, Array(3, 5, 7))
  .build()

// Define a TrainValidationSplit with trainRatio=0.8
val tvs = new TrainValidationSplit()
  .setEstimator(pipeline)
  .setEvaluator(evaluator)
  .setEstimatorParamMaps(paramGrid.build())
  .setTrainRatio(0.8)

//Fit the training data to the tvsModel
val tvsModel = tvs.fit(trainData)

// Evaluate the best model on the test set
val predictions = tvsModel.transform(testData)
val accuracy = evaluator.evaluate(predictions)
println("Test Error = " + (1.0 - accuracy))

//Get the estimator to fit the training data
val estimator = tvsModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[RandomForestClassificationModel]
val bestPipeline = new Pipeline().setStages(stringIndexer ++ Array(labelIndexer, assembler, estimator))
val bestModel = bestPipeline.fit(trainData)

//Store the best model in a directory called "bestmodel"
bestModel.write.overwrite().save("./bestmodel")

// Make predictions on the test set
val newPredictions = bestModel.transform(testData)

// Compute the evaluation metrics
val evaluator = new MulticlassClassificationEvaluator().setLabelCol("label").setPredictionCol("prediction")
val tvsAccuracy = evaluator.evaluate(newPredictions)
val bcMetrics = new BinaryClassificationMetrics(newPredictions.select("prediction", "label").rdd.map(row => (row.getDouble(0), row.getDouble(1))))

// Compute precision and recall
val tvsPrecision = bcMetrics.precisionByThreshold.collect().head._2
val tvsRecall = bcMetrics.recallByThreshold.collect().head._2

//Print performance
println("Output of (d)")
println(s"Accuracy: $tvsAccuracy")
println(s"Precision: $tvsPrecision")
println(s"Recall: $tvsRecall")
