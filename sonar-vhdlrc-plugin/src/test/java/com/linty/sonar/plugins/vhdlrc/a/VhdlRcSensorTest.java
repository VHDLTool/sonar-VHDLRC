/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc.a;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;


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
	public void test_two_good_issues_one_failure() {
	  sensor.execute(context1);
	  List<Issue> issues = new ArrayList(context1.allIssues());
	  
	  assertThat(issues).hasSize(2);
	  issues.forEach(i -> assertThat(i.ruleKey()).isEqualTo("STD_00400"));
	  assertNoIssueOnFile(context1,"file_no_issues.vhd");
	  
	  Issue issue1 = issues.get(0);
	  assertThat(issue1.primaryLocation().inputComponent().key()).isEqualTo(PROJECT_ID + ":file1.vhd");
	  assertThat(issue1.primaryLocation().textRange().start().line()).isEqualTo(1);
	  assertThat(issue1.primaryLocation().message()).isEqualTo("Label is missing");
	  
	  Issue issue2 = issues.get(1);
    assertThat(issue2.primaryLocation().inputComponent().key()).isEqualTo(PROJECT_ID + ":file3.vhd");
    assertThat(issue2.primaryLocation().textRange().start().line()).isEqualTo(2);
    assertThat(issue2.primaryLocation().message()).isEqualTo("Label is missing");
    
    assertThat(logTester.logs(LoggerLevel.INFO)).contains("Importing rc_test_report_STD_00400.xml");
    assertThat(logTester.logs(LoggerLevel.WARN)).contains("No imput file found for no_file.vhd. No issues will be imported on this file.");
	  
	}
	
	//Unset path for scanner means no rule checker analysis should be performed
	public void no_scanner_home_path_set() {
	  SensorContextTester unsetContext = createContext("src/test/files",null);
	  List<Issue> issues = new ArrayList(unsetContext.allIssues());
	  assertThat(issues).isEmpty();
	  assertThat(logTester.logs()).isEmpty();
	}
	
	
	public static void assertNoIssueOnFile(SensorContextTester context, String fileName) {
	  for(Issue i: context.allIssues()) {
	    assertThat(i.primaryLocation().inputComponent().key()).isNotEqualTo(PROJECT_ID + ":" + fileName);
	  }
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
	
