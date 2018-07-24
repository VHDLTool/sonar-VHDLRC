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
	
	@Deprecated
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
