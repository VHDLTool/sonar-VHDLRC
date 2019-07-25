/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2019 Maxime Facquet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.linty.sonar.zamia;


import com.google.common.base.Strings;
import com.linty.sonar.plugins.vhdlrc.rules.ExampleAndFigureLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.fest.util.VisibleForTesting;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BuildPathMaker {
  
  public static final String TOP_ENTITY_KEY = "sonar.vhdlrc.topEntities";
  public static final String DEFAULT_ENTITY = "WORK.TOP";
  
  public static final String CUSTOM_CMD_KEY = "sonar.vhdlrc.customCmd";
  public static final String CUSTOM_CMD_DESCRIPTION_FILE = "/descritpions/CustomCmdDescription.txt";
  
  private static final String VIRGIN_FILE_PATH = "/virgin_conf/BuildPath.txt";
  
  private final Configuration config;
   
  
  private static final Logger LOG = Loggers.get(BuildPathMaker.class);
  
  public BuildPathMaker(Configuration config) {
    this.config = config;
  } 

  public static Path make(Configuration config){
     try {
      return new BuildPathMaker(config).make();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to generate BuildPath.txt", e);
    }
  }

  protected Path make() throws IOException { 
      Path target = Files.createTempFile("BuildPath", ".txt");//Random name will be generated, ex:"BuildPath3100633746685270227.txt"
      target.toFile().deleteOnExit();
      if(LOG.isDebugEnabled()) {
        LOG.debug("TempFile created by buildPathMaker : " + target);
      }
      InputStream source = BuildPathMaker.class.getResourceAsStream(VIRGIN_FILE_PATH);
      Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
      return appendTopEntities(target.toAbsolutePath());     
  }

  @VisibleForTesting
  protected Path appendTopEntities(Path target) throws IOException { 
    StringBuilder builder = new StringBuilder();
    String topEntity = getTopEntities(this.config); 
    builder
    .append("toplevel ")
    .append(topEntity.toUpperCase())
    .append("\r\n");

    return Files.write(target, builder.toString().getBytes(UTF_8), StandardOpenOption.APPEND);
  }
  
  public static String customCmdDescription() {
    StringBuilder builder = new StringBuilder(); 
    try (BufferedReader reader = new BufferedReader(getRessource(CUSTOM_CMD_DESCRIPTION_FILE))){      
      String line;
      while ((line = reader.readLine()) != null) {
        builder
        .append("\r\n")
        .append(line);
      }
      return String.valueOf(builder);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read " + CUSTOM_CMD_DESCRIPTION_FILE, e);
    }    
  }
  
//  private static String toHtml(String s) {
//    return s
//    .replaceAll("\\<", "&lt;")
//    .replaceAll("\\n", "<br>");
//  }

  public static String getTopEntities(Configuration config ) {
    return config.get(BuildPathMaker.TOP_ENTITY_KEY).orElse("");  
  }
  
public static InputStreamReader getRessource(String ressourcePath) throws IOException {    
    InputStream is = BuildPathMaker.class.getResourceAsStream(ressourcePath);
    if(is == null) {
      throw new IOException();
    }
    return new InputStreamReader(is);
  }

  
 

}
