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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.io.FileUtils;
									   
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.lintyservices.sonar.plugins.vhdlrc.issues.ExternalReportProvider;
import com.lintyservices.sonar.plugins.vhdlrc.issues.Issue;
import com.lintyservices.sonar.plugins.vhdlrc.issues.ReportXmlParser;
import com.lintyservices.sonar.zamia.BuildPathMaker;
import com.lintyservices.sonar.zamia.ZamiaRunner;

public class VhdlRcSensor implements Sensor {

  public static final String SCANNER_HOME_KEY = "sonar.vhdlrc.scanner.home";
  public static final String PROJECT_DIR = "rc/Data/workspace/project";
  public static final String REPORTING_PATH = PROJECT_DIR + "/rule_checker/reporting/rule";
  public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
  public static final String RC_SYNTH_REPORT_PATH = IS_WINDOWS ? ".\\report_" : "./report_";
  public static final String SOURCES_DIR = "vhdl";
  public static final String REPORTING_RULE = "rule_checker/reporting/rule";
  private static final String repo = "vhdlrc-repository";
  private static final Logger LOG = Loggers.get(VhdlRcSensor.class);
  private static List<String> unfoundFiles = new ArrayList<>();
  private FilePredicates predicates;

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("Import of RuleChecker Xml Reports")
      .onlyOnLanguage(Vhdl.KEY)
      .name("vhdlRcSensor")
      .onlyWhenConfiguration(conf -> conf.hasKey(SCANNER_HOME_KEY));
  }

  @Override
  public void execute(SensorContext context) {
    this.predicates = context.fileSystem().predicates();
    Configuration config = context.config();
    //ZamiaRunner-------------------------------------------------------
    String top = BuildPathMaker.getTopEntities(config);
    if (top.isEmpty()) {
      LOG.warn("Vhdlrc analysis skipped : No defined Top Entity. See " + BuildPathMaker.TOP_ENTITY_KEY);
      LOG.warn("Zamia Issues will still be imported");
    } else {
      ZamiaRunner.run(context);
    }
    //------------------------------------------------------------------

    Path reportsDir = Paths
      .get(config
        .get(SCANNER_HOME_KEY)
        .orElseThrow(() -> new IllegalStateException("vhdlRcSensor should not execute without " + SCANNER_HOME_KEY)))
      .resolve(REPORTING_PATH);
    List<Path> reportFiles = ExternalReportProvider.getReportFiles(reportsDir);
    Path rcSynthReport = Paths.get("./");
    List<Path> rcReportFiles = ExternalReportProvider.getReportFiles(rcSynthReport);
    rcReportFiles.removeIf(o -> !o.toString().startsWith(RC_SYNTH_REPORT_PATH));
    if (!rcReportFiles.isEmpty())
      reportFiles.addAll(rcReportFiles);
    reportFiles.forEach(report -> importReport(report, context));
    unfoundFiles.forEach(s -> LOG.warn("Input file not found : {}. No rc issues will be imported on this file.", s));

    String scannerHome = context.config()
      .get(VhdlRcSensor.SCANNER_HOME_KEY)
      .orElseThrow(() -> new IllegalStateException("vhdlRcSensor should not execute without " + VhdlRcSensor.SCANNER_HOME_KEY));
    if (!BuildPathMaker.getKeepSource(config)) {
      ZamiaRunner.clean(Paths.get(scannerHome, PROJECT_DIR, SOURCES_DIR));
    }
    String testDir = context.fileSystem().baseDir().toString().replace('/', '\\');
    if (!BuildPathMaker.getKeepReports(config) && !testDir.endsWith("src\\test\\files")) { //Second condition is here to avoid deletion of test files
      Path reportPath = Paths.get(scannerHome, PROJECT_DIR, REPORTING_RULE);
      ZamiaRunner.clean(reportPath);
      try {
        DirectoryStream<Path> dstream = Files.newDirectoryStream(Paths.get(scannerHome, PROJECT_DIR, REPORTING_RULE));
        if (dstream.iterator().hasNext()) {  // Zamiarunner.clean, which uses FileUtils.cleanDirectory, doesn't always delete files in subfolders
          FileUtils.forceDeleteOnExit(reportPath.toFile());
        }
        dstream.close();
      } catch (IOException e) {
        LOG.warn("Error while trying to clean reports directory");
      }
    }
  }

 @VisibleForTesting
  protected void importReport(Path reportFile, SensorContext context) {
    try {
      LOG.info("Importing {}", reportFile.getFileName());
      boolean rcSynth = reportFile.toString().startsWith(RC_SYNTH_REPORT_PATH);
      for (Issue issue : ReportXmlParser.getIssues(reportFile)) {
        try {
          importIssue(context, issue, rcSynth);
        } catch (RuntimeException e) {
          LOG.warn("Can't import an issue from report {} : {}", reportFile.getFileName(), e.getMessage());
        }
      }
    } catch (XMLStreamException e) {
      LOG.error("Error when reading xml report : {}", e.getLocation());
    }
  }

  private void importIssue(SensorContext context, Issue i, boolean reportFromRcsynth) {
    InputFile inputFile;
    NewIssueLocation issueLocation;
    Path p = i.file();
    Path filePath;
    if (reportFromRcsynth)
      filePath = p;
    else {
      Path root = Paths.get("./");
      filePath = root.resolve(p.subpath(2, p.getNameCount()));//Zamia adds "./vhdl" to inputFile path in reports
    }
    //FilePredicates predicates = context.fileSystem().predicates();
    inputFile = context.fileSystem().inputFile(predicates.hasPath(filePath.toString()));
    if (inputFile == null) {
      if (!unfoundFiles.contains(filePath.toString())) {
        unfoundFiles.add(filePath.toString());
      }
    } else {
      NewIssue ni = context.newIssue()
        .forRule(RuleKey.of(repo, i.ruleKey()));
      issueLocation = ni.newLocation()
        .on(inputFile)
        .at(inputFile.selectLine(i.line()))
        .message(i.errorMsg());
      ni.at(issueLocation);
      ni.save();
    }
  }

}
