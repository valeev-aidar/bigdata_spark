import scala.io.Source

object TextProcess {
  def deleteTags(str: String): String = {
    str.replaceAll("<.*?>", "")
  }

  def isProhibited(ch: Char): Boolean = {
    //val str = " \"?!@#$%^&*()-+={}[]|<>;:,./`~']|"
    val alphabet = " 0123456789abcdefghikjlmnopqrstuvwxyz"
    !alphabet.contains(ch.toLower)
  }

  def deleteAts(str: String): String = {
    val sb = StringBuilder.newBuilder
    sb.append(str)
    var has_at = 0
    val ans = StringBuilder.newBuilder
    var ind = 0
    for (ind <- sb.indices) {
      if (sb(ind) == '@') {
        has_at = 1
      }
      else {
        if (has_at == 0) {
          ans.append(sb(ind))
        }
        else {
          if (isProhibited(sb(ind))) {
            has_at = 0
          }
        }
      }
    }
    ans.toString()
  }

  def changeProhibited(str: String): String = {
    val sb = StringBuilder.newBuilder
    val ans = StringBuilder.newBuilder
    sb.append(str)
    var ind = 0
    for (ind <- sb.indices) {
      if (isProhibited(sb(ind))) {
        ans.append(' ')
      }
      else {
        ans.append(sb(ind))
      }
    }
    ans.toString()
  }

  def removePatterns(str: String, patterns: Array[String]): String = {
    val sb = StringBuilder.newBuilder
    sb.append(str)
    var x = 0
    for (x <- patterns.indices) {
      val pattern = patterns(x)
      var ok = 1
      while (ok == 1) {
        val ind = sb.indexOf(pattern)
        if (ind == -1) {
          ok = 0
        }
        else {
          sb.delete(ind, sb.length)
        }
      }
    }
    sb.toString()
  }

  def removeLinks(str: String, patterns: Array[String]): String = {
    val splitted = str.split(" ")
    val ans = StringBuilder.newBuilder
    var x = 0
    for (x <- 0 until splitted.length) {
      ans.append(removePatterns(splitted(x), patterns))
      ans.append(" ")
    }
    ans.toString()
  }

  def deleteSpaces(str: String): String = {
    val sb = StringBuilder.newBuilder
    sb.append(str)
    var ind = 0
    val ans = StringBuilder.newBuilder
    var was_space = 0
    for (ind <- sb.indices) {
      if (sb(ind) == ' ') {
        was_space = 1
      }
      else {
        if (was_space == 1) {
          ans.append(' ')
          was_space = 0
        }
        ans.append(sb(ind))
      }
    }
    ans.toString()
  }

  def removeShort(str: String): String = {
    val res = str.split(" ")
    val ans = StringBuilder.newBuilder
    var x = 0
    for (x <- 0 until res.length) {
      if (res(x).length >= 3) {
        ans.append(res(x))
        ans.append(" ")
      }
    }
    ans.toString()
  }

  def makeLower(str: String): String = {
    val sb = StringBuilder.newBuilder
    sb.append(str)
    var ind = 0
    for (ind <- sb.indices) {
      if (sb(ind) >= 'A' && sb(ind) <= 'Z') {
        sb(ind) = (sb(ind) - 'A' + 'a').toChar
      }
    }
    sb.toString()
  }

  def removeApostroph(str: String): String = {
    val ans = StringBuilder.newBuilder
    for (i <- 0 until str.length) {
      if (str(i) != '\'') {
        ans.append(str(i))
      }
    }
    ans.toString()
  }

  def removeNumbers(str: String): String = {
    val ans = StringBuilder.newBuilder
    val splitted = str.split(" ")
    var x = 0
    for (x <- 0 until splitted.length) {
      var c = 'a'
      var contains = 0
      for (c <- '0' to '9') {
        if (splitted(x).contains(c)) {
          contains = 1
        }
      }
      if (contains == 0) {
        ans.append(splitted(x))
        ans.append(" ")
      }
    }
    ans.toString()
  }

  // just for fun. Don't blame me.
  def applyFunctions(fs: List[Function[String, String]], p: String): String = {
    if (fs.nonEmpty) {
      applyFunctions(fs.slice(1, fs.length), fs.head(p))
    } else {
      p
    }
  }

  def process(line: String): String = {
    val patterns_array: Array[String] = new Array[String](4)
    patterns_array(0) = "http"
    patterns_array(1) = "twitter"
    patterns_array(2) = "instagram"
    patterns_array(3) = "bit"

    val pre_result = applyFunctions(List(
      removeApostroph
      , deleteTags
      , deleteAts
      , changeProhibited
      , deleteSpaces
      , makeLower
      , removeShort
      , removeNumbers
    ), line)

    val result = removeLinks(pre_result, patterns_array).trim
    result
  }

  def main(args: Array[String]): Unit = {
    val file = "input.txt"
    var patterns_array: Array[String] = new Array[String](4)
    patterns_array(0) = "http"
    patterns_array(1) = "twitter"
    patterns_array(2) = "instagram"
    patterns_array(3) = "bit"
    for (line <- Source.fromFile(file).getLines) {
      val res_1 = removeApostroph(line)
      val res = deleteTags(res_1)
      //println(res)
      val res2 = deleteAts(res)

      val res21 = removeLinks(res2, patterns_array)

      val res3 = changeProhibited(res21)

      //println("after deleting ats:")

      val res4 = deleteSpaces(res3)
      val res5 = makeLower(res4)
      val res6 = removeShort(res5)
      val res7 = removeNumbers(res6)
      println(res7.trim())
    }

  }
}





