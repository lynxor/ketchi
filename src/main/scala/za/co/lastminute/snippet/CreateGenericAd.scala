package za.co.lastminute.snippet

import  net.liftweb._
import http._
import common._
import scala.util.matching.Regex
import scala.xml.NodeSeq
import util.Helpers._
import js._
import JsCmds._
import JE._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import za.co.lastminute.model.generic_ad._
import com.foursquare.rogue.LatLong
import za.co.lastminute.model.User


object CreateGenericAd{
  private val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");
  private val excludeWords = List("THE", "OR", "AND", "IN")  //Get a list somewhere
  private val punctuationRegex  = new Regex("[\\w]+")
  
  private object header extends RequestVar("")
  private object content extends RequestVar("")
  private object contacts extends RequestVar("")
  private object link extends RequestVar("")
  private object email extends RequestVar("")
  private object lat extends RequestVar("-26.195308")
  private object long extends RequestVar("28.043861")
  private object startDate extends RequestVar(dateFormatter.print(new DateTime()))
  private object endDate extends RequestVar(dateFormatter.print(new DateTime().plusDays(7)))
  private object tags extends RequestVar("special, amazing")
  
  private object address extends RequestVar("Sandton")
  private object province extends RequestVar("Gauteng")
  private object whence extends RequestVar(S.referer openOr "/")

  
  def render = {


    var imageId:Box[String] = Empty
    var currentAd: Box[GenericAd] = Empty
    val w = whence.is
    
    currentAd = tryToInitFromParams
    currentAd match{
      case Full(ad:GenericAd) => {
          header.set(ad.header.is)
          content.set(ad.contents.is)
          contacts.set(ad.contactInfo.is)
          link.set(ad.link.is)
          email.set(ad.email.is)
          lat.set(ad.location.get.lat.toString)
          long.set(ad.location.get.long.toString)
          startDate.set(dateFormatter.print(new DateTime(ad.lifeTime.get.startDate)))
          endDate.set(dateFormatter.print(new DateTime(ad.lifeTime.get.endDate)))
          tags.set(ad.tags.is.join(","))
          imageId = Full(ad.imageId.toString)
        }
      case _ => Empty
    
    }
    
    def tryToInitFromParams:Box[GenericAd] = {
      S.param("ad_id") match{
        case Full(a: String) => {
            GenericAd.find(a)
          }
        case _ => Empty


      }
    }
    
    def process = {

      val newGAd = currentAd match {
        case f:Full[GenericAd] => f.get
        case _ => {
            GenericAd.createRecord
          }
      };
      
      var validated = true
      if(header.isEmpty)
         S.error("Please provide a header for your add")

    
      

      newGAd.header(header)
      .contents(content)
      .contactInfo(contacts)
      .email(email)
      .link(link)
      .location(LatLong(asDouble(lat).get, asDouble(long).get))
      .lifeTime(LifeTime(dateFormatter.parseDateTime(startDate.get).toDate, dateFormatter.parseDateTime(endDate.get).toDate))
      .userId(User.currentUserId.get.toString)
      .imageId(imageId)
      .tags(tags.is.split(",").toList.map(_.toUpperCase) :::
            punctuationRegex.findAllIn(content).map(_.toUpperCase).filter(!excludeWords.contains(_)).toList)  //get a library to do this better
      .save

      S.redirectTo("/generic_ads/view?ad_id="+newGAd._id.toString)
    }
 
    "name=header" #> (SHtml.textElem(header) ++ SHtml.hidden(() => whence.set(w))) &
    "name=content" #> SHtml.textareaElem(content) &
    "name=contacts" #> SHtml.textElem(contacts) &
    "name=link" #> SHtml.textElem(link) &
    "name=email" #> SHtml.textElem(email) &
    "name=lat" #> SHtml.textElem(lat) &
    "name=long" #> SHtml.textElem(long) &
    "name=startDate" #> SHtml.textElem(startDate) &
    "name=endDate" #> SHtml.textElem(endDate) &
    "name=imageId" #> SHtml.hidden((x:String) => {imageId = Full(x)}, "") &
    "name=tags" #> SHtml.textElem(tags) &
    "#create_ad_button"  #> SHtml.onSubmitUnit(() => process)
  }

   
  
}
