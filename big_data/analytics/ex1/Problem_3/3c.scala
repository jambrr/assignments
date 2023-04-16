import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.functions

case class Average(var sum: Long, var count: Long)

object MyAverage extends Aggregator[Long, Average, Double]{
  def zero: Average = Average(0L, 0L)

  def reduce(buffer: Average, data: Long): Average = {
    if(data.toString == None){
      buffer.sum += 23
    }else{
      buffer.sum += data
    }

    buffer.count += 1
    buffer
  }

  def merge(b1: Average, b2: Average): Average = {
    b1.sum += b2.sum
    b1.count += b2.count
    b1
  }

  def finish(reduction: Average): Double = reduction.sum.toDouble / reduction.count

  def bufferEncoder: Encoder[Average] = Encoders.product

  def outputEncoder: Encoder[Double] = Encoders.scalaDouble
}

spark.udf.register("myAverage", functions.udaf(MyAverage))

val dataframe = spark.read.option("inferSchema", true).option("header", true).csv("./Data/titanic.csv")

dataframe.createOrReplaceTempView("titanic")

var result = spark.sql("SELECT myAverage(age) FROM titanic")

result.show()
