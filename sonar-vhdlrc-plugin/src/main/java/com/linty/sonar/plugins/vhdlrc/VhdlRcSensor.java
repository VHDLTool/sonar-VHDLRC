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

package com.linty.sonar.plugins.vhdlrc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.fest.util.VisibleForTesting;
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
import com.linty.sonar.plugins.vhdlrc.issues.ExternalReportProvider;
import com.linty.sonar.plugins.vhdlrc.issues.Issue;
import com.linty.sonar.plugins.vhdlrc.issues.ReportXmlParser;
import com.linty.sonar.zamia.BuildPathMaker;
import com.linty.sonar.zamia.ZamiaRunner;

public class VhdlRcSensor implements Sensor {
  public static final String SCANNER_HOME_KEY ="sonar.vhdlrc.scanner.home";
	public static final String REPORTING_PATH = "rc/ws/project/rule_checker/reporting/rule";
	private static final Logger LOG = Loggers.get(VhdlRcSensor.class);
	
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
// TODO 
//	  if(getTopEntities(context.config()).length == 0) {
//	    LOG.warn("Vhdlrc anaysis skipped : No defined Top Entity. See BuildPathMaker.TOP_ENTITY_KEY");
//	    LOG.warn("Zamia Issues will still be imported");
//	  } else {
//	    ZamiaRunner.run(context); 
//	  }
		Path reportsDir = Paths
		  .get(context.config()
		    .get(SCANNER_HOME_KEY)
		    .orElseThrow(() -> new IllegalStateException("vhdlRcSensor should not execute without " + SCANNER_HOME_KEY)))
		  .resolve(REPORTING_PATH);
		List<Path> reportFiles = ExternalReportProvider.getReportFiles(reportsDir);
		reportFiles.forEach(report -> importReport(report, context));
	}

	@VisibleForTesting
	protected void importReport(Path reportFile, SensorContext context) {
	  try {
	    LOG.info("Importing {}", reportFile.getFileName());
	    ReportXmlParser.getIssues(reportFile).forEach(issue -> importIssue(context, issue));		
	  } catch (XMLStreamException e) {			
	    LOG.error("Error when reading xml report : {}", e.getLocation());
	  } catch (RuntimeException e) {
	    LOG.warn("Can't import an issue from {} : {}", reportFile.getFileName(), e.getMessage());
	  }
	}

	private void importIssue(SensorContext context, Issue i) {
	  InputFile inputFile;
	  NewIssueLocation issueLocation;
	  String filePath = i.file().toString();
	  FilePredicates predicates = context.fileSystem().predicates();
	  inputFile = context.fileSystem().inputFile(predicates.hasPath(filePath));
	  if (inputFile == null) {
	    LOG.warn("Input file not found : {}. No rc issues will be imported on this file.", filePath);
	  } else {
	    NewIssue ni = context.newIssue()
	      .forRule(RuleKey.of("vhdlrc-repository",i.ruleKey()));
	    issueLocation = ni.newLocation()
	      .on(inputFile)
	      .at(inputFile.selectLine(i.line()))
	      .message(i.errorMsg());
	    ni.at(issueLocation);
	    ni.save();
	  }
	}
	
	
	 public static String[] getTopEntities(Configuration config ) {
	    return Arrays.stream(config.getStringArray(BuildPathMaker.TOP_ENTITY_KEY))
	      .filter(s -> s != null && !s.trim().isEmpty()).toArray(String[]::new);   
	  }

}
