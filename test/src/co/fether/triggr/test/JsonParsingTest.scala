package co.fether.triggr.test

import org.junit.Assert._
import org.junit.Test
import org.scalatest.junit._
import co.fether.triggr.json._

class JsonParsingTest extends JUnitSuite {
  @Test def InAppPurchaseInfoParsingTest() {
    val jsonString = "{" +
      "\"orderId\":\"12999763169054705758.1375942134516811\"," +
      "\"packageName\":\"co.fether.triggr\"," +
      "\"productId\":\"notifications_whatsapp\"," +
      "\"purchaseTime\":1384654360852," +
      "\"purchaseState\":0," +
      "\"purchaseToken\":\"xpsgbqhpqxlwilgfxbskmavg.AO-J1OyLh3JClt1hcqpSpHKQTyREo1CVJJzqxG1C5hI-03EzipT6CKE08CAH-jvRVtZOqMurGMydMC_O4OW0tKOER31FpyCPXRi5SFJzo-3hXpI4f1XdgFqW9Si7N0tpJ3cYz2N3cMTQ\"" +
      "}"

    val purchaseInfo = new InAppPurchaseInfo().deserialize(jsonString)

    assertEquals("12999763169054705758.1375942134516811", purchaseInfo.orderId)
    assertEquals("co.fether.triggr", purchaseInfo.packageName)
    assertEquals("notifications_whatsapp", purchaseInfo.productId)
    assertEquals("1384654360852", purchaseInfo.purchaseTime)
    assertEquals("0", purchaseInfo.purchaseState)
    assertEquals("xpsgbqhpqxlwilgfxbskmavg.AO-J1OyLh3JClt1hcqpSpHKQTyREo1CVJJzqxG1C5hI-03EzipT6CKE08CAH-jvRVtZOqMurGMydMC_O4OW0tKOER31FpyCPXRi5SFJzo-3hXpI4f1XdgFqW9Si7N0tpJ3cYz2N3cMTQ", purchaseInfo.purchaseToken)
  }
}