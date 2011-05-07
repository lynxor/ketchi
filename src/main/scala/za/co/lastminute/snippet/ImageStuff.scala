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
import scala.xml.NodeSeq
import util._
import Helpers._
import za.co.lastminute.model.User
import scala.collection.JavaConversions._

object ImageListing{
  def render = {
    "#images_list" #> {findFilesForCurrentUser match {
        case a @ Full(i:Iterable[GridFSDBFile]) => i.map((x:GridFSDBFile) => (<div><img src={"/images/"+x.getId.toString} alt={x.getFilename}></img></div>))
        case _ => (<span>Could not retrieve files</span>)
      }}}

  
  def asSelectTable ={
    "#select_rows" #> {findFilesForCurrentUser match {
        case a:Full[Iterable[GridFSDBFile]] => a.get.map((x: GridFSDBFile) => (<tr><td><input value={x.getId.toString} name={"image"} type={"radio"}></input><span>{x.getFilename}</span></td><td><img src={"/images/"+x.getId.toString} alt={x.getFilename}></img></td></tr>))
        case Empty => (<span>Could not retrieve files</span>)
        case f:Failure => (<span>Error occurred {f.toString} </span>)
      }}}



    def findFilesForCurrentUser: Box[Iterable[GridFSDBFile]] = {
      val objId: String = User._id.toString
      var fileName = "nothing"
      User.currentUserId match {
        case loggedIn:Full[String] => {
            MongoDB.use(DefaultMongoIdentifier) ( db => {
                val fs = new GridFS(db)
                val query = BasicDBObjectBuilder.start
                .append("user_id", loggedIn.get).get

                val results: java.util.List[GridFSDBFile] =  fs.find(query)
                Full(asScalaIterable(results)) //.map((x) => <div><img src={"/images/"+x.getId.toString} alt={x.getFilename}></img></div>)
              })
          }
        case _ => Empty
      }
    }
  }