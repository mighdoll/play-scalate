package play.mvc
 
abstract class ScalateController extends ScalaController with scalate.Provider {
  override def render(args: Any*) = renderWithScalate(args)
}
