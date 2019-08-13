package com.linty.sonar.params;

import static com.linty.sonar.params.ParamTranslator.*;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

public class ZamiaIntParam extends ZamiaParam {
  
  private static final String SONAR_RE_DESC = "Relation with the limit";
  private static final String SONAR_RE_NAME = "Relation";
  private static final String SONAR_LIMIT_DESC = "Value to be compared with ex: < value ";
  private static final String SONAR_LIMIT_NAME = "Limit";
  public static final String RE_KEY = "Relation";
  public static final String LI_KEY = "Limit";
  
  protected int value;

  //Constructor used when parsing handbook (handbook to Sonar) 
  public ZamiaIntParam() {
    super();
    this.fields = INT_FIELDS_MAP.keySet().stream().collect(ImmutableList.toImmutableList());
  }

  public ZamiaIntParam setValue(int value) {
    this.value = value; 
    return this;
  }
  
  /*Set the parameter for the NewRule created in VhdlRulesDefinition
   The unique selected IntParam leads to 2 parameters in Sonar
  -The relation    [Single List of Relations...]
  -The limit value [Integer]
  */
  @Override
  public void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey) {
    //Define the relation option in a list of possible options
    super.createSingleListParam(
      nr, 
      RE_KEY,
      SONAR_RE_NAME,
      SONAR_RE_DESC,
      INT_FIELDS_MAP);
  
    //Define the integer limit value to set
    super.createIntValueParam(
      nr,
      LI_KEY,
      SONAR_LIMIT_NAME + this.hbParamId, //[NAME][<hb:ParamID>] Ex: LimitP1
      SONAR_LIMIT_DESC,
      this.value);   
  }
}
