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

import javax.xml.stream.XMLStreamException;


import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.internal.apachecommons.io.FilenameUtils;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

public class ReportXmlParserTest {

  static final Path reports_path = Paths.get("src/test/files/log/reporting/rule");

  Path report_no_issue = reports_path.resolve("rc_report_rule_STD_00400_Label for Process/rc_report_rule_STD_00400_Label for Process.xml");

  @Rule
  public LogTester logTester = new LogTester();

  @Test
  public void test() throws XMLStreamException {
    List<Issue> issues = ReportXmlParser.getIssues(report_no_issue);
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    assertThat(issues).hasSize(15);
    for (Issue i : issues) {
      assertThat(i.ruleKey).isNotNull().isNotEmpty();
      assertThat(i.errorMsg).isNotEmpty();
      assertThat(i.file).isNotNull();
    }
    assertThat(issues.get(3).file.toString()).isEqualTo(FilenameUtils.separatorsToSystem("./I2C/top.vhd"));
  }

  @Test
  public void test_no_issues() throws XMLStreamException {
    Path report = reports_path.resolve("rc_report_rule_STD_03800_Synchronous Elements Initialization/rc_report_rule_STD_03800_Synchronous Elements Initialization.xml");
    List<Issue> issues = ReportXmlParser.getIssues(report);
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    assertThat(issues).hasSize(21);
  }

  @Test
  public void no_found_rule_key_should_log_error() throws XMLStreamException {
    Path report = reports_path.resolve("report_with_issues/no_rule_key.xml");
    List<Issue> issues = ReportXmlParser.getIssues(report);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("No RuleKey found in no_rule_key.xml. No issues will not be imported from this report");
    assertThat(issues).isNull();
  }

  @Test(expected = XMLStreamException.class)
  public void non_found_report_should_throw_error() throws XMLStreamException {
    Path report = reports_path.resolve("no/found/file");
    List<Issue> issues = ReportXmlParser.getIssues(report);
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
  }

  @Test(expected = XMLStreamException.class)
  public void parse_error_should_throw_error() throws XMLStreamException {
    Path report = reports_path.resolve("report_with_issues/xml_parse_error.xml");
    List<Issue> issues = ReportXmlParser.getIssues(report);
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
  }

}
