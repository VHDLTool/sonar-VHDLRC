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
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  
  public MapSettings settings = new MapSettings();
  public File project;
  public File vhdl;
  public File ruleChecker;
  public File hb;
  public File bp;
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
    project = testScanner.newFolder("rc","ws","project");
    vhdl = testScanner.newFolder("rc","ws","project","vhdl");
    ruleChecker = testScanner.newFolder("rc","ws","project","rule_checker");
    hb = testScanner.newFolder("rc","ws","project","rule_checker","hb_vhdlrc");
    bp = testScanner.newFile("rc/ws/project/BuildPath.txt");
    //testScanner.newFile("rc/ws/project/rule_checker/hb_vhdlrc/handbook.xml");
  }

  @Test
  public void test() throws IOException {
    logTester.setLevel(LoggerLevel.DEBUG);
    SensorContextTester context = createContext();
    //vhdl folder should be cleaned before analysis
    testScanner.newFolder("rc","ws","project","vhdl","mustBeDeleted");
    testScanner.newFile("rc/ws/project/vhdl/2Delete.vhd");
    //Source files to copy to scanner vhdl folder
    testProject.newFolder("home","project1","src");
    testProject.newFolder("home","project1","src","MUX");
    addTestFile2(context, testProject, "home/project1/src/Top.vhd");
    addTestFile2(context, testProject, "home/project1/src/a.vhd");
    addTestFile2(context, testProject, "home/project1/src/MUX/a.vhd");   
    ZamiaRunner.run(context);
    Path vhdlTargetFolder = Paths.get(testScanner.getRoot().toURI()).resolve("rc/ws/project/vhdl");
    assertThat(vhdlTargetFolder.toFile()).exists();              //vhdl folder should not be deleted after analysis
    //assertThat(vhdlTargetFolder.toFile().listFiles()).isEmpty(); //vhdl folder should be cleaned after analysis
    walkin(testScanner.getRoot(),"+--");
  }
  
  @Test
  public void test_uploading_config() {
    SensorContextTester context = createContext();   
    ZamiaRunner zamiaRunner = new ZamiaRunner(context);
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
    new ZamiaRunner(context).uploadInputFilesToZamia();
    Path vhdlTargetFolder = Paths.get(testScanner.getRoot().toURI()).resolve("rc/ws/project/vhdl");
    assertThat(vhdlTargetFolder.resolve("home/project1/src/Top.vhd").toFile().exists()).isTrue();
    assertThat(vhdlTargetFolder.resolve("home/project1/src/a.vhd").toFile().exists()).isTrue();
    assertThat(vhdlTargetFolder.resolve("home/project1/src/MUX/a.vhd").toFile().exists()).isTrue();
    assertThat(vhdlTargetFolder.resolve("home/project1/src/MUX/b.vhd").toFile().exists()).isTrue();
    //walkin(testScanner.getRoot(),"+--"); 
  }
  
  @Test
  public void read_only_scanner_should_log_errors() throws IOException {
    logTester.setLevel(LoggerLevel.DEBUG);
    SensorContextTester context = createContext();
    System.out.println(bp.setReadOnly());
    //Source files to copy to scanner vhdl folder
    addTestFile2(context, testProject,"Top.vhd");
    ZamiaRunner.run(context);    
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
