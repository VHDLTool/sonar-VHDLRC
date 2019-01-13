/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2019 Maxime Facquet
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
