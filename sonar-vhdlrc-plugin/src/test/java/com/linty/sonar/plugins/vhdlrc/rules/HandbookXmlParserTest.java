package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.File;
import com.linty.sonar.plugins.vhdlrc.rules.HandbookXmlParser;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class HandbookXmlParserTest {
	

	private static final String hbPath="src/test/files/handbooks/VHDL_Handbook_STD-master/Rulesets/handbook_STD.xml";
	
	@Test
	public void Test() {
		File hb = new File(hbPath);
		HandbookXmlParser p = new HandbookXmlParser();
		p.parseXML(hb);
		//assertThat(p.getRules().get(0).name).isEqualTo(" ");
		assertThat(p.getRules()).isNull();

	}
	
	

}
