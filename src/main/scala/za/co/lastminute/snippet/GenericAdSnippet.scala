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
import com.foursquare.rogue.Rogue._
import com.foursquare.rogue.LatLong
import com.foursquare.rogue.Degrees


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

object Search extends Logger{

  val defaultDegrees = 0.8997
  def render = {
    var lat = -26.195308
    var long = 28.043861
    var categories:List[String] = Nil
    var degrees = defaultDegrees //in degrees

    def process:JsCmd = {
      info("Searching with inputs: lat&long: "+lat+","+long+" distance in degrees: "+degrees)
      
      val searchByDeg = GenericAd where (_.location near (lat, long, Degrees(degrees))) fetch

     //val searchResults = <span>appel</span>//GenericAd.findAll("location" -> ( "$near" -> List(asDouble(lat).getOrElse(0.0), asDouble(long).getOrElse(0.0) ))~("$maxDistance" -> asDouble(max_distance_input).getOrElse(0.0))).map(_.getMarkup)
     SetHtml("search_results", searchByDeg.map(_.getMarkup))
    }
    
    "name=lat" #> SHtml.text(lat.toString, (x:String) => lat = asDouble(x).getOrElse(0.0)) &
    "name=long" #> SHtml.text(long.toString, (x:String) => long = asDouble(x).getOrElse(0.0)) &
    "id=max_distance_input" #> (SHtml.hidden((x:String) => {
        val distanceInKm = asDouble(x).getOrElse(100.0)
        degrees = (distanceInKm/6371)*180/math.Pi
        process
      }, "100.0") ) 
  
   

  }

   def quick = {
    def process(value:String) = {
      val ads = GenericAd where (_.tags contains value) fetch;
      SetHtml("main_content", ads.map(_.getMarkup)) & Focus("search_box")
    }
    "name=searchBox" #> SHtml.ajaxText("special", process(_))
  }
}
