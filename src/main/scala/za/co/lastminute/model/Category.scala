package za.co.lastminute.model


object Category extends Enumeration{
  val food = Value("food")
  val accomodation = Value("accomodation")

  def toPairSeq: Seq[(String, String)] = {
    Category.values.map((x) => (x.toString, x.toString)).toList
  }
  def toDefaultSeq = Category.values.toList.map(_.toString)
}