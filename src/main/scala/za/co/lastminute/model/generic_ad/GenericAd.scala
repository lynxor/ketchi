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
import scala.xml.NodeSeq
import scala.xml.XML


class GenericAd extends MongoRecord[GenericAd] with MongoId[GenericAd] {
  def meta = GenericAd


  def primaryKeyField = id
  object refDocId extends ObjectIdField(this)
  object header extends StringField(this, 140)
  object contents extends StringField(this, 300)
  object contactInfo extends StringField(this, 300)
  object email extends EmailField(this, 140)
  object link extends StringField(this, 140)
  object location extends JsonObjectField[GenericAd, LatLong](this, LatLong) {
    def defaultValue = LatLong(0.0,0.0)
  }
  object lifeTime extends JsonObjectField[GenericAd, LifeTime](this, LifeTime){
    def defaultValue = LifeTime(new Date, new DateTime().plusDays(1).toDate)
  }
  object userId extends StringField(this, 140)

  

  def getMarkup(): Elem= {

    <div class={"borderbox"}>
      <h3>{this.header}</h3>
      <div class={"borderbox"}>{this.contents}</div>
      <br></br>
      <div class={"borderbox"}>
        <h4>Contact Information:</h4>
        <ul>
          <li><div>{this.contactInfo}</div></li>
          <li>
            <div>
              <a href={"http://"+this.link} target={"_blank"}>link to website</a>
            </div>
          </li>
          <li>
            <div>
              <a href={"mailto:"+ this.email}>{this.email}</a>
            </div>
          </li>
        </ul>
      </div>
    </div>

  }

}

object GenericAd extends GenericAd with MongoMetaRecord[GenericAd]{}

case class LatLong(lat:Double, long:Double) extends JsonObject[LatLong]{
  def meta = LatLong
}
object LatLong extends JsonObjectMeta[LatLong]


case class LifeTime(startDate:Date, endDate:Date) extends JsonObject[LifeTime]{
  def meta = LifeTime
}
object LifeTime extends JsonObjectMeta[LifeTime]

