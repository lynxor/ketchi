package za.co.lastminute.snippet

import  net.liftweb._
import http._
import com.mongodb.DB
import common._
import util.Helpers._
import mongodb._
import com.mongodb.gridfs._
import za.co.lastminute.model.User


class FileUpload {
  var fileHolder : Box[FileParamHolder] = Empty;
  def render = {
    "name=file" #> SHtml.fileUpload((f) => {Console println("setting fileholder to "+f.toString); fileHolder = Full(f)}) &
    "type=submit"  #> SHtml.submit("dUpload", () => process)
    
  }

  def process = {
    val fileId = fileHolder match {

      case a:Full[FileParamHolder] => {
          val fileParam: FileParamHolder = a.get
          println("MIME TYPE "+fileParam.mimeType)
          MongoDB.use(DefaultMongoIdentifier) ( db => {
              val fs = new GridFS(db)
              val inputFile = fs.createFile(fileParam.file)
              inputFile.setContentType(fileParam.mimeType)
              inputFile.setFilename(fileParam.fileName)
              inputFile.getMetaData.put("user_id", User.currentUserId.get)

              inputFile.save

              println("saved input file "+inputFile.toString)
              inputFile.getId
            })
        }
      case _ => {
          print("ERROR ocurred while uploading")
          print(_)
          S.error("Invalid receipt attachment")
          0
        }
    }

    println("File Id from GridFS"+fileId)
    S.redirectTo("/general/listfiles")
  }
}


