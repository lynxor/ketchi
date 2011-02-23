package za.co.lasminute.snippet


import org.joda.time._
import format._

import org.junit.Assert
import org.junit.Test;


class TestCreateAd {

  private val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");


  @Test
  def testFromString(){
    val dateString = "20110208"
    val dt:DateTime = dateFormatter.parseDateTime(dateString)

    Assert.assertEquals(new LocalDate(2011,2,8).toDateTimeAtStartOfDay, dt)

  }
}
