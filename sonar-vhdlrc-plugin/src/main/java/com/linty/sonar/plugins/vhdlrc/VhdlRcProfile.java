package com.linty.sonar.plugins.vhdlrc;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class VhdlRcProfile implements BuiltInQualityProfilesDefinition{
	
	private static final String BUILT_IN_PROFILE_NAME ="VHDL RC";

	@Override
	public void define(Context context) {
		NewBuiltInQualityProfile vhdlRcQP = context.createBuiltInQualityProfile(BUILT_IN_PROFILE_NAME, Vhdl.KEY);
		//vhdlRcQP.activateRule(BUILT_IN_PROFILE_NAME, "STD_00600");
		vhdlRcQP.setDefault(true)
		.done();
	}
}
