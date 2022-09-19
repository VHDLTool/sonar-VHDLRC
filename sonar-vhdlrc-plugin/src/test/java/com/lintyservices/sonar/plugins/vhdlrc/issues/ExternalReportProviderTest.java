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
package com.lintyservices.sonar.plugins.vhdlrc.issues;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

public class ExternalReportProviderTest {

  @Rule
  public LogTester logTester = new LogTester();

  @Test
  public void test() {
    List<Path> paths = ExternalReportProvider.getReportFiles(Paths.get("src/test/files/log/reporting/rule"));
    assertThat(paths).hasSize(12);
    paths.forEach(p -> assertThat(p.getFileName().toString()).isNotEqualTo("rc_sonarqube_rule_report.xml"));
    paths.forEach(p -> assertThat(p.getFileName().toString()).isNotEqualTo("rc_report_rule.xml"));
  }

  @Test
  public void test_error_should_throws_IOException() {
    List<Path> paths = ExternalReportProvider.getReportFiles(Paths.get("src/test/not_existing"));
    assertThat(logTester.logs(LoggerLevel.ERROR)).isNotEmpty();
    assertThat(logTester.logs(LoggerLevel.ERROR).size()).isEqualTo(1);
    //System.out.println(logTester.logs(LoggerLevel.ERROR).get(0));
  }

  @Test
  public void test_error_with_debug_enable_should_log_stack_trace() {
    logTester.setLevel(LoggerLevel.DEBUG);
    List<Path> paths = ExternalReportProvider.getReportFiles(Paths.get("src/test/not_existing"));
    assertThat(logTester.logs(LoggerLevel.DEBUG)).isNotEmpty();
    assertThat(paths).isEmpty();
    //System.out.println(logTester.logs(LoggerLevel.ERROR).get(0));
  }

}
