package com.linty.sonar.plugins.vhdlrc;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.fest.util.VisibleForTesting;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.linty.sonar.plugins.vhdlrc.issues.ExternalReportProvider;
import com.linty.sonar.plugins.vhdlrc.issues.Issue;
import com.linty.sonar.plugins.vhdlrc.issues.ReportXmlParser;

public class VhdlRcSensor implements Sensor{
	
	public static final String REPORTING_PATH = "log/reporting/rule";
	private ServerFileSystem sfs;
	private static final Logger LOG = Loggers.get(VhdlRcSensor.class);
	
	public VhdlRcSensor(ServerFileSystem sfs) {
		this.sfs = sfs;
	}

	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Import of RuleChecker Xml Reports");		
	}

	@Override
	public void execute(SensorContext context) {
		List<Path> reportFiles = ExternalReportProvider.getReportFiles(sfs.getHomeDir().toPath());
		reportFiles.forEach(report -> importReport(report, context));
	}

	@VisibleForTesting
	protected void importReport(Path reportFile, SensorContext context) {
		try {
			if(reportFile.toFile().exists()) {
				ReportXmlParser.getIssues(reportFile).forEach(issue -> importIssue(issue));
			} else {
				LOG.error("Can't find Xml report : {}", reportFile);
			}
		} catch (XMLStreamException e) {			
			LOG.error("Can't read Xml report : {}", reportFile, e);
		}
	}

	private void importIssue(Issue i) {
		System.out.println("-----------Uploading an issue for" + i.file.toString());
		
	}

}
