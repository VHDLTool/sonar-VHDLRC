package com.linty.sonar.zamia;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/*VLSI project structure that must be respected in ZamiaCad. 
 * {SCANNER_HOME}
 *  :
 *  |__rc
 *     |__ws 
 *        |__project
 *           |   .project
 *           |   BuildPath.txt
 *           |
 *           |__[BP External Source]...
 *           |  
 *           |__rule_checker
 *           |  |   rc_config.txt                (Fixed)
 *           |  |   rc_config.xml                (Fixed)
 *           |  |   rc_config_selected_rules.xml 
 *           |  |   rc_config_selected_tools.xml (fixed)
 *           |  |   rc_handbook_parameters.xml
 *           |  |
 *           |  |__hb_vhdlrc
 *           |  |   handbook_STD.xml
 *           |  |
 *           |  |__reporting...
 *           |
 *           |__vhdl
 *              |   xxx.vhd
 *              :   ...
 * */

public class ZamiaRunner {
  
  private static final String                  PROJECT_DIR = "rc/ws/project";
  
  public static final String               BUILD_PATH_TXT = "BuildPath.txt";
  private static final String                  SOURCES_DIR = "vhdl";
  private static final String                  CONFIG_DIR  = "rule_checker";
  
  private static final String             HANDBOOK_STD_XML = "hb_vhdlrc/handbook_STD.xml";
  private static final String RC_CONFIG_SELECTED_RULES_XML = "rc_config_selected_rules.xml";
  private static final String       RC_HANDBOOK_PARAMETERS = "rc_handbook_parameters.xml";
  
  private static final String WIN_RC_CMD = "eclipsec.exe -nosplash -application org.zamia.plugin.Check";
  private static final String UNIX_RC_CMD = "eclipse -nosplash -application org.zamia.plugin.Check";
  
  private final SensorContext context;
  private static final Logger LOG = Loggers.get(ZamiaRunner.class);
  
  public ZamiaRunner(SensorContext context) {
    this.context = context;
  }
  
  public static void run(SensorContext context) {
    new ZamiaRunner(context).run();
  }

  @VisibleForTesting
  protected void run() {
    LOG.info("----------Vhdlrc Analysis---------");
    try {
      BuildPathMaker.build(this.context.config());
      uploadConfigToZamia();
    } catch (IOException e) {
      LOG.error("Error when setting Top Entities",e);
    }
    
    //uploadInputFilesToZamia();  
    //runZamia();
  }

 

  @VisibleForTesting
  protected void uploadConfigToZamia() {
    LOG.info("--Load configuration");
    // TODO Auto-generated method stub  
    LOG.info("--Load configuration (done)");
  }
  
  protected void uploadInputFilesToZamia() {
    LOG.info("--Load Vhdl files"); 
    // TODO Auto-generated method stub   
    LOG.info("--Load Vhdl files (done)"); 
  }
  
  private void runZamia() {
    LOG.info("--Running analysis");
    // TODO Auto-generated method stub 
    LOG.info("--Running analysis (done)");
  }
 
 public static Path get(String resource) {
   try {
     return Paths.get(ZamiaRunner.class.getResource(resource).toURI());
   } catch (URISyntaxException e) {
     throw new IllegalStateException("Erro when accessing" + resource, e);
   }
 } 

}
