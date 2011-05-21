package za.co.ketchi.snippet

import com.google.common.io.CharStreams
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import net.liftweb.http.S

object ReCaptchaUtil {
  
  import java.io.OutputStreamWriter
  import java.net.HttpURLConnection
  import java.net.URL
    
  val privateKey = "6Lf6PMQSAAAAADJVlOT8_L_ByeGjOun_uOA5SsiS"
  val publicKey = "6Lf6PMQSAAAAANYmb-BYDxXHkq1y4IiYBfORxk9Y"

  def verifyCaptcha(challenge: String, response: String) : Boolean = {
    val url = new URL("http://www.google.com/recaptcha/api/verify")
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.setRequestMethod("POST")
    val writer = new OutputStreamWriter(connection.getOutputStream)
    writer.write("privatekey=" + privateKey)
    writer.write("&remoteip=" + S.containerRequest.map(_.remoteAddress)
                 .openOr("localhost"))
    writer.write("&challenge=" + challenge)
    writer.write("&response=" + response)
    writer.flush
    writer.close

    if (connection.getResponseCode != HttpURLConnection.HTTP_OK) {
      error("Connection problem to reCaptcha")
      false	
    }
    val httpResponse = CharStreams.toString(new InputStreamReader(connection.getInputStream))
    httpResponse match {
      case s:String if(s.trim.contains("true") || s.trim.contains("success")) => true
      case _ => false
    }
  }

}
