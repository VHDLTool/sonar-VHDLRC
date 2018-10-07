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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import static org.assertj.core.api.Assertions.assertThat;


public class BuildPathMakerTest {
  
  @Rule
  public LogTester logTester = new LogTester();
  
  @Test
  public void test() throws IOException, URISyntaxException {
    logTester.setLevel(LoggerLevel.DEBUG);
    MapSettings settings = new MapSettings();
    settings.setProperty(BuildPathMaker.TOP_ENTITY_KEY, "work.my_entity(rtl)");
    Path ComputedBuildPath = BuildPathMaker.make(settings.asConfig());  
    System.out.println(ComputedBuildPath);
    assertThat(Files.exists(ComputedBuildPath)).isTrue();
    assertThat(Files.isWritable(ComputedBuildPath)).isTrue();
    assertThat(getLineOf(ComputedBuildPath,69)).isEqualTo("toplevel WORK.MY_ENTITY(RTL)");
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0)).isNotEmpty();
  }
  
  @Test
  public void test_multiple_entities() throws IOException, URISyntaxException {
      MapSettings settings = new MapSettings();
      settings.setProperty(BuildPathMaker.TOP_ENTITY_KEY, "top, top1(rtl), work.my_entity(rtl)");
      Path ComputedBuildPath = BuildPathMaker.make(settings.asConfig());   
      assertThat(Files.exists(ComputedBuildPath)).isTrue();
      assertThat(Files.isWritable(ComputedBuildPath)).isTrue();
      assertThat(getLineOf(ComputedBuildPath,69)).isEqualTo("toplevel TOP");
      assertThat(getLineOf(ComputedBuildPath,70)).isEqualTo("toplevel TOP1(RTL)");
      assertThat(getLineOf(ComputedBuildPath,71)).isEqualTo("toplevel WORK.MY_ENTITY(RTL)");
  }
  
//  @Test(expected=IllegalStateException.class)
//  public void test_IOException() {
//    
//  }
  

  public static String getLineOf(Path p, int index) throws IOException {
    try(BufferedReader reader = Files.newBufferedReader(p,UTF_8)){
      String line=reader.readLine();
      int lineNumber = 1;
      while(lineNumber < (index)) {        
        line = reader.readLine();
        lineNumber++;
      }
      return line;
    } 

  }

}
