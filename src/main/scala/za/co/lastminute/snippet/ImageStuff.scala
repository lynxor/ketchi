package za.co.lastminute.snippet

import java.util.Arrays
import java.util.Collections
import com.mongodb.{BasicDBObjectBuilder, DBObject, BasicDBObject}
import com.mongodb.gridfs.{GridFS, GridFSDBFile}
import net.liftweb._
import http._
import common._
import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoDB
import util._
import Helpers._
import za.co.lastminute.model.User
import scala.collection.JavaConversions._

object ImageListing{
  def render = {
    "#images_list" #> findFilesForCurrentUser.map((x) => <div>{x}</div>)
  }

  def findFilesForCurrentUser: Iterable[String] = {
    val objId: String = User._id.toString
    var fileName = "nothing"
    User.currentUserId match {
      case loggedIn:Full[String] => {
         MongoDB.use(DefaultMongoIdentifier) ( db => {
        val fs = new GridFS(db)

        println("finding files for current user -> "+loggedIn.get)
        val query = BasicDBObjectBuilder.start
        .append("user_id", loggedIn.get).get

              val results: java.util.List[GridFSDBFile] =  fs.find(query)
              asScalaIterable(results).map(_.getFilename)
      })
      }
      case Empty => List("You are not logged in")
      case f:Failure => List("Something went wrong - please try again later")
    }
  }
}