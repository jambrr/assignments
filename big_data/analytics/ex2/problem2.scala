val dataframe = spark.read.option("inferSchema", true).option("header", true).csv("./heart_2020_cleaned.csv")
dataframe.show
dataframe.createOrReplaceTempView("heart_disease")

var text = spark.sql("SELECT * from heart_disease")

val Array(training, test) = text.randomSplit(Array[Double](0.7, 0.3), 18)

