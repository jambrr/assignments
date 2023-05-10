// ------------------------ Disable excessive logging -------------------------

import org.apache.log4j.Logger
import org.apache.log4j.Level

Logger.getLogger("org").setLevel(Level.ERROR)
Logger.getLogger("akka").setLevel(Level.ERROR)

// ------------------------ Parse the Wikipedia Articles ----------------------

import scala.io.Source._
import scala.collection.mutable._
import scala.collection.JavaConversions._

def parseHeader(line: String): Array[String] = {
  try {
    var s = line.substring(line.indexOf("id=\"") + 4)
    val id = s.substring(0, s.indexOf("\""))
    s = s.substring(s.indexOf("url=\"") + 5)
    val url = s.substring(0, s.indexOf("\""))
    s = s.substring(s.indexOf("title=\"") + 7)
    val title = s.substring(0, s.indexOf("\""))
    Array(id, url, title)
  } catch {
    case e: Exception => Array("", "", "")
  }
}

def parse(lines: Array[String]): Array[(String, String)] = {
  var docs = ArrayBuffer.empty[(String, String)]
  var title = ""
  var content = ""
  for (line <- lines) {
    try {
      if (line.startsWith("<doc ")) {
        title = parseHeader(line)(2)
        content = ""
      } else if (line.startsWith("</doc>")) {
        if (title.length > 0 && content.length > 0) {
          docs += ((title, content))
        }
      } else {
        content += line + "\n"
      }
    } catch {
      case e: Exception => content = ""
    }
  }
  docs.toArray
}

val sampleSize = 0.01 // change to 1.0 for full experiment
val numTerms = 5000 // change to 50000 for full experiment
val k = 250 // number of latent concepts in the reduced matrix

val textFiles = sc.wholeTextFiles("../Data/enwiki-articles/*/*").sample(false, sampleSize)
val numFiles = textFiles.count()

val plainText = textFiles.flatMap { case (uri, text) => parse(text.split("\n")) } // one entry for each Wikipedia article
val numDocs = plainText.cache()
val numDocs = plainText.count()

val bNumDocs = sc.broadcast(numDocs)

def isOnlyLetters(str: String): Boolean = {
  str.forall(c => Character.isLetter(c))
}

val bStopWords = sc.broadcast(
  fromFile("../Data/stopwords.txt").getLines().toSet)

import java.util.Properties
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreAnnotations._

def createNLPPipeline(): StanfordCoreNLP = {
  val props = new Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma")
  new StanfordCoreNLP(props)
}

def plainTextToLemmas(text: String, pipeline: StanfordCoreNLP): Seq[String] = {
  val doc = new Annotation(text)
  pipeline.annotate(doc)
  val lemmas = new ArrayBuffer[String]()
  val sentences = doc.get(classOf[SentencesAnnotation])
  for (
    sentence <- sentences;
    token <- sentence.get(classOf[TokensAnnotation])
  ) {
    val lemma = token.get(classOf[LemmaAnnotation]).toLowerCase
    if (lemma.length > 2 && !bStopWords.value.contains(lemma)
      && isOnlyLetters(lemma)) {
      lemmas += lemma
    }
  }
  lemmas
}

import org.apache.spark.rdd._

val lemmatized: RDD[(String, Seq[String])] =
  plainText.mapPartitions(it => {
    val pipeline = createNLPPipeline()
    it.map {
      case (title, contents) =>
        (title, plainTextToLemmas(contents, pipeline))
    }
  })

val docTermFreqs = lemmatized.map {
  case (title, terms) => {
    val termFreqs = terms.foldLeft(new HashMap[String, Int]()) {
      (map, term) =>
        {
          map += term -> (map.getOrElse(term, 0) + 1)
          map
        }
    }
    (title, termFreqs)
  }
}
docTermFreqs.cache()
docTermFreqs.count()

val docIds = docTermFreqs.map(_._1).zipWithUniqueId().map(_.swap).collectAsMap()
val docFreqs = docTermFreqs.map(_._2).flatMap(_.keySet).map((_, 1)).
  reduceByKey(_ + _, 24)

val ordering = Ordering.by[(String, Int), Int](_._2)
val topDocFreqs = docFreqs.top(numTerms)(ordering)

val idfs = topDocFreqs.map {
  case (term, count) =>
    (term, math.log(bNumDocs.value.toDouble / count))
}.toMap

val idTerms = idfs.keys.zipWithIndex.toMap
val termIds = idTerms.map(_.swap)

