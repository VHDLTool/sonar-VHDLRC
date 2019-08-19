package com.linty.sonar.zamia;

import com.linty.sonar.test.utils.fileTestUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectedRulesMakerTest {
  
  List<String> ruleKeys;
  
  @Rule
  public LogTester logTester = new LogTester();
  
  @Before
  public void init() {
    
  }

  @Test
  public void test() {
    ruleKeys = Arrays.asList(
      "TST_00001",
      "TST_00002",
      "UTI_00001",
      "APK_00022"
      );

    Path result = SelectedRulesMaker.makeWith(ruleKeys);
    Path expected = Paths.get("src/test/parameters/rc_selected_rules/rc_config_selected_rules_expected.xml");
    fileTestUtils.compareFiles(result, expected, false);
  }
  
  @Test
  public void test_empty_list_warning() {
    ruleKeys = Arrays.asList();
    assertThat(ruleKeys).isEmpty();
    Path result = SelectedRulesMaker.makeWith(ruleKeys);
    assertThat(logTester.logs(LoggerLevel.WARN)).isNotEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN).get(0)).contains("No rules to load in " + ZamiaRunner.RC_CONFIG_SELECTED_RULES);
   
    Path expected = Paths.get("src/test/parameters/rc_selected_rules/empty_selected_rules.xml");
    fileTestUtils.compareFiles(result, expected, false);
  }

}
