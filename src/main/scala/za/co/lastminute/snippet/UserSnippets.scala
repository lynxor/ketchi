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

    val users = User fetch
    val selectItems = users.map((u:User) => (u._id.toString, u.email.toString))
    "name=user_list" #> SHtml.ajaxSelect(selectItems, None, (s:String) => {
        selectedUser = (User where (_._id eqs new ObjectId(s)) get);
        Noop
      }) &
    "name=promote_selected" #> SHtml.ajaxButton("Promote selected", () => promoteUser)
  }
}
