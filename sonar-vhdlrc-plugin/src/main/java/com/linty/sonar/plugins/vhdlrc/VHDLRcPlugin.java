/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc;



import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.Version;

import com.google.common.collect.ImmutableList;
import com.linty.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;


public class VHDLRcPlugin implements Plugin {

 public static final Version SQ_6_7 = Version.create(6, 7);
 private static final String VHDL_RULECHEKER_CATEGORY = "VHDL RuleChecker";
	
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
	      .description("Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.")
	      .subCategory("General")
	      .onQualifiers(Qualifiers.PROJECT)
	      .build());
	    builder.add(PropertyDefinition.builder(VhdlRulesDefinition.HANDBOOK_PATH_KEY)
	      .category(Vhdl.NAME)
	      .subCategory(VHDL_RULECHEKER_CATEGORY)
	      .name("Handbook Path")
	      .description(VhdlRulesDefinition.HANDBOOK_PATH_DESC)
	      .build());
	    builder.add(PropertyDefinition.builder(VhdlRcSensor.RC_PATH_KEY)
	      .category(Vhdl.NAME)
	      .subCategory(VHDL_RULECHEKER_CATEGORY)
	      .name("RuleChecker Path")
	      .hidden()
	      .build()
	      );
//    context.addExtensions(VHDLToolConfiguration.getPropertyDefinitions());
//    context.addExtensions(Arrays.asList(
//            VHDLToolProfile.class,
//            VHDLToolSensor.class,
//            VHDLToolConfiguration.class,
//            Vhdl.class,
//            STDRulesDefinition.class));
	  context.addExtensions(builder.build());
  }
}
