package play.mvc
import scala.collection.JavaConversions._

private[mvc] object PlayContext {
   def session = Scope.Session.current.get
   def request = Http.Request.current()
   def flash = Scope.Flash.current.get
   def params = Scope.Params.current.get
   //tags
   def url(action:String):String = {
     play.mvc.Router.reverse(action,new java.util.HashMap[String,Object]()).url
   }
   //for some reason scalate can not understand generic types
   def url(action:String, map:Any):String = {
     try{
       val im = collection.mutable.Map[String,AnyRef]()++=map.asInstanceOf[Map[String,AnyRef]]
       play.mvc.Router.reverse(action,im).url
     } catch {case ex:ClassCastException=> throw new ClassCastException("url tag takes a Map[String,AnyRef] as a second parameter")
     }
   }
}

