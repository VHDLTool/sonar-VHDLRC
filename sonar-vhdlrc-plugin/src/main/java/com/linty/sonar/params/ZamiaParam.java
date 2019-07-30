package com.linty.sonar.params;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewParam;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

public abstract class ZamiaParam {
  
  protected String paramId;
  protected RuleParamType ruleParamType;
  protected static String PARAM_DESCRIPTION;
  
  public static final Map<String, RuleParamType> paramTypeMap = ImmutableMap.<String, RuleParamType>builder()
    .put("IntParam", RuleParamType.INTEGER)
    .put("RangeParam", RuleParamType.INTEGER)
    .put("StringParam", RuleParamType.STRING)
    .build();
  
  public ZamiaParam(String paramId) {
    this.paramId = paramId;
  }
  
  public void setParamId(String paramId) {
    this.paramId = paramId;
  }
  
  public String paramId() {
    return this.paramId;
  }
  
  public NewParam setParam(NewRule nr) {
    return nr.createParam(paramId);
  }
  

}
