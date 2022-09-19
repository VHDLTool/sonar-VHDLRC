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
package com.lintyservices.sonar.zamia;

import com.lintyservices.sonar.test.utils.fileTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.input.BrokenInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectedRulesMakerTest {

  List<String> ruleKeys;

  @Rule
  public LogTester logTester = new LogTester();

  //Build checking
  @Test
  public void test_template_file_is_present_in_ressources() {
    try {
      ruleKeys = Arrays.asList("TST_00001");
      SelectedRulesMaker.makeWith(ruleKeys);
    } catch (NullPointerException e) {
      throw new IllegalStateException("Configuration File template not found : src/main/resources/virgin_conf/rc_config_selected_rules.xml");
    }
  }

  @Test
  public void test() {
    ruleKeys = Arrays.asList(
      "TST_00001",
      "TST_00002",
      "UTI_00001",
      "APK_00022"
    );

    Path result = SelectedRulesMaker.makeWith(ruleKeys);
    Path expected = Paths.get("src/test/parameters/rc_selected_rules/rc_config_selected_rules_expected.xml");
    fileTestUtils.compareFiles(result, expected, false);
  }

  @Test
  public void test_empty_list_warning() {
    logTester.setLevel(LoggerLevel.DEBUG);
    ruleKeys = Arrays.asList();//empty 
    assertThat(ruleKeys).isEmpty();

    Path result = SelectedRulesMaker.makeWith(ruleKeys);

    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.WARN)).isNotEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN).get(0)).contains("No rules to load in " + ZamiaRunner.RC_CONFIG_SELECTED_RULES);

    Path expected = Paths.get("src/test/parameters/rc_selected_rules/empty_selected_rules.xml");
    fileTestUtils.compareFiles(result, expected, false);
  }

  @Test
  public void test_IOExeption() throws IOException {
    ruleKeys = Arrays.asList("a");
    try {
      new SelectedRulesMaker(ruleKeys).make(new BrokenInputStream());
      fail("rc_config_selected_rules not found should trhow Excepetion");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("Unable to generate " + ZamiaRunner.RC_CONFIG_SELECTED_RULES);
    }
  }
}
