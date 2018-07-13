package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.File;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import com.linty.sonar.plugins.vhdlrc.utils.ServerFileSystemTester;


public class VhdlRulesDefinitiontest {
	
	
	@Test
	  public void test() {
		File ServerHome = new File("src");
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);
	    
	}

}
