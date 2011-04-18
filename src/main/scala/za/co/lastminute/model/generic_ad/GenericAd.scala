package za.co.lastminute.model.generic_ad

import java.util.Date
import net.liftweb.mapper._
import net.liftweb.mongodb.JsonObject
import net.liftweb.mongodb.JsonObjectMeta
import net.liftweb.mongodb.record.MongoId
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import org.joda.time.DateTime
import scala.xml.Elem
import com.foursquare.rogue.LatLong
import com.foursquare.rogue.Rogue._


class GenericAd extends MongoRecord[GenericAd] with MongoId[GenericAd] {
  def meta = GenericAd


  def primaryKeyField = id
  object refDocId extends ObjectIdField(this)
  object header extends StringField(this, 140)
  object contents extends StringField(this, 300)
  object contactInfo extends StringField(this, 300)
  object email extends EmailField(this, 140)
  object link extends StringField(this, 140)
  object imageId extends StringField(this, 300)
  object tags extends MongoListField[GenericAd, String](this)
  object comments extends MongoCaseClassListField[GenericAd, Comment](this){override def defaultValue:List[Comment] = Nil}
  object location extends MongoCaseClassField[GenericAd, LatLong](this) { override def name = "latlng" }
  object lifeTime extends JsonObjectField[GenericAd, LifeTime](this, LifeTime){
    def defaultValue = LifeTime(new Date, new DateTime().plusDays(1).toDate)
  }
  
  object clickDates extends MongoListField[GenericAd, Date](this){
    override def defaultValue = Nil
  }
  
  object userId extends StringField(this, 140)

  

  def getMarkup(): Elem= {

    <div class={"borderbox ui-widget ui-widget-content ui-corner-all"} >
      <div id={this._id.toString+"_header"} 
        class="ui-widget-header ui-corner-all ad_header ui-state-default"
        onmouseover="hover(this)"
        onmouseout="unhover(this)"
        onclick={"toggleExpandedView('"+this._id.toString+"');"}>
        
        <span >{this.header}</span>
      </div>
      
      <div id={this._id.toString+"_expanded"} style={"display:none;"} class="ui-widget-content ui-corner-bottom">
        <img src={"/images/"+this.imageId} alt="Cannot display image" style="display: block; padding 10px" />
        <div class={"borderbox"}>{this.contents}</div>
        <br></br>
        <div class={"borderbox"}>
          Location : {this.location.is.lat +", "+ this.location.is.long}
        </div>
        <div class={"borderbox"}>
          <h4>Contact Information:</h4>
          <ul>
            <li><div>{this.contactInfo}</div></li>
            <li>
              <div>
                <a href={"/stats/statsredirect?ad_id="+this._id.toString+"&link_url="+this.link.is} target={"_blank"} >Link to website</a>
              </div>
            </li>
            <li>
              <div>
                <a href={"mailto:"+ this.email}>{this.email}</a>
              </div>
            </li>
          </ul>
        </div>
        <div id={this._id.toString+"_comments"} class="lift:Commenting.comment">
          <h2>Comments:</h2>
          <lift:TestCond.loggedin>
            <div style="width: 300px">
              <input id="new_comment_text" type="text" style="display:block" ></input>
              <input id="new_comment_add_button" type="button" style="float: right"></input>
            </div>
         
          </lift:TestCond.loggedin>
          <div id="comments" >comments here</div>
        </div>
      </div>
      
      <!-- compacted -->
      <div id={this._id.toString+"_compacted"} >

        <div class={"borderbox"}>{this.contents.is match {
              case f:String if(f.length > 100) => f.substring(0, 100) + " ..."
              case f:String => f
              case _ => "No content defined"
            }
          }
        </div>    
      </div>
    </div>

  }

}

object GenericAd extends GenericAd with MongoMetaRecord[GenericAd]{}

//case class LatLong(lat:Double, long:Double) //extends JsonObject[LatLong]{
//  def meta = LatLong
//}
//object LatLong extends JsonObjectMeta[LatLong]


case class LifeTime(startDate:Date, endDate:Date) extends JsonObject[LifeTime]{
  def meta = LifeTime
}
object LifeTime extends JsonObjectMeta[LifeTime]{}


case class Comment(commenter:String, comment:String, date:Date) extends JsonObject[Comment]{
  def meta = Comment
}
object Comment extends JsonObjectMeta[Comment]{
//  def apply(tuple: (String, String, Date)){
//    return new Comment
//  }
}
