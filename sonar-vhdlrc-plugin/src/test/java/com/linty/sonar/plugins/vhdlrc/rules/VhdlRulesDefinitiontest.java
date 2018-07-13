package com.linty.sonar.plugins.vhdlrc.rules;

import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinition;


public class VhdlRulesDefinitiontest {
	
	private ServerFileSystem serverFileSystem;
	
	@Test
	  public void test() {
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(serverFileSystem);
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);
	}

}
