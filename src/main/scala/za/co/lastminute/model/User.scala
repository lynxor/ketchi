package za.co.lastminute {
  package model {


    import _root_.net.liftweb.util._
    import _root_.net.liftweb.common._
//    import net.liftweb.mongodb.record.MongoId
//import net.liftweb.mongodb.record.MongoMetaRecord
//    import net.liftweb.mongodb.record.MongoRecord
//    import net.liftweb.record.MegaProtoUser
//    import net.liftweb.record.MetaMegaProtoUser
    import za.co.lastminute.lib._

    /**
     * The singleton that has methods for accessing the database
     */
    object User extends User with MetaMegaProtoUser[User]{


      override def screenWrap = Full(<lift:surround with="default" at="content">
          <lift:bind /></lift:surround>)
      // define the order fields will appear in forms and output
     // override def fieldOrder = List(id, firstName, lastName, email,
      //                               locale, timezone, password)

      // comment this line out to require email validations
      override def skipEmailValidation = true

    }

    /**
     * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
     */
    class User extends MegaProtoUser[User]{
      def meta = User // what's the "meta" server
    }

  }
}
