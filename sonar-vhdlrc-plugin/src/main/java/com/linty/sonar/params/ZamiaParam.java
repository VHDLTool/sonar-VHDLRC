package com.linty.sonar.params;


import java.util.List;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

public abstract class ZamiaParam {
  
  //Handbook Xml parameter elements
  protected String hbParamId;
  /*Possible values of
  <hb:Position>
  <hb:Relation>
  <hb:Range>
  */
  protected String field;
  protected static List<String> FIELDS;
  
  //Constructor from handbook.xml definition
  public ZamiaParam(String hbParamId) {
    this.hbParamId = hbParamId;
  }
  
  //Checks if position is authorized 
  public void setField(String field) {
    if(FIELDS.contains(field)) {
      this.field = field;
    } else {
      throw new IllegalStateException("\"" + field + "\"" + " is not one of: " + FIELDS.toString());
    }
  }
  
  /*Set the parameter for the NewRule created in VhdlRulesDefinition
   
   */
  public abstract void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey);
}
