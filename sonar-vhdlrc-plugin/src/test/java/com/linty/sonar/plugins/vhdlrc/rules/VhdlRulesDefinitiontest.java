package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.apachecommons.io.FilenameUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import com.linty.sonar.plugins.vhdlrc.utils.ServerFileSystemTester;
import static org.assertj.core.api.Assertions.assertThat;

public class VhdlRulesDefinitiontest {
	
	
	
	MapSettings settings = new MapSettings();
	File ServerHome = new File("src/test/files/handbooks/");
	@Rule
	public LogTester logTester = new LogTester();
	
	
	@Test
	  public void test_with_custom_handbook_path() {	
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "VHDL_Handbook_STD-master");
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));     
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    RulesDefinition.Repository repository = context.repository("vhdl-repository");
	    
	    assertThat(repository).isNotNull();
	    assertThat(repository.name()).isEqualTo("VhdlRulecker");
	    assertThat(repository.language()).isEqualTo("vhdl");
	    assertThat(repository.rules()).hasSize(74);
	    assertThat(repository.rules()).isNotNull().isNotEmpty();
	    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
	    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	}
	
	@Test
	  public void test_absolute_path() {
		String absolutePath = FilenameUtils.separatorsToUnix(System.getProperty("user.dir") + "/src/test/files/handbooks/VHDL_Handbook_STD-master");
		System.out.println(absolutePath);
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, absolutePath );
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    RulesDefinition.Repository repository = context.repository("vhdl-repository");
	    
	    assertThat(repository).isNotNull();
	    assertThat(repository.name()).isEqualTo("VhdlRulecker");
	    assertThat(repository.language()).isEqualTo("vhdl");
	    assertThat(repository.rules()).hasSize(74);
	    assertThat(repository.rules()).isNotNull().isNotEmpty();
	    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
	    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	}
	
	@Test
	  public void test_with_default_handbook_path() {
		MapSettings settings = new MapSettings();
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    RulesDefinition.Repository repository = context.repository("vhdl-repository");
	    
	    assertThat(repository).isNotNull();
	    assertThat(repository.name()).isEqualTo("VhdlRulecker");
	    assertThat(repository.language()).isEqualTo("vhdl");
	    assertThat(repository.rules()).hasSize(74);
	    assertThat(repository.rules()).isNotNull().isNotEmpty();
	    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
	    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	}
	
	@Test (expected = IllegalStateException.class)
	public void wrong_handbook_path_should_log_error() {
		MapSettings settings = new MapSettings();
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "None_existing_hb");
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/None_existing_hb");
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    
		assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
		assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("Wrong path to handbook : " + filename +" ; Check parameter " + VhdlRulesDefinition.HANDBOOK_PATH_KEY );
	}
	
	@Test (expected = IllegalStateException.class)
	public void no_matching_hb_should_log_error() {
		MapSettings settings = new MapSettings();
		settings.setProperty(VhdlRulesDefinition.HANDBOOK_PATH_KEY, "bad_handbook");
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/bad_handbook/Rulesets");
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig(),new ServerFileSystemTester(ServerHome));
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    definition.define(context);	    
	    
		assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
		assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("No handbook found in : " + filename);
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
		assertThat(logTester.logs(LoggerLevel.WARN)).contains("File " + filename + " is empty and won't be analyzed.");
		assertThat(logTester.logs(LoggerLevel.WARN)).contains("No rules loaded!");
	}
}
