package com.linty.sonar.plugins.vhdlrc;

import java.util.Arrays;

import org.sonar.api.Plugin;
import org.sonar.api.utils.Version;

import com.google.common.collect.ImmutableList;


public class VHDLRcPlugin implements Plugin {

 private static final Version SQ_6_7 = Version.create(6, 7);
	
 @Override 
 public void define(Context context) {
	  ImmutableList.Builder<Object> builder = ImmutableList.builder();
	    if (!context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_6_7)) {
	      throw new IllegalStateException("SonarQube 6.7 is required for VHDL plugin");
	    }
	    context.addExtension(
	    		Vhdl.class	    		
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
