/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2018 Maxime Facquet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.linty.sonar.zamia;


import com.google.common.annotations.VisibleForTesting;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


public class ZamiaRunner {
  
  public static final String                  PROJECT_DIR = "rc/ws/project";
  
  public static final String                BUILD_PATH_TXT = "BuildPath.txt";
  public static final String                  SOURCES_DIR = "vhdl";
  public static final String                  CONFIG_DIR  = "rule_checker";
  
  public static final String             HANDBOOK_STD_XML = "hb_vhdlrc/handbook_STD.xml";
  public static final String RC_CONFIG_SELECTED_RULES_XML = "rc_config_selected_rules.xml";
  public static final String       RC_HANDBOOK_PARAMETERS = "rc_handbook_parameters.xml";
  
  public static final String                COMPUTED_CONF = "computed_conf";
  public static final String                  VIRGIN_CONF = "virgin_conf";
  
  private static final String WIN_RC_CMD = "eclipsec.exe -nosplash -application org.zamia.plugin.Check";
  private static final String UNIX_RC_CMD = "eclipse -nosplash -application org.zamia.plugin.Check";
  
  
  private final SensorContext context;
  private final String scannerHome;
  private static final Logger LOG = Loggers.get(ZamiaRunner.class);
  
  public ZamiaRunner(SensorContext context) {
    this.context = context;
    this.scannerHome = this.context.config()
      .get(VhdlRcSensor.SCANNER_HOME_KEY)
      .orElseThrow(() -> new IllegalStateException("vhdlRcSensor should not execute without " + VhdlRcSensor.SCANNER_HOME_KEY));
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
    //Resolving source configuration file paths 
    Path buildPath = ZamiaRunner.get(Paths.get(COMPUTED_CONF, BUILD_PATH_TXT).toString());
    
    //Resolving destination paths
    Path buildPathTarget = Paths.get(this.scannerHome, PROJECT_DIR, BUILD_PATH_TXT);  
    
    //Copying config files to scanner zamia project
    try {
      Files.copy(buildPath, buildPathTarget, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      LOG.error("unable to upload configuration files to scanner",e);
    }
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
     return Paths.get(ZamiaRunner.class.getResource("/" + resource).toURI());
   } catch (URISyntaxException | NullPointerException e) {
     throw new IllegalStateException("Error trying to access " + resource, e);
   }
 } 

}
