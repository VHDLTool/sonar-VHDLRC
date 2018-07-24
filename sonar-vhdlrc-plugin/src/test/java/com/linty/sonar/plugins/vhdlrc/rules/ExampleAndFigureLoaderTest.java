package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ExampleAndFigureLoaderTest {
	
	String goodPath = "src/test/files/handbooks/VHDL_Handbook_STD-master";
	List<com.linty.sonar.plugins.vhdlrc.rules.Rule> rules = new ArrayList<>();
	
	com.linty.sonar.plugins.vhdlrc.rules.Rule r1 = new Rule();//no figure
	com.linty.sonar.plugins.vhdlrc.rules.Rule r2 = new Rule();
	com.linty.sonar.plugins.vhdlrc.rules.Rule r3 = new Rule();
	com.linty.sonar.plugins.vhdlrc.rules.Rule r4 = new Rule();
	com.linty.sonar.plugins.vhdlrc.rules.Rule r5 = new Rule();

	@Before
	public void init() {
		
		r1.goodExampleRef="STD_01400_good";
	    r1.goodExDesc="Extracted from STD_01400_good.vhd";
		r1.badExampleRef="STD_01400_bad";
		r1.badExDesc="Extracted from STD_01400_bad.vhd";
		
		r2.badExampleRef="STD_00000_bad"; // doesn't exists
		r2.badExDesc="Extracted from STD_01400_bad.vhd";	
	
		r3.goodExampleRef="STD_03700_good";
	    r3.goodExDesc="Extracted from STD_03700_good.vhd";
		r3.figureDesc="This is a figure";
		r3.figure = new FigureSvg("STD_03700.svg","250px","450px");
		

		r4.figureDesc="This is a figure";
		r4.figure = new FigureSvg("STD_00000.svg","250px","450px"); // doesn't exists
		r4.goodExampleRef="STD_05500_good";
	    r4.goodExDesc="Extracted from STD_05500_good.vhd";
		r4.badExampleRef="STD_05500_bad";
		r4.badExDesc="Extracted from STD_05500_bad.vhd";
	   
		r5.figureDesc="This is a figure";
		r5.figure = new FigureSvg("STD_05500.svg","600px","");//trying empty height
		
		rules.add(r1);
		rules.add(r2);
		rules.add(r3);
		rules.add(r4);
		rules.add(r5);
		new ExampleAndFigureLoader(Paths.get(goodPath)).load(rules);
	}
	@Test //r1
	public void loading_with_no_trouble() {	
		assertThat(r1.goodExampleCode).isEqualTo(
				"architecture Behavioral of STD_01400_good is\r\n" + 
				"begin\r\n" + 
				"   -- instantiate Mux\r\n" + 
				"   Mux1 : Mux\r\n" + 
				"      port map (\r\n" + 
				"         i_A => i_Mux_Input1,\r\n" + 
				"         i_B => i_Mux_Input2,\r\n" + 
				"         i_S => i_Sel,\r\n" + 
				"         o_O => o_Mux_Output\r\n" + 
				"         );\r\n" + 
				"end Behavioral;\r\n");
		assertThat(r1.badExampleCode).isEqualTo(
				"architecture Behavioral of STD_01400_bad is\r\n" + 
				"begin\r\n" + 
				"   Mux1 : Mux\r\n" + 
				"      port map (i_Mux_Input1, i_Mux_Input2, i_Sel, o_Mux_Output);\r\n" + 
				"end Behavioral;\r\n");
		assertThat(r2.figure).isNull();
	}
	
	@Test //r2
	public void not_found_Example_should_show_message_in_sonar() {		
		assertThat(r2.goodExampleCode).isNullOrEmpty();
		assertThat(r2.badExampleCode).isEqualTo(ExampleAndFigureLoader.NOT_FOUND_EXAMPLE_MSG + "STD_00000_bad.vhd");
		assertThat(r2.figure).isNull();
	}
	
	@Test //r3
	public void test_with_Example_and_figure() {	
		assertThat(r3.goodExampleCode).startsWith("entity STD_03700_good is").endsWith("\r\nend Behavioral;\r\n");
		assertThat(r3.badExampleCode).isNull();
		assertThat(r3.figure.figureCode).startsWith("<svg");
		assertThat(r3.figure.figureCode).endsWith("</svg>\r\n");
	}
	
	@Test //r4
	public void not_found_Figure_should_show_message_in_sonar() {		
		assertThat(r4.goodExampleCode).isNotNull().isNotEmpty().doesNotContain(ExampleAndFigureLoader.NOT_FOUND_EXAMPLE_MSG);
		assertThat(r4.badExampleCode).isNotNull().isNotEmpty().doesNotContain(ExampleAndFigureLoader.NOT_FOUND_EXAMPLE_MSG);
		assertThat(r4.figure.figureCode).isNotNull().isNotEmpty();
		assertThat(r4.figure.figureCode).isEqualTo(ExampleAndFigureLoader.NOT_FOUND_IAMGE_MSG + "STD_00000.svg");
	}
	
	@Test //r5
	public void no_examples_should_not_display_message() {	
		assertThat(r5.goodExampleCode).isNullOrEmpty();
		assertThat(r5.badExampleCode).isNullOrEmpty();
		assertThat(r5.figure.figureCode).isNotNull();
	}
	
	@Test
	public void empty_file_should_not_log_anything() {	
		ExampleAndFigureLoader loader = new ExampleAndFigureLoader(Paths.get("src/test/files/handbooks"));
		String s = loader.collectExample("empty_file");
		assertThat(s).isNullOrEmpty();
	}
	
	@Test
	public void not_foud_balise_should_not_log_anything() {
		ExampleAndFigureLoader loader = new ExampleAndFigureLoader(Paths.get("src/test/files/handbooks"));
		String s = loader.collectExample("no_balises");
		assertThat(s).isNullOrEmpty();
	}
	
	@Test
	public void empty_image_should_not_log_anything() {
		ExampleAndFigureLoader loader = new ExampleAndFigureLoader(Paths.get("src/test/files/handbooks"));
		String s = loader.collectImage("empty_image.svg");
		assertThat(s).isNullOrEmpty();
	}
	
	

}
