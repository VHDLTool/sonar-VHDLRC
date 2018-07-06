package com.linty.sonar.plugins.vhdlrc.rules;


public class Rule {
	
	String name;
	
    String subCategoty;    
    String rationale;
    String shortDescription;
    String longDescription;   
   
    String category;
    String remediationEffort;
    String sonarSeverity;
    String tag;
    String type;
    
    String goodExDesc;
    String goodExample;
    
    String badExDesc;
    String badExample;
    
    String figureDesc;
    FigureSvg figure;
    
    String param;
    
    
    	public String buildHtmlDescritpion() {

        	StringBuffer htmlCode = new StringBuffer();

        	htmlCode
        	.append(theCategorySection())
        	.append(theDesciptionSection())
        	.append(theRationaleSection())
        	.append(theExamplesSection())
        	.append(theFigureSection());

        	return String.valueOf(htmlCode);    
        }
          
        
        private String theCategorySection() {
        	StringBuffer text = new StringBuffer();    	
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
        	StringBuilder rationale = new StringBuilder();
        	rationale
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
        	if(this.goodExample!=null && !this.goodExample.isEmpty()) {  		
        		example
        		.append("<div>")
        		.append("<h1><b>Compliant code example</b></h1>")
        		.append("<pre>"+this.goodExample+"</pre>")
        		.append("<p><font size=1 color=\"grey\">")
        		.append(this.goodExDesc)
        		.append("</font></p>")
        		.append("</div>\n");
        	}
        	if(this.badExample!=null && !this.badExample.isEmpty()) {
        		example
        		.append("<div>")
        		.append("<h1><b>Non compliant code example</b></h2>")
        		.append("<pre>"+this.badExample+"</pre>")
        		.append("<p><font size=1 color=\"grey\">")
        		.append(this.badExDesc)
        		.append("</font></p>")
        		.append("</div>\n");
        	}
        	return String.valueOf(example);
        }
        
        private String theFigureSection() {
        	StringBuilder s = new StringBuilder();	 
        	if(this.figure.figureCode!=null && !this.figure.figureCode.isEmpty()) {
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
        
        private static boolean IsNotNullOrEmpty(String s) {
        	return (s!=null && !s.isEmpty());
        }
        
    	
	

}
