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
        r.longDescription=  "No additional information.";         
        r.goodExDesc=       "Extracted from STD_00400_good.vhd";
        r.goodExampleRef=   "STD_00400_good";
        r.goodExampleCode=  "if() \n then \n else";
        r.badExDesc=        "Extracted from STD_00400_bad.vhd";
        r.badExampleRef=    "STD_00400_bad";       
        
        assertThat(r.figureDesc).isNotNull();
        
        String htmlDesc = r.buildHtmlDescritpion();
        System.out.println(htmlDesc);
	}
	
	
	

}
