package za.co.ketchi.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JsCmd
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeBuffer
import za.co.ketchi.model._
import za.co.ketchi.model.generic_ad._
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
  
  def listClient = "#listing *" #> {
    val ads = GenericAd where (_.userId eqs User.currentUserId.getOrElse("guest")) fetch;
    ads.map((ad:GenericAd) => clientAdMarkup(Full(ad)))
  }
  
  private def clientAdMarkup(adBox:Box[GenericAd]) = {
    adBox match{
      case Full(ad: GenericAd) => {
          <div class="client_ad ui-corner-all">
            <div> {ad.getMarkup} </div>
            <div> {editLinkXml(ad)} </div>
            <div> {deleteLinkXml(ad)} </div>
          </div>
        }
      case _ => <span>No ad</span>
    }
  }
  
  private def editLinkXml(ad:GenericAd) = <div id="edit_ad" class="lift:GenericAdSnippet.editLink" ad_id={ad._id.toString} >Edit ad link here</div>
  private def deleteLinkXml(ad:GenericAd) = <div id="delete_ad" class="lift:GenericAdSnippet.deleteLink" ad_id={ad._id.toString} >Delete ad link here</div> 
  
  def adId(xml:NodeSeq):String = xml \\ "@ad_id" toString
  
  def viewAd(xml:NodeSeq) : NodeSeq = {   
    val ad:Option[GenericAd] = GenericAd where (_._id eqs new ObjectId(S.param("ad_id").getOrElse("no_id"))) get;   
    "#view_ad" #> clientAdMarkup(ad) apply xml
  }
  
  def deleteLink(incoming:NodeSeq):NodeSeq = {
    "#delete_ad" #> SHtml.a(() => {
        GenericAd where (_._id eqs new ObjectId(adId(incoming))) bulkDelete_!!;
        RedirectTo("/generic_ads/client_listing")
    }, <span>delete this ad</span>) apply incoming
  }
  
  def editLink(incoming:NodeSeq):NodeSeq = {
    "#edit_ad" #> SHtml.a(() => {
        RedirectTo("/generic_ads/create?ad_id="+adId(incoming))
    }, <span>edit this ad</span>) apply incoming
  } 
}

object Search extends Logger{
  private val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");
  implicit def fromStringSearch(searchString: String):StringSearch = StringSearch(searchString, new Date())
  implicit def fromLatLong(latLng: LatLong):LocationSearch = LocationSearch(latLng, new Date())
    
  val defaultDegrees = 0.8997
  def render = {
    
    var lat = S.param("lat").getOrElse("-26.195308").toDouble
    var long = S.param("long").getOrElse("28.043861").toDouble
    var stringQuery = S.param("q").getOrElse("")
    var startDate = new Date
    var endDate = (new DateTime).plusDays(7).toDate

    var degrees = defaultDegrees //in degrees

    def process:JsCmd = {
      info("Searching with inputs: lat&long: "+lat+","+long+" distance in degrees: "+degrees+" , from "+startDate+" to "+endDate+", and with query: "+stringQuery)

      //Validate
      if(startDate.compareTo(endDate) > 0) return SetHtml("search_results", <span class="error">Your date selection is not valid</span>)
      if(lat == 0.0 || long == 0.0) return SetHtml("search_results", <span class="error">Please select a valid location</span>)

      val searchByDegAll = stringQuery match{
        case s:String if(s == null || s.equals("")) => GenericAd where (_.location near (lat, long, Degrees(degrees))) fetch;
        case _ => GenericAd where (_.location near (lat, long, Degrees(degrees))) and (_.tags contains stringQuery.toUpperCase) fetch;
      }
      //filter afterwards?? fix somehow
      val searchByDeg = searchByDegAll.filter((a:GenericAd) => {         
          a.lifeTime.is.endDate.compareTo(startDate) >= 0 &&
          a.lifeTime.is.startDate.compareTo(endDate) <= 0
        })
          

      // User.storeSearch(LatLong(lat, long))
      //val searchResults = <span>appel</span>//GenericAd.findAll("location" -> ( "$near" -> List(asDouble(lat).getOrElse(0.0), asDouble(long).getOrElse(0.0) ))~("$maxDistance" -> asDouble(max_distance_input).getOrElse(0.0))).map(_.getMarkup)
      SetHtml("search_results", searchByDeg.map(_.getMarkup))
    }
    
    "name=lat" #> SHtml.text(lat.toString, (x:String) => lat = asDouble(x).getOrElse(0.0)) &
    "name=long" #> SHtml.text(long.toString, (x:String) => long = asDouble(x).getOrElse(0.0)) &
    "#string_query" #> SHtml.text(stringQuery, (x:String) => stringQuery = x) &
    "#start_date" #> SHtml.text(dateFormatter.print(new DateTime(startDate)), (s:String) => startDate = dateFormatter.parseDateTime(s).toDate) &
    "#end_date" #> SHtml.text(dateFormatter.print(new DateTime(endDate)), (s:String) => endDate = dateFormatter.parseDateTime(s).toDate) &
    "#max_distance_input" #> (SHtml.hidden((x:String) => {
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
      SetHtml("main_content", ads.filter(_.lifeTime.is.endDate.compareTo(new Date) >= 0).map(_.getMarkup)) & Focus("search_box")
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
