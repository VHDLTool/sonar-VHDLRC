package com.linty.sonar.plugins.vhdlrc.a;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;


public class VhdlRcSensorTest  {
	
	public static SensorContextTester sensorContextTester = SensorContextTester.create(Paths.get("src/test/files/log"));
	private static final SonarRuntime SQ67 = SonarRuntimeImpl.forSonarQube(VHDLRcPlugin.SQ_6_7, SonarQubeSide.SERVER);
	//private VhdlRcSensor sensor = new VhdlRcSensor();
	
	@Before
	public void init() {
		sensorContextTester.setRuntime(SQ67);
		
	}
	
	@Rule
	public LogTester logTester = new LogTester();
	
	@Test
	public void test() {
		
	}

}
