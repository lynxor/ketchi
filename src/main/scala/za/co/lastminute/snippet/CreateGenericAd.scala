package za.co.lastminute.snippet

import  net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import net.liftweb.sitemap.Menu
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import scala.None
import scala.xml.NodeSeq
import za.co.lastminute.model.User
import za.co.lastminute.model.generic_ad._


object CreateGenericAd{
  
  private object header extends RequestVar("")
  private object content extends RequestVar("")
  private object contacts extends RequestVar("")
  private object link extends RequestVar("")
  private object email extends RequestVar("")
  private object lat extends RequestVar("")
  private object long extends RequestVar("")
  private object startDate extends RequestVar("")
  private object endDate extends RequestVar("")
  

  private val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");

   
    
  def render = {


    var imageId:Box[String] = Empty
    var currentAd: Box[GenericAd] = Empty

    currentAd = tryToInitFromParams
    currentAd match{
      case f:Full[GenericAd] => {
          val ad = f.get
          header.set(ad.header.toString)
          content.set(ad.contents.toString)
          contacts.set(ad.contactInfo.toString)
          link.set(ad.link.toString)
          email.set(ad.email.toString)
          lat.set(ad.location.get.lat.toString)
          long.set(ad.location.get.long.toString)
          startDate.set(dateFormatter.print(new DateTime(ad.lifeTime.get.startDate)))
          endDate.set(dateFormatter.print(new DateTime(ad.lifeTime.get.endDate)))
          imageId = Full(ad.imageId.toString)
        }
      case _ => Empty
    
    }
    
    def tryToInitFromParams:Box[GenericAd] = {
      S.param("ad_id") match{
        case a: Full[String] => {
            GenericAd.find(a.get)
          }
        case a:Failure => println("Error occurred trying to edit ad "+a.toString);Empty
        case Empty => Empty

      }
    }
    
    def process = {

      val newGAd = currentAd match {
        case f:Full[GenericAd] => f.get
        case _ => {
            GenericAd.createRecord
          }
      }

      newGAd.header(header)
      .contents(content)
      .contactInfo(contacts)
      .email(email)
      .link(link)
      .location(LatLong(asDouble(lat).get, asDouble(long).get))
      .lifeTime(LifeTime(dateFormatter.parseDateTime(startDate.get).toDate, dateFormatter.parseDateTime(endDate.get).toDate))
      .userId(User.currentUserId.get.toString)
      .imageId(imageId)
      .save

      S.redirectTo("/generic_ads/view?ad_id="+newGAd._id.toString)
    }

    "name=header" #> SHtml.textElem(header) &
    "name=content" #> SHtml.textareaElem(content) &
    "name=contacts" #> SHtml.textElem(contacts) &
    "name=link" #> SHtml.textElem(link) &
    "name=email" #> SHtml.textElem(email) &
    "name=lat" #> SHtml.textElem(lat) &
    "name=long" #> SHtml.textElem(long) &
    "name=startDate" #> SHtml.textElem(startDate) &
    "name=endDate" #> SHtml.textElem(endDate) &
    "name=imageId" #> SHtml.hidden((x:String) => {imageId = Full(x)}, "") &
    "type=submit"  #> SHtml.onSubmitUnit(() => process)
  }

   
  
}
