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

package com.linty.sonar.plugins.vhdlrc.rules;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class FigureSvg {
	
	String figureRef;
	String height;
	String width;
	private String originalHeight;
	private String originalWidth;
	String figureCode;
	
	private static final String DEFAULT_DIM = "300";
	
	private Pattern widthPattern = Pattern.compile(".*width=\\\"(\\d+|\\d*\\.\\d+|\\d+\\.\\d*)(\\w*)\\\"");
	private Pattern heightPattern = Pattern.compile(".*height=\\\"(\\d+|\\d*\\.\\d+|\\d+\\.\\d*)(\\w*)\\\"");
	
	//For testing
	public FigureSvg() {
		this.figureRef="";
		this.height=DEFAULT_DIM;
		this.width=DEFAULT_DIM;
		this.figureCode="";
	}
	
	public FigureSvg(String figureRef, String height, String width) {
		this.figureRef = figureRef;
		this.height = (height!=null && !height.isEmpty()) ? height : DEFAULT_DIM;
		this.width= (width!=null && !width.isEmpty()) ? width : DEFAULT_DIM;
		this.figureCode="";
	}
	
	public String originalWidth() {
		return this.originalWidth != null ? this.originalWidth : this.width;
	}
	
	public String originalHeight() {
		return this.originalHeight != null ? this.originalHeight : this.height;
	}
	
	public boolean hasImage() {
		return !StringUtils.isEmpty(this.figureCode);
	}

	public void loadOriginialDim() {
		Matcher m1;
		Matcher m2;

		m1 = widthPattern.matcher(this.figureCode);
		//find the internal width
		if(m1.find()) {
			this.originalWidth=m1.group(1);
		}

		m2 = heightPattern.matcher(this.figureCode);
		//find the internal width
		if(m2.find()) {
			this.originalHeight=m2.group(1);
		}			
	}	
	
}
