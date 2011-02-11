package za.co.lastminute.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb.http._
import net.liftweb.record.Field
import net.liftweb.sitemap.Menu
import scala.xml.Elem
import za.co.lastminute.model.generic_ad.GenericAd
import Helpers._

case class ParamInfo(theParam: String)

object GenericAdSnippet {
  //Is there a better way to do this??????? model and view mixed a bit here
  def listAll = "#listing *" #> GenericAd.findAll.map((x: GenericAd) => x.getMarkup)

}

object  Preview {
  // Create a menu for /preview/ad_id
//  val menu = Menu.param[ParamInfo]("Preview", "Preview",
//                                   s => Full(ParamInfo(s)),
//                                   pi => pi.theParam) / "generic_ads" / "preview"
//  lazy val loc = menu.toLoc
//

def toffie : Elem = {
  S.param("ad_id") match{ 
    case a: Box[String] => {
        GenericAd.find(a.get).map(_.getMarkup).get
      }
    case _ => <p>ERROR getting preview</p>
  }
}


  def render = "#preview *" #> toffie
}
//(curVal:ParamInfo) => GenericAd.find(asInt(_.theParam)).map(_.getMarkup)

//   .map((x:String) => GenericAd.find(x).map((y:GenericAd) => y.getMarkup))   GenericAd.find("sfdklj").get.getMarkup;