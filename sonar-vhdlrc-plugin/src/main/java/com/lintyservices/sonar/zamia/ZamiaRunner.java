/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2020 Linty Services
 * mailto:contact@linty-services.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.lintyservices.sonar.zamia;


import com.google.common.annotations.VisibleForTesting;
import com.lintyservices.sonar.plugins.vhdlrc.Vhdl;
import com.lintyservices.sonar.plugins.vhdlrc.VhdlRcSensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.apache.commons.io.FileUtils;


public class ZamiaRunner {

  public static class RunnerContext {
    private static final String ECLIPSE_DIR = "rc/App/eclipse";
    //Arguments are passed into script in eclipse(_rv)|(.bat) 
    //"-clean -nosplash -application org.zamia.plugin.Check"
    private static final String WIN_EXE = "eclipsec.bat";
    private static final String UNIX_EXE = "eclipse_rc";
    private static final String DOUBLE_QUOTE = "\"";

    protected ArrayList<String> buildCmd(String scannerHome) {
      ArrayList<String> cmd = new ArrayList<>();
      Path programDir = Paths.get(scannerHome, ECLIPSE_DIR).normalize();
      Path target = Paths.get(scannerHome, PROJECT_DIR).normalize();
      if (isWindows()) {
        cmd.add(doubleQuote(programDir.resolve(WIN_EXE).normalize()));
        cmd.add(doubleQuote(target));
      } else {
        cmd.add(programDir.resolve(UNIX_EXE).normalize().toString());
        cmd.add(target.toString());
      }
      return cmd;
    }

    protected boolean isWindows() {
      return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    private String doubleQuote(Path p) {
      return DOUBLE_QUOTE + p.toString() + DOUBLE_QUOTE;
    }
  }

  //----------------------------------------------------------------------------------------------------
  /*
   * File structure of an rc directory containing the eclipse instance, a jre, and a configured project
   */

  public static final String PROJECT_DIR = VhdlRcSensor.PROJECT_DIR;
  public static final String BUILD_PATH_TXT = "BuildPath.txt";
  public static final String SOURCES_DIR = "vhdl";
  public static final String CONFIG_DIR = "rule_checker";
  public static final String REPORTING_RULE = CONFIG_DIR + "/reporting/rule";

  public static final String HANDBOOK_XML = "hb_vhdlrc/handbook.xml";
  public static final String RC_CONFIG_SELECTED_RULES = "rc_config_selected_rules.xml";
  public static final String RC_HANDBOOK_PARAMETERS = "rc_handbook_parameters.xml";

  public static final String CONFIGURATION = "configuration";
  public static final String VIRGIN_CONF = "virgin_conf";
  public static final String RULESET_PATH = "HANDBOOK/Rulesets/handbook.xml";
  public static final String RC_HANDBOOK_PARAMETERS_PATH = CONFIGURATION + "/" + RC_HANDBOOK_PARAMETERS;

  //------------------------------------------------------------------------------------------------------
  private final SensorContext context;
  private final RunnerContext runnerContext;
  private final String scannerHome;

  private static final Logger LOG = Loggers.get(ZamiaRunner.class);

  public ZamiaRunner(SensorContext sensorContext, RunnerContext runnerContext) {
    this.context = sensorContext;
    this.scannerHome = this.context.config()
      .get(VhdlRcSensor.SCANNER_HOME_KEY)
      .orElseThrow(() -> new IllegalStateException("vhdlRcSensor should not execute without " + VhdlRcSensor.SCANNER_HOME_KEY));
    this.runnerContext = runnerContext;
  }

  public static void run(SensorContext sensorContext) {
    new ZamiaRunner(sensorContext, new RunnerContext()).run();
  }

  @VisibleForTesting
  protected void run() {
    Configuration config = this.context.config();

    LOG.info("----------Vhdlrc Analysis---------");

    LOG.info("--Generating configuration");
    //Generating content for BuidlPath.txt
    Path tempBuildPath = BuildPathMaker.make(config);
    //Parser for injected rc_handbook_parameters.xml
    ActiveRuleLoader loader = new ActiveRuleLoader(this.context.activeRules(), "/" + RC_HANDBOOK_PARAMETERS_PATH);
    //Generating content for rc_handbook_parameters.xml
    Path rcHandbookParameters = loader.makeRcHandbookParameters();
    //Generating content for rc_config_selected_rules.xml
    Path rcConfigSelectedRules = SelectedRulesMaker.makeWith(loader.activeRuleKeys());
    LOG.info("--Generating configuration");

    uploadConfigToZamia(tempBuildPath, rcHandbookParameters, rcConfigSelectedRules);
    clean(Paths.get(this.scannerHome, PROJECT_DIR, SOURCES_DIR));    //Prepare clean /vhdl folder for sources
    clean(Paths.get(this.scannerHome, PROJECT_DIR, REPORTING_RULE)); //Prepare clean /rule folder for reports
    uploadInputFilesToZamia();
    runZamia();
    //After analysis
    LOG.info("----------Vhdlrc Analysis---------(done)");
    if (BuildPathMaker.getPauseExec(config)) {
      System.out.println("Press ENTER to resume execution");
      try {
        System.in.read();
      } catch (IOException e) {
        LOG.warn("Input exception");
      }
    }
  }

