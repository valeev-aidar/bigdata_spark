object TextProcess {

  implicit class ProcessingString(text: String) {
    def removeApostrophe(): String = text.replaceAll("\'", "")

    def removeTags(): String = text.replaceAll("<.*?>", "")

    def removeAliases(): String = {
      val result = StringBuilder.newBuilder
      var isInAlias = false
      text.foreach(c => {
        if (c == '@')
          isInAlias = true
        else if (!isInAlias)
          result.append(c)
        else if (c.toString.matches("[^_0-9a-z]"))
          isInAlias = false
      })
      result.toString()
    }

    private def removePatterns(word: String, patterns: Array[String]): String = {
      val result = StringBuilder.newBuilder
      result.append(word)
      patterns.foreach(p => {
        val i = result.indexOf(p)
        if (i != -1)
          result.delete(i, result.length)
      })
      result.toString()
    }

    def removeLinks(patterns: Array[String]): String = {
      val split = text.split(" ")
      val result = StringBuilder.newBuilder
      split.foreach(s => result ++= removePatterns(s, patterns) ++ " ")
      result.toString()
    }

    def replaceProhibited(): String =
      text.replaceAll("[^ 0-9a-zA-Z]", " ")

    def deleteSpaces(): String = {
      val result = StringBuilder.newBuilder
      var wasSpace = false
      text.foreach(c => {
        if (c == ' ')
          wasSpace = true
        else if (!wasSpace)
          result.append(c)
        else {
          wasSpace = false
          result.append(" " + c)
        }
      })
      result.toString()
    }

    def removeShort(): String = {
      val split = text.split(" ")
      val result = StringBuilder.newBuilder
      split.foreach(s => if (s.length >= 3) result ++= s ++ " ")
      result.toString()
    }

    def removeNumbers(): String = {
      val split = text.split(" ")
      val result = StringBuilder.newBuilder
      split.foreach(s => if (s.matches("^[a-z]*$")) result ++= s ++ " ")
      result.toString()
    }

    def removeLetterRepetitions(): String =
      text.split(" ").map(
        s =>
          if (s.matches("^([a-z]*)(([a-z])\\3{1,}([a-z])\\4{1,}|([a-z])\\5{2,})([a-z]*)$"))
            s.replaceAll("([a-z])\\1{1,}", "$1")
          else s
      ).mkString(" ")
  }

  def process(line: String): String = {
    val patterns_array: Array[String] = Array(
      "http",
      "pic.twitter",
      "instagram",
      "bit.ly",
      "twitter"
    )

    new ProcessingString(line)
      .removeApostrophe()
      .removeTags()
      .removeAliases()
      .removeLinks(patterns_array)
      .replaceProhibited()
      .deleteSpaces()
      .removeShort()
      .toLowerCase
      .removeNumbers()
      .removeLetterRepetitions()
      .trim()
  }
}
