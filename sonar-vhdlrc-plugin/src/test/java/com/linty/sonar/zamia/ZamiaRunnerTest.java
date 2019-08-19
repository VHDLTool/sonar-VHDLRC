/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2019 Maxime Facquet
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


import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import com.linty.sonar.plugins.vhdlrc.Vhdl;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
import com.linty.sonar.zamia.ZamiaRunner.RunnerContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ZamiaRunnerTest {
  
  public static final String PROJECT_DIRECTORY = VhdlRcSensor.PROJECT_DIR;
  public MapSettings settings = new MapSettings();
  public ArrayList<String> projectPathList = new ArrayList<>(Arrays.asList(PROJECT_DIRECTORY.split("/")));
  public File project;    //
  public File vhdl;         //
  public File ruleChecker;  //
  public File reporting;      //
  public File rule;             //
  public File hb;             //
  public File bp;           //
  public ArrayList<String> v = new ArrayList<>(projectPathList);
  public ArrayList<String> r = new ArrayList<>(projectPathList);
  public static Path projectRoot;
  
  SensorContextTester context;
  RunnerContextTester runnerContext;
  
  @Rule
  public LogTester logTester = new LogTester();
  @Rule
  public TemporaryFolder testScanner = new TemporaryFolder();
  @Rule
  public TemporaryFolder testProject = new TemporaryFolder();
  
  @Before
  public void initialize() throws IOException {
    
    //Build a similar project file structure
    projectRoot = Paths.get(testProject.getRoot().toURI());//temporary project for testing
    
    //Temporary Scanner home structure
    project = testScanner.newFolder(projectPathList.toArray(new String[projectPathList.size()]));    
           vhdl = newFolderInScannerProject("vhdl");      
    ruleChecker = newFolderInScannerProject("rule_checker");   
             hb = newFolderInScannerProject("rule_checker", "hb_vhdlrc");
      reporting = newFolderInScannerProject("rule_checker", "reporting");
           rule = newFolderInScannerProject("rule_checker", "reporting","rule");
    bp = testScanner.newFile(PROJECT_DIRECTORY + "/BuildPath.txt");
    //testScanner.newFile("rc/ws/project/rule_checker/hb_vhdlrc/handbook.xml");
    //walkin(project, "+--");
    logTester.clear();
    
    context = createContext();
    runnerContext = new RunnerContextTester();
  }

  @Test
  public void test_no_home_scanner_key() {
    try {
      new ZamiaRunner(SensorContextTester.create(projectRoot), runnerContext);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("vhdlRcSensor should not execute without");
    }
  }
  
  
  @Test
  public void test() throws IOException {

    //vhdl folder should be cleaned before analysis
    newFolderInScannerProject("vhdl","folder2eleted");//create a folder
    testScanner.newFile(PROJECT_DIRECTORY + "/vhdl/file2delete.vhd");//create file in folder
    
    //reports should be cleaned before analysis
    newFolderInScannerProject("rule_checker", "reporting","rule","rc_report_to_delete");
    testScanner.newFile(PROJECT_DIRECTORY + "/rule_checker/reporting/rule/rc_report_to_delete/report.xml");//create file in folder
    
    //walkin(testScanner.getRoot(),"+--");
    //Source files to copy to scanner vhdl folder
    testProject.newFolder("home","project1","src");
    testProject.newFolder("home","project1","src","MUX");
    addTestFile2(context, testProject, "home/project1/src/Top.vhd");
    addTestFile2(context, testProject, "home/project1/src/a.vhd");
    addTestFile2(context, testProject, "home/project1/src/MUX/a.vhd");   
    
    ZamiaRunner.run(context);
    
    Path vhdlTargetFolder = Paths.get(testScanner.getRoot().toURI()).resolve(PROJECT_DIRECTORY).resolve("vhdl");
    assertThat(vhdlTargetFolder.toFile()).exists();              //vhdl folder should not be deleted after analysis
    assertThat(vhdlTargetFolder.toFile().listFiles()).isEmpty(); //vhdl folder should be cleaned after analysis when debug is off
    assertThat(rule).exists();    
    assertThat(rule.listFiles()).isEmpty();  //reports should be cleaned before analysis, since no reports are genrated it should be empty at the end here
    //walkin(testScanner.getRoot(),"+--");
  }
  
  @Test
  public void test_uploading_config() {
  
    ZamiaRunner zamiaRunner = new ZamiaRunner(context, runnerContext);
    Path tempBuildPath =  createConfigTempFile("temp");
    
    zamiaRunner.uploadConfigToZamia(tempBuildPath);  
    
    assertThat(new File(project,"BuildPath.txt").exists()).isTrue();
    assertThat(new File(ruleChecker,"rc_config_selected_rules.xml").exists()).isTrue();
    assertThat(new File(ruleChecker,"rc_handbook_parameters.xml").exists()).isTrue();
    assertThat(new File(hb,"handbook.xml").exists()).isTrue();
  }
  
  @Test
  public void test_uploading_input_files() throws IOException {
    testProject.newFolder("home","project1","src");
    testProject.newFolder("home","project1","src","MUX");
    addTestFile2(context, testProject, "home/project1/src/Top.vhd");
    addTestFile2(context, testProject, "home/project1/src/a.vhd");
    addTestFile2(context, testProject, "home/project1/src/MUX/a.vhd");
    addTestFile2(context, testProject, "home/project1/src/MUX/b.vhd");
    addTestFile2(context, testProject, "home/project1/src/c.txt");
    
    new ZamiaRunner(context, new RunnerContext()).uploadInputFilesToZamia();
    
    Path vhdlTargetFolder = Paths.get(testScanner.getRoot().toURI()).resolve(PROJECT_DIRECTORY).resolve("vhdl");
    assertThat(vhdlTargetFolder.resolve("home/project1/src/Top.vhd").toFile().exists()).isTrue();
    assertThat(vhdlTargetFolder.resolve("home/project1/src/a.vhd").toFile().exists()).isTrue();
    assertThat(vhdlTargetFolder.resolve("home/project1/src/MUX/a.vhd").toFile().exists()).isTrue();
    assertThat(vhdlTargetFolder.resolve("home/project1/src/MUX/b.vhd").toFile().exists()).isTrue();
    //walkin(testScanner.getRoot(),"+--"); 
  }
  
  @Test
  public void test_read_only_vhdl_dir_should_log_errors() throws IOException {
    testScanner.newFile(PROJECT_DIRECTORY+"/vhdl/Top.vhd").setReadOnly();
    addTestFile2(context, testProject, "Top.vhd");
    
    new ZamiaRunner(context, runnerContext).uploadInputFilesToZamia();
    
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).contains("Unable to upload this vhdl source to project:");
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).contains("Top.vhd");
  }
  
  @Test
  public void test_clean_should_log_error_when_IOException() throws IOException {
    vhdl.delete();
    new ZamiaRunner(context, runnerContext).run();
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).contains("Unable to reset folder in scanner ");
  }
  
  @Test
  public void test_uploading_config_io_exeption_with_debug_on() throws IOException {
    logTester.setLevel(LoggerLevel.DEBUG);  
    ZamiaRunner zamiaRunner = new ZamiaRunner(context, runnerContext);
    Path tempBuildPath =  createConfigTempFile("temp");
        
    bp.setReadOnly();
    zamiaRunner.uploadConfigToZamia(tempBuildPath);   
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).contains("unable to upload configuration files to scanner:");
  }
  
  @Test
  public void test_cmd() {    
    //sets a cmd that works (linux and windows)    
    this.runnerContext.tryThisCmd(Arrays.asList("java", "-version"));
    
    new ZamiaRunner(context, runnerContext).runZamia();     
    
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(0);
    assertThat(logTester.logs(LoggerLevel.INFO)).hasSize(2);
    
    logTester.clear();
    logTester.setLevel(LoggerLevel.DEBUG);
    
    new ZamiaRunner(context, runnerContext).runZamia();
    
    assertThat(logTester.logs(LoggerLevel.INFO).size()).isGreaterThan(2);
    assertThat(logTester.logs(LoggerLevel.INFO).get(1)).contains("Running [java, -version]");    
  }
  
  @Test
  public void test_cmd_fail() {    
   //cmd fails by default
    new ZamiaRunner(context, runnerContext).runZamia();    
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).contains("Analysis has failed");
    assertThat(logTester.logs(LoggerLevel.INFO)).hasSize(2);        
  }
  

   
  
  public SensorContextTester createContext() {
    return SensorContextTester.create(projectRoot)
      .setSettings(settings
        .setProperty(VhdlRcSensor.SCANNER_HOME_KEY, testScanner.getRoot().toString())
        .setProperty(BuildPathMaker.TOP_ENTITY_KEY, "TOP"))
      .setRuntime(SonarRuntimeImpl.forSonarQube(VHDLRcPlugin.SQ_6_7, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
  }
  
  
  public static Path createConfigTempFile(String name) {
    try {
      Path tempFile = Files.createTempFile(name, ".tmp");
      List<String> lines = Arrays.asList("Line1", "Line2");
      Files.write(tempFile, lines, Charset.defaultCharset(), StandardOpenOption.WRITE);
      return tempFile;
    } catch (IOException e) {
      throw new IllegalStateException("Error in junit trying to generate temp file " + name, e);
    }
  }
    
  public static void addTestFile2(SensorContextTester context,  TemporaryFolder temp, String file) throws IOException {
    System.out.println("temp file created : " + temp.newFile(file).getAbsolutePath());
    DefaultInputFile f = TestInputFileBuilder.create("project-id", file)
      .setModuleBaseDir(projectRoot)
      .setLanguage(Vhdl.KEY)
      .setCharset(UTF_8)
      .setContents("a random content for this file")
      .build();
    System.out.println("input file created : " + f.absolutePath());
    context.fileSystem().add(f);
  }
  
  public File newFolderInScannerProject(String... folderNames) throws IOException {
    ArrayList<String> p = new ArrayList<>(projectPathList);
    for(String f : folderNames) {
      p.add(f);
    }
    return testScanner.newFolder(p.toArray(new String[p.size()]));
  }
  
  public static void walkin(File dir, String space) {
    System.out.println(space + dir.getName());
    if(dir.isDirectory()) {
      for(File f : dir.listFiles()) {
        walkin(f,"   " + space);
      }
    }
  }
  
  public static class RunnerContextTester extends RunnerContext{

    List<String> customCmd = new ArrayList<>();
    
    public void tryThisCmd(List<String> customCmd) {
      this.customCmd = customCmd;
    }
    
    @Override
    protected ArrayList<String> buildCmd(String scannerHome) {
      ArrayList<String> cmd = new ArrayList<>();
      if(!customCmd.isEmpty()) {
        customCmd.forEach(s -> cmd.add(s));
      } else { 
        cmd.add("fail_command");
      }
      return cmd;
    }
  }
  
//  public class TestProcessBuilder extends ProcessBuilder {
//    
//  }
  
}
