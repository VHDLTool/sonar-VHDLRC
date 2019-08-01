package com.linty.sonar.plugins.vhdlrc.a;

import com.linty.sonar.plugins.vhdlrc.Vhdl;
import com.linty.sonar.plugins.vhdlrc.VhdlRcProfile;
import org.junit.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import static org.assertj.core.api.Assertions.assertThat;


public class VhdlRcProfileTest {
  

  public BuiltInQualityProfilesDefinition.BuiltInQualityProfile getProfile() {
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    new VhdlRcProfile().define(context);
    return context.profile(Vhdl.KEY, VhdlRcProfile.BUILT_IN_PROFILE_NAME);
  }

  @Test
  public void test() {
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = getProfile();
    assertThat(profile.language()).isEqualTo(Vhdl.KEY);
    assertThat(profile.name()).isEqualTo(VhdlRcProfile.BUILT_IN_PROFILE_NAME);
    //assertThat(profile.language()).isNotEqualTo("vhdl");
  }

}
