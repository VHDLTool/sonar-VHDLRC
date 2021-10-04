/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2021 Linty Services
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
package com.lintyservices.sonar.plugins.vhdlrc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;

import com.lintyservices.sonar.plugins.vhdlrc.VhdlRcPlugin;
import com.lintyservices.sonar.plugins.vhdlrc.metrics.CustomMetrics;
import com.lintyservices.sonar.zamia.BuildPathMaker;

import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class PureJavaSensorTest {

  private static final SonarRuntime SQ67 = SonarRuntimeImpl.forSonarQube(VhdlRcPlugin.SONARQUBE_LTS_VERSION, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
  private static final String PROJECT_ID = "vhdlrc-test";
  private static final String repo="vhdlrc-repository";
  private PureJavaSensor sensor = new PureJavaSensor();

  private SensorContextTester context = createContext("src");

  private void init() {    
    context = createContext("src");
  }

  @Rule
  public LogTester logTester = new LogTester();

  @Test
  public void test_6900() {
    init();
    addTestFile(context,"src/test/files/javasensor/test6900.vhd");
    addRule(context, "STD_06900");
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(3);
  }

  @Test
  public void test_3300() {
    init();
    addTestFile(context,"src/test/files/javasensor/test3300.vhd");
    addRule(context, "STD_03300");
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(4);
  }

  @Test
  public void test_6700() {
    init();
    addTestFile(context,"src/test/files/javasensor/test6700.vhd");
    addRule(context, "STD_06700");
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(8);
  }

  @Test
  public void test_2600() {
    init();
    addTestFile(context,"src/test/files/javasensor/test2600.vhd");
    addRule(context, "STD_02600");
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(2);
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(3);
    assertThat(issues.get(1).primaryLocation().textRange().start().line()).isEqualTo(4);
  }

  @Test
  public void test_2800() {
    init();
    addTestFile(context,"src/test/files/javasensor/testEmpty.vhd");
    addRule(context, "STD_02800", "Limit", "60");
    sensor.execute(context);
    init();
    addTestFile(context,"src/test/files/javasensor/test2800.vhd");
    addRule(context, "STD_02800", "Limit", "60");
    sensor.execute(context);
    assertThat(context.measure(context.module().key(), CustomMetrics.COMMENT_LINES_STD_02800).value()).isEqualTo(6);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(0);
    init();
    addTestFile(context,"src/test/files/javasensor/test2800.vhd");
    addRule(context, "STD_02800", "Limit", "40");
    sensor.execute(context);
    assertThat(context.measure(context.module().key(), CustomMetrics.COMMENT_LINES_STD_02800).value()).isEqualTo(6);
    issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
  }

  @Test
  public void test_2000() {
    init();
    addTestFile(context,"src/test/files/javasensor/test2000.vhd");
    addRule(context, "STD_02000", "Format", " ");   
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(8);
  }

  @Test
  public void test_2200() {
    init();
    addTestFile(context,"src/main/resources/configuration/HANDBOOK/Extras/VHDL/STD_02200_good.vhd");
    addRule(context, "STD_02200", "Format", "*Ver*,*Version*"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(0);
    init();
    addTestFile(context,"src/main/resources/configuration/HANDBOOK/Extras/VHDL/STD_02200_bad.vhd");
    addRule(context, "STD_02200", "Format", "*Version*");
    sensor.execute(context);
    issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
  }
  
  @Test
  public void test_4200() {
    init();
    addTestFile(context,"src/test/files/javasensor/test4200.vhd");
    addRule(context, "CNE_04200", "Format", "*aaa*, *Creation date*"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(0);
    init();
    addTestFile(context,"src/test/files/javasensor/test4200.vhd");
    addRule(context, "CNE_04200", "Format", "*aaa*, *bbb*");
    sensor.execute(context);
    issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
  }
  
  @Test
  public void test_2700() {
    init();   
    addTestFile(context,"src/test/files/javasensor/test2700_good.vhd");
    addRule(context, "CNE_02700", "Limit", "10"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(0);
    init();
    addTestFile(context,"src/test/files/javasensor/test2700_bad.vhd");
    addRule(context, "CNE_02700", "Limit", "10"); 
    sensor.execute(context);
    issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
  }
  
  @Test
  public void test_600() {
    init();
    addTestFile(context,"src/test/files/javasensor/STD_00600.vhd");
    addRule(context, "STD_00600", "Format", "vhd"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(0);
    init();
    addTestFile(context,"src/test/files/javasensor/STD_00600.vhdl");
    addRule(context, "STD_00600", "Format", "vhd"); 
    sensor.execute(context);
    issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
  }
  
  @Test
  public void test_300() {
    init();
    addTestFile(context,"src/test/files/javasensor/test300top.vhd");
    addTestFile(context,"src/test/files/javasensor/test300notTop.vhd");
    addRule(context, "CNE_00300"); 
    MapSettings settings = new MapSettings();
    settings.setProperty(BuildPathMaker.TOP_ENTITY_KEY, "test300top.vhd");
    context.setSettings(settings);
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertTrue(issues.get(0).primaryLocation().inputComponent().key().endsWith("test300top.vhd"));
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(6);
  }
  
  @Test
  public void test_6000() {
    init();
    addTestFile(context,"src/test/files/javasensor/testRange.vhd");
    addRule(context, "STD_06000"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(2);
  }
  
  @Test
  public void test_6100() {
    init();
    addTestFile(context,"src/test/files/javasensor/testRange.vhd");
    addRule(context, "STD_06100"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(2);
  }
  
  @Test
  public void test_5400() {
    init();
    addTestFile(context,"src/main/resources/configuration/HANDBOOK/Extras/VHDL/STD_05400_good.vhd");
    addTestFile(context,"src/main/resources/configuration/HANDBOOK/Extras/VHDL/STD_05400_bad.vhd");
    addRule(context, "STD_05400"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(4);
    assertTrue(issues.get(0).primaryLocation().inputComponent().key().endsWith("STD_05400_bad.vhd"));
    assertThat(issues.get(0).primaryLocation().textRange().start().line()).isEqualTo(64);
  }
  
  @Test
  public void test_std2700() {
    init();   
    addTestFile(context,"src/test/files/javasensor/teststd2700.vhd");
    addRule(context, "STD_02700"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(4);
  }
  
  @Test
  public void test_cne5400() {
    init();   
    addTestFile(context,"src/test/files/javasensor/CNE5400/a.vhd");
    addTestFile(context,"src/test/files/javasensor/CNE5400/b.vhd");
    addTestFile(context,"src/test/files/javasensor/CNE5400/c.vhd");
    addTestFile(context,"src/test/files/javasensor/CNE5400/d.vhd");
    addRule(context, "CNE_05400", "Limit", "2"); 
    sensor.execute(context);
    List<Issue> issues = new ArrayList<>(context.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().message()).isEqualTo("Too many nested packages : d <- c <- b <- a");
  }

  public static SensorContextTester createContext(String projectHomePath) {
    return SensorContextTester.create(Paths.get(projectHomePath))
      .setRuntime(SQ67);
  }

  public static void addRules(SensorContextTester context, String... args) {
    ActiveRulesBuilder builder = new ActiveRulesBuilder();
    for (String ruleKey : args) {
      builder.addRule (
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(repo, ruleKey))
        .setLanguage("vhdl")
        .build()
        );
    }
    context.setActiveRules(builder.build());
  }

  public static void addRule(SensorContextTester context, String ruleKey) {
    ActiveRulesBuilder builder = new ActiveRulesBuilder();
    builder.addRule (
      new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of(repo, ruleKey))
      .setLanguage("vhdl")
      .build()
      );
    context.setActiveRules(builder.build());
  }
  
  public static void addRule(SensorContextTester context, String ruleKey, String paramKey, String paramValue) {
    ActiveRulesBuilder builder = new ActiveRulesBuilder();
    builder.addRule (
      new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of(repo, ruleKey))
      .setLanguage("vhdl")
      .setParam(paramKey, paramValue)
      .build()
      );
    context.setActiveRules(builder.build());
  }


  public static void addTestFile(SensorContextTester context, String filePath) {
    Path path = Paths.get(filePath);
    try {
      context.fileSystem().add(TestInputFileBuilder.create(PROJECT_ID, context.fileSystem().baseDir(), path.toFile()).setLanguage("vhdl").initMetadata(new String(Files.readAllBytes(path), UTF_8)).build());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
}
