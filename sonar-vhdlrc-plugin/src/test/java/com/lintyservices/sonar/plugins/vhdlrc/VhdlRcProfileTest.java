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
package com.lintyservices.sonar.plugins.vhdlrc.a;

import com.lintyservices.sonar.plugins.vhdlrc.Vhdl;
import com.lintyservices.sonar.plugins.vhdlrc.VhdlRcProfile;
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
