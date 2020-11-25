
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

package com.linty.sonar.plugins.vhdlrc;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.Version;

import com.google.common.collect.ImmutableList;
import com.linty.sonar.plugins.vhdlrc.metrics.MetricSensor;
import com.linty.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;
import com.linty.sonar.zamia.BuildPathMaker;

public class VHDLRcPlugin implements Plugin {

 public static final Version SQ_6_7 = Version.create(7, 4);
 private static final String VHDL_RULECHEKER_SUBCATEGORY = "VHDL RuleChecker";
	
 @Override 
 public void define(Context context) {
   
  ImmutableList.Builder<Object> builder = ImmutableList.builder();
      if (!context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_6_7)) {
        throw new IllegalStateException("SonarQube " + SQ_6_7.major() + "." + SQ_6_7.minor() + " is required for VHDLRC plugin");
      }
      builder.add(
      Vhdl.class,
      VhdlRulesDefinition.class,
      VhdlRcProfile.class,
      VhdlRcSensor.class,
      YosysGhdlSensor.class,
      MetricSensor.class
        );
	    builder.add(PropertyDefinition.builder(Vhdl.FILE_SUFFIXES_KEY)
	      .category(Vhdl.VHDLRC_CATEGORY)
	      .subCategory("General")
	      .defaultValue(Vhdl.DEFAULT_FILE_SUFFIXES)
	      .name("File suffixes")
	      .index(1)
	      .multiValues(true)
	      .description("Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.")
	      .onQualifiers(Qualifiers.PROJECT)
	      .build());
	    builder.add(PropertyDefinition.builder(VhdlRcSensor.SCANNER_HOME_KEY)
	      .category(Vhdl.VHDLRC_CATEGORY)
	      .subCategory(VHDL_RULECHEKER_SUBCATEGORY)
	      .name("RuleChecker Path")
	      .hidden()
	      .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.TOP_ENTITY_KEY)
	      .category(Vhdl.VHDLRC_CATEGORY)
        .subCategory("BuildPath")
        .name("Top Entities")
        .description("Toplevel Entity. Should be uppercased. This parameter also affects yosys' ghdl execution. \r\n" + "Format:  LIBRARY.ENTITY(ARCHITECTURE) \r\n" + "Example: WORK.My_entity(Rtl)")
        .index(2)
        .defaultValue(BuildPathMaker.DEFAULT_ENTITY)
        .onQualifiers(Qualifiers.PROJECT)
        .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.RCSYNTH_PATH_KEY)
	  	      .category(Vhdl.VHDLRC_CATEGORY)
	          .subCategory("Rcsynth")
	          .name("GhdlCompileScript")
	          .description("Path to the project's ghdl compilation script")
	          .defaultValue(BuildPathMaker.DEFAULT_RCSYNTH)
	          .onQualifiers(Qualifiers.PROJECT)
	          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.FILE_LIST_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Rcsynth")
		          .name("File list")
		          .description("List of files to analyse with ghdl, separated with spaces")
		          .defaultValue(BuildPathMaker.DEFAULT_FILE_LIST)
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.AUTOEXEC_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Rcsynth")
		          .name("Autoexec")
		          .description("Automatically execute vhdlrcsynth when running scanner if true")
		          .type(PropertyType.BOOLEAN)
		          .defaultValue(String.valueOf(BuildPathMaker.DEFAULT_AUTOEXEC))
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.KEEP_SOURCE_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Rcsynth")
		          .name("Keep sources")
		          .description("Keep source files in /vhdl directory when scanner is run if true")
		          .type(PropertyType.BOOLEAN)
		          .defaultValue(String.valueOf(BuildPathMaker.DEFAULT_KEEP_SOURCE))
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.KEEP_REPORTS_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Rcsynth")
		          .name("Keep reports")
		          .description("Keep report files in /rule_checker/reporting/rule directory when scanner is run if true")
		          .type(PropertyType.BOOLEAN)
		          .defaultValue(String.valueOf(BuildPathMaker.DEFAULT_KEEP_REPORTS))
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.PAUSE_EXEC_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Rcsynth")
		          .name("Pause execution")
		          .description("Pause execution after executing all rules and before importing logs if true")
		          .type(PropertyType.BOOLEAN)
		          .defaultValue(String.valueOf(BuildPathMaker.DEFAULT_PAUSE_EXEC))
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.CUSTOM_CMD_KEY)
	      .category(Vhdl.VHDLRC_CATEGORY)
        .subCategory("BuildPath")
        .name("Custom Commands")
        .description(BuildPathMaker.customCmdDescription())
        .index(3)
        .multiValues(false)
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.TEXT)
        .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.FEXPLICIT_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Yosys")
		          .name("fexplicit")
		          .description("Execute ghdl with -fexplicit parameter in yosys")
		          .type(PropertyType.BOOLEAN)
		          .defaultValue(String.valueOf(BuildPathMaker.DEFAULT_FEXPLICIT))
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.FSYNOPSYS_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Yosys")
		          .name("fsynopsys")
		          .description("Execute ghdl with -fsynopsys parameter in yosys")
		          .type(PropertyType.BOOLEAN)
		          .defaultValue(String.valueOf(BuildPathMaker.DEFAULT_FSYNOPSYS))
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	    builder.add(PropertyDefinition.builder(BuildPathMaker.WORKDIR_KEY)
		  	      .category(Vhdl.VHDLRC_CATEGORY)
		          .subCategory("Yosys")
		          .name("Ghdl working directory")
		          .description("Ghdl working directory, relative to the project directory. Needed to execute yosys")
		          .type(PropertyType.STRING)
		          .defaultValue(BuildPathMaker.DEFAULT_WORKDIR)
		          .onQualifiers(Qualifiers.PROJECT)
		          .build());
	  context.addExtensions(builder.build());
  }
}
