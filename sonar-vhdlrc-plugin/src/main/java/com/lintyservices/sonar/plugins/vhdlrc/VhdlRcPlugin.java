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
package com.lintyservices.sonar.plugins.vhdlrc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.Version;

import com.google.common.collect.ImmutableList;
import com.lintyservices.sonar.plugins.vhdlrc.metrics.MetricSensor;
import com.lintyservices.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;
import com.lintyservices.sonar.zamia.BuildPathMaker;

public class VhdlRcPlugin implements Plugin {

  public static final Version SONARQUBE_LTS_VERSION = Version.create(7, 9);
  private static final String VHDL_RULECHEKER_SUBCATEGORY = "VHDL RuleChecker";
  public static boolean withoutVhdl = false;

  @Override
  public void define(Context context) {

    ImmutableList.Builder<Object> builder = ImmutableList.builder();
    if (!context.getSonarQubeVersion().isGreaterThanOrEqual(SONARQUBE_LTS_VERSION)) {
      throw new IllegalStateException("SonarQube " + SONARQUBE_LTS_VERSION.major() + "." + SONARQUBE_LTS_VERSION.minor() + " is required for VHDLRC plugin");
    }
    builder.add(
      VhdlRulesDefinition.class,
      VhdlRcProfile.class,
      VhdlRcSensor.class
      );
    InputStream is = VhdlRcPlugin.class.getClassLoader().getResourceAsStream("strings.properties");
    Properties props = new Properties();
    try {
      props.load(is);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if(!(props.get("withoutVhdl")!=null && props.get("withoutVhdl").equals("true"))) {
      builder.add(
        Vhdl.class,
        MetricSensor.class,
        PropertyDefinition.builder(Vhdl.FILE_SUFFIXES_KEY)
        .category(Vhdl.VHDLRC_CATEGORY)
        .subCategory("General")
        .defaultValue(Vhdl.DEFAULT_FILE_SUFFIXES)
        .name("File suffixes")
        .index(1)
        .multiValues(true)
        .description("Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.")
        .onQualifiers(Qualifiers.PROJECT)
        .build()
        );
    }
    else
    {
      withoutVhdl = true;
    }

    if(!(props.get("withoutYosys")!=null && props.get("withoutYosys").equals("true"))) {
      builder.add(YosysGhdlSensor.class);
      builder.add(PropertyDefinition.builder(BuildPathMaker.GHDLSCRIPT_KEY)
        .category(Vhdl.VHDLRC_CATEGORY)
        .subCategory("Rcsynth")
        .name("Ghdl compilation script")
        .description("Path to the project's ghdl compilation script")
        .defaultValue(BuildPathMaker.DEFAULT_GHDLSCRIPT)
        .onQualifiers(Qualifiers.PROJECT)
        .index(0)
        .build());
      builder.add(PropertyDefinition.builder(BuildPathMaker.SCRIPT_PARAMS_KEY)
        .category(Vhdl.VHDLRC_CATEGORY)
        .subCategory("Rcsynth")
        .name("Additional parameters")
        .description("The project's ghdl compilation script will be called with these parameters")
        .defaultValue(BuildPathMaker.DEFAULT_SCRIPT_PARAMS)
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
      builder.add(PropertyDefinition.builder(BuildPathMaker.GHDL_OPTIONS_KEY)
        .category(Vhdl.VHDLRC_CATEGORY)
        .subCategory("Yosys")
        .name("Ghdl options")
        .description("Execute ghdl with those options")
        .defaultValue(BuildPathMaker.DEFAULT_GHDL_OPTIONS)
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
    }
    
    builder.add(PropertyDefinition.builder(BuildPathMaker.TOP_ENTITY_KEY)
      .category(Vhdl.VHDLRC_CATEGORY)
      .subCategory("BuildPath")
      .name("Top Entities")
      .description("Toplevel Entity. Should be uppercased. This parameter also affects yosys' ghdl execution. \r\n" + "Format:  LIBRARY.ENTITY(ARCHITECTURE) \r\n" + "Example: WORK.My_entity(Rtl)")
      .index(2)
      .defaultValue(BuildPathMaker.DEFAULT_ENTITY)
      .onQualifiers(Qualifiers.PROJECT)
      .build());
    builder.add(PropertyDefinition.builder(VhdlRcSensor.SCANNER_HOME_KEY)
      .category(Vhdl.VHDLRC_CATEGORY)
      .subCategory(VHDL_RULECHEKER_SUBCATEGORY)
      .name("RuleChecker Path")
      .hidden()
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
    context.addExtensions(builder.build());
  }
}
