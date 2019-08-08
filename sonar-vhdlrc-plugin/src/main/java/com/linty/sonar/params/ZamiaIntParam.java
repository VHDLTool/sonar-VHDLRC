package com.linty.sonar.params;

import static com.linty.sonar.params.ParamTranslator.*;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

public class ZamiaIntParam extends ZamiaParam {
  
  private final static String SONAR_DESC_1 = "Relation with the limit";
  private final static String SONAR_NAME_1 = "Relation";
  private final static String SONAR_DESC_2 = "Value to be compared with ex: >= value ";
  private final static String SONAR_NAME_2 = "Limit";

  private int value;
  
  //Possible values of <hb:Relation> in handbook
  static {
    FIELDS = new ArrayList<>(INT_FIELDS_MAP.keySet());
  }

  //Constructor used when parsing handbook (handbook to Sonar) 
  public ZamiaIntParam(String hbParamId) {
    super(hbParamId);
  }

  public void setValue(int value) {
    this.value = value;    
  }
  
  @Override
  public void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey) {
    nr
    .createParam(ruleKey + "-" + this.hbParamId + "-RE")
    .setName(SONAR_NAME_1)
    .setDescription(SONAR_DESC_1)
    .setType(RuleParamType
      .singleListOfValues(INT_FIELDS_MAP
        .values()
        .toArray(new String[0])))
    .setDefaultValue(INT_FIELDS_MAP.get(this.field));
    
    nr
    .createParam(ruleKey + "-" + this.hbParamId + "-LI")
    .setName(SONAR_NAME_2)
    .setDescription(SONAR_DESC_2)
    .setType(RuleParamType.INTEGER)
    .setDefaultValue(String.valueOf(this.value));
   
  }

 


}
