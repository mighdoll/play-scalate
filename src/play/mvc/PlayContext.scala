package play.mvc
import play.Play
import scala.collection.JavaConversions._


private[mvc] object PlayContext {
   def session = Scope.Session.current()
   def request = Http.Request.current()
   def flash = Scope.Flash.current()
   def params = Scope.Params.current()
   //tags
   def staticurl(uri:String):String = {
      Router.reverse(Play.getVirtualFile(uri))
   }
   def absoluteurl (action:String):String = {
      request.getBase+url(action)
   }
   def absoluteurl (action:String, map:Any):String = {
      request.getBase+url(action, map)
   }
   def url(uri:String):String = {
     Router.reverse(uri,new java.util.HashMap[String,Object]()).url
   }
   //for some reason scalate can not understand generic types
   def url(uri:String, map:Any):String = {
     try{
       val im = collection.mutable.Map[String,AnyRef]()++=map.asInstanceOf[Map[String,AnyRef]]
       Router.reverse(uri,im).url
     } catch {
       case ex: ClassCastException => throw new ClassCastException("url tag takes a Map[String,AnyRef] as a second parameter")
     }
   }
}

