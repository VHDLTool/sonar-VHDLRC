/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2021 Linty Services
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

  @SuppressWarnings("unchecked")
  public <T extends ZamiaParam> T setHbParamId(String hbParamId) {
    this.hbParamId = hbParamId;
    return (T) this;
  }

  //Checks if position is authorized 
  @SuppressWarnings("unchecked")
  public <T extends ZamiaParam> T setField(String field) {
    if (fields.contains(field)) {
      this.field = field;
      return (T) this;
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
