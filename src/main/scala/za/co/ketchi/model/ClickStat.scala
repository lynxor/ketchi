package za.co.ketchi.model

import java.util.Date
import net.liftweb.mongodb.JsonObject
import net.liftweb.mongodb.JsonObjectMeta
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field.JsonObjectField
import net.liftweb.mongodb.record.field.ObjectIdField
import net.liftweb.record.field.StringField

object ClickStat extends ClickStat with MongoMetaRecord[ClickStat]{}

class ClickStat extends MongoRecord[ClickStat] with MongoId[ClickStat] {
  def meta = ClickStat
  def primaryKeyField = id

  object refDocId extends ObjectIdField(this)

  object genericAdId extends StringField(this,300)
  object click extends JsonObjectField[ClickStat, AdClick](this, AdClick){
    def defaultValue = AdClick(List(new Date()))
  }

  def date(date:Date):ClickStat = {
    click.is.dates = date :: click.is.dates
    this
  }
}

case class AdClick(var dates:List[Date]) extends JsonObject[AdClick]{
  def meta = AdClick
}
object AdClick extends JsonObjectMeta[AdClick]
