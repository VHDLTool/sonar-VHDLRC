package com.linty.sonar.params;


import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

public abstract class ZamiaParam {
  
  //Handbook xml parameter elements

  protected String hbParamId;
  /*can either be enumtype of
  <hb:Position>
  <hb:Relation>
  <hb:Range>
  */

  protected String field;
  //Possible values of the enumtype
  protected ImmutableList<String> fields;
  
  //Constructor from handbook.xml definition
  public ZamiaParam() {
  }
  
  //Generates a key for the parameter for a given rule key
  //Use to retrieve parameters from key for a given rule key when loading activeRules
  public String paramKeyFor(String ruleKey, String key) {
    return ruleKey + "-" + key;
  }
  
  @SuppressWarnings("unchecked")
  public <T extends ZamiaParam> T setHbParamId(String hbParamId) {
    this.hbParamId = hbParamId;
    return (T) this;
  }
  
  //Checks if position is authorized 
  @SuppressWarnings("unchecked")
  public <T extends ZamiaParam> T setField(String field) {
    if(fields.contains(field)) {
      this.field = field;
      return (T)this;
    } else {
      throw new IllegalStateException("\"" + field + "\"" + " is not one of: " + fields.toString());
    }
  }
  
  
  //Set the parameter for the NewRule created in VhdlRulesDefinition
  public abstract void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey);
  
  public void createIntValueParam(NewRule nr, String paramKey, String name, String description, int value) {
    nr
    .createParam(paramKey)
    .setName(name)
    .setDescription(description)
    .setType(RuleParamType.INTEGER)
    .setDefaultValue(String.valueOf(value));
  }
  
  public void createSingleListParam(NewRule nr, String paramKey, String name, String description, ImmutableBiMap<String, String> fieldMap) {
    nr
    .createParam(paramKey)
    .setName(name)
    .setDescription(description)
    .setType(RuleParamType
      .singleListOfValues(fieldMap
        .values()
        .stream()
        .toArray(String[]::new)))
    .setDefaultValue(fieldMap.get(this.field));
  }
}
