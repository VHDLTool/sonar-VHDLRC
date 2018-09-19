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

package com.linty.sonar.plugins.vhdlrc.issues;


import com.google.common.collect.ImmutableList;
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
	
  private static final ImmutableList<String> IGNORE = ImmutableList.of(
    "rc_sonarqube_rule_report.xml",
    "rc_report_rule.xml"
    );
    
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
					.filter(f -> !IGNORE.contains(f.getFileName().toString()))
					){
				paths.forEach(reports::add);
			} catch (IOException e) {
				LOG.error("Unable to get xml reports, check path : {}",reportsDir);
				if(LOG.isDebugEnabled()) {
					LOG.debug("{}",e);
				}
			}
		return reports;
	}
}
