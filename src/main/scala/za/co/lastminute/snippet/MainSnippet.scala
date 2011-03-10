package za.co.lastminute {
  package snippet {

    import _root_.scala.xml.{NodeSeq, Text}
    import  net.liftweb._
    import http._
    import common._
    import util.Helpers._
    import _root_.java.util.Date
    import za.co.lastminute.lib._
    import za.co.lastminute.model.User;
    import com.foursquare.rogue.Rogue._

    object MainSnippet {
  
      def time = "#time *" #> new Date().toString()
      def inlineLogin = "#inline_login" #> User.loginXhtml;
      def loggedOut = "#inline_login [style]" #>  (User.currentUser match{
          case Empty => ""
          case _ => "hidden"
        })

     
    }
  }
}
