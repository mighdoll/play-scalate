package tag
import scala.collection.JavaConversions._

object url {
  def apply(action:String,m:Map[String,AnyRef]=Map()):String = {
    val im = collection.mutable.Map[String,AnyRef]()++=m
    play.mvc.Router.reverse(action,im).url
  }
}
