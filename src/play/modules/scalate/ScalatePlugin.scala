import play.PlayPlugin
import play.Play

import org.fusesource.scalate.scaml.ScamlOptions

package play.modules.scalate {
  class ScalatePlugin extends PlayPlugin {
    override def onConfigurationRead() {
      ScamlOptions.autoclose = ScamlOptions.autoclose.filterNot(_ == "script")
    }
  }
}