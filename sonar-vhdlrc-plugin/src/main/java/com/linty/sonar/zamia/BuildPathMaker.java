/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2018 Maxime Facquet
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


import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
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
      appendTopEntities(target);     
  }

  private void appendTopEntities(Path target) throws IOException { 
    StringBuilder builder = new StringBuilder();
    for(String entity : VhdlRcSensor.getTopEntities(this.config)) {
      builder
      .append("toplevel ")
      .append(entity.toUpperCase())
      .append("\r\n");
    }
    Files.write(target, builder.toString().getBytes(UTF_8), StandardOpenOption.APPEND);
  }
  
  
 

}
