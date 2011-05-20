package za.co.lastminute.snippet

import  net.liftweb._
import http._
import com.foursquare.rogue._
import com.google.common.base._
import com.google.common.io.CharStreams
import common._
import scala.xml.NodeSeq
import scala.xml.XML
import util.Helpers._
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import js._
import JsCmds._
import net.liftweb.json._
import JsonParser._

case class Address(lat:String, long:String, address:String, city:String, state:String, country:String) {
  def toXhtml={

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
    </table>

  }
  
  def toSearchXhml = {
    <div class="ui-corner-all address_result">
      {toXhtml}  
      <a href={"/general/search?lat="+lat+"&long="+long}>Search with this location</a>
    </div>
  }
  
  def toSetLatLngXhml = {
    <div class="ui-corner-all address_result">
      {toXhtml}
      <input type="button" 
        class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
        style="padding: 3px"
        onclick={"$('#the_lat').val('"+lat+"');$('#the_long').val('"+long+"');"}
        onmouseover="hover(this)"
        onmouseout="unhover(this)"
        value="Set location">
      </input>
    </div>
  }

}

trait GeoCodingService{
  def findLatLong(address:String): Seq[Address]
  
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
  
  override def findLatLong(address:String) : Seq[Address] = {
    
    val stringUrl:String = restfulAddress + 
    "?appid=" + appId + 
    "&street=" + URLEncoder.encode(address, Charsets.UTF_8.displayName)

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

class GoogleGeoCodingService extends GeoCodingService with Logger{
  val restfulAddress = "http://maps.googleapis.com/maps/api/geocode/json"
  
  override def findLatLong(address: String) : Seq[Address] = {
    val stringUrl = restfulAddress +"?address="+ URLEncoder.encode(address, Charsets.UTF_8.displayName)+"&sensor=false"
    
    info("Requesting URL: "+ stringUrl)
    
    val con:URLConnection = new URL(stringUrl).openConnection
    con.setDoOutput(true)
    con.getOutputStream.flush
    con.getOutputStream.close
    
    val jsonResponse = CharStreams.toString(new InputStreamReader(con.getInputStream))
    info(jsonResponse)
    val parsed = parse(jsonResponse)
    
    implicit val formats = DefaultFormats
    val formattedAddress = (parsed \\ "formatted_address").extract[String]
    val acomp = (parsed \\ "address_components").extract[List[AddressComponent]]
    val location = (parsed \\ "location").extract[Location]
    
    val emptyAddressComponent = AddressComponent("","",Nil)
    List(Address(location.lat.toString, location.lng.toString, formattedAddress, 
            acomp.find( _.types.contains("administrative_area_level_2")).getOrElse(emptyAddressComponent).long_name,
            acomp.find( _.types.contains("administrative_area_level_1")).getOrElse(emptyAddressComponent).long_name,
            acomp.find(_.types.contains("country")).getOrElse(emptyAddressComponent).long_name
          ))
          
  }
}

case class AddressComponent(short_name:String, long_name:String, types: List[String])

case class Location(lat: Double, lng:Double)

object GeoCodingSnippet {
  private object address extends RequestVar("Sandton")
  //private object province extends RequestVar("Gauteng")
  val service = new GoogleGeoCodingService
  val provinces = List("Gauteng", "Limpopo", "Mpumalanga","North West", "Northern Cape", "Western Cape", "Eastern Cape","Free State", "KwaZulu-Natal")
  
  def locationFromName = {
    "#address" #> SHtml.ajaxTextElem(address) &
    "#geocode_button" #> SHtml.ajaxButton("Find", () => {
        val transformedAddress = service.findLatLong(address).map(_.toSearchXhml)
        SetHtml("addressesFound", NodeSeq.fromSeq(transformedAddress.flatMap(_.toList)))
      })
  }
  
  def locationFromNameSetLatLong = {
    "#address" #> SHtml.ajaxTextElem(address) &
    "#geocode_button" #> SHtml.ajaxButton("Find", () => {
        val transformedAddress = service.findLatLong(address).map(_.toSetLatLngXhml)
        SetHtml("addressesFound", NodeSeq.fromSeq(transformedAddress.flatMap(_.toList)))
      })
  }
  
}
