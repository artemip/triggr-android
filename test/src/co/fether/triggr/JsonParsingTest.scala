package co.fether.triggr

import org.junit.Test
import org.junit.Assert._
import org.scalatest.junit._
import co.fether.triggr.json.InAppPurchaseInfo

class JsonParsingTest extends JUnitSuite {
  @Test def InAppPurchaseInfoParsingTest() {
    val jsonString = "{" +
      "\"orderId\":\"12999763169054705758.1331621945138764\"," +
      "\"packageName\":\"co.fether.triggr\"," +
      "\"productId\":\"notifications_whatsapp\"," +
      "\"purchaseTime\":1384585858114," +
      "\"purchaseState\":0," +
      "\"purchaseToken\":\"ydjnvlhrjyrmfafeojjtixbu.AO-J1OyV5RssR1zZJs_ZqJAp_j8Q2SnLVevFCBQSiMrZTcZ_n4T5w4a88Qd1Hmq49A9a6BlVVKcjOnnonfKZF0Ug1UfLMkXOfWNTdkSFooclTBY-p2j_mVhVu9W_X83AgA_DfnD5fMcU\"" +
      "}"

    val purchaseInfo = new InAppPurchaseInfo().deserialize(jsonString)

    assertEquals(purchaseInfo.orderId, "12999763169054705758.1331621945138764")
    assertEquals(purchaseInfo.packageName, "co.fether.triggr")
    assertEquals(purchaseInfo.productId, "notifications_whatsapp")
    assertEquals(purchaseInfo.purchaseTime, "1384585858114")
    assertEquals(purchaseInfo.purchaseState, "0")
    assertEquals(purchaseInfo.purchaseToken, "ydjnvlhrjyrmfafeojjtixbu.AO-J1OyV5RssR1zZJs_ZqJAp_j8Q2SnLVevFCBQSiMrZTcZ_n4T5w4a88Qd1Hmq49A9a6BlVVKcjOnnonfKZF0Ug1UfLMkXOfWNTdkSFooclTBY-p2j_mVhVu9W_X83AgA_DfnD5fMcU")
  }
}