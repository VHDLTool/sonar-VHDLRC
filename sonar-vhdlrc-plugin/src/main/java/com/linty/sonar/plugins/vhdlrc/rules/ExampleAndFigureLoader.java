package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ExampleAndFigureLoader {
	
    private static final String IMAGES_PATH = "Extras/Images";
    private static final String EXAMPLES_PATH = "Extras/VHDL";
    public static final String CODE_BALISE = "--CODE";
    public static final String NOT_FOUND_EXAMPLE_MSG = "Code not found : ";
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
			if(r.figure != null)
			collectImage(r);
		}
	}

	

	private String collectExample(String fileRef) {
		
		StringBuilder codeExample = new StringBuilder();	
		String fileName = fileRef.concat(".vhd");
		//System.out.println(this.imagePath.resolve(fileName).toString());//TODO
		try(BufferedReader reader = Files.newBufferedReader(this.examplePath.resolve(fileName), Charset.forName("UTF-8"))){		      
			String Line = null;
			System.out.println("Parsing : " + fileName);
			while((Line = reader.readLine()) != null){
				if(Line.contains(CODE_BALISE)) {
					while((Line = reader.readLine()) != null && !Line.contains(CODE_BALISE)){
						codeExample.append(Line).append("\n");
					}
				}
			}
		} catch (IOException e) {
			System.out.println(NOT_FOUND_EXAMPLE_MSG + fileName);//TODO
			return NOT_FOUND_EXAMPLE_MSG + fileName;		
		}
		return String.valueOf(codeExample);
	}
	
	private void collectImage(Rule r) {
		
//		 try(BufferedReader reader = Files.newBufferedReader(this.imagePath.resolve(), Charset.forName("UTF-8"))){
//
//		      
//		      String currentLine = null;
//		      while((currentLine = reader.readLine()) != null){}
//		        
//		 }
	}

}
