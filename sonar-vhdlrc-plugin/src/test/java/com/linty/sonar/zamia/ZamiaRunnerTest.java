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
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import static org.assertj.core.api.Assertions.assertThat;


public class ZamiaRunnerTest {
  
  private static final String PROJECT_HOME = "src/test/files/vhdl";
  
  public File project;
  public File vhdl;
  public File ruleChecker;
  public File hb;
  
  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();
  
  @Before
  public void initialize() throws IOException {
    project = testFolder.newFolder("rc","ws","project");
    vhdl = testFolder.newFolder("rc","ws","project","vhdl");
    ruleChecker = testFolder.newFolder("rc","ws","project","rule_checker");
    hb = testFolder.newFolder("rc","ws","project","rule_checker","hb_vhdlrc");
  }

  @Test
  public void test() {
    SensorContextTester context = createContext();
    ZamiaRunner zamiaRunner = new ZamiaRunner(context);
    zamiaRunner.run();
  }
  
  @Test
  public void test_uploading_config() {
    SensorContextTester context = createContext();   
    ZamiaRunner zamiaRunner = new ZamiaRunner(context);
    Path tempBuildPath =  createConfigTempFile("temp");
    zamiaRunner.uploadConfigToZamia(tempBuildPath);  
    assertThat(new File(project,"BuildPath.txt").exists()).isTrue();
  }


  public SensorContextTester createContext() {
    return SensorContextTester.create(Paths.get(PROJECT_HOME))
      .setSettings(new MapSettings()
        .setProperty(VhdlRcSensor.SCANNER_HOME_KEY, testFolder.getRoot().toString())
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
  
}
