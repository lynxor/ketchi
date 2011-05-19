package za.co.lastminute.snippet

import  net.liftweb._
import http._
import com.mongodb.DB
import common._
import util.Helpers._
import mongodb._
import com.mongodb.gridfs._
import za.co.lastminute.model.User


class FileUpload extends Logger {
  
  val acceptedMimeTypes = List("image/jpeg", "image/gif", "image/pjpeg")
  var fileHolder : Box[FileParamHolder] = Empty;
  def render = {
    
    S.param("errorMsg").foreach(S.error(_))
    
    "name=file" #> SHtml.fileUpload((f) => {fileHolder = Full(f)}) &
    "type=submit"  #> SHtml.submit("Upload", () => process)
    
  }

  def process = {
    fileHolder match {
      case f @ Full(fileParam:FileParamHolder) if ( acceptedMimeTypes.contains(fileParam.mimeType)) => {
          info("Got an file: "+fileParam.fileName +" with mime type "+fileParam.mimeType) 
          MongoDB.use(DefaultMongoIdentifier) ( db => {
              val fs = new GridFS(db)
              val inputFile = fs.createFile(fileParam.file)
              inputFile.setContentType(fileParam.mimeType)
              inputFile.setFilename(fileParam.fileName)
              inputFile.getMetaData.put("user_id", User.currentUserId.getOrElse("nobody"))
              inputFile.save
              info("saved input file "+inputFile.toString)
              S.redirectTo("/general/listfiles")
            })
        }
      case f:Full[FileParamHolder] => S.error("This kind of file is not supported. Supported filetypes include jpg and gif")
      case e:Failure => {
          error("ERROR ocurred while uploading" + e.mkString)
          S.error("Could not upload file at this stage. See limitations on uploads below.")
        }
      case Empty => {
          warn("No file in holder")
          S.error("Could not upload file at this stage. Contact the system administrator.")
        }
    }


  }
}


