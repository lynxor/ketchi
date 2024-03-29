package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.za.co.ketchi.model.User
import  _root_.za.co.ketchi.model.generic_ad._
import java.io.File
import java.io.FileInputStream
import java.net.URL
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog
import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoAddress
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.MongoHost
import net.liftweb.widgets.flot.Flot
import net.liftweb.widgets.sparklines.Sparklines
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException
import za.co.ketchi.lib.ImageLogic


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "test_direct"))
    
    // where to search snippet
    LiftRules.addToPackages("za.co.ketchi")
   
    def clientFunction = User.loggedIn_? && User.isCurrentUserInRole(User.Client)
    val notLoggedIn = "You are not logged in"
    val clientTest = If(() => clientFunction, S ? notLoggedIn)
    // Build SiteMap
    def sitemap() = SiteMap(
     
      Menu("Home") / "index" >> Hidden,
      Menu.i("Help") / "static"  / "help" >> LocGroup("bottom"),
      Menu.i("Contact") / "static" / "contact" >>LocGroup("bottom"),
      Menu.i("Search") / "general" / "search"  >> LocGroup("search"),
      Menu.i("List all ads") / "generic_ads" / "listing" >> LocGroup("search") ,
      Menu.i("SignupRecaptcha") / "user" / "signup_recaptcha" >> Hidden,
      Menu.i("View") / "generic_ads" / "view" >> Hidden,
      Menu.i("StatsRedirect") / "stats" / "statsredirect" >> Hidden,
      Menu.i("Quick Search") / "general" / "quicksearch" >> Hidden ,
      Menu.i("Error Page") /"static" / "errorpage" >> Hidden,             
      Menu.i("Upload images") /"general" / "fileupload">> clientTest >> LocGroup("client"),
      Menu.i("View your images") /"general" / "listfiles" >> clientTest >> LocGroup("client"),
      Menu.i("View your ads") /"generic_ads" / "client_listing" >> clientTest >> LocGroup("client"),
      Menu.i("Create a new ad") / "generic_ads" / "create" >>clientTest >> LocGroup("client"),
      Menu.i("View Stats") / "stats" / "viewstats" >> clientTest >> LocGroup("client"),
      Menu.i("Admin") / "user" /"admin" >> If(() => User.loggedIn_? && User.isCurrentUserInRole(User.Admin), S ? "Has to be admin" ) >> LocGroup("client"),
      Menu.i("Advertise here!") / "user" / "advertiserequest" >> If(() => !User.loggedIn_?, S ? "Can't do for existing user" ) >> LocGroup("client"),
      Menu.i("GeoCoding test") / "geocode" / "geocode" >> Hidden)
    
    LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap()))

    LiftRules.dispatch.append(ImageLogic.matcher)
    LiftRules.dispatch.append(User.formLogin)

    //LOGGING
    val logUrl = LiftRules.getResource("/logconfig.xml")
    logUrl.foreach((x:URL) => Logger.setup = Full(Logback.withFile(x)))

    
    val localFile = () => {
      val file = new File("/opt/ketchi/config/production.props")
      if (file.exists) Full(new FileInputStream(file)) else Empty
    }
    Props.whereToLook = () => (("local", localFile) :: Nil)

    //EMAIL
    configMailer(Props.get("email.server", "mail.ketchi.co.za"),
                 Props.get("email", "admin@ketchi.co.za"),
                 Props.get("email.password", "admin")); 
    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)
    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    //Initialise support for Flot
    Flot.init
    
    LiftRules.handleMimeFile = OnDiskFileParamHolder.apply
    LiftRules.maxMimeSize = 200 * 1024// 200kb

    LiftRules.early.append(makeUtf8)

    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    

    LiftRules.exceptionHandler.prepend {
      {
        case (_, r, t: SizeLimitExceededException) =>
          
          RedirectResponse("/general/fileupload?errorMsg="+urlEncode("Could not upload - too large"))
      }
    }
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

  def configMailer(host: String, user: String, password: String) {
    // Enable TLS support
    System.setProperty("mail.smtp.starttls.enable","false");
    // Set the host name
    System.setProperty("mail.smtp.host", host) // Enable authentication
    System.setProperty("mail.smtp.auth", "true") // Provide a means for authentication. Pass it a Can, which can either be Full or Empty
    Mailer.authenticator = Full(new Authenticator {
        override def getPasswordAuthentication = new PasswordAuthentication(user, password)
      })
  }
}
