package jp.co.lepidum.mochizuki.apptestbyscala.tests

import jp.co.lepidum.mochizuki.apptestbyscala._
import junit.framework.Assert._
import _root_.android.test.AndroidTestCase
import _root_.android.test.ActivityInstrumentationTestCase2

class AndroidTests extends AndroidTestCase {
  def testPackageIsCorrect() {
    assertEquals("jp.co.lepidum.mochizuki.apptestbyscala", getContext.getPackageName)
  }
}

class ActivityTests extends ActivityInstrumentationTestCase2(classOf[MainActivity]) {
   def testHelloWorldIsShown() {
      val activity = getActivity
      val textview = activity.findView(TR.textview)
      val a = Some(1)
      assertEquals(textview.getText, "hello, world!")
      assertEquals(1, a.getOrElse(0))
    }
}
