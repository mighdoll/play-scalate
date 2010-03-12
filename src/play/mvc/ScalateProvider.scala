package play.mvc

import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer
import play.Play
import play.exceptions.{UnexpectedException,PlayException,TemplateNotFoundException}
import play.data.validation.Validation
import org.fusesource.scalate._
import java.io.{StringWriter,PrintWriter}
import scala.collection.JavaConversions._
import java.io.{File,BufferedReader, FileReader}
import org.fusesource.scalate.util.SourceCodeHelper
import play.vfs.{VirtualFile => VFS}

private[mvc] trait ScalateProvider  {

  // Create and configure the Scalate template engine
  def initEngine(usePlayClassloader:Boolean = true, customImports: String="import controllers._;import models._;import play.utils._" ):TemplateEngine = {
    val engine = new TemplateEngine
    engine.bindings = List(
      Binding("context", SourceCodeHelper.name(classOf[DefaultRenderContext]), true),
      Binding("playcontext", SourceCodeHelper.name(PlayContext.getClass), true)
    )
    if (Play.mode == Play.Mode.PROD) engine.allowReload = false

    engine.workingDirectory = new File(Play.applicationPath,"/tmp")
   
    engine.resourceLoader = new FileResourceLoader(Some(new File(Play.applicationPath+"/app/views")))
    engine.classpath = (new File(Play.applicationPath,"/tmp/classes")).toString
    engine.combinedClassPath = true
    engine.customImports = customImports
    if (usePlayClassloader) engine.classLoader = Play.classloader
    engine
  }
  val engine = initEngine()
  def defaultTemplate = Http.Request.current().action
  def requestFormat = Http.Request.current().format 
  def controller = Http.Request.current().controller
  def validationErrors = Validation.errors

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

  private[this] def reggroup = "<%@[^>]*%>".r
  val Re="<%@.*var(.*):.*%>".r

  def precompileTemplates = walk (new File(Play.applicationPath,"/app/views")) ( (filePath: String) => {
    val playPath = filePath.replace((new File(Play.applicationPath+"/app/views")).toString,"")
    println("compiling: "+playPath+ " ...")
    val buffer = new StringWriter()
    var context = new DefaultRenderContext(engine, new PrintWriter(buffer))
    // populate playcontext
    context.attributes("playcontext") = PlayContext
    //set layout
    context.attributes("layout") = locateLayout(Play.configuration.getProperty("scalate"))
    // open file & try to find context variables and initialize them
    for ( contextVariable <- reggroup findAllIn readFileToString(filePath)) 
        contextVariable match {
          case Re(key) => context.attributes(key.trim) = ""
          case _=>
        }
    
    //compile template
    val template = engine.load(playPath)
    try {
      engine.layout(template, context)
    } catch {case  ex:ClassCastException => }
   } )
  

  //render with scalate
  def renderScalateTemplate(templateName:String, args:Seq[AnyRef]) = {
    val renderMode = Play.configuration.getProperty("scalate")
    val otherMode = renderMode match {
      case "ssp" => "scaml"
      case "scaml" => "ssp"
    }
    //loading template
    val buffer = new StringWriter()
    var context = new DefaultRenderContext(engine, new PrintWriter(buffer))
    val renderArgs = Scope.RenderArgs.current()
     // try to fill context
    for (o <-args) {
        for (name <-LocalVariablesNamesTracer.getAllLocalVariableNames(o).iterator) {
           context.attributes(name) = o
        }   
    }        
    context.attributes("playcontext") = PlayContext

    // now add renderArgs as well
    if (renderArgs != null && renderArgs.data != null) {
      for (pair <- renderArgs.data) context.attributes(pair._1) = pair._2
    }
    
    // add the default layout to the context if it exists
    context.attributes("layout") = locateLayout(renderMode,otherMode) 
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
          val template = engine.load(templatePath)
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
  

  //determine if we need to render with scalate
private[this]  def shouldRenderWithScalate(template:String):Boolean = {
    val ignore = Play.configuration.getProperty("scalate.ignore") 
    if (Play.configuration.containsKey("scalate")) {
      if (ignore != null) {
         ignore.split(",").filter(template.startsWith(_)).size == 0
      } else true
    } else false 
}

private[this]  def discardLeadingAt(templateName:String):String = {
        if(templateName.startsWith("@")) {
            if(!templateName.contains(".")) {
                determineURI(controller + "." + templateName.substring(1))
            }
            determineURI(templateName.substring(1))
        } else templateName
  }

private[this]  def determineURI(template:String = defaultTemplate):String = {
     template.replace(".", "/") + "." + 
     (if (requestFormat == null)  "html" else requestFormat)
  }

private[this] def locateLayout(renderMode: String, otherMode: String="" ):String =  
  VFS.search(Play.templatesPath, "/default." + renderMode) match {
      case null => VFS.search(Play.templatesPath, "/default." + otherMode) match {
          case null => ""
          case f: VFS if f.exists() => "/default." + otherMode
          case f: VFS if !f.exists() => ""
        }
      case f: VFS if f.exists() => "/default." + renderMode
      case f: VFS if !f.exists() => ""
    }

private[this] def walk(file: File)(func: String=>Unit):Boolean = {
    if (file.isFile  && (file.getName.endsWith(".ssp") || file.getName.endsWith(".scaml")) && !file.getName.contains("default.ssp") && !file.getName.contains("default.scaml") )  func(file.getPath)
    if (file.isDirectory) for (i <- 0 until file.listFiles.length) walk(file.listFiles()(i))(func)
    true
}

private[this] def readFileToString(filePath: String) = {
    val scanLines = if (Play.configuration.getProperty("scalate.linescanned") != null) Play.configuration.getProperty("scalate.linescanned").toInt else 20
    var counter=0
    val reader = new BufferedReader(new FileReader(filePath))
    var line: String = reader.readLine
    val sb = new StringBuffer
    while (line != null && counter != scanLines) {
      sb.append(line)
      counter = counter+1
      line = reader.readLine()
    }
    reader.close()
    sb.toString 
  } 
}
private[mvc] object ScalateProvider extends ScalateProvider



