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
import com.linty.sonar.params.ZamiaStringParam;
import com.linty.sonar.plugins.vhdlrc.rules.HandbookXmlParser;

import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class HandbookXmlParserTest {

  private HandbookXmlParser XmlParser = new HandbookXmlParser();
  List<com.linty.sonar.plugins.vhdlrc.rules.Rule> rl1;
  List<com.linty.sonar.plugins.vhdlrc.rules.Rule> rl;

  @Rule
  public LogTester logTester = new LogTester();

  @Before
  public void setup() throws FileNotFoundException {
    InputStream hb = new FileInputStream( new File("src/test/files/handbooks/VHDL_Handbook_STD-master/Rulesets/handbook_STD.xml"));		
    rl1 = XmlParser.parseXML(hb);	
  }
  
  @After
  public void clean() {
    logTester.clear();
  }


  @Test
  public void Test() {		
    assertThat(rl1).isNotNull();
    assertThat(rl1).hasSize(74);
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
  }

  @Test
  public void test_content_nullity() {
    assertThat(rl1.get(0).ruleKey).isEqualTo("STD_00600");
    assertThat(rl1.get(1).ruleKey).isEqualTo("STD_00800");
    assertThat(rl1.get(1).category).isEqualTo("Formatting");
    assertThat(rl1.get(1).badExampleRef).isEmpty();
    assertThat(rl1.get(rl1.size()-1).ruleKey).isEqualTo("STD_07000");
    for(com.linty.sonar.plugins.vhdlrc.rules.Rule r : rl1) {
      assertThat(r.ruleKey).matches("[A-Z]{3}_[0-9]{5}");
      assertThat(r.category).isNotEmpty();
      assertThat(r.subCategoty).isNotNull();
      assertThat(r.rationale).isNotNull();
      assertThat(r.parentUid).isNotNull();
      assertThat(r.technology).isNotEmpty();
      assertThat(r.applicationFields).isNotEmpty();
      assertThat(r.shortDescription).isNotNull();
      assertThat(r.longDescription).isNotNull();
      assertThat(r.type).isNotEmpty();
      assertThat(r.sonarSeverity).isNotEmpty();
      assertThat(r.remediationEffort).isNotEmpty();
      assertThat(r.tag).isNotEmpty();
      assertThat(r.goodExDesc).isNotNull();
      assertThat(r.goodExampleRef).isNotNull();
      assertThat(r.badExDesc).isNotNull();
      assertThat(r.badExampleRef).isNotNull();
      assertThat(r.figureDesc).isNotNull();
      if(r.figure!=null) {
        assertThat(r.figure.figureRef).isNotNull().isNotEmpty();
        assertThat(r.figure.width).isNotNull().isNotEmpty();
        assertThat(r.figure.height).isNotNull().isNotEmpty();				
      }
    }
  }
  

  @Test
  public void test_parameter_capture() throws FileNotFoundException {
    rl = XmlParser.parseXML(new FileInputStream( new File("src/test/parameters/hbs/handbook_5_rules.xml")));
    assertThat(rl).hasSize(5);
    com.linty.sonar.plugins.vhdlrc.rules.Rule r1 = rl.get(0);
    com.linty.sonar.plugins.vhdlrc.rules.Rule r2 = rl.get(1);
    com.linty.sonar.plugins.vhdlrc.rules.Rule r3 = rl.get(2);
    com.linty.sonar.plugins.vhdlrc.rules.Rule r4 = rl.get(3);
    com.linty.sonar.plugins.vhdlrc.rules.Rule r5 = rl.get(4);
    //check params
    assertThat(r1.ruleKey).isEqualTo("CNE_01200");
    assertThat(r1.parameters()).hasSize(1);
    assertThat(r1.parameters().get(0)).isInstanceOf(ZamiaStringParam.class);
    
    assertThat(r2.ruleKey).isEqualTo("STD_00200");
    assertThat(r2.parameters()).hasSize(2);
    
    assertThat(r3.ruleKey).isEqualTo("STD_00300");
    assertThat(r3.parameters()).hasSize(3);
    
    assertThat(r4.ruleKey).isEqualTo("STD_04600");
    assertThat(r4.parameters()).hasSize(1);
    
    assertThat(r5.ruleKey).isEqualTo("STD_04700");
    assertThat(r5.parameters()).hasSize(1);
    
  }

  //Existing but empty file should return a null List<Rule> and raise a warning
  @Test
  public void empty_xml_file_should_log_warning() throws FileNotFoundException {
    InputStream hb = new FileInputStream( new File("src/test/files/handbooks/empty_file.xml"));
    rl1 = XmlParser.parseXML(hb);
    assertThat(rl1).isNull();
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN)).contains("Handbook.xml is empty, no rules will be loaded");
  }
  
  @Test
  public void parse_error_should_log_location() throws FileNotFoundException {
    try {
      rl1 = XmlParser.parseXML(new FileInputStream(new File("src/test/files/handbooks/parsing_issue.xml")));
      fail();
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("Error when parsing rules in " + VhdlRulesDefinition.RULESET_PATH + " line 29");
    }

  }


  @Test 
  public void no_rule_key_should_be_is_illegale() throws FileNotFoundException {
    try {
    rl1 = XmlParser.parseXML(new FileInputStream(new File("src/test/files/handbooks/no_rule_key.xml")));
    fail();
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("Error when parsing rules in " + VhdlRulesDefinition.RULESET_PATH + " line 142");
      assertThat(e.getCause().getMessage()).contains("No mandatory RuleUID is defined");
    }
      
  }

  @Test (expected = IllegalStateException.class)
  public void test_broken_input_stream() {
    rl1 = XmlParser.parseXML(new BrokenInputStream());
  }


}
