/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2020 Linty Services
 * mailto:contact@linty-services.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.lintyservices.sonar.params;

import com.google.common.collect.ImmutableList;

import java.util.List;

import org.sonar.api.server.rule.RulesDefinition.NewRule;

import static com.lintyservices.sonar.params.ParamTranslator.*;

public class ZamiaRangeParam extends ZamiaParam {
  private static final String SONAR_MIN_DESC = "Minimum value to respect";
  private static final String SONAR_MIN_NAME = "Min";
  private static final String SONAR_MAX_DESC = "Maximum value to respect";
  private static final String SONAR_MAX_NAME = "Max";
  private static final String SONAR_RANGE_DESC = "Inclusive and exclusive options";
  private static final String SONAR_RANGE_NAME = "Range";

  public static final String MIN_KEY = "Min  "; //use 2 spaces to have the good hash order
  public static final String RANGE_KEY = "Range";
  public static final String MAX_KEY = "Max  "; //use 2 spaces to have the good hash order

  protected int min;
  protected int max;

  //Constructor used when parsing handbook (handbook to Sonar) 
  public ZamiaRangeParam() {
    super();
    this.fields = RANGE_FIELDS_MAP.keySet().stream().collect(ImmutableList.toImmutableList());
  }

  public ZamiaRangeParam setMin(int min) {
    this.min = min;
    return this;
  }

  public ZamiaRangeParam setMax(int max) {
    this.max = max;
    return this;
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
      MIN_KEY,
      SONAR_MIN_NAME + this.hbParamId, //[NAME][<hb:ParamID>] Ex: MinP1
      SONAR_MIN_DESC,
      this.min);

    //Relation list of option
    super.createSingleListParam(
      nr,
      RANGE_KEY,
      SONAR_RANGE_NAME,
      SONAR_RANGE_DESC,
      RANGE_FIELDS_MAP);

    //Max value parameter 
    super.createIntValueParam(
      nr,
      MAX_KEY,
      SONAR_MAX_NAME + this.hbParamId, //[NAME][<hb:ParamID>] Ex: MinP1
      SONAR_MAX_DESC,
      this.max);
  }

}
