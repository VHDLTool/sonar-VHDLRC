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


import com.linty.sonar.params.ZamiaParam;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class Rule {
	
  String ruleKey;
  String name;

  String parentUid;
  String technology;
  String applicationFields;
  String category;
  String subCategory;
  String status;
  String rationale;
  String shortDescription;
  String longDescription; 

  String type;
  String sonarSeverity;
  String remediationEffort;
  String tag;

  String goodExDesc;
  String goodExampleRef;
  String goodExampleCode;

  String badExDesc;
  String badExampleRef;
  String badExampleCode;

  String figureDesc;
  FigureSvg figure;
  
  private List<ZamiaParam> parameters = new ArrayList<>();
  
  public List<ZamiaParam> parameters(){
    return this.parameters;
  }

  private static final String EMPTY_STRING="";

  public Rule(){
    this.name=             EMPTY_STRING;
    this.parentUid=        EMPTY_STRING;
    this.technology=       EMPTY_STRING;
    this.applicationFields=EMPTY_STRING;
    this.category=         EMPTY_STRING;
    this.remediationEffort=EMPTY_STRING;
    this.sonarSeverity=    EMPTY_STRING;
    this.tag=              EMPTY_STRING;
    this.type=             EMPTY_STRING;  
    this.subCategory=      EMPTY_STRING;    
    this.status=     	   EMPTY_STRING;    
    this.rationale=        EMPTY_STRING;
    this.shortDescription= EMPTY_STRING;
    this.longDescription=  EMPTY_STRING;         
    this.goodExDesc=       EMPTY_STRING;
    this.goodExampleRef=   EMPTY_STRING;        
    this.badExDesc=        EMPTY_STRING;
    this.badExampleRef=    EMPTY_STRING;       
    this.figureDesc=       EMPTY_STRING; 
  }


    public String buildHtmlDescritpion() {

      StringBuilder htmlCode = new StringBuilder();

      htmlCode
      .append(theCategorySection())
      .append(theDesciptionSection())
      .append(theRationaleSection())
      .append(theExamplesSection())
      .append(theFigureSection());

      return String.valueOf(htmlCode);    
    }

    
    private String theCategorySection() {
    	StringBuilder text = new StringBuilder(); 
    	final String SPACE = "&nbsp; &nbsp; ";
    	text
    	.append("<div>")
    	
    	.append("<b>Category : </b>").append(this.category).append(SPACE)
    	.append("<b>SubCategory : </b>").append(this.subCategory).append(SPACE)   	
    	.append("<b>Application Fields : </b>").append(this.applicationFields).append(SPACE)
        .append("<b>Technology : </b>").append(this.technology).append(SPACE)
    	.append("<b>Status : </b>").append(this.status).append(SPACE);
    	if(!StringUtils.isEmpty(this.parentUid)) {
    	  text.append("<b>Parent Rule : </b>").append(this.parentUid).append(SPACE);
    	}
    	text
    	.append("</div>")
    	.append("<br>\n");
    	return String.valueOf(text);
    }
    
    private String theDesciptionSection() {
    	 StringBuilder desc = new StringBuilder();    	 
    	 desc
    	 .append("<div>")
    	 .append("<h1><b>Short Description</b></h1>")
    	 .append("<p>")
    	 .append(formatToHtml(this.shortDescription));
    	 
    	 if(! "No additional information.".equals(this.longDescription)){
    		 desc
    		 .append("<h1><b>Description</b></h1>")
    		 .append("<br>").append(formatToHtml(this.longDescription));
    	 }
    	 desc.append("</p></div>\n");
    	 return String.valueOf(desc);
   }
    
    private String theRationaleSection() {
    	StringBuilder rt = new StringBuilder();
    	rt
    	.append("<div>")
    	.append("<h1><b>Rationale</b></h1>")
    	.append("<p>")
    	.append(formatToHtml(this.rationale))
    	.append("</p>")
    	.append("</div>\n");
    	return String.valueOf(rt);
    }
    
    private String theExamplesSection() {
    	StringBuilder example = new StringBuilder();  	
    	if(!StringUtils.isEmpty(this.goodExampleCode)) {  		
    		example
    		.append("<div>")
    		.append("<h1><b>Compliant code example</b></h1>")
    		.append("<pre>"+this.goodExampleCode+"</pre>")
    		.append("<p><font size=1 color=\"grey\">")
    		.append(this.goodExDesc)
    		.append("</font></p>")
    		.append("</div>\n");
    	}
    	if(!StringUtils.isEmpty(this.badExampleCode)) {
    		example
    		.append("<div>")
    		.append("<h1><b>Non compliant code example</b></h2>")
    		.append("<pre>"+this.badExampleCode+"</pre>")
    		.append("<p><font size=1 color=\"grey\">")
    		.append(this.badExDesc)
    		.append("</font></p>")
    		.append("</div>\n");
    	}
    	return String.valueOf(example);
    }
    
    private String theFigureSection() {
    	StringBuilder s = new StringBuilder();	 
    	if(this.figure!=null && this.figure.hasImage()) {
    		this.figure.loadOriginialDim();
    		s
    		.append("<div>")
    		
    		.append("\n<div>")
        .append("<p><font size=1 color=\"grey\">")  
        .append(this.figureDesc)
        .append("</font></p>")
        .append("</div>\n")
        
    		.append(buildSvgHeader(figure.width,figure.height,figure.originalWidth(),figure.originalHeight()))
    		.append("\n"+this.figure.figureCode)
    		.append("</svg>")
    		
    		.append("\n<div>")
    		.append("<p><font size=1 color=\"grey\">") 	
    		.append("Extracted from ")
    		.append(this.figure.figureRef)
    		.append("</font></p>")
    		.append("</div>\n")
    		
    		.append("</div>\n");
    	}
    	return String.valueOf(s);
    }
    
    private String buildSvgHeader(String w, String h, String oW, String oH) {
    	StringBuilder s = new StringBuilder();
    	s.append("<svg width=\"").append(w).append("\"")
    	.append(" height=\"").append(h).append("\"")
    	.append(" viewBox=\"0 0 ").append(oW).append(" ").append(oH)
    	.append("\">");
    	return String.valueOf(s);
    }
 
    private static String formatToHtml(String s) {
    	return s.replaceAll("\\n", "<br>");   	
    }


    
    
    
	

}
