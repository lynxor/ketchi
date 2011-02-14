package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}
import _root_.java.sql.{Connection, DriverManager}
import _root_.za.co.lastminute.model._

import  _root_.za.co.lastminute.model.generic_ad._
import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoAddress
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.MongoHost
import za.co.lastminute.lib.ImageLogic


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    // DB.defineConnectionManager(DefaultConnectionIdentifier, MySqlManager)
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
       Menu.i("Preview") / "generic_ads" / "preview" >> Hidden,
       Menu.i("Upload images") /"general" / "fileupload" >> If(() => User.loggedIn_?, S ? "Can't View now"),
       Menu.i("List images") /"general" / "listfiles" >> If(() => User.loggedIn_?, S ? "Can't View now"),
       Menu.i("Create generic ad") / "generic_ads" / "create",
       Menu.i("Search") / "general" / "search" )
     LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap()))

    LiftRules.dispatch.append(ImageLogic.matcher)
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
