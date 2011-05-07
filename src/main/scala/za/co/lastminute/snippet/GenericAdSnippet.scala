package za.co.lastminute.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JsCmd
import org.bson.types.ObjectId
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeBuffer
import za.co.lastminute.model._
import za.co.lastminute.model.generic_ad._
import Helpers._
import java.util.Date
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

  implicit def fromStringSearch(searchString: String):StringSearch = StringSearch(searchString, new Date())
  implicit def fromLatLong(latLng: LatLong):LocationSearch = LocationSearch(latLng, new Date())
    
  val defaultDegrees = 0.8997
  def render = {
    var lat = S.param("lat").getOrElse("-26.195308").toDouble
    var long = S.param("long").getOrElse("28.043861").toDouble
    
    var categories:List[String] = Nil
    var degrees = defaultDegrees //in degrees

    def process:JsCmd = {
      info("Searching with inputs: lat&long: "+lat+","+long+" distance in degrees: "+degrees)
      val searchByDeg = GenericAd where (_.location near (lat, long, Degrees(degrees))) fetch;

     // User.storeSearch(LatLong(lat, long))
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

    var text = ""
    def process() = {
      User.storeSearch(text) 
      val ads = GenericAd where (_.tags contains text.toUpperCase) fetch;
      SetHtml("main_content", ads.map(_.getMarkup)) & Focus("search_box")
    }
    "#search_box" #> SHtml.ajaxText("", (x:String) => {text = x;Noop}) &
    "#quick_button" #> SHtml.ajaxButton(<span>go</span>, () => process)
  }

 
  
}

object Commenting extends Logger{

  private val commentIdSuffix = "_comments";
  def comment(xml:NodeSeq): NodeSeq = {

    val adId = xml \ "@id" match {
      case null => warn("Could not extract id for ad"); "no id"
      case s:NodeSeq => {
          val theId = s.toString
          theId.substring(0, theId.length - (commentIdSuffix.length));
        }
    }

    var newComment = ""

    val value = "#new_comment_text" #> SHtml.ajaxTextarea("type your comment here ...", (text:String) => {newComment = text;Noop} ) &
    "#new_comment_add_button" #> SHtml.ajaxButton("Post comment", () => {
        val theAd:GenericAd = (GenericAd where (_._id eqs new ObjectId(adId)) fetch).head;

        val currentUsername = User.currentUser match{
          case f @ Full(user) => user.firstName.is
          case _ => "Anonymous"
        }
        
        theAd.comments.set(Comment(currentUsername, newComment, new Date) :: theAd.comments.is)
        theAd.save
        Alert("Comment submitted")
      }) &
    "#comments" #> buildComments(adId)

    value.apply(xml) 
  }

  def buildComments(adId:String):NodeSeq = {
    val ad = (GenericAd where (_._id eqs new ObjectId(adId)) fetch).head
   
    ad.comments.is match{
      case list: List[Comment] if(!list.isEmpty) => { list.map((comment: Comment) => {
              <div class="ui-widget ui-corner-all comment_box">
                <div>
                  <p>{comment.comment}</p>
                </div>
                <span>{"by "+comment.commenter +" on "+comment.date}</span>
              </div>
            }).flatMap(_.toSeq)
        }
      case _ => <div><span>No comments</span></div>
    }  
  }
}
