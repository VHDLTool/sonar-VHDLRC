
package com.linty.sonar.plugins.vhdlrc.issues;


import java.nio.file.Path;

public class Issue {
	String ruleKey;
	String remediationMsg;
	String errorMsg;
	Path file;
	int line;
	
	public String ruleKey() {
		return ruleKey;
	}
	public String remediationMsg() {
		return remediationMsg;
	}
	public String errorMsg() {
		return errorMsg;
	}
	public Path file() {
		return file;
	}
	public int line() {
		return line;
	}


}
