package com.linty.sonar.plugins.vhdlrc.issues;

import java.nio.file.Path;

public class Issue {
	public String ruleKey;
	public String remediationMsg;
	public String errorMsg;
	
	public Path file;
	public int line;
	
}
