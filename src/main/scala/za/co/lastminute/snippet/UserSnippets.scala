package za.co.lastminute.snippet

import  net.liftweb._
import http._
import java.io.BufferedReader
import java.io.InputStreamReader
import js._
import JsCmds._
import com.google.common.io.CharStreams
import common._
import org.bson.types.ObjectId
import util.Helpers._

import za.co.lastminute.model.User;
import com.foursquare.rogue.Rogue._
import net.liftweb.util.Mailer
import Mailer._
    
object UserSnippets extends Logger {

  def greeting = {
    "#greeting" #> <span>Hello {User.currentUser match {
          case f @ Full(user) => user.firstName
          case Empty => ""
          case f:Failure => ""
        }
      }</span>
  }
  
  def admin = {
    var selectedUser:Option[User] = None

    def promoteUser:JsCmd = {
      selectedUser match {
        case None => SetHtml("promote_notification", <span>{"Could not promote user"}</span>)
        case f:Some[User] => {
            val user = f.get
            user.roles.set(User.Client :: user.roles.is)
            user.save

            SetHtml("promote_notification", <span>{"Success!"}</span>)
          }
      }
    }

    def userinfo = {
      selectedUser match {
        case f @ Some(user) => <span> name : {user.firstName + " " +user.lastName}</span>
        case None => <span>No user selected</span>
      }
    }

    val users = User where (_.roles size 1) fetch
    
    val selectItems = ("", "Please select a user") :: users
    .filter(!_.roles.is.contains("client", "admin"))
    .map((u:User) => (u._id.toString, u.email.toString))
    
    "name=user_list" #> SHtml.ajaxSelect(selectItems, None, (s:String) => {
        selectedUser = (User where (_._id eqs new ObjectId(s)) get);
        SetHtml("user_info", userinfo)
      }) &
    "name=promote_selected" #> SHtml.ajaxButton("Promote selected", () => promoteUser)
  }

  def advertiseRequest = {
    var requestText = "";
    var email = "";
    var challenge = ""
    var challengeResponse = ""

    
    
    "#email" #> SHtml.text("text", (text:String) => {email = text; Noop}) &
    "#request_par" #> SHtml.textarea("Enter your request details here", (text: String) => {requestText = text; Noop}) &
    "#challenge" #> SHtml.hidden((s:String) => challenge = s, "") &
    "#challenge_response" #> SHtml.hidden( (s:String) => challengeResponse = s, "") &
    "#request_button" #> SHtml.submit("Submit", () => {
        if(email.isEmpty || requestText.isEmpty){
          S.error("Please fill in all fields")
        }
        else if(verifyCaptcha(challenge, challengeResponse)){
          info("Captcha verified!!!!!!!!!!!!!!!")
          
          sendMail(From("admin@ketchi.co.za"), Subject("Request for advertising"), To("dawid.malan@ketchi.co.za"),
                   PlainMailBodyType("email: "+email+"\n"+"request: "+requestText));

     
          S.notice("Your request has been submitted successfully")
          S.redirectTo("/index")
        }
        else{
          S.error("Recaptcha failed")
        }
      }) 
   
  }

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
