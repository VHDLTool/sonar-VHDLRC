package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.File;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.server.rule.RulesDefinition;
import com.linty.sonar.plugins.vhdlrc.utils.ServerFileSystemTester;
import static org.assertj.core.api.Assertions.assertThat;

public class VhdlRulesDefinitiontest {
	
	MapSettings settings = new MapSettings();
	
	
	@Test
	  public void test() {
		File ServerHome = new File("src");
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "test/files/handbooks");
		
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);
	    
	    RulesDefinition.Repository repository = context.repository("vhdl-repository");
	    
	    assertThat(repository).isNotNull();
	    assertThat(repository.name()).isEqualTo("VhdlRulecker");
	    assertThat(repository.language()).isEqualTo("vhdl");
	    assertThat(repository.rules()).hasSize(74);

	    assertThat(repository.rules()).isNotNull().isNotEmpty();
	    
	    
	}

}
