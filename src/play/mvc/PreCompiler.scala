package play.mvc

import play.Play
import java.io.File

object PreCompiler {
  def main(args: Array[String]) {
    //kick off precompiler
    val root = new File(System.getProperty("application.path"));
     Play.init(root, System.getProperty("play.id", ""));
     play.Logger.info("Precompiling scalate templates...")
     Play.start()
     ScalateProvider.precompileTemplates 
  }
}


