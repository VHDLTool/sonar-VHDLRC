package com.linty.sonar.params;

import static com.linty.sonar.params.ParamTranslator.*;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

public class ZamiaIntParam extends ZamiaParam {
  
  private final static String SONAR_RE_DESC = "Relation with the limit";
  private final static String SONAR_RE_NAME = "Relation";
  private final static String SONAR_LIMIT_DESC = "Value to be compared with ex: >= value ";
  private final static String SONAR_LIMIT_NAME = "Limit";
  public final static String RE_KEY = "IP1";
  public final static String LI_KEY = "IP2";
  
  

  private int value;
  
  //Possible values of <hb:Relation> in handbook

    


  //Constructor used when parsing handbook (handbook to Sonar) 
  public ZamiaIntParam(String hbParamId) {
    super(hbParamId);
    this.fields = INT_FIELDS_MAP.keySet().stream().collect(ImmutableList.toImmutableList());
  }

  public void setValue(int value) {
    this.value = value;    
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
      super.paramKeyFor(ruleKey, RE_KEY),
      SONAR_RE_NAME,
      SONAR_RE_DESC,
      INT_FIELDS_MAP);
  
    //Define the integer limit value to set
    super.createIntValueParam(
      nr,
      super.paramKeyFor(ruleKey, LI_KEY),
      SONAR_LIMIT_NAME + this.hbParamId, //[NAME][<hb:ParamID>] Ex: LimitP1
      SONAR_LIMIT_DESC,
      this.value);   
  }
}
