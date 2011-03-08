package za.co.lastminute.snippet


import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb.http._
import Helpers._
import java.util.Date
import  js._
import JsCmds._
import net.liftweb.widgets.flot.{Flot, FlotOptions, FlotSerie, FlotAxisOptions}
import net.liftweb.widgets.sparklines._
import org.bson.types.ObjectId
import org.joda.time._
import scala.xml.NodeSeq
import za.co.lastminute.model._
import za.co.lastminute.model.generic_ad._
import net.liftweb.http.js._
import JE._
import net.liftweb.json.JsonDSL._
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import scala.collection.mutable.Map
import com.foursquare.rogue.Rogue._


class StatsRedirect extends Logger{
  def render(in:NodeSeq):NodeSeq = {

    val link:String = S.param("link_url").getOrElse(RedirectTo("/static/errorpage"))
    val adId:String = S.param("ad_id").getOrElse(RedirectTo(link))
    
    info("Storing click for ad : "+adId +" link: "+link)

    (GenericAd where (_._id eqs new ObjectId(adId)) get) match{
      case g:Some[GenericAd] => {
          val currentValue = g.get.clickDates.is
          g.get.clickDates.set(new Date() :: currentValue)
          g.get.save
        }
      case None => RedirectTo("/static/errorpage")
    }
    S.redirectTo(link);
  }
}

object ViewStats extends Logger{
  private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    
  def render(xhtml: NodeSeq) = {
    var adData:List[List[(Double, Double)]] = Nil

    val ads:List[GenericAd] = GenericAd where (_.userId eqs User.currentUserId.getOrElse("guest")) fetch

    val allPlot = ads.map(transformAd(_)).map((x:List[(Double, Double)]) => new FlotSerie(){override val data = x})

    val options = new FlotOptions () {
      override val xaxis = Full (new FlotAxisOptions () {
          override val mode = Full ("time")
        })
    }

    Flot.render ( "graph", allPlot, options, Flot.script(xhtml))

  }


  def transformAd(ad:GenericAd): List[(Double, Double)] = {

    info("Generating stats for ad "+ad.header.is)

    val dates:List[LocalDate] = ad.clickDates.is.map(new DateTime(_).toLocalDate).sortWith((item1,item2) => item1.compareTo(item2) < 0)

    if(dates.length == 0) return Nil

    val startDate = dates(0)
    val endDate = dates(dates.length-1)

    val dateToCount = dates.foldLeft(Map[LocalDate, Int]())((map:Map[LocalDate, Int], date:LocalDate) => {
        map.get(date) match {
          case f:Some[Int] => map += (date -> (f.get+1))
          case None => map += (date -> 1)
        }
      })

    for ( i <- 0 to Days.daysBetween(startDate, endDate).getDays){
      val dt:LocalDate = startDate.plusDays(i)
      dateToCount.get(dt) match {
        case None => dateToCount += (dt -> 0); info("Adding date "+dt)
        case f:Some[Int] => info("Matched something!")
      }
    }

    dateToCount.map((x) => (x._1.toDateTimeAtStartOfDay.toDate.getTime.toDouble, x._2.toDouble)).toList
    .sortWith((item1,item2) => item1._1 < item2._1)  
  }
 
}
