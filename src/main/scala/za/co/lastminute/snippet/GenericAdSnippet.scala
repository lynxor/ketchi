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

object Preview {
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

object Search {
 
  def render = {
    var lat = "0.0"
    var long = "0.0"
    var categories:List[String] = Nil

    def process:JsCmd = {
      
      GenericAd.findAll("genericads" -> "milk"); Noop
    }
    
    "name=lat" #> SHtml.text(lat, (x) => lat = x) &
    "name=long" #> SHtml.text(long, (x) => long = x) &
    "name=categories" #> (SHtml.multiSelect(Category.toPairSeq, Category.toDefaultSeq, (x) => categories = x) ++ SHtml.hidden(() => process))

  }
}