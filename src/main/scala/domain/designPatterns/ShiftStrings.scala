/*
// Sample code to perform I/O:

val name = scala.io.StdIn.readLine()        // Reading input from STDIN
println("Hi, " + name + ".")                // Writing output to STDOUT

// Warning: Printing unwanted or ill-formatted data to output will cause the test cases to fail
*/

// Write your code here
import scala.collection.mutable.Map

object Solution extends App{
  val s = scala.io.StdIn.readLine()
  val t = scala.io.StdIn.readLine()
  val k = scala.io.StdIn.readLine()

  val source = s.toList
  val target = t.toList
  val shifts = Integer.parseInt(k)

  val alloptions = (1 to shifts).toList.permutations.toList
  println(alloptions)

  val letters = ('a' to 'z')
  val charPos = Map.empty[Char,Int]

  for (pos <- 0 to 26){
    charPos += (letters(pos) -> pos)
  }
  println(charPos)

  val possibleResults = alloptions.map(
    eachCombo => getNewStrings(source, eachCombo)
  )

  println(possibleResults)

  def getNewStrings(source:List[Char], shiftList:List[Int]):List[Char] = {
    var i = 0
    shiftList.map(
      pos => {
        val sourceChar = s(i)
        val newChar = letters(charPos(sourceChar) + pos)
        i = i + 1
        newChar
      }
    )
  }


}