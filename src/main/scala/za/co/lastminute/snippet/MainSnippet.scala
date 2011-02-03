package za.co.lastminute {
package snippet {

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.java.util.Date
import za.co.lastminute.lib._
import Helpers._

object MainSnippet {
  
      def time = "#time *" #> new Date().toString()
      
}

}
}
