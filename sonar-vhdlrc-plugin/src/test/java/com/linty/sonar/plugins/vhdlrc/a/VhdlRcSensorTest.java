package com.linty.sonar.plugins.vhdlrc.a;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTester;

import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;

import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;


public class VhdlRcSensorTest  {
	
	public static SensorContextTester sensorContextTester;
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
