package za.co.ketchi.model {



  import _root_.net.liftweb.util._
  import _root_.net.liftweb.common._
//    import net.liftweb.mongodb.record.MongoId
//import net.liftweb.mongodb.record.MongoMetaRecord
//    import net.liftweb.mongodb.record.MongoRecord
//    import net.liftweb.record.MegaProtoUser
//    import net.liftweb.record.MetaMegaProtoUser
  import net.liftweb.http.js.JsCmds.Alert
  import net.liftweb.mongodb.JsonObject
  import net.liftweb.mongodb.JsonObjectMeta
  import net.liftweb.mongodb.record.field.JsonObjectField
  import net.liftweb.mongodb.record.field.MongoCaseClassListField
  import net.liftweb.mongodb.record.field.MongoListField
  import net.liftweb.record.field.EmailField
  import net.liftweb.record.field.StringField
  import scala.xml.NodeSeq
  import scala.xml.Text
  import za.co.ketchi.lib._
  import java.util.Date
  import net.liftweb.http._
  import com.foursquare.rogue.LatLong
  import com.foursquare.rogue.Rogue._
  import za.co.ketchi.snippet.UserSnippets
  import net.liftweb.util._
  import Helpers._




  /**
   * The singleton that has methods for accessing the database
   */
  object User extends User with MetaMegaProtoUser[User] with Logger{

    val Admin = "admin"
    val Client = "client"
    val Normal = "normal"

    override def screenWrap = Full(<lift:surround with="default" at="content">
        <lift:bind /></lift:surround>)
    // define the order fields will appear in forms and output
//      override def fieldOrder = List(firstName, lastName, email,
//                                     locale, timezone, password)

    // comment this line out to require email validations
    override def skipEmailValidation = true

    def isCurrentUserInRole(role:String*): Boolean = {
      User.currentUser match {
        case f @ Full(user) => user.retrieveRoles.foldLeft(false)(_ || role.contains(_))
        case _ => false
      }

    }
   
    def storeSearch(search: StringSearch) = {
      User.currentUser foreach { (user: User) => {
          val currentHistory = user.stringSearches.is
          user.stringSearches.set(search :: currentHistory);
          user.save
        }
      }
    }
    
   
    
    def formLogin: LiftRules.DispatchPF = {
      case Req("form_login" :: Nil, _, PostRequest) if !loggedIn_? =>
        () => {
          (for {
              uname <- S.param("username")
              pw <- S.param("password")
              user <- User where (_.email eqs uname) get

              if user.validated.is &&
              user.password.is.equals(pw)
            } yield user) match {
            case Full(user) => logUserIn(user)
              S.notice("Logged In")
            case _ => S.error("Unable to verify username/password")
          }
          S.redirectTo(S.referer openOr "/")
        }
        
        
    }
    
//    override def signupFields: List[FieldPointerType]= {super.signupFields ::: List(recaptcha)}
//    
    override def signupXhtml(user: TheUserType) = {
      UserSnippets.passedReCaptcha match {
        case true => 
          <div><h2>Sign up</h2>
            <div class="ui-corner-all ui-widget ui-widget-content" style="padding: 15px">
              <form method="post" action={S.uri}>
                <table><tr><td
                      colspan="2"></td></tr>
                  {localForm(user, false, signupFields)}
                  <tr><td>&nbsp;</td><td><user:submit/></td></tr>
                </table></form>
            </div></div>
        case false => <span class="error">You haven't passed the ReCaptcha!</span>
      }
    }
    
    override def signup = {
      val theUser: TheUserType = mutateUserOnSignup(createNewUserInstance())
      val theName = signUpPath.mkString("")

      def testSignup() {
        validateSignup(theUser) match {
          case Nil =>
            actionsAfterSignup(theUser, () => S.redirectTo(homePage))

          case xs => S.error(xs) ; signupFunc(Full(innerSignup _))
            
        }
      }

      def innerSignup = bind("user",
                             signupXhtml(theUser),
                             "submit" -> SHtml.submit(S.??("sign.up"), testSignup _, 
                                                      ("class", "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"),
                                                      ("onmouseover","hover(this)"),
                                                      ("onmouseout", "unhover(this)")))

      innerSignup
    }

    
  }
  class User extends MegaProtoUser[User]{
    def meta = User // what's the "meta" server
   
//    val recaptcha: StringField[User] = new MyRecaptchaField(this, 32)
//    
//    protected class MyRecaptchaField(obj: User, size: Int) extends StringField(obj, size) {
//      override def displayName = owner.lastNameDisplayName
//      override val fieldId = Some(Text("txtRecaptchaField"))
//      override def validations(response: ValueType): List[FieldError] = {
//        if(!ReCaptchaUtil.verifyCaptcha("", response))
//          Text("Response for Recaptcha is invalid!")
//        else
//          Nil
//      }
//      
//      override def toForm: Box[NodeSeq] =
//        Full(SHtml.text("HELLO!!", (s) => Alert("hello "+s)))
//    }

    object roles extends MongoListField[User, String](this){
      override def defaultValue = List(meta.Normal)
    }
    object stringSearches extends MongoCaseClassListField[User, StringSearch](this)
      
    def retrieveRoles:Seq[String] = roles.is
     
  }

  case class StringSearch(searchString:String, date:Date) extends JsonObject[StringSearch]{
    override def meta = StringSearch
  }
  case class LocationSearch(latLong: LatLong, date:Date) extends JsonObject[LocationSearch]{
    override def meta = LocationSearch
  }

  object StringSearch extends JsonObjectMeta[StringSearch]
  object LocationSearch extends JsonObjectMeta[LocationSearch];
}
