package com.linty.sonar.params;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static com.linty.sonar.params.ParamTranslator.*;

public class ZamiaStringParam extends ZamiaParam {
  
  private static final String SONAR_DESCRIPTION = "Comma seperated pattern to match, use *";
  private static final String SONAR_NAME = "Format";
  public static final String PARAM_KEY = "Format";
  
  protected String value;

  //Constructor used when parsing handbook (handbook to Sonar) 
  public ZamiaStringParam() {
    super();
    this.fields = ImmutableList.copyOf(STRING_FIELDS_LIST);
  }
  
  
  public ZamiaStringParam setValue(String value) {
    this.value = value;
    return this;
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
   All the StringParam are concatenated into a single parameter 
   (Handbook <- n:1 -> NewParam)
   -The format [String] (Comma separated)
   */
  @Override
  public void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey) {    
    nr
    .createParam(PARAM_KEY)
    .setName(this.hbParamId + SONAR_NAME)// [<hb:ParamID>][NAME] Ex: P1Format
    .setDescription(SONAR_DESCRIPTION)
    .setType(RuleParamType.STRING)
    .setDefaultValue(params
      .stream()
      .filter(ZamiaStringParam.class::isInstance) //will ignore non-StringParam parameters
      .map(p -> ((ZamiaStringParam) p).hbValueToSonar())
      .collect(Collectors.joining(","))
      );    
  }


}
