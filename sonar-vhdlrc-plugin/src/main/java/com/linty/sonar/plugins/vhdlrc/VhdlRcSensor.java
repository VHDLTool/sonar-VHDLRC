/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc;


import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.fest.util.VisibleForTesting;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.linty.sonar.plugins.vhdlrc.issues.ExternalReportProvider;
import com.linty.sonar.plugins.vhdlrc.issues.Issue;
import com.linty.sonar.plugins.vhdlrc.issues.ReportXmlParser;

public class VhdlRcSensor implements Sensor{
	public static final String PROJECT_PATH = "rc/ws/project";
	public static final String REPORTING_PATH = "log/reporting/rule";
	private static final Logger LOG = Loggers.get(VhdlRcSensor.class);
	

	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor
		.name("Import of RuleChecker Xml Reports")
		.onlyOnLanguage(Vhdl.KEY);		
	}

	@Override
	public void execute(SensorContext context) {
	    URL cwd = getClass().getProtectionDomain().getCodeSource().getLocation();
		LOG.info("\no-o-o-o-o-o-o-o-o--o-o\nCurrent working directory : " + cwd.toString() + "\no-o-o-o-o-o-o-o-o-o-o-o-o-o-o");
//		Path reportsDir = Paths.get(PROJECT_PATH).resolve(REPORTING_PATH);
//		List<Path> reportFiles = ExternalReportProvider.getReportFiles(reportsDir);
//		reportFiles.forEach(report -> importReport(report, context));
	}

	@VisibleForTesting
	protected void importReport(Path reportFile, SensorContext context) {
		try {
			if(reportFile.toFile().exists()) {
				ReportXmlParser.getIssues(reportFile).forEach(issue -> importIssue(issue));
			} else {
				LOG.error("Can't find Xml report : {}", reportFile);
			}
		} catch (XMLStreamException | RuntimeException  e) {			
			LOG.error("Can't read Xml report : {}", reportFile, e);
		}
	}

	private void importIssue(Issue i) {
		System.out.println("-----------Uploading an issue for" + i.file().toString());
		
	}

}
