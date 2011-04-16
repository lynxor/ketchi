package za.co.lastminute.snippet

import  net.liftweb._
import http._
import js._
import JsCmds._
import common._
import org.bson.types.ObjectId
import util.Helpers._

import za.co.lastminute.model.User;
import com.foursquare.rogue.Rogue._
    
object UserSnippets {

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
}
