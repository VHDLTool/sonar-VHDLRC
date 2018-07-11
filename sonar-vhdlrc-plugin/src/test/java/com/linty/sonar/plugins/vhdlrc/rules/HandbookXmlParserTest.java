package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.File;
import com.linty.sonar.plugins.vhdlrc.rules.HandbookXmlParser;

import org.sonar.api.internal.apachecommons.io.FilenameUtils;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Before;
import org.junit.Test;


public class HandbookXmlParserTest {
	
	private HandbookXmlParser XmlParser = new HandbookXmlParser();
	List<com.linty.sonar.plugins.vhdlrc.rules.Rule> rl1;
	
	@Rule
	public LogTester logTester = new LogTester();
	
	@Before
	public void setup() {
		File hb = new File("src/test/files/handbooks/VHDL_Handbook_STD-master/Rulesets/handbook_STD.xml");		
		rl1 = XmlParser.parseXML(hb);		
	}

	
	@Test
	public void Test() {		
		//assertThat(rl1).isNotNull();
		assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
		assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
	}
	
	//Existing but empty file should return a null List<Rule> and raise a warning
	@Test
	public void empty_xml_file_test_should_log_warning() {
		rl1 = XmlParser.parseXML(new File("src/test/files/handbooks/empty_file.xml"));
		assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/empty_file.xml");
		assertThat(logTester.logs(LoggerLevel.WARN)).contains("File " + filename + " is empty and won't be analyzed.");
	}
	
	//File not found should return a null List<Rule> and raise a warning,
	//FileNotFoundException is not raised in case multiple handbook would be supported
	@Test 
	public void not_existing_file_should_log_error() {
		rl1 = XmlParser.parseXML(new File("src/test/files/handbooks/non_existing_file.xml"));
		String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/non_existing_file.xml");
		assertThat(logTester.logs(LoggerLevel.WARN)).contains("File " + filename + " was not found or is not a file and won't be analysed");
		assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
	}
	

	@Test
	public void file_is_a_directory_log_treated_has_not_found() {
		rl1 = XmlParser.parseXML(new File("src/test/files/handbooks/"));
		String filename = "src\\test\\files\\handbooks";
		assertThat(logTester.logs(LoggerLevel.WARN)).contains("File " + filename + " was not found or is not a file and won't be analysed");
		assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();		
	}
	

	@Test (expected = IllegalStateException.class)
	public void should_not_log_debug_when_null_argument() {
		File nullFile = null;
		rl1 = XmlParser.parseXML(nullFile);
		assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("Null argument in parseXML()");
		assertThat(logTester.logs(LoggerLevel.DEBUG)).isEmpty();
	}
	
	@Test (expected = IllegalStateException.class)
	  public void should_log_debug_when_null_argument() {
	    logTester.setLevel(LoggerLevel.DEBUG);
	    File nullFile = null;
		rl1 = XmlParser.parseXML(nullFile);
	    assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("Null argument in parseXML()");
	    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1).isNotEmpty();
	}
	
	
	//Existing file with parsing error should raise an error
//	@Test
//	  public void should_return_null_when_encountering_parsing_issue() {
//	    logTester.setLevel(LoggerLevel.DEBUG);
//	    assertThat(XmlParser.parseXML(new File("src/test/files/handbooks/parsing-issue.xml"))).isNull();
//	    String filename = FilenameUtils.separatorsToSystem("src/test/files/handbooks/parsing-issue.xml");
//	    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Unable to parse xml file: " + filename);
//	    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1).contains("XML file parsing failed because of : org.xml.sax.SAXParseException; systemId: file: ");
//	}
	
	
	
	

}
