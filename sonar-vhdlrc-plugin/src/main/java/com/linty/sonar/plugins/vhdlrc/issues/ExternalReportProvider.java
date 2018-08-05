/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc.issues;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


public class ExternalReportProvider {
	
	private List<Path> reports = new ArrayList<>();
	private Path reportsDir;
	private static final Logger LOG = Loggers.get(ExternalReportProvider.class);
	
	public ExternalReportProvider(Path reportsDir) {
		this.reportsDir = reportsDir;
	}
	
	public static  List<Path> getReportFiles(Path reportsDir){
		return new ExternalReportProvider(reportsDir).collectReportFiles();
	}
	
	public List<Path> collectReportFiles() {

			try (Stream<Path> paths = Files.walk(reportsDir)
					.filter(f -> ! f.toFile().isDirectory())
					.filter(f -> FilenameUtils.getExtension(f.toString()).equals("xml"))
					.filter(f -> f.toFile().length()!=0)
					){
				paths.forEach(reports::add);
			} catch (IOException e) {
				LOG.error("Unable to get xml report in {}",reportsDir,e);
				if(LOG.isDebugEnabled()) {
					LOG.debug("{}",e);
				}
			}
		return reports;
	}
}
