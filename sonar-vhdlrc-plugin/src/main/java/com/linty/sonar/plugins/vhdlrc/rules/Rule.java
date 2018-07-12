package com.linty.sonar.plugins.vhdlrc.rules;

import com.linty.sonar.plugins.vhdlrc.rules.FigureSvg;

public class Rule {
	
	String ruleKey;
	String name;
	  
    String category;
    String subCategoty; 
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
    
    private static final String EMPTY_STRING="";
     
    public Rule(){
    	this.name=             EMPTY_STRING;  	  
        this.category=         EMPTY_STRING;
        this.remediationEffort=EMPTY_STRING;
        this.sonarSeverity=    EMPTY_STRING;
        this.tag=              EMPTY_STRING;
        this.type=             EMPTY_STRING;  
        this.subCategoty=      EMPTY_STRING;    
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
    	text
    	.append("<div>")
    	.append("<b>Category : </b>")
    	.append(this.category)
    	.append("&nbsp; &nbsp; ")
    	.append("<b>SubCategory : </b>")
    	.append(this.subCategoty)
    	.append("</div>")
    	.append("<br>\n");
    	return String.valueOf(text);
    }
    
    private String theDesciptionSection() {
    	 StringBuilder desc = new StringBuilder();    	 
    	 desc
    	 .append("<div>")
    	 .append("<h1><b>Descritpion</b></h1>")
    	 .append("<p>")
    	 .append(formatToHtml(this.shortDescription));
    	 
    	 if(this.longDescription != null && ! "No additional information.".equals(this.longDescription)){
    		 desc.append("<br>").append(formatToHtml(this.longDescription));
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
    	return String.valueOf(rationale);
    }
    
    private String theExamplesSection() {
    	StringBuilder example = new StringBuilder();  	
    	if(this.goodExampleCode!=null && !this.goodExampleCode.isEmpty()) {  		
    		example
    		.append("<div>")
    		.append("<h1><b>Compliant code example</b></h1>")
    		.append("<pre>"+this.goodExampleCode+"</pre>")
    		.append("<p><font size=1 color=\"grey\">")
    		.append(this.goodExDesc)
    		.append("</font></p>")
    		.append("</div>\n");
    	}
    	if(this.badExampleCode!=null && !this.badExampleCode.isEmpty()) {
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
    		this.figure.changeToScalable();
    		s
    		.append("<div style=\"width:" + this.figure.width + ";height:" + this.figure.height + "\">")
    		.append("\n"+this.figure.figureCode)
    		.append("</div>\n<div>")
    		.append("<p><font size=1 color=\"grey\">") 	
    		.append(this.figureDesc)
    		.append("</font></p>")
    		.append("</div>\n");
    	}
    	return String.valueOf(s);
    }
    
 
    private static String formatToHtml(String s) {
    	return s.replaceAll("\\n", "<br>");   	
    }


    
    
    
	

}
