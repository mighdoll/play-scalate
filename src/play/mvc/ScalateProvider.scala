package play.mvc

import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer
import play.Play
import play.exceptions.{UnexpectedException,PlayException,TemplateNotFoundException}
import play.data.validation.Validation
import org.fusesource.scalate._
import java.io.{StringWriter,PrintWriter}
import scala.collection.JavaConversions._
import java.io.File
import org.fusesource.scalate.util.SourceCodeHelper
import play.vfs.{VirtualFile => VFS}

private[mvc] trait ScalateProvider  {

  // Create and configure the Scalate template engine
  def initEngine(useStandardWorkdir:Boolean = false, usePlayClassloader:Boolean = true ):TemplateEngine = {
    val engine = new TemplateEngine
    engine.bindings = List(
      Binding("context", SourceCodeHelper.name(classOf[DefaultRenderContext]), true),
      Binding("playcontext", SourceCodeHelper.name(PlayContext.getClass), true)
    )
    if (Play.mode == Play.Mode.PROD) engine.allowReload = false

    engine.workingDirectory = if (Play.mode == Play.Mode.PROD && !useStandardWorkdir ) 
        new File(System.getProperty("java.io.tmpdir"), "scalate")
     else 
       new File(Play.applicationPath,"/tmp")
   
    engine.resourceLoader = new FileResourceLoader(Some(new File(Play.applicationPath+"/app/views")))
    engine.classpath = (new File(Play.applicationPath,"/tmp/classes")).toString
    engine.combinedClassPath = true
    if (usePlayClassloader) engine.classLoader = Play.classloader
    engine
  }
  val engine = initEngine()
  def defaultTemplate = Http.Request.current().action
  def requestFormat = Http.Request.current().format 
  def controller = Http.Request.current().controller
  def validationErrors = Validation.errors
  def preCompedContextName = if (Play.configuration.containsKey("scalate.precompile.name")) Play.configuration.getProperty("scalate.precompile.name") else "x"

  def renderOrProvideTemplate(args:Seq[AnyRef]):Option[String] = {
    //determine template
    val templateName:String =
        if (args.length > 0 && args(0).isInstanceOf[String] && 
          LocalVariablesNamesTracer.getAllLocalVariableNames(args(0)).isEmpty) {
          discardLeadingAt(args(0).toString)
        } else {
          determineURI()
        }
    if (shouldRenderWithScalate(templateName)) {
      renderScalateTemplate(templateName,args)
      None
    } else {
      Some(templateName)
    }  

  }

  //determine if we need to render with scalate
  def shouldRenderWithScalate(template:String):Boolean = {
    val ignore = Play.configuration.getProperty("scalate.ignore") 
    if (Play.configuration.containsKey("scalate")) {
      if (ignore != null) {
         ignore.split(",").filter(template.startsWith(_)).size == 0
      } else true
    } else false 
  }

  //render with scalate
  def renderScalateTemplate(templateName:String, args:Seq[AnyRef]) = {
    val renderMode = Play.configuration.getProperty("scalate")
    val otherMode = renderMode match {
      case "ssp" => "scaml"
      case "scaml" => "ssp"
    }
    //loading template
    val lb = new scala.collection.mutable.ListBuffer[Binding]
    val buffer = new StringWriter()
    var context = new DefaultRenderContext(engine, new PrintWriter(buffer))
    val templateBinding = Scope.RenderArgs.current()
    
    // try to fill context
    for (o <-args) {
      for (name <-LocalVariablesNamesTracer.getAllLocalVariableNames(o).iterator) {
        context.attributes(name) = o
        lb += Binding(name,SourceCodeHelper.name(o.getClass))
      }
    }
    context.attributes("playcontext") = PlayContext
    
    if (templateBinding != null && templateBinding.data != null) {
      for (pair <- templateBinding.data) context.attributes(pair._1) = pair._2
    }
    
    // add the default layout to the context if it exists
    context.attributes("layout") = VFS.search(Play.templatesPath, "/default." + renderMode) match {
      case null => VFS.search(Play.templatesPath, "/default." + otherMode) match {
          case null => ""
          case f: VFS if f.exists() => "/default." + otherMode
          case f: VFS if !f.exists() => ""
        }
      case f: VFS if f.exists() => "/default." + renderMode
      case f: VFS if !f.exists() => ""
    }
    
    try {
       context.attributes("errors") = validationErrors
    } catch { case ex:Exception => throw new UnexpectedException(ex)}
    
    try {
          val baseName = templateName.replaceAll(".html", "")
          val templatePath = VFS.search(Play.templatesPath, baseName + "." + renderMode) match {
            case null => VFS.search(Play.templatesPath, baseName + "." + otherMode) match {
              case null => ""
              case f: VFS if f.exists() => baseName + "." + otherMode
              case f: VFS if !f.exists() => ""
            }
            case f: VFS if f.exists() => baseName + "." + renderMode
            case f: VFS if !f.exists() => ""
          }
          val template = engine.load(templatePath, lb.toList)
          engine.layout(template, context)
          throw new ScalateResult(buffer.toString,templateName)
    } catch { 
        case ex:TemplateNotFoundException => {
          if(ex.isSourceAvailable) {
            throw ex
          }
          val element = PlayException.getInterestingStrackTraceElement(ex)
          if (element != null) {
             throw new TemplateNotFoundException(templateName, Play.classes.getApplicationClass(element.getClassName()), element.getLineNumber());
          } else {
             throw ex
          }
       }
    }
  }
  
  def discardLeadingAt(templateName:String):String = {
        if(templateName.startsWith("@")) {
            if(!templateName.contains(".")) {
                determineURI(controller + "." + templateName.substring(1))
            }
            determineURI(templateName.substring(1))
        } else templateName
  }

  def determineURI(template:String = defaultTemplate):String = {
     template.replace(".", "/") + "." + 
     (if (requestFormat == null)  "html" else requestFormat)
  }
}
private[mvc] object ScalateProvider extends ScalateProvider

