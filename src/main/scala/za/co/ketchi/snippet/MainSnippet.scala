package za.co.ketchi {
  package snippet {

    import _root_.scala.xml.{NodeSeq, Text}
    import  net.liftweb._
    import http._
    import common._
    import util.Helpers._
    import _root_.java.util.Date
    import za.co.ketchi.lib._
    import za.co.ketchi.model.User;
    import com.foursquare.rogue.Rogue._
      import net.liftweb.util._
  import Helpers._

    object MainSnippet {
  
      def time = "#time *" #> new Date().toString()
      def inlineLogin = "#inline_login" #> User.loginXhtml;
      def loggedOut = "#inline_login [style]" #>  (User.currentUser match{
          case Empty => ""
          case _ => "hidden"
        })

      var testValue = ""
      def test = "whatever:pof" #> ((n: NodeSeq) => {testValue = (n \ "@value").toString; n})
    }
  }
}
