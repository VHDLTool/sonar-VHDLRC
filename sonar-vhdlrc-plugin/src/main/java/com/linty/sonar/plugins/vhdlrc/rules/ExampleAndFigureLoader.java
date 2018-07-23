package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.annotations.VisibleForTesting;

public class ExampleAndFigureLoader {
	
    private static final String IMAGES_PATH = "Extras/Images";
    private static final String EXAMPLES_PATH = "Extras/VHDL";
    public static final String CODE_BALISE = "--CODE";
    public static final String NOT_FOUND_EXAMPLE_MSG = "Example not found : ";
	public static final String NOT_FOUND_IAMGE_MSG = "Image not found : ";
    
	private final Path examplePath;
	private final Path imagePath;
	
	ExampleAndFigureLoader(Path dir){
		this.examplePath = dir.resolve(EXAMPLES_PATH);
		this.imagePath = dir.resolve(IMAGES_PATH);
	}

	public void load(List<Rule> rules) {
		for(Rule r : rules) {
			if(!StringUtils.isEmpty(r.goodExampleRef)) {
				r.goodExampleCode = collectExample(r.goodExampleRef);
			}
			if(!StringUtils.isEmpty(r.badExampleRef)) {
				r.badExampleCode = collectExample(r.badExampleRef);
			}
			if(r.figure != null) {
				r.figure.figureCode = collectImage(r.figure.figureRef);	
			}
			
		}
	}

	
	@VisibleForTesting
	protected String collectExample(String fileRef) {		
		StringBuilder codeExample = new StringBuilder();	
		String fileName = fileRef.concat(".vhd");
		try(BufferedReader reader = Files.newBufferedReader(this.examplePath.resolve(fileName), Charset.forName("UTF-8"))){		      

			String line = reader.readLine();
			while(line != null && !line.contains(CODE_BALISE)) {//waiting for 1rst --CODE to start
				line=reader.readLine();
			}
			while((line = reader.readLine()) != null && !line.contains(CODE_BALISE)){//waiting for 2nd --CODE to stop
				codeExample.append(line).append("\r\n");
			}
			
		} catch (IOException e) {
			System.out.println(NOT_FOUND_EXAMPLE_MSG + fileName);//TODO
			return NOT_FOUND_EXAMPLE_MSG + fileName;		
		}
		return String.valueOf(codeExample);
	}
	
	protected String collectImage(String fileName) {
		StringBuilder figureCode = new StringBuilder();
		try(BufferedReader reader = Files.newBufferedReader(this.imagePath.resolve(fileName), Charset.forName("UTF-8"))){
			String line = reader.readLine();
			while(line != null && !line.contains("<svg")) {
				line=reader.readLine();
			}
			if(line!=null) {
				figureCode.append(line).append("\r\n");
			}
			while((line = reader.readLine()) != null){
				figureCode.append(line).append("\r\n");
			}

		 } catch (IOException e) {
			 System.out.println(NOT_FOUND_IAMGE_MSG + fileName);//TODO
			 return NOT_FOUND_IAMGE_MSG + fileName;
		}
		 return String.valueOf(figureCode);
	}
}
