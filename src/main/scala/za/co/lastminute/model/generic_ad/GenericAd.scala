package za.co.lastminute.model.generic_ad

import net.liftweb.mapper._
import net.liftweb.mongodb.record.MongoId
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._


class GenericAd extends MongoRecord[GenericAd] with MongoId[GenericAd] {
  def meta = GenericAd


  def primaryKeyField = id
  object refDocId extends ObjectIdField(this)
  object header extends StringField(this, 140)
  object contents extends StringField(this, 300)
  object contactInfo extends StringField(this, 300)
  object email extends StringField(this, 140)
  object link extends StringField(this, 140)

}

object GenericAd extends GenericAd with MongoMetaRecord[GenericAd]{}

