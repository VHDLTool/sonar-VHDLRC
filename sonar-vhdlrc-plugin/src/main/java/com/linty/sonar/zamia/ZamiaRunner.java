package com.linty.sonar.zamia;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ZamiaRunner {
  
  private final SensorContext context;
  
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
  private static final String                  PROJECT_DIR = "rc/ws/project";
  
  private static final String               BUILD_PATH_TXT = "BuildPath.txt";
  private static final String                  SOURCES_DIR = "vhdl";
  private static final String                  CONFIG_DIR  = "rule_checker";
  
  private static final String             HANDBOOK_STD_XML = "hb_vhdlrc/handbook_STD.xml";
  private static final String RC_CONFIG_SELECTED_RULES_XML = "rc_config_selected_rules.xml";
  private static final String       RC_HANDBOOK_PARAMETERS = "rc_handbook_parameters.xml";
  
  private static final String WIN_RC_CMD = "eclipsec.exe -nosplash -application org.zamia.plugin.Check";
  private static final String UNIX_RC_CMD = "eclipse -nosplash -application org.zamia.plugin.Check";
  
  private static final Logger LOG = Loggers.get(ZamiaRunner.class);
  
  
  public ZamiaRunner(SensorContext context) {
    this.context = context;
  }
  
  public static void run(SensorContext context) {
    new ZamiaRunner(context).run();
  }
  
  protected void run() {
    
  }

  public void testing() {
    LOG.info("\n---------------ZamiaRunner getRessource(/virgin_cong/buildPath)----------------------------\n" 
      + ZamiaRunner.class.getResource("/virgin_conf/BuildPath.txt").getPath()
      + "\n lenght of File :" 
      + ZamiaRunner.class.getResource("/virgin_conf/BuildPath.txt").getFile().length()
      + "\n----------------------------------------------------------------------\n");
  }

}
