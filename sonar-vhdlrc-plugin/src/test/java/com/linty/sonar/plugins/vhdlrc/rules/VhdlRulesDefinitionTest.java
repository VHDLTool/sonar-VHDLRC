
package com.linty.sonar.plugins.vhdlrc.rules;


import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.apachecommons.io.FilenameUtils;
import org.sonar.api.server.rule.RuleTagFormat;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import com.linty.sonar.plugins.vhdlrc.utils.ServerFileSystemTester;
import static org.assertj.core.api.Assertions.assertThat;

public class VhdlRulesDefinitionTest {
	
	
	
	MapSettings settings = new MapSettings();
	File ServerHome = new File("src/test/files/handbooks");
	@Rule
	public LogTester logTester = new LogTester();
	
	@Test
	public void test_new_rule() {
		MapSettings settings = new MapSettings();
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
		com.linty.sonar.plugins.vhdlrc.rules.Rule r = new com.linty.sonar.plugins.vhdlrc.rules.Rule();
		r.ruleKey="TEST_1000";
		NewRepository repository = context.createRepository("test", "vhdl").setName("test-repo");
		definition.newRule(r,repository);
	}
	
	
	@Test
	  public void test_with_custom_handbook_path() {	
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "VHDL_Handbook_STD-master");
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));     
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    RulesDefinition.Repository repository = context.repository("vhdlrc-repository");
	    
	    assertThat(repository).isNotNull();
	    assertThat(repository.name()).isEqualTo("VhdlRuleChecker");
	    assertThat(repository.language()).isEqualTo("vhdl");
	    assertThat(repository.rules()).hasSize(74);
	    assertThat(repository.rules()).isNotNull().isNotEmpty();
	    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
	    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	    
	}
	
	@Test
	  public void test_absolute_path() {
		String absolutePath = FilenameUtils.separatorsToUnix(System.getProperty("user.dir") + "/src/test/files/handbooks/VHDL_Handbook_STD-master/");
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, absolutePath );
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    RulesDefinition.Repository repository = context.repository("vhdlrc-repository");
	    
	    assertThat(repository).isNotNull();
	    assertThat(repository.name()).isEqualTo("VhdlRuleChecker");
	    assertThat(repository.language()).isEqualTo("vhdl");
	    assertThat(repository.rules()).hasSize(74);
	    assertThat(repository.rules()).isNotNull().isNotEmpty();
	    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
	    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	}
	
	@Test
	  public void test_with_default_embedded_handbook() {
		MapSettings settings = new MapSettings();
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    RulesDefinition.Repository repository = context.repository("vhdlrc-repository");
	    
	    assertThat(repository).isNotNull();
	    assertThat(repository.name()).isEqualTo("VhdlRuleChecker");
	    assertThat(repository.language()).isEqualTo("vhdl");
	    assertThat(repository.rules()).hasSize(74);
	    assertThat(repository.rules()).isNotNull().isNotEmpty();
	    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
	    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	    
	    for(RulesDefinition.Rule r : repository.rules()) {
	    	assertThat(r.key()).isNotNull().isNotEmpty();
	    	for(String t : r.tags()) {
	    		assertThat(RuleTagFormat.isValid(t)).isTrue();
	    		assertThat(t).isNotEqualToIgnoringCase("tbd");
	    	}
	    }
	}
	
	@Test
	public void wrong_handbook_path_should_log_error() {
		MapSettings settings = new MapSettings();
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "None_existing_hb");
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/None_existing_hb");
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    
		assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
		assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("Handbook directory not found : " + filename +" ; Check parameter " + VhdlRulesDefinition.HANDBOOK_PATH_KEY );
	}
	
	@Test 
	public void no_matching_hb_should_log_error() {
		MapSettings settings = new MapSettings();
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "bad_handbook");
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/bad_handbook/Rulesets");
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    
		assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
		assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("No handbook.xml found in : " + filename);
	}
	
	@Test
	public void Empty_handbook_should_log_warn() {
		MapSettings settings = new MapSettings();
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "empty_handbook");
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/empty_handbook/Rulesets/handbook_Empty.xml");
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    
		assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
		assertThat(logTester.logs(LoggerLevel.WARN)).contains("File is empty and won't be analyzed : " + filename);
		assertThat(logTester.logs(LoggerLevel.WARN)).contains("No VHDL RuleCheker rules loaded!");
	}
	
	@Test
	public void bad_tag_should_be_handled() {
		MapSettings settings = new MapSettings();
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "handbook_parse_issues");
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/handbook_parse_issues/Rulesets/handbook_STD_issues.xml");
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	   
	}
}
