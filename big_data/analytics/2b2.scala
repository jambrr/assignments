import org.apache.spark.(SparkConf, SparkContext)
import org.apache.spark.SparkContext._

object SparkWorkCount{
  def main(args: Array[String]){
    if (args.length < 2){
      System.err.println("Correct")
      System.exit(1)
    }

    val sparkConf = new SparkConf().setAppName("Sparkwordcount")
    val ctx = new SparkContext(sparkConf)
    val textFile = ctx.textFile(args(0))
    val counts = file.flatMap(line => line.replaceAll("[a-zA-Z0-9 ]", " ")).flatMap(_.split("\\s+")).map(_.toLowerCase).map((_, 1)).reduceByKey(_ + _).sortBy(_._2, ascending=false)
    val counts2 = counts.filter(x => x._1.length >= 5).filter(x => x._1.length <= 25).take(100)
    ctx.parallelize(counts2, 1).coalesce(1).saveAsTextFile(args(1))
    ctx.stop()

  }
}
