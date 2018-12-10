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
import com.linty.sonar.plugins.vhdlrc.Vhdl;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
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
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.apache.commons.io.FileUtils;


public class ZamiaRunner {
  
  public static class RunnerContext{
    private final String ECLIPSE_DIR = "rc/eclipse";
    private final String WIN_EXE = "eclipsec.exe";
    //private final String ECLIPSE_DIR = "rc/notepad++";//TODO:for testing only
    //private final String WIN_EXE = "notepad++.exe";//TODO:for testing only
    private final String UNIX_EXE = "eclipse";
    private final String ARGS = "-clean -nosplash -application org.zamia.plugin.Check";
    private final String DOUBLE_QUOTE = "\"";

    protected ArrayList<String> buildCmd(String scannerHome) {
      ArrayList<String> cmd = new ArrayList<>();
      boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
      Path programDir = Paths.get(scannerHome, this.ECLIPSE_DIR).normalize();
      Path target = Paths.get(scannerHome, PROJECT_DIR).normalize();
      if (isWindows) {
        cmd.add(doubleQuote(programDir.resolve(this.WIN_EXE).normalize()));
      } else {
        cmd.add(doubleQuote(programDir.resolve(this.UNIX_EXE).normalize()));
      }
      cmd.addAll(Arrays.asList(ARGS.split(" ")));
      cmd.add(doubleQuote(target));  
      return cmd;
    }

    private String doubleQuote(Path p) {
      return DOUBLE_QUOTE + p.toString() + DOUBLE_QUOTE;
    }
  }
  
  public static final String                  PROJECT_DIR = VhdlRcSensor.PROJECT_DIR;
  public static final String               BUILD_PATH_TXT = "BuildPath.txt";
  public static final String                  SOURCES_DIR = "vhdl";
  public static final String                  CONFIG_DIR  = "rule_checker";
  
  public static final String                 HANDBOOK_XML = "hb_vhdlrc/handbook.xml";
  public static final String     RC_CONFIG_SELECTED_RULES = "rc_config_selected_rules.xml";
  public static final String       RC_HANDBOOK_PARAMETERS = "rc_handbook_parameters.xml";
  
  public static final String                CONFIGURATION = "configuration";
  public static final String                  VIRGIN_CONF = "virgin_conf";
  public static final String                 RULESET_PATH = "HANDBOOK/Rulesets/handbook.xml";
  
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
    LOG.info("----------Vhdlrc Analysis---------");
    Path tempBuildPath = BuildPathMaker.make(this.context.config());
    uploadConfigToZamia(tempBuildPath);
    clean(Paths.get(this.scannerHome, PROJECT_DIR, SOURCES_DIR));
    uploadInputFilesToZamia();
    runZamia();
    clean(Paths.get(this.scannerHome, PROJECT_DIR, SOURCES_DIR));
    LOG.info("----------Vhdlrc Analysis---------(done)");
  }


  @VisibleForTesting
  protected void uploadConfigToZamia(Path tempBuildPath) {
    LOG.info("--Load configuration");
    //Embedded resources configuration files 
    String configuration = "/" + CONFIGURATION + "/";
    InputStream conf1 = ZamiaRunner.class.getResourceAsStream(configuration + RC_CONFIG_SELECTED_RULES);
    InputStream conf2 = ZamiaRunner.class.getResourceAsStream(configuration + RC_HANDBOOK_PARAMETERS);
    InputStream hb    = ZamiaRunner.class.getResourceAsStream(configuration + RULESET_PATH);
    //Configuration files destinations in scanner
    Path projectDir = Paths.get(this.scannerHome, PROJECT_DIR);
    Path targetConf1     = projectDir.resolve(CONFIG_DIR).resolve(RC_CONFIG_SELECTED_RULES);
    Path targetConf2     = projectDir.resolve(CONFIG_DIR).resolve(RC_HANDBOOK_PARAMETERS);
    Path hbTarget        = projectDir.resolve(CONFIG_DIR).resolve(HANDBOOK_XML);
    Path buildPathTarget = projectDir.resolve(BUILD_PATH_TXT);
    if(LOG.isDebugEnabled()) {
      LOG.debug("Load configuration to" + buildPathTarget);
    }
    try {
      Files.copy(tempBuildPath, buildPathTarget, StandardCopyOption.REPLACE_EXISTING);
      Files.copy(conf1, targetConf1, StandardCopyOption.REPLACE_EXISTING);
      Files.copy(conf2, targetConf2, StandardCopyOption.REPLACE_EXISTING);
      Files.copy(hb, hbTarget, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      LOG.error("unable to upload configuration files to scanner",e);
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
        LOG.error("Unable to upload vhdl sources to scanner",e);
      }
    });
    LOG.info("--Load Vhdl files (done)"); 
  }
   
  private void uploadInputFile(InputFile file) throws IOException {
    if(LOG.isDebugEnabled()) {
      LOG.info("File name : " + file.filename());
    }
    Path target = Paths.get(this.scannerHome, PROJECT_DIR, SOURCES_DIR, file.toString());
    FileUtils.copyFile(new File(file.uri()), target.toFile()); 
  }
  
  private void clean(Path path) {
    try {
      FileUtils.cleanDirectory(path.toFile());
    } catch (IOException | IllegalArgumentException e) {
      LOG.error("Unable to reset folder in scanner : {}", e.getMessage() , e );
    }
  }

  @VisibleForTesting
  protected void runZamia() {
    LOG.info("--Running analysis");
    Process process;
    ProcessBuilder builder = new ProcessBuilder();
    builder.command(runnerContext.buildCmd(scannerHome));
    System.out.println(builder.command());//TODO
    builder.redirectErrorStream(true);
  try {
    process = builder.start();
    consume(process.getInputStream());
    process.waitFor(120, TimeUnit.SECONDS);
    process.destroy();       
    } catch (IOException | InterruptedException e) {
      LOG.error("Analysis has failed : {}", e.getMessage());
    }
    LOG.info("--Running analysis (done)");
  }

  private void consume(InputStream is) throws IOException {
    String line;
    int i = 0;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    line = br.readLine();
    while (line != null && i++ < 1) { 
      br.readLine();
      System.out.println("output : " + line);//TODO
    }

  }
 
}
