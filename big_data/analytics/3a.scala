
val dataframe = spark.read.option("inferSchema", true).option("header", true).csv("./Data/titanic.csv")

dataframe.createOrReplaceTempView("titanic")

val avger = dataframe.select(avg($"age")).head().getDouble(0)

var data = dataframe.filter($"age" > avger).select("Name").distinct

data.show()

data.write.text("output3.txt")
