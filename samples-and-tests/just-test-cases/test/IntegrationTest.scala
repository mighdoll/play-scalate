import org.junit._
import play.test._
import play.mvc._
import play.mvc.Http._
import models._
import org.junit.Assert._

class IntegrationTest extends FunctionalTestCase with Matchers{

    @Test
    def testLayout {
        var response = GET("/")
        response shouldBeOk()
        println(getContent(response))
        assertEquals(getContent(response).contains("layout header goes here..."), true)
        assertEquals(getContent(response).contains("layout footer goes here..."), true)
    }
    
    @Test
    def testExtraParam {
        var response = GET("/")
        response shouldBeOk()
        assertEquals(getContent(response).contains("Extra paramteter User["), true)
    }
    @Test
    def testUserList {
        var response = GET("/")
        response shouldBeOk()
        assertEquals(getContent(response).contains("userlist password!!!"), true)
    }
    @Test
    def testParameterPassing {
        var response = GET("/")
        response shouldBeOk()
        assertEquals(getContent(response).contains("Hello Guest user!!!!"), true)
    }


}
