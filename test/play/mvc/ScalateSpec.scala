package play.mvc

import play.Play
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io._
import java.util._
import scala.tools.nsc.Settings
class ScalateDummy extends ScalateProvider {
  override def defaultTemplate = "Application/test"
  override def requestFormat = "html"
  override def controller = "Application"
  override def validationErrors = null
}

class ScalateSpec extends FlatSpec with ShouldMatchers {
   val settings = new Settings() 
   Play.applicationPath=new File((new File(".")).getCanonicalPath+"/samples-and-tests/simpleapp")
   val prop = new Properties
   prop.load(new FileInputStream((new File(".")).getCanonicalPath+"/samples-and-tests/simpleapp/conf/application.conf"))
   Play.configuration = prop 
     
   "A scalate template" should "render" in {
     val provider = new ScalateDummy
     provider.renderOrProvideTemplate(Array[AnyRef]())
   }
}
