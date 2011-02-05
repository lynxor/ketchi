package za.co.lastminute.snippet

import  net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import scala.xml.NodeSeq
import za.co.lastminute.model.generic_ad._


class CreateGenericAd{
    var header = "";
    var content = "";
    var contacts = "";
  def render = {
   
    def process : JsCmd = {
      Thread.sleep(200)
      val newGAd = GenericAd.createRecord.header(header).contents(content).contactInfo(contacts).save
      println(newGAd.toString)
      Noop
    }

    "name=header" #> SHtml.text(header, header = _) &
    "name=content" #> SHtml.textarea(content, content = _) &
    "name=contacts" #> (SHtml.text(contacts, contacts = _) ++ SHtml.hidden(() => process))
  }
}
