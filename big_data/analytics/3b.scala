val dataframe = spark.read.option("inferSchema", true).option("header", true).csv("./Data/titanic.csv")

dataframe.createOrReplaceTempView("titanic")
    
var data = spark.sql("SELECT DISTINCT name FROM titanic WHERE age > (SELECT AVG(age) FROM titanic)")

data.show()

data.write.text("3b_output.txt")

