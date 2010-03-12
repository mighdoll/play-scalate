package play.mvc

import play.Play
import java.io.File

object PreCompiler {
  def main(args: Array[String]) {
    //kick off precompiler
    val root = new File(System.getProperty("application.path"));
     Play.init(root, System.getProperty("play.id", ""));
     println("~")
     println("~ Precompiling scalate templates...")
     println("~")
     Play.start()
     ScalateProvider.precompileTemplates 
  }
}


