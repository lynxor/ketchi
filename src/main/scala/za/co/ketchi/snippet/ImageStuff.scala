package za.co.ketchi.snippet

import java.util.Arrays
import java.util.Collections
import com.mongodb.{BasicDBObjectBuilder, DBObject, BasicDBObject}
import com.mongodb.gridfs.{GridFS, GridFSDBFile}
import net.liftweb._
import http._
import common._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.http.js.JsCmds.RedirectTo
import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoDB
import scala.xml.NodeSeq
import util._
import Helpers._
import za.co.ketchi.model.User
import org.bson.types.ObjectId
import scala.collection.JavaConversions._

object ImageListing extends Logger{
  def render = {
    "#images_list" #> {findFilesForCurrentUser match {
        case a @ Full(i:Iterable[GridFSDBFile]) => i.map((x:GridFSDBFile) => (
              <div class="lift:ImageListing.deleteImageLink">
                <img src={"/images/"+x.getId.toString} alt={x.getFilename}></img>
                <div id={"delete_image"} image_id={x.getId.toString}></div>
              </div>))
        case _ => (<span>Could not retrieve files</span>)
      }}}

  
  def asSelectTable ={
    "#select_rows" #> {findFilesForCurrentUser match {
        case Full( iter : Iterable[GridFSDBFile]) => iter.map((x: GridFSDBFile) => (<tr><td><input value={x.getId.toString} name={"image"} type={"radio"}></input><span>{x.getFilename}</span></td><td><img src={"/images/"+x.getId.toString} alt={x.getFilename}></img></td></tr>))
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
    
  def deleteImageLink(incoming:NodeSeq) : NodeSeq = {
    val imageId = incoming \\ "@image_id" match {
      case null => warn("Could not extract id for image"); "no id"
      case s:NodeSeq => s.toString
    }
    val value = "#delete_image" #> SHtml.a(() => {
        MongoDB.use(DefaultMongoIdentifier) ( db => {
            val fs = new GridFS(db)
            fs.remove(new ObjectId(imageId))
            RedirectTo("/general/listfiles")
          })
      }, <span>delete</span>)
    
    value.apply(incoming)
  }
}