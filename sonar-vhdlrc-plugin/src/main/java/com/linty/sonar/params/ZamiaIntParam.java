/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2019 Maxime Facquet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
