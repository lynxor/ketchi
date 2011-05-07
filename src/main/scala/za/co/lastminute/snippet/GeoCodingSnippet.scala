package za.co.lastminute.snippet

import  net.liftweb._
import http._
import com.foursquare.rogue._
import com.google.common.base._
import common._
import scala.xml.NodeSeq
import scala.xml.XML
import util.Helpers._
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import js._
import JsCmds._

case class Address(lat:String, long:String, address:String, city:String, state:String, country:String) {
  def toXhtml()={
    <div class={"address_result ui-corner-all"}>
    <table>
      <tr>
        <td><span>Lat Long</span></td><td><span>{"("+lat +", "+long+")"}</span></td>
      </tr>
      <tr>
        <td><span>Address</span></td><td><span>{address}</span></td>
      </tr>
      <tr>
        <td><span>City</span></td><td><span>{city}</span></td>
      </tr>
      <tr>
        <td><span>State/Province</span></td><td><span>{state}</span></td>
      </tr>
      <tr>
        <td><span>Country</span></td><td><span>{country}</span></td>
      </tr>
      <tr>
        <td><a href={"/general/search?lat="+lat+"&long="+long}>Search with this location</a></td>
      </tr> 
    </table>
    </div>
  }
}

trait GeoCodingService{
  def findLatLong(address:String, province:String): Seq[Address]
  
}
class YahooGeoCodingService extends GeoCodingService{
  
//  <?xml version="1.0"?> 
//<ResultSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:yahoo:maps" xsi:schemaLocation="urn:yahoo:maps http://local.yahooapis.com/MapsService/V1/GeocodeResponse.xsd">
//<Result precision="zip" warning="The exact location could not be found. Here is a nearby neighorhood.">
//<Latitude>-26.099730</Latitude>
//<Longitude>28.043221</Longitude>
//<Address></Address>
//<City>2010 Sandton</City>
//<State>South Africa</State>
//<Zip></Zip>
//<Country>ZA</Country>
//</Result>
//</ResultSet> 
//<!-- ws10.ydn.ac4.yahoo.com compressed/chunked Sat May  7 01:49:20 PDT 2011 --> 
//
  
  val appId = "GUMuZ13V34GAe1ejGhJqyT093q.LKc_CUBbPm2VIIOvTlhjMjfItKYxzVi78s71nEKJ23mQ9BtuOZyXodmbuGBw8aNSei7M-"
  val restfulAddress = "http://local.yahooapis.com/MapsService/V1/geocode"
  
  override def findLatLong(address:String, province:String) : Seq[Address] = {
    
    val stringUrl:String = restfulAddress + 
    "?appid=" + appId + 
    "&street=" + URLEncoder.encode(address, Charsets.UTF_8.displayName)+
    "&state="+URLEncoder.encode(province, Charsets.UTF_8.displayName)
    
    
    
    val xmlResult = XML.load(new URL(stringUrl))
    (xmlResult \ "Result").map( (result) => {
        val long = (result \ "Longitude").text
        val lat = (result \ "Latitude").text
        val address = (result \ "Address").text
        val city = (result \ "City").text
        val state = (result \ "State").text
        val country = (result \ "Country").text
        
        Address(lat,long,address,city,state,country)
      })
  }
  
}
object GeoCodingSnippet {
  private object address extends RequestVar("Sandton")
  private object province extends RequestVar("Gauteng")
  
  val provinces = List("Gauteng", "Limpopo", "Mpumalanga","North West", "Northern Cape", "Western Cape", "Eastern Cape","Free State", "KwaZulu-Natal")
  
  def locationFromName = {
    "#address" #> SHtml.ajaxTextElem(address) &
    "#province" #> SHtml.ajaxSelect(provinces.map((prov) => (prov, prov)), Full("Gauteng"), (prov) => { province.apply(prov);Noop}) &
    "#geocode_button" #> SHtml.ajaxButton("Find", () => {
        val service:GeoCodingService = new YahooGeoCodingService
        val transformedAddress = service.findLatLong(address, province).map(_.toXhtml)
        SetHtml("addressesFound", NodeSeq.fromSeq(transformedAddress.flatMap(_.toList)))
      })
  }
  
}
