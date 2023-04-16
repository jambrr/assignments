var dataframe = spark.read.option("inferSchema", true).option("header", true).csv("./Data/titanic.csv")
dataframe.createOrReplaceTempView("titanic")

val survived = spark.sql("SELECT AVG(age) FROM titanic WHERE survived='1'").head.getDouble(0)
val not_survived = spark.sql("SELECT AVG(age) FROM titanic WHERE survived='0'").head.getDouble(0)

val result = (survived - not_survived).abs

println("Average age of those who survived: " + survived)
println("Average age of those who didn't survived: " + not_survived)
println("Age difference between the 2 groups: " +result)
