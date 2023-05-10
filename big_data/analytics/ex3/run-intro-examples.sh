# Refer to our GettingStarted.pdf guide on Moodle on how to set up your local Hadoop and Spark environments.
# Set your default HOME and PATH variables: here using Scala 2.12.10 (based on Java JDK 8), Hadoop 2.7.7 & Spark 3.2.1

export SCALA_HOME=/usr/local/bin/scala
export HADOOP_HOME=/usr/local/bin/hadoop
export SPARK_HOME=/usr/local/spark
export PATH=$SCALA_HOME/bin:$HADOOP_HOME/bin:$SPARK_HOME/bin:$PATH
export SPARK_LOCAL_IP="127.0.0.1" # fixes possible binding issue for macOS (also try: sudo hostname -s 127.0.0.1)

# Download HadoopWordCount.java, HadoopWordStripes.java & HadoopWordPairs.java from Moodle into a new directory called 'HadoopWordCount'.
# Then compile the three Java files per command-line shell into a new jar file called 'HadoopWordCount.jar':

#cd ./
#javac -classpath $(echo $HADOOP_HOME/share/hadoop/common/*.jar $HADOOP_HOME/share/hadoop/mapreduce/*.jar | tr ' ' ':') *.java
#jar -cvf ./.jar *.class
#cd ..

# And run the various HadoopWordCount examples on the 'AA' subdirectory from Wikipedia-En-41784-Articles.tar.gz (also available from Moodle):

#hadoop jar ./HadoopWordCount/HadoopWordCount.jar HadoopWordCount ./Data/enwiki-articles/AA ./hadoop-output1
#hadoop jar ./HadoopWordCount/HadoopWordCount.jar HadoopWordPairs ./Data/enwiki-articles/AA ./hadoop-output2
#hadoop jar ./HadoopWordCount/HadoopWordCount.jar HadoopWordStripes ./Data/enwiki-articles/AA ./hadoop-output3

# Download SparkWordCount.scala from Moodle into a new directory called 'SparkWordCount'.
# Then compile the single Scala file per command-line shell into a new jar file called 'SparkWordCount.jar':

cd ./
scalac -classpath $(echo $SPARK_HOME/jars/*.jar | tr ' ' ':') problem1.scala
jar -cvf ./problem1.jar *.class
cd ..

# And use 'spark-submit' to run the SparkWordCount example on the 'AA' subdirectory from Wikipedia-En-41784-Articles.tar.gz (also available from Moodle):

spark-submit --class problem1 ./problem1.jar ../../Data/wiki_movie_plots_deduped.csv ./spark-output1

# Finally open an interactive Spark/Scala shell with some extra driver memory (try out some Scala/Spark examples from the lecture slides):

spark-shell --driver-memory 4G --jars $(echo ./Jars/*.jar | tr ' ' ',')

