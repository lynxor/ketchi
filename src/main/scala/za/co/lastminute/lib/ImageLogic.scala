package za.co.lastminute.lib

import net.liftweb.http._
import net.liftweb.mapper._
import com.google.common.io._
import com.mongodb.gridfs._
import net.liftweb.common._
import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoDB
import net.liftweb.util.Helpers._
import org.bson.types.ObjectId


object ImageLogic {

  object AdImage {
    def unapply(in: String): Option[GridFSDBFile] =
      MongoDB.use(DefaultMongoIdentifier) ( db => {
          val fs = new GridFS(db)
          Some(fs.findOne(new ObjectId(in.trim)))
        })
    
  }
  def matcher: LiftRules.DispatchPF = {
   
    case req @ Req("images" :: AdImage(img) :: Nil, _, GetRequest) => {
        () => serveImage(img, req)
      }
  }

  def serveImage(img: GridFSDBFile, req: Req) : Box[LiftResponse] = {
    if (!req.testIfModifiedSince(img.getUploadDate.getTime+1)) {
      Full(InMemoryResponse(
          new Array[Byte](0),
          List("Last-Modified" -> toInternetDate(img.getUploadDate.getTime)),
          Nil,
          304))
    } else {
      Full(InMemoryResponse(
          ByteStreams.toByteArray(img.getInputStream),
          List("Last-Modified" -> toInternetDate(img.getUploadDate.getTime),
               "Content-Type" -> "image/jpeg",//img.mimeType.is,
               "Content-Length" -> img.getLength.toString),
    Nil /*cookies*/,
          200)
      )
    }
  }

}