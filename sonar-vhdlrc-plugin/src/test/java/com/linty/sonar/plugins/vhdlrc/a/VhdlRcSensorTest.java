/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc.a;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;


public class VhdlRcSensorTest  {
	
	public static SensorContextTester sensorContextTester = SensorContextTester.create(Paths.get("src/test/files"));
	private static final SonarRuntime SQ67 = SonarRuntimeImpl.forSonarQube(VHDLRcPlugin.SQ_6_7, SonarQubeSide.SCANNER);
	//private VhdlRcSensor sensor = new VhdlRcSensor();
	
	@Before
	public void init() {
		//adding a server instance
		sensorContextTester.setRuntime(SQ67);
		//adding rules to context
		ActiveRules activesRules = new ActiveRulesBuilder()
				.create(RuleKey.of("vhdlrc-repository","STD_00400")).setLanguage("vhdl").activate()
				.create(RuleKey.of("vhdlrc-repository","STD_04500")).setLanguage("vhdl").activate()
				.build();
		sensorContextTester.setActiveRules(activesRules);
		//adding Input files to context
		
	}
	
	@Rule
	public LogTester logTester = new LogTester();
	
	@Test
	public void test() {
		
	}

}
