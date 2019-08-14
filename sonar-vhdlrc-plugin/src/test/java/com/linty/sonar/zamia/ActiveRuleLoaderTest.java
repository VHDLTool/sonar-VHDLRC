package com.linty.sonar.zamia;

import com.linty.sonar.test.utils.fileTestUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.rule.RuleKey;
import org.xmlunit.builder.Input;
import org.xmlunit.matchers.CompareMatcher;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ActiveRuleLoaderTest {
  
  ActiveRulesBuilder builder;
  ActiveRules activeRules;
  
  @Before
  public void init() {
    
  }

  @Test
  public void test_normal_list() {
    Path testFile = Paths.get("src/test/parameters/rc_parameters/expected.xml");
    //A StringParam
//    builder.addRule(StringRule("","",""));
//    //A IntParam
//    builder.addRule(IntRule("")
//    //A RangeParam
//    builder.addRule(addActiveRule("").setParam("", "").build());
    Path createdXml = new ActiveRuleLoader(activeRules).makeRcHandbookParameters();
    assertThat(
      Input.fromFile(createdXml.toFile()), 
      CompareMatcher
      .isIdenticalTo(Input.fromFile(testFile.toFile()))
      .ignoreComments()
      .ignoreWhitespace()
      );
  }
  
  @Test
  public void test_repo_not_found() {
    fail("Not yet implemented");
  }
  
  public NewActiveRule StringRule(String ruleKey, String paramKey, String value) {
    return new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of("vhdlrc-repository",ruleKey))
      .setLanguage("vhdl")
      .setParam(paramKey, value)
      .build();   
  }
  
  public NewActiveRule IntRule(String ruleKey, String pk1, String v1, String pk2,  String v2) {
    return new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of("vhdlrc-repository",ruleKey))
      .setLanguage("vhdl")
      .setParam(pk1, v1)
      .setParam(pk2, v2)
      .build();   
  }
  
  public NewActiveRule RangeRule(String ruleKey, String pk1, String v1, String pk2, String v2, String pk3, String v3) {
    return new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of("vhdlrc-repository",ruleKey))
      .setLanguage("vhdl")
      .setParam(pk1, v1)
      .setParam(pk2, v2)
      .setParam(pk3, v3)
      .build();   
  }
  
}
