/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2021 Linty Services
 * mailto:contact@linty-services.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.lintyservices.sonar.plugins.vhdlrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PureJavaSensor implements Sensor {

  public static final String SCANNER_HOME_KEY ="sonar.vhdlrc.scanner.home";
  private static final String repo="vhdlrc-repository";
  private static final Logger LOG = Loggers.get(PureJavaSensor.class);
  
  private SensorContext context;
  private String baseProjDir;
  private FilePredicates predicates;

  private ActiveRule std6900;
  private ActiveRule std3300;
  private ActiveRule std6700;
  private ActiveRule std2600;



  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
    .name("Import of issues using java analysis")
    .onlyOnLanguage(Vhdl.KEY)
    .name("pureJavaSensor")
    .onlyWhenConfiguration(conf -> conf.hasKey(SCANNER_HOME_KEY));
  }

  @Override
  public void execute(SensorContext context) {    
    
    this.context=context;
    context.fileSystem().predicates();
    Configuration config = context.config();
    this.predicates = context.fileSystem().predicates();
    System.getProperty("user.dir");
    baseProjDir=System.getProperty("user.dir");
    
    std6900 = context.activeRules().findByInternalKey(repo, "STD_06900");

    
    String[] filesExtension = config.getStringArray(Vhdl.FILE_SUFFIXES_KEY);
    try {
      Files.walk(Paths.get(baseProjDir)).filter(Files::isRegularFile).filter(path->path.toString().toLowerCase().endsWith(".vhdl")||path.toString().toLowerCase().endsWith(".vhd")).forEach(sourceFilePath->checkJavaRules(sourceFilePath.toFile()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    };

  }
  
  private void checkJavaRules(File sourceFile) {
    InputFile inputFile = context.fileSystem().inputFile(predicates.hasPath(sourceFile.getPath()));
    if (inputFile!=null) {
      try (FileReader fReader = new FileReader(sourceFile)){
        BufferedReader bufRead = new BufferedReader(fReader);
        String currentLine = null;
        int lineNumber=0;
        while ((currentLine = bufRead.readLine()) != null) {                           
          lineNumber++;
          boolean inComment=false;
          Scanner input = new Scanner(currentLine);
          input.useDelimiter("((\\p{javaWhitespace})|;|,|\\.|\\(|\\))+");
          while(input.hasNext() && !inComment) {
            String currentToken = input.next();
            if (currentToken.startsWith("--"))
              inComment=true;
            else if(std6900!=null && (currentToken.equalsIgnoreCase("procedure")||currentToken.equalsIgnoreCase("function")))
              addNewIssue("STD_06900", inputFile, lineNumber, "Found function or procedure");                
          }
          input.close();
        }
      } catch (IOException e) {
        LOG.warn("Could not read source file");
      }
    }
    else {
      LOG.warn("Could not resolve inputFile");
    }
  }


  private void addNewIssue(String ruleId, InputFile inputFile, int line, String msg) {
    NewIssue ni = context.newIssue()
      .forRule(RuleKey.of(repo,ruleId));
    NewIssueLocation issueLocation = ni.newLocation()
      .on(inputFile)
      .at(inputFile.selectLine(line))
      .message(msg);
    ni.at(issueLocation);
    ni.save(); 
  }

}