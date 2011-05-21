//package za.co.ketchi.snippet
//
//import com.google.common.io.CharStreams
//import java.io.InputStreamReader
//import org.specs2.mutable._
//import org.specs2.specification._
//import org.specs2.execute.Success
//import za.co.ketchi.snippet._
//import net.liftweb.json._
//import JsonParser._
//
//
//class GeoCodingSpec extends Specification{
//
//  val resultJson = CharStreams.toString(new InputStreamReader(classOf[GeoCodingSpec].getResourceAsStream("test_response.json")))
//  val service = GoogleGeoCodingService
//  
//  val addresses = service.fromJSON(parse(resultJson))
//  
//  "decoding a json geocoding result" should {
//    "contain return a list of addresses" in {
//      addresses mustEqual(Nil)
//    }
//  }
//}
