package com.linty.sonar.params;

import org.junit.Test;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.rule.RuleKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


public class ParamTranslatorTest {

  @Test
  public void test_translating_param_to_zamia() {
    assertThat(ParamTranslator.positionOf("*aa")).isEqualTo("Suffix");
    assertThat(ParamTranslator.positionOf("aa")).isEqualTo("Equal");
    assertThat(ParamTranslator.positionOf("*aa*")).isEqualTo("Contain");
    assertThat(ParamTranslator.positionOf("aa*")).isEqualTo("Prefix");
    
    assertThat(ParamTranslator.stringValueOf("*aa")).isEqualTo("aa");
    assertThat(ParamTranslator.stringValueOf("*aa*")).isEqualTo("aa");
    assertThat(ParamTranslator.stringValueOf("aa*")).isEqualTo("aa");
    assertThat(ParamTranslator.stringValueOf("aa")).isEqualTo("aa");
    
    assertThat(ParamTranslator.relationOf("<")).isEqualTo("LT");
    assertThat(ParamTranslator.rangeOf("<_<=")).isEqualTo("LT_GET");
    
    ActiveRules rules = new ActiveRulesBuilder()
      .addRule( 
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","1"))
        .setLanguage("vhdl")
        .setParam(ZamiaStringParam.PARAM_KEY, "*abc,*e*")
        .build()
        )
      .addRule(
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","2"))
        .setLanguage("vhdl")
        .setParam(ZamiaIntParam.RE_KEY, "<")
        .setParam(ZamiaIntParam.LI_KEY, "8")
        .build()
        )
      .addRule(
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","2.1"))
        .setLanguage("vhdl")
        .setParam(ZamiaIntParam.RE_KEY, "<")
        .setParam("not_a_key", "8")
        .build()
        )
      .addRule(
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","2.2"))
        .setLanguage("vhdl")
        .setParam("no", "<")
        .setParam(ZamiaIntParam.LI_KEY, "8")
        .build()
        )
      .addRule(
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","3"))
        .setLanguage("vhdl")
        .setParam(ZamiaRangeParam.MIN_KEY, "7")
        .setParam(ZamiaRangeParam.RANGE_KEY, "<_<")
        .setParam(ZamiaRangeParam.MAX_KEY, "9")
        .build()
        )
      .addRule(
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","3.1"))
        .setLanguage("vhdl")
        .setParam(ZamiaRangeParam.MIN_KEY, "7")
        .setParam("not_a_key", "<_<")
        .setParam(ZamiaRangeParam.MAX_KEY, "9")
        .build()
        )
      .addRule(
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","3.2"))
        .setLanguage("vhdl")
        .setParam(ZamiaRangeParam.MIN_KEY, "7")
        .setParam(ZamiaRangeParam.RANGE_KEY, "<_<")
        .setParam("not_a_key", "9")
        .build()
        )
      .addRule(
        new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of("repo","4"))
        .setLanguage("vhdl")
        .setParam("not_a_ZamiaK_key","6")
        .build()
        )
      .build();
    
    assertThat(ParamTranslator.hasStringParam(rules.find(RuleKey.of("repo","1")))).isTrue();
    assertThat(ParamTranslator.hasStringParam(rules.find(RuleKey.of("repo","2")))).isFalse();
    
    assertThat(ParamTranslator.hasIntParam(rules.find(RuleKey.of("repo","2")))).isTrue();
    assertThat(ParamTranslator.hasIntParam(rules.find(RuleKey.of("repo","2.1")))).isFalse();
    assertThat(ParamTranslator.hasIntParam(rules.find(RuleKey.of("repo","3")))).isFalse();
    assertThat(ParamTranslator.hasIntParam(rules.find(RuleKey.of("repo","2.2")))).isFalse();
    
    assertThat(ParamTranslator.hasRangeParam(rules.find(RuleKey.of("repo","3")))).isTrue();
    assertThat(ParamTranslator.hasRangeParam(rules.find(RuleKey.of("repo","3.1")))).isFalse();
    assertThat(ParamTranslator.hasRangeParam(rules.find(RuleKey.of("repo","3.2")))).isFalse();
    assertThat(ParamTranslator.hasRangeParam(rules.find(RuleKey.of("repo","1")))).isFalse();
    
    assertThat(ParamTranslator.hasZamiaParam(rules.find(RuleKey.of("repo","1")))).isTrue();
    assertThat(ParamTranslator.hasZamiaParam(rules.find(RuleKey.of("repo","2")))).isTrue();
    assertThat(ParamTranslator.hasZamiaParam(rules.find(RuleKey.of("repo","3")))).isTrue();
    assertThat(ParamTranslator.hasZamiaParam(rules.find(RuleKey.of("repo","4")))).isFalse();
  }
  
  @Test
  public void test_instanciatingParamTranlator() {
    try {
      new ParamTranslator();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("Utility class");
    }
  }

}
