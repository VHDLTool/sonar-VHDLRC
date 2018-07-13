package com.linty.sonar.plugins.vhdlrc.utils;

import java.io.File;
import org.sonar.api.platform.ServerFileSystem;
import org.junit.Test;


public class ServerFileSystemTester implements ServerFileSystem{
	
	private File serverHome;
	
	
	public ServerFileSystemTester(File serverHome) {
		this.serverHome = serverHome;
	}
	
	@Override
	public File getHomeDir() {		
		return serverHome;	
	}
	
	@Override
	public File getTempDir() {		
		return serverHome;
	}
	
	
	
}