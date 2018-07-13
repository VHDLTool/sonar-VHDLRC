package com.linty.sonar.plugins.vhdlrc.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FigureSvg {
	
	String figureRef;
	String height;
	String width;
	String figureCode;
	
	private static final String DEFAULT_DIM = "300px";
	
	private Pattern widthPattern = Pattern.compile(".*width=\\\"(\\d*\\.\\d+|\\d+\\.\\d*)\\\"");
	private Pattern heightPattern = Pattern.compile(".*height=\\\"(\\d*\\.\\d+|\\d+\\.\\d*)\\\"");
	
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
	
	public boolean hasImage() {
		return !this.figureCode.isEmpty();
	}


	public void changeToScalable() {
		
		Matcher m1;
		Matcher m2;

		m1 = widthPattern.matcher(this.figureCode);
		StringBuffer sb = new StringBuffer();
		
		//replace the width
		if(m1.find()) {
			m1.appendReplacement(sb, "   width=\"100%\"");
			m1.appendTail(sb);
			this.figureCode=sb.toString();
		}
		
		m2 = heightPattern.matcher(this.figureCode);
		//clear StringBuffer
		sb.setLength(0);
		
		//replace the height
		if(m2.find()) {
			m2.appendReplacement(sb, "   height=\"100%\"");
			m2.appendTail(sb);
			this.figureCode=sb.toString();
		}	
		
	}


	
}
