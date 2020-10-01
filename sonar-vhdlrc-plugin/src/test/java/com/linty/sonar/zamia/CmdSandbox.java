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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CmdSandbox {
  
  public String home = "src/test/files/";
  public String notePadDir = "";
  public String notePad = ".";
  public String arguments = "";
  public String fileDir = "dev/eclipse_test.bat";
  
  private static final Logger LOG = Loggers.get(CmdSandbox.class);
  
  public void execute(String program, String args, String target) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder();
    Process process;
    boolean isWindows = System.getProperty("os.name")
      .toLowerCase().startsWith("windows");
    ArrayList<String> fullCmd = new ArrayList<>();
    fullCmd.add(program);
    fullCmd.addAll(Arrays.asList(args.split(" ")));
    //fullCmd.add(target);
    
    System.out.println(fullCmd);
    System.out.println(target);
    
    if (isWindows) {
      //builder.directory(directory.toFile());
      builder.command(fullCmd);
      
      System.out.println("TRY : " + builder.command());
      process = builder.start();
      process.waitFor();
      
      String line;
      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      while ((line = input.readLine()) != null)
        System.out.println(line);
      while ((line = error.readLine()) != null)
        System.out.println(line);
      input.close();
      error.close();
    }
  }
  
  @Test
  public void test() {
    Path program = Paths.get(home, notePadDir, notePad);
    Path target = Paths.get(home,fileDir);
    String programPath = "\"" + program.toString() + "\"";
    String targetPath = "\"" + target.toString() + "\"";
    try {
      execute(programPath, arguments, targetPath );
    } catch (IOException | InterruptedException e) {
      LOG.warn("Thread interruption");
	  Thread.currentThread().interrupt();
    }
  }
  
}
