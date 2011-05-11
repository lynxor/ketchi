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
  var fileHolder : Box[FileParamHolder] = Empty;
  def render = {
    "name=file" #> SHtml.fileUpload((f) => {Console println("setting fileholder to "+f.toString); fileHolder = Full(f)}) &
    "type=submit"  #> SHtml.submit("Upload", () => process)
    
  }

  def process = {
    val fileId = fileHolder match {

      case a:Full[FileParamHolder] => {
        
          val fileParam: FileParamHolder = a.get
          info("Got an file: "+fileParam.fileName)
          MongoDB.use(DefaultMongoIdentifier) ( db => {
              val fs = new GridFS(db)
              val inputFile = fs.createFile(fileParam.file)
              inputFile.setContentType(fileParam.mimeType)
              inputFile.setFilename(fileParam.fileName)
              inputFile.getMetaData.put("user_id", User.currentUserId.get)

              inputFile.save

              info("saved input file "+inputFile.toString)
              inputFile.getId
            })
        }
      case e:Failure => {
          error("ERROR ocurred while uploading")
          S.error("Invalid receipt attachment")
          0
        }
      case Empty => warn("No file in holder")
    }

    S.redirectTo("/general/listfiles")
  }
}


