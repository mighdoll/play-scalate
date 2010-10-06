package play.mvc.scalate
import play.Play
import scala.collection.JavaConversions._
import play.mvc._


object PlayContext {
   def session =  Scope.Session.current()
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

   def url(uri:String, params:List[Any]):String = {

    def isSimpleParam[X](typ: Class[X]): Boolean = {
      classOf[Number].isAssignableFrom(typ) || 
      typ.equals(classOf[String]) ||
      typ.isPrimitive
    }

     import play.mvc.ActionInvoker
     import java.lang.reflect.Method
     import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer
     import play.exceptions.NoRouteFoundException
     import play.mvc.Router
     import play.data.binding.Unbinder

     val action = uri
     try{
       val r = new java.util.HashMap[String, java.lang.Object]
       val actionMethod = ActionInvoker.getActionMethod(action)(1).asInstanceOf[Method]
       val names = actionMethod.getDeclaringClass.getDeclaredField("$" + actionMethod.getName + LocalVariablesNamesTracer.computeMethodHash(actionMethod.getParameterTypes)).get(null).asInstanceOf[Array[String]]

       if(names.length < params.length){
         throw new NoRouteFoundException(action, null);
       }

       for{
         (param, i) <- params.zipWithIndex
         val name = if(i < names.length) names(i) else ""}{
         if(param.isInstanceOf[Router.ActionDefinition] && param != null){
           Unbinder.unBind(r, param.toString, name)
         } 
         else if (isSimpleParam(actionMethod.getParameterTypes.apply(i))){
           if (param != null) {
             Unbinder.unBind(r, param.toString, name)
           }
         } 
         else {
           Unbinder.unBind(r, param, name)
         }
       }

       Router.reverse(action, r).url
     } 
     catch {
       case ex: ClassCastException => 
         throw new ClassCastException("url tag takes a List[String,AnyRef] as a second parameter")
     }
   }

}

