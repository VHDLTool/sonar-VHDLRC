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

import com.lintyservices.sonar.plugins.vhdlrc.VhdlRcPlugin;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VhdlRcPluginTest {

  private static final Version SONARQUBE_LTS_VERSION = Version.create(7, 9);
  private VhdlRcPlugin vhdlRcPlugin = new VhdlRcPlugin();

  @SuppressWarnings("unchecked")
  @Test
  public void test_plugin_extensions_compatible_with_7_9() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(SONARQUBE_LTS_VERSION, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
    Plugin.Context context = new Plugin.Context(runtime);
    vhdlRcPlugin.define(context);
    int extensionsNumber=17;
    if (System.getProperty("withoutVhdlLanguageSupport")!=null && System.getProperty("withoutVhdlLanguageSupport").equals("true")) {
      extensionsNumber-=3;
    }
    if (System.getProperty("withoutYosys")!=null && System.getProperty("withoutYosys").equals("true")) {
      extensionsNumber-=6;
    }
    assertThat(context.getExtensions()).hasSize(extensionsNumber);
    runtime.getApiVersion().isGreaterThanOrEqual(Version.create(6, 5));
  }

  @Test(expected = IllegalStateException.class)
  public void test_sonar_not_compatible_with_sonar_version() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(6, 5), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
    Plugin.Context context = new Plugin.Context(runtime);
    vhdlRcPlugin.define(context);
  }

}
