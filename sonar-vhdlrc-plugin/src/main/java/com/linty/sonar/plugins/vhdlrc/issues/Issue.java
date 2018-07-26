package com.linty.sonar.plugins.vhdlrc.issues;

import java.nio.file.Path;

public class Issue {
	String ruleKey;
	String remediationMsg;
	String errorMsg;
	
	Path file;
	int line;
	
}
