/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2020 Linty Services
 * mailto:contact@linty-services.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.lintyservices.sonar.zamia;

import com.lintyservices.sonar.params.ZamiaIntParam;
import com.lintyservices.sonar.params.ZamiaRangeParam;
import com.lintyservices.sonar.params.ZamiaStringParam;
import com.lintyservices.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;
import com.lintyservices.sonar.test.utils.fileTestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTester;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ActiveRuleLoaderTest {

  private static final String FORMAT = ZamiaStringParam.PARAM_KEY;
  private static final String LIMIT = ZamiaIntParam.LI_KEY;
  private static final String RELATION = ZamiaIntParam.RE_KEY;
  private static final String MIN__ = ZamiaRangeParam.MIN_KEY;
  private static final String RANGE = ZamiaRangeParam.RANGE_KEY;
  private static final String MAX__ = ZamiaRangeParam.MAX_KEY;

  private static final String REPO_KEY = VhdlRulesDefinition.VHDLRC_REPOSITORY_KEY;

  ActiveRulesBuilder builder;
  ActiveRules activeRules;
  ActiveRuleLoader activeRuleLoader;

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  @Before
  public void init() {

    builder = new ActiveRulesBuilder();
    //[RuleKey] [Line in source.xml] [Parameters in source.xml] -> [Parameters in expected.xml] [Param Type]

    //STD_00001 l.13 1 -> 2 STRING
    builder.addRule(stringRule("STD_00001", FORMAT, "*_TOTO,*TITI*"));

    //CNE_00002 l.25 2 -> 1 STRING
    builder.addRule(stringRule("CNE_00002", FORMAT, "AAA*"));

    //CNE_00003 l.42 0 -> 0 (UNTOUCHED)
    builder.addRule(aRule("CNE_00003").build());

    //STD_00004 l.48 2 -> INT
    builder.addRule(intRule("STD_00004", RELATION, "<", LIMIT, "99"));

    //STD_00005 l.65 0 -> RANGE
    builder.addRule(rangeRule("STD_00005", MIN__, "-5", RANGE, "<=_<", MAX__, "150"));

    //STD_00006 l.70 1 -> 1 (UNTOUCHED because no parameter in Sonar)
    builder.addRule(aRule("STD_00006").build());

    //CNE_00007 l.82 0 -> 0 (UNTOUCHED because not in Sonar ActiveRules)

    //STD_00008 l.87 1 -> 1 (UNTOUCHED because parameter in sonar is not a Zamia Type)
    builder.addRule(stringRule("STD_00008", "not_a_zamia_key", "AAA*"));

    activeRules = builder.build();
  }

  /*
 > For each rule in  rc_handbook_parameters.xml :
 | <Rule is activated in Sonar ActiveRules ?>
 |next() <- No -|
 |             yes -> [add rule to list of active implemented rules]
 |              |
 | <Rule has a parameter in Sonar ?>
 |next() <- No -|
 |             yes -> [Overwrite parameter in rc_handbook_parameters.xml]
 |______________|

 A Rule in rc_handbook_parameters.xml sonar activeRule has no parameter =>
 Existing parameters in  are left untouched

   */
  @Test
  public void test_writing_normal_list() {

    //Expected result to match
    Path expected = Paths.get("src/test/parameters/rc_parameters/expected.xml");

    //Generate the new rc_handbook_parameter.xml
    Path result = tryWritingXmlFrom(activeRules, "src/test/parameters/rc_parameters/source.xml");

    //Compare Xml Files
    fileTestUtils.compareXml(result, expected);

    //Check active rules in zamia
    List<String> selectedRules = activeRuleLoader.activeRuleKeys();
    assertThat(selectedRules).hasSize(7);
    assertThat(selectedRules.get(0)).isEqualTo("STD_00001");
    assertThat(selectedRules.get(1)).isEqualTo("CNE_00002");
    assertThat(selectedRules.get(2)).isEqualTo("CNE_00003");
    assertThat(selectedRules.get(3)).isEqualTo("STD_00004");
    assertThat(selectedRules.get(4)).isEqualTo("STD_00005");
    assertThat(selectedRules.get(5)).isEqualTo("STD_00006");
    assertThat(selectedRules.get(6)).isEqualTo("STD_00008");
  }

  /*activeRuleKeys() should be filled with rule that:
 [1] Are present in rc_handbook_parameters.xml
       AND
 [2] Are activated in Sonar <=> are present in activeRules
   */
  @Test
  public void test_getting_active_rule_keys_before_parsing_should_raise_exeption() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, TransformerException {
    activeRules = new ActiveRulesBuilder().build();
    ActiveRuleLoader arl = new ActiveRuleLoader(activeRules, "");
    //Try getting activeRuleList before loading them
    try {
      arl.activeRuleKeys();
      fail("Expected IllegaleStateException");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isNotNull();
    }
    String source = "src/test/parameters/rc_parameters/no_rules.xml";
    arl.writeParametersInXml(new FileInputStream(new File(source)));
    List<String> l = arl.activeRuleKeys();
    assertThat(l).isNotNull();
    assertThat(l).hasSize(0);
  }

  @Test
  public void test_repo_not_found() {
    ActiveRuleLoader arl = new ActiveRuleLoader(activeRules, "src/test/do_not_exist.no");
    try {
      arl.makeRcHandbookParameters();
      fail("Expected IllegaleStateException");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isNotNull();
    }
  }

  /*
   * This test verifies the presence and readability of rc_handbook_parameters.xml
   * If it fails or throw and error you must verify this configuration file
   */
  @Test
  public void verify_that_rc_handbook_parameters_is_present() {
    ActiveRuleLoader arl = new ActiveRuleLoader(activeRules, "/" + ZamiaRunner.RC_HANDBOOK_PARAMETERS_PATH);
    try {
      Path result = arl.makeRcHandbookParameters();
      assertThat(result).isNotNull();
    } catch (IllegalStateException e) {
      throw new IllegalStateException("Fail to find or read configuration file : " + ZamiaRunner.RC_HANDBOOK_PARAMETERS_PATH, e);
    }
  }

  public NewActiveRule.Builder aRule(String ruleKey) {
    return new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of(REPO_KEY, ruleKey))
      .setLanguage("vhdl");
  }

  public NewActiveRule stringRule(String ruleKey, String paramKey, String value) {
    return aRule(ruleKey)
      .setParam(paramKey, value)
      .build();
  }

  public NewActiveRule intRule(String ruleKey, String pk1, String v1, String pk2, String v2) {
    return aRule(ruleKey)
      .setParam(pk1, v1)
      .setParam(pk2, v2)
      .build();
  }

  public NewActiveRule rangeRule(String ruleKey, String pk1, String v1, String pk2, String v2, String pk3, String v3) {
    return aRule(ruleKey)
      .setParam(pk1, v1)
      .setParam(pk2, v2)
      .setParam(pk3, v3)
      .build();
  }

  private Path tryWritingXmlFrom(ActiveRules activeRules, String source) {
    try (InputStream sourceIs = new FileInputStream(new File(source))) {
      activeRuleLoader = new ActiveRuleLoader(activeRules, "");
      return activeRuleLoader.writeParametersInXml(sourceIs);
    } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
      throw new IllegalStateException("source file not found in test", e);
    }
  }
}