val bIdfs = sc.broadcast(idfs).value
val bIdTerms = sc.broadcast(idTerms).value

// -------------  Prepare the Input Matrix and Compute the SVD ----------------

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.mllib.linalg.SingularValueDecomposition

import org.apache.spark.mllib.linalg.distributed.RowMatrix

val vecs = docTermFreqs.map(_._2).map(termFreqs => {
  val docTotalTerms = termFreqs.values.sum
  val termScores = termFreqs.filter {
    case (term, freq) => bIdTerms.contains(term)
  }.map {
    case (term, freq) => (bIdTerms(term), bIdfs(term) * termFreqs(term) / docTotalTerms)
  }.toSeq
  Vectors.sparse(bIdTerms.size, termScores)
})

vecs.cache()
vecs.count()

val mat = new RowMatrix(vecs)
val svd = mat.computeSVD(k, computeU = true)

// ------------------- Query the Latent Semantic Index ------------------------

def topTermsInTopConcepts(
  svd: SingularValueDecomposition[RowMatrix, Matrix],
  numConcepts: Int, numTerms: Int): Seq[Seq[(String, Double)]] = {
  val v = svd.V
  val topTerms = new ArrayBuffer[Seq[(String, Double)]]()
  val arr = v.toArray
  for (i <- 0 until numConcepts) {
    val offs = i * v.numRows
    val termWeights = arr.slice(offs, offs + v.numRows).zipWithIndex
    val sorted = termWeights.sortBy(-_._1)
    topTerms += sorted.take(numTerms).map {
      case (score, id) =>
        (bIdTerms.find(_._2 == id).getOrElse(("", -1))._1, score)
    }
  }
  topTerms
}

def topDocsInTopConcepts(
  svd: SingularValueDecomposition[RowMatrix, Matrix],
  numConcepts: Int, numDocs: Int): Seq[Seq[(String, Double)]] = {
  val u = svd.U
  val topDocs = new ArrayBuffer[Seq[(String, Double)]]()
  for (i <- 0 until numConcepts) {
    val docWeights = u.rows.map(_.toArray(i)).zipWithUniqueId()
    topDocs += docWeights.top(numDocs).map {
      case (score, id) => (docIds(id), score)
    }
  }
  topDocs
}

val topConceptTerms = topTermsInTopConcepts(svd, 12, 12)
val topConceptDocs = topDocsInTopConcepts(svd, 12, 12)
for ((terms, docs) <- topConceptTerms.zip(topConceptDocs)) {
  println("Concept terms: " + terms.map(_._1).mkString(", "))
  println("Concept docs: " + docs.map(_._1).mkString(", "))
  println()
}

// Keyword Queries

import breeze.linalg.{DenseMatrix => BDenseMatrix, SparseVector => BSparseVector}
import org.apache.spark.mllib.linalg.{Matrices, Matrix, SingularValueDecomposition, Vectors, Vector => MLLibVector}

def termsToQueryVector(
  terms: scala.collection.immutable.Seq[String],
  idTerms: scala.collection.immutable.Map[String, Int],
  idfs: scala.collection.immutable.Map[String, Double]): BSparseVector[Double] = {
  val indices = terms.map(idTerms(_)).toArray
  val values = terms.map(idfs(_)).toArray
  new BSparseVector[Double](indices, values, idTerms.size)
}

def topDocsForTermQuery(
  US: RowMatrix,
  V: Matrix,
  query: BSparseVector[Double]): Seq[(Double, Long)] = {
  val breezeV = new BDenseMatrix[Double](V.numRows, V.numCols, V.toArray)
  val termRowArr = (breezeV.t * query).toArray
  val termRowVec = Matrices.dense(termRowArr.length, 1, termRowArr)
  val docScores = US.multiply(termRowVec)
  val allDocWeights = docScores.rows.map(_.toArray(0)).zipWithUniqueId()
  allDocWeights.top(10) 
}

def multiplyByDiagonalRowMatrix(mat: RowMatrix, diag: MLLibVector): RowMatrix = {
  val sArr = diag.toArray
  new RowMatrix(mat.rows.map { vec =>
    val vecArr = vec.toArray
    val newArr = (0 until vec.size).toArray.map(i => vecArr(i) * sArr(i))
    Vectors.dense(newArr)
  })
}

val US = multiplyByDiagonalRowMatrix(svd.U, svd.s)

val terms = List("serious", "incident")
val queryVec = termsToQueryVector(terms, idTerms, idfs)
topDocsForTermQuery(US, svd.V, queryVec)

