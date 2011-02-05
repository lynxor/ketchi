package za.co.lastminute.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb.record.Field
import za.co.lastminute.model.generic_ad.GenericAd
import Helpers._

object GenericAdSnippet {

  def listAll = "#listing *" #> GenericAd.findAll.map((x: GenericAd) => <li class={"borderbox"}>
                                                                          <h3>{x.header}</h3>
                                                                          <div class={"borderbox"}>{x.contents}</div>
                                                                          <br></br>
                                                                          <div class={"borderbox"}>
                                                                          <h4>Contact Information:</h4>
                                                                          <ul>
                                                                            <li><div>{x.contactInfo}</div></li>
                                                                            <li>
                                                                            <div>
                                                                              <a href={"http://"+x.link} target={"_blank"}>link to website</a>
                                                                            </div>
                                                                            </li>
                                                                            <li>
                                                                            <div>
                                                                              <a href={"mailto:"+ x.email}>{x.email}</a>
                                                                            </div>
                                                                            </li>
                                                                          </ul>
                                                                          </div>
                                                                        </li>)

  


}
