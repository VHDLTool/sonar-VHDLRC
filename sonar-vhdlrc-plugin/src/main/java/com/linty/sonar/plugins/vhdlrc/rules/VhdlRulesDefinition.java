package com.linty.sonar.plugins.vhdlrc.rules;

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Context;

import com.linty.sonar.plugins.vhdlrc.Vhdl;

@ServerSide
public class VhdlRulesDefinition implements RulesDefinition {

	
	public static final String PATH_TO_HANDBOOK_KEY = "sonar.vhdlrc.handbookPath"; 
	public static final String PATH_TO_REPORTING_DEFAULT = "";
	private static final String PATH_TO_REPORTING_DESC = "Path to the directory containing handbook.xml file. The path may be absolute or relative to the server base directory.";
	private final ServerFileSystem serverFileSystem;
	
	public VhdlRulesDefinition (ServerFileSystem serverFileSystem) {
		this.serverFileSystem = serverFileSystem;
	}
	
	@Override
	public void define(Context context) {
		
		System.out.println("_______________"+serverFileSystem.getHomeDir().getPath());
	}

}
