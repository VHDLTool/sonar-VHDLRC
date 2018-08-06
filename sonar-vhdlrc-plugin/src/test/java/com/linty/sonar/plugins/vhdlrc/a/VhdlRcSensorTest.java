/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc.a;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;
import static java.nio.charset.StandardCharsets.UTF_8;


public class VhdlRcSensorTest  {
	
	private static final SonarRuntime SQ67 = SonarRuntimeImpl.forSonarQube(VHDLRcPlugin.SQ_6_7, SonarQubeSide.SERVER);
	private static final String PROJECT_ID = "vhdlrc-test";
	private VhdlRcSensor sensor = new VhdlRcSensor();
	
  private SensorContextTester context1 = createContext("src/test/files","src/test/files/scanner-home");
	
	@Before
	public void init() {
		addRules(context1,"STD_00400","STD05000");
		addTestFile(context1,"I2C/file1.vhd","This file has one line");
		addTestFile(context1,"I2C/file3.vhd","This one has \r\n 3 \r\nlines");
		addTestFile(context1,"I2C/file_no_issues.vhd","");	
	}
	
	@Rule
	public LogTester logTester = new LogTester();
	
	@Test
	public void test() {
	  System.out.println(context1.config().get("sonar.vhdlrc.rc"));
	  sensor.execute(context1);
		
	}

	
	public static SensorContextTester createContext(String projectHomePath, String scannerHomePath) {
    return SensorContextTester.create(Paths.get(projectHomePath))
      .setSettings(new MapSettings().setProperty("sonar.vhdlrc.rc", scannerHomePath))
      .setRuntime(SQ67);
	}
	
	public static void addRules(SensorContextTester context, String...args) {
	  ActiveRulesBuilder builder = new ActiveRulesBuilder();
	  for(String ruleKey : args) {
	    builder.create(RuleKey.of("vhdlrc-repository",ruleKey)).setLanguage("vhdl").activate();
	  }
    context.setActiveRules(builder.build());	  
	}
	
	public static void addTestFile(SensorContextTester context, String filePath, String content) {
	  context.fileSystem().add(TestInputFileBuilder.create(PROJECT_ID,filePath)
      .setLanguage("vhdl")
      .setCharset(UTF_8)
      .setContents(content)
      .build());
	}
}
	
