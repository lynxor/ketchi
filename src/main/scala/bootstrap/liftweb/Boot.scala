package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.za.co.lastminute.model.User
import  _root_.za.co.lastminute.model.generic_ad._
import java.net.URL
import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoAddress
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.MongoHost
import net.liftweb.widgets.flot.Flot
import net.liftweb.widgets.sparklines.Sparklines
import za.co.lastminute.lib.ImageLogic


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "test_direct"))
    
    // where to search snippet
    LiftRules.addToPackages("za.co.lastminute")
   
    // Build SiteMap
    def sitemap() = SiteMap(
     
      Menu("Home") / "index" >>  User.AddUserMenusAfter,
      // Menu with special Link
      Menu.i("Help") / "static"  / "help",
      Menu.i("Contact") / "static" / "contact",
      Menu.i("Listing") / "generic_ads" / "listing",
      Menu.i("View") / "generic_ads" / "view" >> Hidden,
      Menu.i("StatsRedirect") / "stats" / "statsredirect" >> Hidden,
      Menu.i("Upload images") /"general" / "fileupload", //>> If(() => User.loggedIn_? && User.isInRole(User.Client, User.Admin), S ? "Can't View now"),
      Menu.i("List images") /"general" / "listfiles", //>> If(() => User.loggedIn_? && User.isInRole(User.Admin), S ? "Can't View now"),
      Menu.i("Error Page") /"static" / "errorpage" >> Hidden,
      Menu.i("Create generic ad") / "generic_ads" / "create", //>> If(() => User.loggedIn_?, S ? "Can't View now"),
      Menu.i("Search") / "general" / "search" ,
      Menu.i("Quick Search") / "general" / "quicksearch" ,
      Menu.i("View Stats") / "stats" / "viewstats")
    LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap()))

    LiftRules.dispatch.append(ImageLogic.matcher)

    val logUrl = LiftRules.getResource("/logconfig.xml")
    logUrl.foreach((x:URL) => Logger.setup = Full(Logback.withFile(x)))
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

    LiftRules.early.append(makeUtf8)

    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    

  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
