package za.co.lastminute.snippet

import  net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import net.liftweb.sitemap.Menu
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import scala.None
import scala.xml.NodeSeq
import za.co.lastminute.model.User
import za.co.lastminute.model.generic_ad._


object CreateGenericAd{
  
  private object header extends SessionVar("")
  private object content extends SessionVar("")
  private object contacts extends SessionVar("")
  private object link extends SessionVar("")
  private object email extends SessionVar("")
  private object lat extends SessionVar("")
  private object long extends SessionVar("")
  private object startDate extends SessionVar("")
  private object endDate extends SessionVar("")


  private var currentAd: Box[GenericAd] = Empty

  private val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");

  
  def render = {
    "name=header" #> SHtml.textElem(header) &
    "name=content" #> SHtml.textareaElem(content) &
    "name=contacts" #> SHtml.textElem(contacts) &
    "name=link" #> SHtml.textElem(link) &
    "name=email" #> SHtml.textElem(email) &
    "name=lat" #> SHtml.textElem(lat) &
    "name=long" #> SHtml.textElem(long) &
    "name=startDate" #> SHtml.textElem(startDate) &
    "name=endDate" #> SHtml.textElem(endDate) &
    "type=submit"  #> SHtml.onSubmitUnit(() => process)
  }

  private def process = {

    val newGAd = GenericAd.createRecord.header(header)
    .contents(content)
    .contactInfo(contacts)
    .email(email)
    .link(link)
    .location(LatLong(asDouble(lat).get, asDouble(long).get))
    .lifeTime(LifeTime(dateFormatter.parseDateTime(startDate.get).toDate, dateFormatter.parseDateTime(endDate.get).toDate))
    .userId(User.currentUserId.get.toString)
    .save
    // println(newGAd.toString)
    currentAd = Full(newGAd)
     
    S.redirectTo("/generic_ads/preview?ad_id="+newGAd._id.toString)
  }
  
  
}
