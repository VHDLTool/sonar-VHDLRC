package com.linty.sonar.plugins.vhdlrc.a;

import org.junit.Test;
import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;


import static org.assertj.core.api.Assertions.assertThat;

public class VHDLRcPluginTest {
	
	private static final Version VERSION_6_7 = Version.create(6, 7);
	private VHDLRcPlugin vhdlRcPlugin = new VHDLRcPlugin();
	
	@Test
	 public void test_plugin_extensions_compatible_with_6_7() {
	   SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_6_7, SonarQubeSide.SERVER);
	   Plugin.Context context = new Plugin.Context(runtime);
	   vhdlRcPlugin.define(context);
	   assertThat(context.getExtensions()).hasSize(1);
	 }
	

}