package com.linty.sonar.params;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static com.linty.sonar.params.ParamTranslator.*;

public class ZamiaStringParam extends ZamiaParam {
  
  private final static String SONAR_DESCRIPTION = "Comma seperated pattern to match, ex : *aa,*b*,c ";
  private final static String SONAR_NAME = "Format";
  
  private String value;
  
  static {
    FIELDS = ImmutableList.of(
      PREFIX,
      SUFFIX,
      CONTAIN,
      EQUAL);
  }
  
  //Constructor used when parsing handbook (handbook to Sonar) 
  public ZamiaStringParam(String hbParamId) {
    super(hbParamId);
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  /*Translates a handbook parameter into a pseudo regular expression 
   by adding * according to the given position in handbook
    PREFIX  <=>  a*
    SUFFIX  <=> *a
    CONTAIN <=> *a*
    EQUAL   <=>  a
    ex: DefaultValue = "*a,b*,*c*,d"
   */
  private String hbValueToSonar() {
    if(PREFIX.equals(field)) {
      return this.value + STAR;
    } else if (SUFFIX.equals(field)) {
      return STAR + this.value;
    } else if (CONTAIN.equals(field)) {
      return STAR + this.value + STAR;
    } else {
      return this.value;
    }
  }
  
  /*Set the parameter for the NewRule created in VhdlRulesDefinition
    String parameters relation :
    Handbook <- n:1 -> NewParam
   */
  @Override
  public void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey) {
    nr
    .createParam(ruleKey + "-" + this.hbParamId)
    .setName(SONAR_NAME)
    .setDescription(SONAR_DESCRIPTION)
    .setType(RuleParamType.STRING)
    .setDefaultValue(params
      .stream()
      .map(p -> ((ZamiaStringParam) p).hbValueToSonar())
      .collect(Collectors.joining(","))
      );    
  }


}
