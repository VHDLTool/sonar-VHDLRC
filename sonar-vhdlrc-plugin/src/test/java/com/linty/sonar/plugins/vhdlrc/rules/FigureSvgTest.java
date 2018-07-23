package com.linty.sonar.plugins.vhdlrc.rules;
import com.linty.sonar.plugins.vhdlrc.rules.FigureSvg;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FigureSvgTest {
	
	
	@Test
	public void test_no_issues() {
		FigureSvg figure = new FigureSvg();	

		figure.figureCode=
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" + 
				"<!-- Created with Inkscape (http://www.inkscape.org/) -->\r\n" + 
				"\r\n" + 
				"<svg\r\n" + 
				"   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
				"   xmlns:cc=\"http://creativecommons.org/ns#\"\r\n" + 
				"   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
				"   xmlns:svg=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\"\r\n" + 
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   width=\"722.12146\"\r\n" + 
				"   height=\"971.87701\"\r\n" + 
				"   id=\"svg3507\"\r\n" + 
				"   version=\"1.1\"\r\n" + 
				"   inkscape:version=\"0.48.4 r9939\"\r\n" + 
				"   sodipodi:docname=\"STD_04600.svg\">\r\n" + 
				"  <defs"
				;
		if(figure.hasImage()) {
		figure.changeToScalable();
		}
		assertEquals(figure.figureCode,
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" + 
				"<!-- Created with Inkscape (http://www.inkscape.org/) -->\r\n" + 
				"\r\n" + 
				"<svg\r\n" + 
				"   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
				"   xmlns:cc=\"http://creativecommons.org/ns#\"\r\n" + 
				"   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
				"   xmlns:svg=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\"\r\n" + 
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   width=\"100%\"\r\n" + 
				"   height=\"100%\"\r\n" + 
				"   id=\"svg3507\"\r\n" + 
				"   version=\"1.1\"\r\n" + 
				"   inkscape:version=\"0.48.4 r9939\"\r\n" + 
				"   sodipodi:docname=\"STD_04600.svg\">\r\n" + 
				"  <defs"
				);
		
	}
	
	@Test
	public void test_no_decimal_dimensensions() {
		FigureSvg figure = new FigureSvg();	

		figure.figureCode=
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" + 
				"<!-- Created with Inkscape (http://www.inkscape.org/) -->\r\n" + 
				"\r\n" + 
				"<svg\r\n" + 
				"   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
				"   xmlns:cc=\"http://creativecommons.org/ns#\"\r\n" + 
				"   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
				"   xmlns:svg=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\"\r\n" + 
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   width=\"585\"\r\n" + 
				"   height=\"360\"\r\n" + 
				"   id=\"svg2\"\r\n" + 
				"   version=\"1.1\"\r\n" + 
				"   inkscape:version=\"0.91 r13725\"\r\n" + 
				"   sodipodi:docname=\"STD_90700 Reset assertion and deassertion.svg\">\r\n" + 
				"  <defs"
				;
		if(figure.hasImage()) {
		figure.changeToScalable();
		}
		assertEquals(figure.figureCode,
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" + 
				"<!-- Created with Inkscape (http://www.inkscape.org/) -->\r\n" + 
				"\r\n" + 
				"<svg\r\n" + 
				"   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
				"   xmlns:cc=\"http://creativecommons.org/ns#\"\r\n" + 
				"   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
				"   xmlns:svg=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns=\"http://www.w3.org/2000/svg\"\r\n" + 
				"   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\"\r\n" + 
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   width=\"100%\"\r\n" + 
				"   height=\"100%\"\r\n" + 
				"   id=\"svg2\"\r\n" + 
				"   version=\"1.1\"\r\n" + 
				"   inkscape:version=\"0.91 r13725\"\r\n" + 
				"   sodipodi:docname=\"STD_90700 Reset assertion and deassertion.svg\">\r\n" + 
				"  <defs"
				);	
	}
	
	@Test
	public void test_no_width() {
		FigureSvg figure = new FigureSvg();	

		figure.figureCode=
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   height=\"971.87701\"\r\n" + 
				"   id=\"svg3507\"\r\n"
				;
		
		System.out.println(figure.figureCode+"\n---end1------");//
		figure.changeToScalable();
		System.out.println(figure.figureCode+"\n----end2-----");//
		
		assertEquals(figure.figureCode, 
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   height=\"100%\"\r\n" + 
				"   id=\"svg3507\"\r\n"
				);
		
	}
	
	@Test
	public void test_no_dimensions() {
		FigureSvg figure = new FigureSvg();	

		figure.figureCode=
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   id=\"svg3507\"\r\n"
				;
		
		figure.changeToScalable();
		
		assertEquals(figure.figureCode, 
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" +  
				"   id=\"svg3507\"\r\n"
				);
		
	}
	
	@Test
	public void test_mutiple_dim() {
		FigureSvg figure = new FigureSvg();	

		figure.figureCode=
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" + 
				"   width=\"722.12146\"\r\n" + 
				"   height=\"971.87701\"\r\n" + 
				"   id=\"svg3507\"\r\n" +
				"   width=\"722.12146\"\r\n" + 
				"   height=\"971.87701\"\r\n"
				;
		
		//System.out.println(figure.figureCode+"\n---end1------");//
		figure.changeToScalable();
		//System.out.println(figure.figureCode+"\n----end2-----");//
		
		assertEquals(figure.figureCode, 
				"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" +  
				"   width=\"100%\"\r\n" + 
				"   height=\"100%\"\r\n" + 
				"   id=\"svg3507\"\r\n" +
				"   width=\"722.12146\"\r\n" + 
				"   height=\"971.87701\"\r\n"
				);
		
	}
	
	@Test
	public void test_no_code() {
		FigureSvg figure = new FigureSvg();	
		figure.changeToScalable();
		if(!figure.hasImage())
		assertThat(figure.figureCode).isEmpty();
	}


}
