
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

package com.linty.sonar.plugins.vhdlrc.a;



import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;


public class VHDLRcPluginTest {
  
	private static final Version VERSION_6_7 = Version.create(7, 4);
	private VHDLRcPlugin vhdlRcPlugin = new VHDLRcPlugin();
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_plugin_extensions_compatible_with_6_7() {
		SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_6_7, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
		Plugin.Context context = new Plugin.Context(runtime);
		vhdlRcPlugin.define(context);		
		assertThat(context.getExtensions()).hasSize(17);
		runtime.getApiVersion().isGreaterThanOrEqual(Version.create(6, 5));
	}
	
	@Test (expected = IllegalStateException.class)
	public void test_sonar_not_compatible_with_sonar_version() {
		SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(6, 5), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
		Plugin.Context context = new Plugin.Context(runtime);
		vhdlRcPlugin.define(context);
	}


}
