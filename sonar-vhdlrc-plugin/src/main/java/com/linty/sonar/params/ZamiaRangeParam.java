package com.linty.sonar.params;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static com.linty.sonar.params.ParamTranslator.*;

public class ZamiaRangeParam extends ZamiaParam {
  private final static String SONAR_MIN_DESC = "Minimum value to respect";
  private final static String SONAR_MIN_NAME = "Min";
  private final static String SONAR_MAX_DESC = "Maximum value to respect";
  private final static String SONAR_MAX_NAME = "Max";
  private final static String SONAR_RANGE_DESC = "Inclusive and exclusive options";
  private final static String SONAR_RANGE_NAME = "Range";
  
  public final static String MIN_KEY = "1RG";
  public final static String RANGE_KEY = "2RG";
  public final static String MAX_KEY = "3RG";
  
  private int min;
  private int max;
  
    


  //Constructor used when parsing handbook (handbook to Sonar) 
  public ZamiaRangeParam(String hbParamId) {
    super(hbParamId);
    this.fields = RANGE_FIELDS_MAP.keySet().stream().collect(ImmutableList.toImmutableList());
  }
  
  public void setMin(int min) {
    this.min = min;    
  }
  
  public void setMax(int max) {
    this.max = max;    
  }
  
  /*Set the parameter for the NewRule created in VhdlRulesDefinition
   The unique selected RangeParam leads to 3 parameters in Sonar
   (RangeParam <- 1:3 -> NewParam)
  -The minimum value [Integer]
  -The relation      [Single List of Relations...]
  -The maximum value [Integer]
  */
  @Override
  public void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey) {
    //Min value parameter
    super.createIntValueParam(
      nr, 
      super.paramKeyFor(ruleKey, MIN_KEY),
      SONAR_MIN_NAME + this.hbParamId, //[NAME][<hb:ParamID>] Ex: MinP1
      SONAR_MIN_DESC,
      this.min);
    
    //Relation list of option
    super.createSingleListParam(
      nr, 
      super.paramKeyFor(ruleKey, RANGE_KEY),
      SONAR_RANGE_NAME,
      SONAR_RANGE_DESC,
      RANGE_FIELDS_MAP);     
    
    //Max value parameter 
    super.createIntValueParam(
      nr, 
      super.paramKeyFor(ruleKey, MAX_KEY),
      SONAR_MAX_NAME + this.hbParamId, //[NAME][<hb:ParamID>] Ex: MinP1
      SONAR_MAX_DESC,
      this.max);
    
  }

}
