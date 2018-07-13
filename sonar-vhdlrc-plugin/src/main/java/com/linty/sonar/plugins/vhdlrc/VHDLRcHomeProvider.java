package com.linty.sonar.plugins.vhdlrc;

import org.sonar.api.config.Configuration;
import org.sonar.api.platform.ServerFileSystem;

public class VHDLRcHomeProvider {
	
	private Configuration configuration;
    private ServerFileSystem serverFileSystem;
      
    public static final String VHDLRC_SUB_CATEGORY = "VHDL RuleCkecker";

}
