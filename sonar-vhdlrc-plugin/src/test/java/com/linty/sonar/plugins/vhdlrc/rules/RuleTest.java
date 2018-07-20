package com.linty.sonar.plugins.vhdlrc.rules;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RuleTest {
	
	@Test
	public void test_rule_values() {
		Rule r = new Rule();
		r.name=             "A rule name";       
        r.remediationEffort="Easy";
        r.sonarSeverity=    "Major";
        r.type=             "Code_Smell";
        r.category=         "Traceability";
        r.subCategoty=      "Naming";    
        r.rationale=        "Labels improve readability of simulations, VHDL source code and synthesis logs.";
        r.shortDescription= "Processes are identified by a label.";
        r.longDescription=  "A long description";         
        r.goodExDesc=       "Extracted from STD_00400_good.vhd";
        r.goodExampleRef=   "STD_00400_good";
        r.goodExampleCode=  "if() \n then \n else";
        r.badExDesc=        "Extracted from STD_00400_bad.vhd";
        r.badExampleRef=    "STD_00400_bad";       
        
        assertThat(r.figureDesc).isNotNull();
        
        String htmlDesc = r.buildHtmlDescritpion();
	}
	
	@Test
	public void no_additionnal_info_should_not_be_a_desc() {
		Rule r = new Rule();
		r.name=             "A other rule name";       
        r.remediationEffort="Easy";
        r.sonarSeverity=    "Major";
        r.type=             "BUG";
        r.category=         "Traceability";
        r.subCategoty=      "Naming";    
        r.rationale=        "Labels improve readability of simulations, VHDL source code and synthesis logs.";
        r.shortDescription= "Processes are identified by a label.";
        r.longDescription=  "No additional information.";         
        r.goodExDesc=       "Extracted from STD_00400_good.vhd";
        r.goodExampleRef=   "STD_00400_good";
        r.goodExampleCode=  "";
        r.badExDesc=        "Extracted from STD_00400_bad.vhd";
        r.badExampleRef=    "STD_00400_bad"; 
        r.figure = new FigureSvg();
        
        assertThat(r.figureDesc).isNotNull();
        assertThat(r.figure.hasImage()).isFalse();
        
        String htmlDesc = r.buildHtmlDescritpion();
	}
	@Test
	public void with_bad_Example_code_and_figure() {
		Rule r = new Rule();
		r.name=             "A other rule name";       
        r.longDescription=  "No additional information.";         
        r.goodExDesc=       "Extracted from STD_00400_good.vhd";
        r.goodExampleRef=   "STD_00400_good";
        r.goodExampleCode=  "";
        r.badExDesc=        "Extracted from STD_00400_bad.vhd";
        r.badExampleRef=    "STD_00400_bad";  
        r.badExampleCode=   "code code code";
        r.figure = new FigureSvg();
        r.figure.figureCode="< image>";
        
        assertThat(r.figureDesc).isNotNull();
        assertThat(r.figure.height).isEqualTo("300px");
        assertThat(r.figure.figureCode).isNotNull();
        String htmlDesc = r.buildHtmlDescritpion();
	}
	
	
	

}