  /*
   * BuidlPath.txt:                      Embedded default -> Custom of {sonar.vhdlrc.topEntities, sonar.vhdlrc.customCmd}
   * rc_handbook_parameters.xml:   Injected at build time -> Custom of API:ActiveRules (Quality profile on project)
   * rc_config_selected_rules.xml:       Embedded default -> Custom of API:ActiveRules && rc_handbook_parameters.xml
   * handbook.xml:                 Injected at build time -> Untouched
   */

  @VisibleForTesting
  protected void uploadConfigToZamia(Path tempBuildPath, Path rcHbParam, Path rcSelectedRules) {
    LOG.info("--Load configuration");

    //Embedded resources configuration files 
    String configuration = "/" + CONFIGURATION + "/";
    InputStream hb = ZamiaRunner.class.getResourceAsStream(configuration + RULESET_PATH); //handbook.xml                 

    //Configuration files destinations in scanner
    Path projectDir = Paths.get(this.scannerHome, PROJECT_DIR); // $SCANNER_HOME/rc/Data/workspace/project
    Path buildPathTarget = projectDir.resolve(BUILD_PATH_TXT);       // rc/Data/workspace/project/ BuidlPath.txt
    Path targetConf1 = projectDir.resolve(CONFIG_DIR).resolve(RC_CONFIG_SELECTED_RULES); // rc/Data/workspace/project/rule_checker/ rc_config_selected_rules.xml
    Path targetConf2 = projectDir.resolve(CONFIG_DIR).resolve(RC_HANDBOOK_PARAMETERS);   // rc/Data/workspace/project/rule_checker/ rc_handbook_parameters.xml
    Path hbTarget = projectDir.resolve(CONFIG_DIR).resolve(HANDBOOK_XML);             // rc/Data/workspace/project/rule_checker/ handbook.xml

    LOG.debug("Loading configuration to " + buildPathTarget);
    try {
      Files.copy(tempBuildPath, buildPathTarget, StandardCopyOption.REPLACE_EXISTING); // BuidlPath.txt 
      Files.copy(rcSelectedRules, targetConf1, StandardCopyOption.REPLACE_EXISTING); // rc_config_selected_rules.xml
      Files.copy(rcHbParam, targetConf2, StandardCopyOption.REPLACE_EXISTING); // rc_handbook_parameters.xml
      Files.copy(hb, hbTarget, StandardCopyOption.REPLACE_EXISTING); // handbook.xml
    } catch (IOException e) {
      LOG.error("unable to upload configuration files to scanner: \n{} \n{}", e.getClass(), e.getMessage());
    }
    LOG.info("--Load configuration (done)");
  }

  @VisibleForTesting
  protected void uploadInputFilesToZamia() {
    LOG.info("--Load Vhdl files");
    FilePredicates p = context.fileSystem().predicates();
    Iterable<InputFile> files = context.fileSystem().inputFiles(p.hasLanguage(Vhdl.KEY));
    files.forEach(file -> {
      try {
        uploadInputFile(file);
      } catch (IOException e) {
        LOG.error("Unable to upload this vhdl source to project: \n {} ", e.getMessage());
      }
    });
    LOG.info("--Load Vhdl files (done)");
  }

  private void uploadInputFile(InputFile file) throws IOException {
    LOG.debug("File name : " + file.filename());
    Path target = Paths.get(this.scannerHome, PROJECT_DIR, SOURCES_DIR, file.toString());
    FileUtils.copyFile(new File(file.uri()), target.toFile());
  }

  public static void clean(Path path) {
    try {
      FileUtils.cleanDirectory(path.toFile());
    } catch (IOException | IllegalArgumentException e) {
      LOG.error("Unable to reset folder in scanner : {}", e.getMessage());
    }
  }

  @VisibleForTesting
  protected void runZamia() {
    LOG.info("--Running analysis");

    ProcessBuilder builder = new ProcessBuilder()
      .command(this.runnerContext.buildCmd(scannerHome))
      .redirectErrorStream(true);
    Process process;

    LOG.debug("Running " + Arrays.toString(builder.command().toArray()));

    try {
      process = builder.start();
      consume(process.getInputStream());
      process.waitFor(300, TimeUnit.SECONDS);
      process.destroy();
    } catch (IOException e) {
      LOG.error("Analysis has failed : {} {}", e.getClass(), e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.error("Analysis has failed : {}", e.getMessage());
    }
    LOG.info("--Running analysis (done)");
  }

  @VisibleForTesting
  protected void consume(InputStream is) throws IOException {
    String line;
    int i = 0;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    if (LOG.isDebugEnabled()) {
      while ((line = br.readLine()) != null && i++ < 15000) {
        LOG.info("Zamia : " + line);
      }
    } else {
      while ((line = br.readLine()) != null && i++ < 15000) {
      } //just consume the output to prevent the process from hanging
    }
  }
}
