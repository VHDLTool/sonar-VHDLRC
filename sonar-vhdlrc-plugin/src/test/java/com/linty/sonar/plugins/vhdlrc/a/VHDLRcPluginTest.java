package com.linty.sonar.plugins.vhdlrc.a;


import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.platform.ServerFileSystem;

import org.sonar.api.utils.Version;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

public class VHDLRcPluginTest {
	
	private static final Version VERSION_6_7 = Version.create(6, 7);
	private VHDLRcPlugin vhdlRcPlugin = new VHDLRcPlugin();
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_plugin_extensions_compatible_with_6_7() {
		SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_6_7, SonarQubeSide.SERVER);
		Plugin.Context context = new Plugin.Context(runtime);
		vhdlRcPlugin.define(context);		
		assertThat(context.getExtensions()).hasSize(3);
		runtime.getApiVersion().isGreaterThanOrEqual(Version.create(6, 5));
	}
	
	@Test (expected = IllegalStateException.class)
	public void test_sonar_not_compatible_with_sonar_version() {
		SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(6, 5), SonarQubeSide.SERVER);
		Plugin.Context context = new Plugin.Context(runtime);
		vhdlRcPlugin.define(context);
	}


}
