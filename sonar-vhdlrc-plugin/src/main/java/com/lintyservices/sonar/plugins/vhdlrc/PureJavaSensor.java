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
import java.util.Scanner;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.lintyservices.sonar.plugins.vhdlrc.metrics.CustomMetrics;

public class PureJavaSensor implements Sensor {

  public static final String SCANNER_HOME_KEY ="sonar.vhdlrc.scanner.home";
  private static final String repo="vhdlrc-repository";
  private static final Logger LOG = Loggers.get(PureJavaSensor.class);

  private SensorContext context;
  private FilePredicates predicates;
  private int totalComments;

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
    this.predicates = context.fileSystem().predicates();
    totalComments=0;

    std6900 = context.activeRules().find(RuleKey.of(repo, "STD_06900"));
    std3300 = context.activeRules().find(RuleKey.of(repo, "STD_03300"));
    std6700 = context.activeRules().find(RuleKey.of(repo, "STD_06700"));
    std2600 = context.activeRules().find(RuleKey.of(repo, "STD_02600"));
        
    Iterable<InputFile> files = context.fileSystem().inputFiles(predicates.hasLanguage(Vhdl.KEY));
    files.forEach(file->checkJavaRules(file));
    context.<Integer>newMeasure().forMetric(CustomMetrics.COMMENT_LINES_STD_02800).on(context.project()).withValue(totalComments).save();

  }

  private void checkJavaRules(InputFile inputFile) {
    if (inputFile!=null) {
      File sourceFile = inputFile.file();
      try (FileReader fReader = new FileReader(sourceFile)) {
        BufferedReader bufRead = new BufferedReader(fReader);
        String currentLine = null;
        int lineNumber=0;
        int commentedLines = 0;
        boolean inBlockComment=false;
        while ((currentLine = bufRead.readLine()) != null) {                           
          lineNumber++;
          if (inBlockComment) {
            commentedLines++;
          }
          boolean inComment=false;
          Scanner input = new Scanner(currentLine);
          input.useDelimiter("((\\p{javaWhitespace})|;|,|\\.|\\(|\\))+");
          while(input.hasNext() && !inComment) {
            String currentToken = input.next();
            if (currentToken.startsWith("--")) {
              inComment=true;
              commentedLines++;
            }
            else if (currentToken.startsWith("/*")) {
              inBlockComment=true;
              commentedLines++;
            }
            else if (currentToken.endsWith("*/")) {
              inBlockComment=false;
            }
            else if (!inBlockComment) {
              if (std6900!=null && (currentToken.equalsIgnoreCase("procedure") || currentToken.equalsIgnoreCase("function"))) {
                addNewIssue("STD_06900", inputFile, lineNumber, "Procedures and functions should not be used in RTL design");   
              }
              else if (std3300!=null && (currentToken.equalsIgnoreCase("buffer"))) {
                addNewIssue("STD_03300", inputFile, lineNumber, "Buffer port type is not recommended for synthesis");   
              }
              else if (std6700!=null && (currentToken.equalsIgnoreCase("wait"))) {
                addNewIssue("STD_06700", inputFile, lineNumber, "Wait instruction is not synthesizable");   
              }
              else if (std2600!=null && (currentToken.equalsIgnoreCase("std_logic_arith") || currentToken.equalsIgnoreCase("std_logic_signed") || currentToken.equalsIgnoreCase("std_logic_unsigned"))) {
                addNewIssue("STD_02600", inputFile, lineNumber, "\"std_logic_arith\", \"std_logic_signed\" and \"std_logic_unsigned\" libraries are not standardized and should not be used");   
              }
            }
          }
          input.close();
        }
        totalComments+=commentedLines;
        context.<Integer>newMeasure().forMetric(CustomMetrics.COMMENT_LINES_STD_02800).on(inputFile).withValue(commentedLines).save();
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