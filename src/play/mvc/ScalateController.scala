package play.mvc
 
abstract class ScalateController extends ScalaController with ScalateProvider {
  override def render(args: Any*) = renderWithScalate(args)
}
