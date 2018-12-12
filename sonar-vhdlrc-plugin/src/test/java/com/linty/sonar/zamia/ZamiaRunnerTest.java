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


import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
import com.linty.sonar.zamia.ZamiaRunner.RunnerContext;
import java.io.ByteArrayInputStream;
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


public class ZamiaRunnerTest {
  
  public static class RunnerContextTester extends RunnerContext{
    
    //private final String WIN_EXE = "src/test/files/eclipse_test.bat";
    //private final String UNIX_EXE = "src/test/files/eclipse_test_linux.bash";
    //private final String ARGS = "-version";
    
    @Override
    protected ArrayList<String> buildCmd(String scannerHome) {
      ArrayList<String> cmd = new ArrayList<>();
//      cmd.add("P:\\Tools\\notepad++\\notepad++.exe");  
//      cmd.add("-alwaysOnTop"); 
//      cmd.add("P:\\dev\\eclipse_test.bat"); 
      cmd.add("no");
      return cmd;
    }
  }
  
  public static final String PROJECT_DIRECTORY = VhdlRcSensor.PROJECT_DIR;
  public MapSettings settings = new MapSettings();
  public ArrayList<String> projectPathList = new ArrayList<>(Arrays.asList(PROJECT_DIRECTORY.split("/")));
  public File project;
  public File vhdl;
  public File ruleChecker;
  public File hb;
  public File bp;
  public ArrayList<String> v = new ArrayList<>(projectPathList);
  public ArrayList<String> r = new ArrayList<>(projectPathList);
  public static Path projectRoot;
  
  @Rule
  public LogTester logTester = new LogTester();
  @Rule
  public TemporaryFolder testScanner = new TemporaryFolder();
  @Rule
  public TemporaryFolder testProject = new TemporaryFolder();
  
  @Before
  public void initialize() throws IOException {
    projectRoot = Paths.get(testProject.getRoot().toURI());//temporary project for testing
    //Temporary Scanner home structure
    project = testScanner.newFolder(projectPathList.toArray(new String[projectPathList.size()]));
    
    v.add("vhdl");   
    r.add("rule_checker");
    
    vhdl = testScanner.newFolder(v.toArray(new String[v.size()]));
    ruleChecker = testScanner.newFolder(r.toArray(new String[r.size()]));
    
    r.add("hb_vhdlrc");
    
    hb = testScanner.newFolder(r.toArray(new String[r.size()]));
    bp = testScanner.newFile(PROJECT_DIRECTORY + "/BuildPath.txt");
    //testScanner.newFile("rc/ws/project/rule_checker/hb_vhdlrc/handbook.xml");
  }

  @Test
  public void test() throws IOException {
    logTester.setLevel(LoggerLevel.DEBUG);
    SensorContextTester context = createContext();
    //vhdl folder should be cleaned before analysis
    ArrayList<String> v2 = new ArrayList<>(v);
    v2.add("mustBeDeleted");
    testScanner.newFolder(v2.toArray(new String[v2.size()]));
    testScanner.newFile(PROJECT_DIRECTORY + "/vhdl/2Delete.vhd");
    walkin(testScanner.getRoot(),"+--");
    //Source files to copy to scanner vhdl folder
    testProject.newFolder("home","project1","src");
    testProject.newFolder("home","project1","src","MUX");
    addTestFile2(context, testProject, "home/project1/src/Top.vhd");
    addTestFile2(context, testProject, "home/project1/src/a.vhd");
    addTestFile2(context, testProject, "home/project1/src/MUX/a.vhd");   
    ZamiaRunner.run(context);
    Path vhdlTargetFolder = Paths.get(testScanner.getRoot().toURI()).resolve(PROJECT_DIRECTORY).resolve("vhdl");
    assertThat(vhdlTargetFolder.toFile()).exists();              //vhdl folder should not be deleted after analysis
    assertThat(vhdlTargetFolder.toFile().listFiles()).isEmpty(); //vhdl folder should be cleaned after analysis
    //walkin(testScanner.getRoot(),"+--");
  }
  
  @Test
  public void test_uploading_config() {
    SensorContextTester context = createContext();   
    ZamiaRunner zamiaRunner = new ZamiaRunner(context, new RunnerContext());
    Path tempBuildPath =  createConfigTempFile("temp");
    zamiaRunner.uploadConfigToZamia(tempBuildPath);  
    assertThat(new File(project,"BuildPath.txt").exists()).isTrue();
    assertThat(new File(ruleChecker,"rc_config_selected_rules.xml").exists()).isTrue();
    assertThat(new File(ruleChecker,"rc_handbook_parameters.xml").exists()).isTrue();
    assertThat(new File(hb,"handbook.xml").exists()).isTrue();
  }
  
