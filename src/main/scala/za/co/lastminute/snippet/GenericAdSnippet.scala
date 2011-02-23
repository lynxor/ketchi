package za.co.lastminute.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JsCmd
import scala.xml.Elem
import za.co.lastminute.model._
import za.co.lastminute.model.generic_ad.GenericAd
import Helpers._
import  js._
import JsCmds._
import net.liftweb.json.JsonDSL._

case class ParamInfo(theParam: String)

object GenericAdSnippet {
  def listAll = "#listing *" #> GenericAd.findAll.map((x: GenericAd) => x.getMarkup)
}

object View {
  def toffie : (Elem, Elem) = {
    S.param("ad_id") match{
      case a: Box[String] => {
          GenericAd.find(a.get).map((x) => (x.getMarkup, <a href={"/generic_ads/create?ad_id="+x._id.toString}>{"<< Edit "}</a>)).get
        }
      case _ => (<p>ERROR getting preview</p>, <a href={"/generic_ads/create"}></a>)
    }
  }
  def render = "#view_ad *" #> toffie._1 &
               "#edit_link" #> toffie._2
}

object Search {
 
  def render = {
    var lat = "0.0"
    var long = "0.0"
    var categories:List[String] = Nil
    var max_distance_input = "1.0"

    def process:JsCmd = {
      val searchResults = GenericAd.findAll("location" -> ( "$near" -> List(asDouble(lat).getOrElse(0.0), asDouble(long).getOrElse(0.0) ))~("$maxDistance" -> asDouble(max_distance_input).getOrElse(0.0))).map((x) => x.getMarkup)
      SetHtml("search_results", searchResults)
    }
    
    "name=lat" #> SHtml.text(lat, (x) => lat = x) &
    "name=long" #> SHtml.text(long, (x) => long = x) &
    "id=max_distance_input" #> SHtml.hidden((x:String) => {max_distance_input = x}, "1.0") &
    "name=categories" #> (SHtml.multiSelect(Category.toPairSeq, Category.toDefaultSeq, (x) => categories = x) ++ SHtml.hidden(() => process))

  }
}