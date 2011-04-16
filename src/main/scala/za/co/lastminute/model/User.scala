package za.co.lastminute {
  package model {


    import _root_.net.liftweb.util._
    import _root_.net.liftweb.common._
//    import net.liftweb.mongodb.record.MongoId
//import net.liftweb.mongodb.record.MongoMetaRecord
//    import net.liftweb.mongodb.record.MongoRecord
//    import net.liftweb.record.MegaProtoUser
//    import net.liftweb.record.MetaMegaProtoUser
    import net.liftweb.mongodb.JsonObject
    import net.liftweb.mongodb.JsonObjectMeta
    import net.liftweb.mongodb.record.field.JsonObjectField
    import net.liftweb.mongodb.record.field.MongoListField
import net.liftweb.record.field.StringField
    import za.co.lastminute.lib._
    import net.liftweb.http._
    import com.foursquare.rogue.Rogue._


    /**
     * The singleton that has methods for accessing the database
     */
    object User extends User with MetaMegaProtoUser[User]{

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

    }

    class User extends MegaProtoUser[User]{
      def meta = User // what's the "meta" server

     
      object roles extends MongoListField[User, String](this){
        override def defaultValue = List(meta.Normal)
      }

      def retrieveRoles:Seq[String] = roles.is
     
    }
  }

 
}
