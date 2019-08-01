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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.server.rule.RuleTagFormat;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import com.linty.sonar.plugins.vhdlrc.Vhdl;
import com.linty.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition.HbRessourceContext;
import static org.assertj.core.api.Assertions.assertThat;

public class VhdlRulesDefinitionTest {
	
	
	
	MapSettings settings = new MapSettings();
	@Rule
	public LogTester logTester = new LogTester();
	
	 public class HbRessourceContextTester extends HbRessourceContext{
	    public final String HANDBOOK_DIR;
	    public final String RULESET_PATH;
	    public HbRessourceContextTester(String handbookDir, String RuleSetPath) {
	      this.HANDBOOK_DIR = handbookDir;
	      this.RULESET_PATH = HANDBOOK_DIR + RuleSetPath;
	    }
	    @Override
	    protected InputStream getRuleset() {
	      try {
          return new FileInputStream(new File(RULESET_PATH));
        } catch (FileNotFoundException e) {
          InputStream nullInputStream = null;
          return nullInputStream;
        }
	    }
	  }
	
	@Test
	public void test_new_rule() {
		MapSettings settings = new MapSettings();
		VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig());
	  RulesDefinition.Context context = new RulesDefinition.Context();
		com.linty.sonar.plugins.vhdlrc.rules.Rule r = new com.linty.sonar.plugins.vhdlrc.rules.Rule();
		r.ruleKey="TEST_1000";
		NewRepository repository = context.createRepository("test", "vhdl").setName("test-repo");
		definition.newRule(r,repository);
	}
	
	/*
	 * This test is meant to verify the given configuration at build time :
	 * - Checks that the handbook content contains a Ruleset name handbook.xml
	 * - Checks that rc_config_selected_rules.xml is present
	 * - checks that rc_handbook_parameters.xml is present
	 */
	@Test
	public void verify_embedded_hanbdook_for_build() {
	  MapSettings settings = new MapSettings();
    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig());
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
//    RulesDefinition.Repository repository = context.repository("vhdlrc-repository");
//    assertThat(repository.rules()).hasSize(74);
	}
	
	@Test
	public void test_with_normal_handbook() {
	  MapSettings settings = new MapSettings();
	  VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig());
	  RulesDefinition.Context context = new RulesDefinition.Context();
	  HbRessourceContextTester hbContext = new HbRessourceContextTester("src/test/files/handbooks/VHDL_Handbook_STD-master","/Rulesets/handbook_STD.xml");
	  definition.defineFromRessources(context,hbContext);	    
	  RulesDefinition.Repository repository = context.repository("vhdlrc-repository");
	  
	  assertThat(repository).isNotNull();
	  assertThat(repository.name()).isEqualTo("VhdlRuleChecker");
	  assertThat(repository.language()).isEqualTo(Vhdl.KEY);
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
	public void unfound_hb_should_be_reported() {
		MapSettings settings = new MapSettings();
    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig());
    RulesDefinition.Context context = new RulesDefinition.Context();
    HbRessourceContextTester hbContext = new HbRessourceContextTester("src/test/files/handbooks/does_not_exists","/Rulesets/handbook_STD.xml");
    definition.defineFromRessources(context,hbContext);        
		assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
		assertThat(logTester.logs(LoggerLevel.ERROR)).contains("handboo not found in jar ressources, re-build with " + VhdlRulesDefinition.RULESET_PATH);
	}
	
	 @Test 
	  public void no_rules_to_load_should_be_reported() {
	    MapSettings settings = new MapSettings();
	    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig());
	    RulesDefinition.Context context = new RulesDefinition.Context();
	    HbRessourceContextTester hbContext = new HbRessourceContextTester("src/test/files/handbooks/empty_handbook","/Rulesets/handbook_Empty.xml");
	    definition.defineFromRessources(context,hbContext);        
	    assertThat(logTester.logs(LoggerLevel.WARN)).contains("No VHDL RuleCheker rules loaded!");
	    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	  }
	
	
	@Test
	public void bad_tag_should_be_handled() {
    MapSettings settings = new MapSettings();
    VhdlRulesDefinition definition = new VhdlRulesDefinition(settings.asConfig());
    RulesDefinition.Context context = new RulesDefinition.Context();
    HbRessourceContextTester hbContext = new HbRessourceContextTester("src/test/files/handbooks/handbook_parse_issues","/Rulesets/handbook_STD_issues.xml");
    definition.defineFromRessources(context,hbContext);   
	}
  
}