  @Test
  public void test_uploading_input_file_2() throws IOException {
    SensorContextTester context = createContext();
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
  public void read_only_vhdl_dir_should_log_errors() throws IOException {
    SensorContextTester context = createContext();
    System.out.println(testScanner.newFile(PROJECT_DIRECTORY+"/vhdl/Top.vhd").setReadOnly());
    addTestFile2(context, testProject, "Top.vhd");
    System.out.println();
    new ZamiaRunner(context, new RunnerContextTester()).uploadInputFilesToZamia();
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Unable to upload vhdl sources to scanner");
  }
  
  @Test
  public void clean_should_log_error_when_IOException() throws IOException {
    SensorContextTester context = createContext();
    System.out.println(vhdl.delete());
    new ZamiaRunner(context, new RunnerContextTester()).run();
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).contains("Unable to reset folder in scanner ");
  }
  
  @Test
  public void read_only_scanner_should_log_errors() throws IOException {
    logTester.setLevel(LoggerLevel.DEBUG);
    SensorContextTester context = createContext();
    RunnerContextTester runnerContext = new RunnerContextTester();
    System.out.println(bp.setReadOnly());
    //Source files to copy to scanner vhdl folder
    addTestFile2(context, testProject,"Top.vhd");
    new ZamiaRunner(context, runnerContext).run();    
  }
  
  @Test
  public void test_cmd() {
    SensorContextTester context = createContext();
    RunnerContextTester runnerContext = new RunnerContextTester();
    new ZamiaRunner(context, runnerContext).runZamia();    
  }
  
  @Test
  public void should_log_all_zamiacad_output_when_debbug_on() throws IOException {
    logTester.setLevel(LoggerLevel.DEBUG);
    SensorContextTester context = createContext();
    RunnerContextTester runnerContext = new RunnerContextTester();
    String msg ="line one\r\n" + 
      "line2\r\n" + 
      "line3\r\n" + 
      "line4\r\n" + 
      "line5\r\n" + 
      "line6\r\n" + 
      "line7\r\n" + 
      "line8\r\n" + 
      "l9\r\n" + 
      "l10\r\n" + 
      "l11\r\n" + 
      "l12\r\n" + 
      "l13\r\n" + 
      "l14";
    new ZamiaRunner(context, runnerContext).consume(new ByteArrayInputStream(msg.getBytes()));
    assertThat(logTester.logs(LoggerLevel.INFO).size()).isEqualTo(14);
    System.out.println("_");
    logTester.clear();
    msg ="line one\r\n" + 
      "line2\r\n" + 
      "line3\r\n" + 
      "line4\r\n";
    new ZamiaRunner(context, runnerContext).consume(new ByteArrayInputStream(msg.getBytes()));
    assertThat(logTester.logs(LoggerLevel.INFO).size()).isEqualTo(4);
  }
  
  @Test
  public void should_log_x_last_lines_of_zamiacad_output_when_debbug_off() throws IOException {
    SensorContextTester context = createContext();
    RunnerContextTester runnerContext = new RunnerContextTester();
    String msg ="line one\r\n" + 
      "line2\r\n" + 
      "line3\r\n" + 
      "line4\r\n" + 
      "line5\r\n" + 
      "line6\r\n" + 
      "line7\r\n" + 
      "line8\r\n" + 
      "l9\r\n" + 
      "l10\r\n" + 
      "l11\r\n" + 
      "l12\r\n" + 
      "l13\r\n" + 
      "l14";
    new ZamiaRunner(context, runnerContext).consume(new ByteArrayInputStream(msg.getBytes()));
    assertThat(logTester.logs(LoggerLevel.INFO).size()).isEqualTo(0);
    System.out.println("_");
    logTester.clear();
    msg ="line one\r\n" + 
      "line2\r\n" + 
      "line3\r\n" + 
      "line4\r\n";
    new ZamiaRunner(context, runnerContext).consume(new ByteArrayInputStream(msg.getBytes()));
    assertThat(logTester.logs(LoggerLevel.INFO).size()).isEqualTo(0);
  }
  
  
  public SensorContextTester createContext() {
    return SensorContextTester.create(projectRoot)
      .setSettings(settings
        .setProperty(VhdlRcSensor.SCANNER_HOME_KEY, testScanner.getRoot().toString())
        .setProperty(BuildPathMaker.TOP_ENTITY_KEY, "TOP"))
      .setRuntime(SonarRuntimeImpl.forSonarQube(VHDLRcPlugin.SQ_6_7, SonarQubeSide.SCANNER));
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
      .setLanguage("vhdl")
      .setCharset(UTF_8)
      .setContents("a random content for this file")
      .build();
    System.out.println("input file created : " + f.absolutePath());
    context.fileSystem().add(f);
  }
  
  public static void walkin(File dir, String space) {
    System.out.println(space + dir.getName());
    if(dir.isDirectory()) {
      for(File f : dir.listFiles()) {
        walkin(f,"   " + space);
      }
    }
  }
  
}
