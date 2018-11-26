/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2018 Maxime Facquet
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

package com.linty.sonar.plugins.vhdlrc;




import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.Version;

import com.google.common.collect.ImmutableList;
import com.linty.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;
import com.linty.sonar.zamia.BuildPathMaker;


public class VHDLRcPlugin implements Plugin {

 public static final Version SQ_6_7 = Version.create(6, 7);
 private static final String VHDL_RULECHEKER_SUBCATEGORY = "VHDL RuleChecker";
	
 @Override 
 public void define(Context context) {
	  ImmutableList.Builder<Object> builder = ImmutableList.builder();
	    if (!context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_6_7)) {
	      throw new IllegalStateException("SonarQube 6.7 is required for VHDL plugin");
	    }
	    builder.add(
	      Vhdl.class,
	      VhdlRulesDefinition.class,
	      VhdlRcProfile.class,
	      VhdlRcSensor.class
	      );
	    builder.add(PropertyDefinition.builder(Vhdl.FILE_SUFFIXES_KEY)
	      .defaultValue(Vhdl.DEFAULT_FILE_SUFFIXES)
	      .name("File suffixes")
	      .multiValues(true)
	      .description("Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.")
	      .subCategory("General")
	      .onQualifiers(Qualifiers.PROJECT)
	      .build());
	    builder.add(PropertyDefinition.builder(VhdlRcSensor.SCANNER_HOME_KEY)
	      .category(Vhdl.NAME)
	      .subCategory(VHDL_RULECHEKER_SUBCATEGORY)
	      .name("RuleChecker Path")
	      .hidden()
	      .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.TOP_ENTITY_KEY)
	      .category(Vhdl.NAME)
        .subCategory("General")
        .name("Top Entities")
        .description("Toplevel Entities (each toplevel will be elaborated automatically) " + 
          "\r\nFormat:  LIBRARY.ENTITY(ARCHITECTURE) \r\n" +
          "Example: WORK.MY_ENTITY(RTL)")
        .multiValues(true)
        .defaultValue(BuildPathMaker.DEFAULT_ENTITY)
        .onQualifiers(Qualifiers.PROJECT)
        .build());
	  context.addExtensions(builder.build());
  }
}
