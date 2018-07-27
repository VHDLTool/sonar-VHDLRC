package com.linty.sonar.plugins.vhdlrc.issues;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

public class ExternalReportProvider {
	
	private List<Path> reports = new ArrayList<>();
	private Path serverHomeDir;
	
	public ExternalReportProvider(Path serverHomeDir) {
		this.serverHomeDir = serverHomeDir;
	}
	
	public static  List<Path> getReportFiles(Path serverHomeDir){
		return new ExternalReportProvider(serverHomeDir).collectReportFiles();
	}
	
	public List<Path> collectReportFiles() {

			try (Stream<Path> paths = Files.walk(serverHomeDir)
					.filter(f -> ! f.toFile().isDirectory())
					.filter(f -> FilenameUtils.getExtension(f.toString()).equals("xml"))
					){
				paths.forEach(reports::add);
			} catch (IOException e) {
			
			}
		return reports;
	}
}
