package com.linty.sonar.zamia;

import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BuildPathMaker {
  
  public static final String TOP_ENTITY_KEY = "sonar.vhdl.topEntities";
  public static final String DEFAULT_ENTITY = "WORK.TOP";
  private static final String BUID_PATH_NAME = ZamiaRunner.BUILD_PATH_TXT;
  private final Configuration config;
  private boolean result = false;
  
  private static final Logger LOG = Loggers.get(BuildPathMaker.class);
  
  public BuildPathMaker(Configuration config) {
    this.config = config;
  } 

  public static void build(Configuration config) throws IOException {
     new BuildPathMaker(config).build();
  }

  private void build() throws IOException { 
      Path source = ZamiaRunner.get("/virgin_conf/" + BUID_PATH_NAME );
      Path targetDir = ZamiaRunner.get("/computed_conf/"); 
      Path target = Files.copy(source, targetDir.resolve(source.getFileName()),StandardCopyOption.REPLACE_EXISTING);
      System.out.println("1"+target);//TODO
      appendTopEntities(target);     
  }

  private void appendTopEntities(Path target) throws IOException { 
    
    System.out.println("2"+target);//TODO
    StringBuilder builder = new StringBuilder();
    for(String entity : VhdlRcSensor.getTopEntities(this.config)) {
      builder
      .append("toplevel ")
      .append(entity.toUpperCase())
      .append("\r\n");
    }
    System.out.println(builder.toString());
    Files.write(target, builder.toString().getBytes(UTF_8), StandardOpenOption.APPEND);
  }
  
  public static void printFile(Path target) throws IOException {
    try(BufferedReader reader = Files.newBufferedReader(target,UTF_8)){
      String line;
      int lineNumber = 1;
      while((line = reader.readLine()) != null) {        
        System.out.println(lineNumber + ":" + line);
        lineNumber++;
      }
    }
  }
 

}
