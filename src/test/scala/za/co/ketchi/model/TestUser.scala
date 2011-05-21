//package za.co.lastminute.model
//
//import org.junit.Assert
//import org.junit.Test;
//
//import za.co.lastminute.model.User
//
//class TestUser {
//
//  @Test
//  def testIsInRole(){
//    val user:User = new User
//
//    Assert.assertTrue(user.isInRole("normal"))
//    Assert.assertTrue(user.isInRole(User.Normal))
//    Assert.assertTrue(user.isInRole(User.Normal, User.Admin, User.Client))
//
//    Assert.assertFalse(user.isInRole(User.Admin))
//    Assert.assertFalse(user.isInRole(User.Admin, User.Client))
//
//
//  }
//}
