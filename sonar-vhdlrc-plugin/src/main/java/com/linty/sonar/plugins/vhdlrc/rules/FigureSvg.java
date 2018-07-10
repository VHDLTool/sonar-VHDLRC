package com.linty.sonar.plugins.vhdlrc.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FigureSvg {
	
	String height;
	String width;
	String figureCode;
	
	private Pattern widthPattern = Pattern.compile(".*width=\\\"(\\d*\\.\\d+|\\d+\\.\\d*)\\\"");
	private Pattern heightPattern = Pattern.compile(".*height=\\\"(\\d*\\.\\d+|\\d+\\.\\d*)\\\"");


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