package za.co.lastminute.snippet


import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb.http._
import Helpers._
import java.util.Date
import  js._
import JsCmds._
import net.liftweb.widgets.sparklines._
import org.joda.time.DateTime
import org.joda.time.LocalDate
import scala.xml.NodeSeq
import za.co.lastminute.model._
import za.co.lastminute.model.generic_ad._
import net.liftweb.http.js._
import JE._
import net.liftweb.json.JsonDSL._
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import scala.collection.mutable.Map
import com.mongodb.BasicDBObjectBuilder



class StatsRedirect extends Logger{
  def render(in:NodeSeq):NodeSeq = {

    val link:String = S.param("link_url").getOrElse(RedirectTo("/static/errorpage"))
    val adId:String = S.param("ad_id").getOrElse(RedirectTo(link))
    
    info("Storing click for ad :"+adId +" link: "+link)

    val query = BasicDBObjectBuilder.start
    .append("genericAdId", adId).get

    ClickStat.findAll(query) match{
      case f:List[ClickStat] if(f.isEmpty) => ClickStat.createRecord.genericAdId(adId).save //by default creates now click
      case f:List[ClickStat] => {
          val statsItem = f(0)
          statsItem.date(new Date()).save

        }
    }
    S.redirectTo(link);
  }
}

object ViewStats extends Logger{
    private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    
  def render = {

    val adId = GenericAd.find(BasicDBObjectBuilder.start.append("userId",User.currentUserId.get.toString).get).map(_._id.toString).getOrElse("NOADS")
    
  // val adId:String = S.param("ad_id").getOrElse("/static/errorpage")
    info("Generating stats for ad "+adId)
    val query = BasicDBObjectBuilder.start
    .append("genericAdId", adId).get

   
    val dates:List[Date] = ClickStat.find(query).map((x:ClickStat) => x.click.is.dates).getOrElse(Nil)
    val dateToCount = dates.foldLeft(Map[LocalDate, Int]())((map:Map[LocalDate, Int], d:Date) => {
        val localDate:LocalDate = new DateTime(d).toLocalDate
        map.get(localDate) match {
          case f:Some[Int] => map += (localDate -> (f.get+1))
          case None => map += (localDate -> 1)
        }
      })

    val expList:List[JsExp] = dateToCount.map((x) => JsArray(formatter.print(x._1), x._2)).toList

    val data = JsArray(expList:_*)

    val opts = JsObj(("percentage_lines" -> JsArray(0.5, 0.75)),
                     ("fill_between_percentage_lines" -> true),
                     ("extend_markings" -> false));
    Sparklines.onLoad("graph", SparklineStyle.LINE, data, opts);

  }
  
}
