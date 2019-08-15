package com.linty.sonar.zamia;

import com.linty.sonar.params.ZamiaIntParam;
import com.linty.sonar.params.ZamiaRangeParam;
import com.linty.sonar.params.ZamiaStringParam;
import com.linty.sonar.test.utils.fileTestUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.xml.sax.SAXException;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonControllers;
import org.xmlunit.diff.ComparisonFormatter;
import org.xmlunit.matchers.CompareMatcher;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ActiveRuleLoaderTest {
  
  private static final String FORMAT = ZamiaStringParam.PARAM_KEY;
  private static final String LIMIT = ZamiaIntParam.LI_KEY;
  private static final String RELATION = ZamiaIntParam.RE_KEY;
  private static final String MIN__ = ZamiaRangeParam.MIN_KEY;
  private static final String RANGE = ZamiaRangeParam.RANGE_KEY;
  private static final String MAX__ = ZamiaRangeParam.MAX_KEY;
  
  ActiveRulesBuilder builder;
  ActiveRules activeRules;

  
  @org.junit.Rule
  public LogTester logTester = new LogTester();
  
  @Before
  public void init() {
    builder = new ActiveRulesBuilder();
    //[RuleKey] [Ligne] [Parameters in source.xml] -> [Parameters in expected.xml] [Param Type]
    
    //STD_00001 l.13 1 -> 2 STRING
    builder.addRule(stringRule("STD_00001", FORMAT,"*_TOTO,*TITI*"));
    
    //STD_00002 l.30 2 -> 1 STRING
    builder.addRule(stringRule("STD_00002", FORMAT,"AAA*"));
    
    //STD_00003 l.42 0 -> 0 (UNTOUCHED)
    builder.addRule(aRule("STD_00003").build());
    
    //STD_00004 l.48 2 -> INT
    builder.addRule(intRule("STD_00004", RELATION, "<", LIMIT, "99" ));
    
    //STD_00005 l.65 0 -> RANGE
    builder.addRule(rangeRule("STD_00005", MIN__, "-5", RANGE, "<=_<", MAX__, "150" ));
    
    //STD_00006 l.70 1 -> 1 (UNTOUCHED because no parameter in Sonar) 
    builder.addRule(aRule("STD_00006").build());
    
    //STD_00007 l.42 0 -> 0 (UNTOUCHED because not in Sonar ActiveRules)
    builder.addRule(aRule("STD_00007").build());
    
    activeRules = builder.build();
  }

  /*
   > For each rule in  rc_handbook_parameters.xml :
   | <Rule is activated in Sonar ActiveRules ?>
   |next() <- No -| 
   |             yes -> [add rule to list of active implemented rules]
   |              |
   | <Rule has a parameter in Sonar ?>
   |next() <- No -| 
   |             yes -> [Overwrite parameter in rc_handbook_parameters.xml]
   |______________|
   
   A Rule in rc_handbook_parameters.xml sonar activeRule has no parameter =>
   Existing parameters in  are left untouched

   */
  @Test
  public void test_writing_normal_list() {

    //Expected result to match
    Path expected = Paths.get("src/test/parameters/rc_parameters/expected.xml");

    //Generate the new rc_handbook_parameter.xml
    Path result = tryWritingXmlFrom(activeRules, "src/test/parameters/rc_parameters/source.xml");

    //Compare Xml Files
    fileTestUtils.compareXml(result, expected);

  }
  
  /*activeRuleKeys() should be filled with rule that:
   [1] Are present in rc_handbook_parameters.xml
         AND
   [2] Are activated in Sonar <=> are present in activeRules
   */
  @Test
  public void test_content_of__active_Rule_Keys() {
    fail("Not yet implemented");
  }
  
  @Test
  public void test_getting_active_rule_keys() {
    activeRules = new ActiveRulesBuilder().build();
    ActiveRuleLoader arl = new ActiveRuleLoader(activeRules);
    //Try getting activeRuleList before loading them
    try {
      arl.activeRuleKeys();
      fail("Expected IllegaleStateException");
    } catch (IllegalStateException e) { 
      assertThat(e.getMessage()).isNotNull();
    }
    arl.makeRcHandbookParameters();
    assertThat(arl.activeRuleKeys()).isEmpty();;
    
  }
    
  
  @Test
  public void test_repo_not_found() {
    fail("Not yet implemented");
  }
  
  public NewActiveRule.Builder aRule(String ruleKey) {
    return new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of("vhdlrc-repository",ruleKey))
      .setLanguage("vhdl");
  }
  public NewActiveRule stringRule(String ruleKey, String paramKey, String value) {
    return aRule(ruleKey)
      .setParam(paramKey, value)
      .build();   
  }
  
  public NewActiveRule intRule(String ruleKey, String pk1, String v1, String pk2,  String v2) {
    return aRule(ruleKey)
      .setParam(pk1, v1)
      .setParam(pk2, v2)
      .build();   
  }
  
  public NewActiveRule rangeRule(String ruleKey, String pk1, String v1, String pk2, String v2, String pk3, String v3) {
    return aRule(ruleKey)
      .setParam(pk1, v1)
      .setParam(pk2, v2)
      .setParam(pk3, v3)
      .build();   
  }
  
  private Path tryWritingXmlFrom(ActiveRules activeRules, String source) {   
    InputStream sourceIS;
    try {
      sourceIS = new FileInputStream(new File(source));
      return new ActiveRuleLoader(activeRules).writeParametersInXml(sourceIS); 
    } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
      e.printStackTrace();
      throw new IllegalStateException();
    }
   
  }
  
  
}
