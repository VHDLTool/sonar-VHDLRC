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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
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
import com.lintyservices.sonar.zamia.BuildPathMaker;

public class PureJavaSensor implements Sensor {

  private static final String repo="vhdlrc-repository";
  private static final Logger LOG = Loggers.get(PureJavaSensor.class);
  private static final Set <String> notAPackageName = new HashSet<>(Arrays.asList("all", "std", "ieee"));

  private SensorContext context;
  private FilePredicates predicates;
  private int totalComments;
  private String top;
  private Set<VhdlPackage> allVhdlPackages;
  private Stack<String> packagesHierarchy;
  private String defaultLanguage = "";

  private ActiveRule std6900;
  private ActiveRule std3300;
  private ActiveRule std6700;
  private ActiveRule std2600;
  private ActiveRule std2000;
  private ActiveRule std2800;
  private ActiveRule std2200;
  private ActiveRule cne2700;
  private ActiveRule std600;
  private ActiveRule cne300;
  private ActiveRule std2400;
  private ActiveRule std2300;
  private ActiveRule std2500;
  private ActiveRule cne2800;
  private ActiveRule cne2900;
  private ActiveRule cne3000;
  private ActiveRule cne3100;
  private ActiveRule cne3200;
  private ActiveRule cne3300;
  private ActiveRule cne3400;
  private ActiveRule cne3500;
  private ActiveRule cne3600;
  private ActiveRule cne3700;
  private ActiveRule cne3800;
  private ActiveRule cne3900;
  private ActiveRule cne4000;
  private ActiveRule cne4100;
  private ActiveRule cne4200;
  private ActiveRule cne4300;
  private ActiveRule cne4400;
  private ActiveRule std6000;
  private ActiveRule std6100;
  private ActiveRule std5400;
  private ActiveRule std2700;
  private ActiveRule cne5400;


  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
    .name("Import of issues using java analysis")
    .onlyOnLanguage(Vhdl.KEY)
    .name("pureJavaSensor");
  }

  @Override
  public void execute(SensorContext context) {

    this.context=context;
    this.predicates = context.fileSystem().predicates();
    totalComments=0;
    top = BuildPathMaker.getTopEntities(context.config());
    allVhdlPackages = new HashSet<>();
    packagesHierarchy = new Stack<>();

    std6900 = context.activeRules().find(RuleKey.of(repo, "STD_06900"));
    std3300 = context.activeRules().find(RuleKey.of(repo, "STD_03300"));
    std6700 = context.activeRules().find(RuleKey.of(repo, "STD_06700"));
    std2600 = context.activeRules().find(RuleKey.of(repo, "STD_02600"));
    std2000 = context.activeRules().find(RuleKey.of(repo, "STD_02000"));
    std2800 = context.activeRules().find(RuleKey.of(repo, "STD_02800"));
    std2200 = context.activeRules().find(RuleKey.of(repo, "STD_02200"));
    std2300 = context.activeRules().find(RuleKey.of(repo, "STD_02300"));
    std2400 = context.activeRules().find(RuleKey.of(repo, "STD_02400"));
    std2500 = context.activeRules().find(RuleKey.of(repo, "STD_02500"));
    cne2800 = context.activeRules().find(RuleKey.of(repo, "CNE_02800"));
    cne2900 = context.activeRules().find(RuleKey.of(repo, "CNE_02900"));
    cne3000 = context.activeRules().find(RuleKey.of(repo, "CNE_03000"));
    cne3100 = context.activeRules().find(RuleKey.of(repo, "CNE_03100"));
    cne3200 = context.activeRules().find(RuleKey.of(repo, "CNE_03200"));
    cne3300 = context.activeRules().find(RuleKey.of(repo, "CNE_03300"));
    cne3400 = context.activeRules().find(RuleKey.of(repo, "CNE_03400"));
    cne3500 = context.activeRules().find(RuleKey.of(repo, "CNE_03500"));
    cne3600 = context.activeRules().find(RuleKey.of(repo, "CNE_03600"));
    cne3700 = context.activeRules().find(RuleKey.of(repo, "CNE_03700"));
    cne3800 = context.activeRules().find(RuleKey.of(repo, "CNE_03800"));
    cne3900 = context.activeRules().find(RuleKey.of(repo, "CNE_03900"));
    cne4000 = context.activeRules().find(RuleKey.of(repo, "CNE_04000"));
    cne4100 = context.activeRules().find(RuleKey.of(repo, "CNE_04100"));
    cne4200 = context.activeRules().find(RuleKey.of(repo, "CNE_04200"));
    cne4300 = context.activeRules().find(RuleKey.of(repo, "CNE_04300"));
    cne4400 = context.activeRules().find(RuleKey.of(repo, "CNE_04400"));
    std2800 = context.activeRules().find(RuleKey.of(repo, "STD_02800"));
    cne2700 = context.activeRules().find(RuleKey.of(repo, "CNE_02700"));
    std600 = context.activeRules().find(RuleKey.of(repo, "STD_00600"));
    cne300 = context.activeRules().find(RuleKey.of(repo, "CNE_00300"));
    std6000 = context.activeRules().find(RuleKey.of(repo, "STD_06000"));
    std6100 = context.activeRules().find(RuleKey.of(repo, "STD_06100"));
    std5400 = context.activeRules().find(RuleKey.of(repo, "STD_05400"));
    std2700 = context.activeRules().find(RuleKey.of(repo, "STD_02700"));
    cne5400 = context.activeRules().find(RuleKey.of(repo, "CNE_05400"));
    
    if (std2700!=null) {
      defaultLanguage = std2700.param("Format");
    }



    Iterable<InputFile> files = context.fileSystem().inputFiles(predicates.hasLanguage(Vhdl.KEY));
    files.forEach(file->checkJavaRules(file));
    allVhdlPackages.forEach(vhdlPackage->checkVhdlPackage(vhdlPackage.getPackageName(), 0, vhdlPackage.getPackageFile()));
    context.<Integer>newMeasure().forMetric(CustomMetrics.COMMENT_LINES_STD_02800).on(context.project()).withValue(totalComments).save();

  }

  private void checkVhdlPackage (String packageName, int level, InputFile packageFile) {
    if (packagesHierarchy.contains(packageName)) {
      StringBuilder builder = new StringBuilder("Infinite loop : ");
      for(String hierPackageName : packagesHierarchy) {
        builder.append(hierPackageName);
        builder.append(" <- ");
      }
      addNewIssue("CNE_05400", packageFile, builder.toString());
    }
    else {
      packagesHierarchy.push(packageName);
      for (VhdlPackage vhdlPackage : allVhdlPackages) {
        String visitedPackageName = vhdlPackage.getPackageName();
        InputFile visitedPackageFile = vhdlPackage.getPackageFile();
        if (!visitedPackageName.equals(packageName)) {
          for (String usedPackageName : vhdlPackage.getUsedPackages()) {
            if (usedPackageName.equals(packageName)) {
              level++;
              Integer cne5400Limit = null;
              String cne5400Relation = null;
              if (cne5400!=null) {
                cne5400Limit = Integer.parseInt(cne5400.param("Limit"));
                cne5400Relation = cne5400.param("Relation");
              }
              if (cne5400Limit!=null && !intParamRelation(cne5400Relation, level, cne5400Limit)) {
                StringBuilder builder = new StringBuilder("Too many nested packages : ");
                for(String hierPackageName : packagesHierarchy) {
                  builder.append(hierPackageName);
                  builder.append(" <- ");
                }
                builder.append(visitedPackageName);
                addNewIssue("CNE_05400", packageFile, builder.toString());
              }
              checkVhdlPackage (visitedPackageName, level, visitedPackageFile);
            }
          }
        }
      }
      packagesHierarchy.pop();
    }
  }

  private void checkJavaRules(InputFile inputFile) {
    if (inputFile!=null) {
      String fileName = inputFile.filename();
      boolean inTopFile = false;
      boolean inPackageFile = false;
      VhdlPackage vhdlPackage = new VhdlPackage("");
      boolean cne300Issue = false;
      String std600Regex = null;
      if (std600!=null) {
        std600Regex = std600.param("Format");
      }     
      if (std600Regex!=null && !fileName.endsWith(std600Regex)) {
        addNewIssue("STD_00600", inputFile, "All source files should have the same extension");
      }
      if (top.length()>0 && fileName.startsWith(top)) {
        inTopFile = true;
      }
      File sourceFile = new File(inputFile.uri());
      try (FileReader fReader = new FileReader(sourceFile)) {

        BufferedReader bufRead = new BufferedReader(fReader);
        String currentLine = null;
        int lineNumber=0;
        int commentedLines = 0;
        boolean inBlockComment=false;
        boolean inHeader=true;
        boolean arrayDeclaration = false;
        boolean stdDeclaration = false;
        boolean packageDeclaration = false;
        boolean useClause1 = false;
        boolean useClause2 = false;

        // Initialize header-related rules

        boolean std2200issue = true;
        String std2200Regex = null;
        if (std2200!=null) {
          String format = std2200.param("Format");
          std2200Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean std2300issue = true;
        String std2300Regex = null;
        if (std2300!=null) {
          String format = std2300.param("Format");
          std2300Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean std2400issue = true;
        String std2400Regex = null;
        if (std2400!=null) {
          String format = std2400.param("Format");
          std2400Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean std2500issue = true;
        String std2500Regex = null;
        if (std2300!=null) {
          String format = std2500.param("Format");
          std2500Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne2800issue = true;
        String cne2800Regex = null;
        if (cne2800!=null) {
          String format = cne2800.param("Format");
          cne2800Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne2900issue = true;
        String cne2900Regex = null;
        if (cne2900!=null) {
          String format = cne2900.param("Format");
          cne2900Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3000issue = true;
        String cne3000Regex = null;
        if (cne3000!=null) {
          String format = cne3000.param("Format");
          cne3000Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3100issue = true;
        String cne3100Regex = null;
        if (cne3100!=null) {
          String format = cne3100.param("Format");
          cne3100Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3200issue = true;
        String cne3200Regex = null;
        if (cne3200!=null) {
          String format = cne3200.param("Format");
          cne3200Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3300issue = true;
        String cne3300Regex = null;
        if (cne3300!=null) {
          String format = cne3300.param("Format");
          cne3300Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3400issue = true;
        String cne3400Regex = null;
        if (cne3400!=null) {
          String format = cne3400.param("Format");
          cne3400Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3500issue = true;
        String cne3500Regex = null;
        if (cne3500!=null) {
          String format = cne3500.param("Format");
          cne3500Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3600issue = true;
        String cne3600Regex = null;
        if (cne3600!=null) {
          String format = cne3600.param("Format");
          cne3600Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3700issue = true;
        String cne3700Regex = null;
        if (cne3700!=null) {
          String format = cne3700.param("Format");
          cne3700Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3800issue = true;
        String cne3800Regex = null;
        if (cne3800!=null) {
          String format = cne3800.param("Format");
          cne3800Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne3900issue = true;
        String cne3900Regex = null;
        if (cne3900!=null) {
          String format = cne3900.param("Format");
          cne3900Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne4000issue = true;
        String cne4000Regex = null;
        if (cne4000!=null) {
          String format = cne4000.param("Format");
          cne4000Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne4100issue = true;
        String cne4100Regex = null;
        if (cne4100!=null) {
          String format = cne4100.param("Format");
          cne4100Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne4200issue = true;
        String cne4200Regex = null;
        if (cne4200!=null) {
          String format = cne4200.param("Format");
          cne4200Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne4300issue = true;
        String cne4300Regex = null;
        if (cne4300!=null) {
          String format = cne4300.param("Format");
          cne4300Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        boolean cne4400issue = true;
        String cne4400Regex = null;
        if (cne4400!=null) {
          String format = cne4400.param("Format");
          cne4400Regex = YosysGhdlSensor.stringParamToRegex(format);
        }

        // End of header-related rules

        Integer cne2700Limit = null;
        String cne2700Relation = null;
        if (cne2700!=null) {
          cne2700Limit = Integer.parseInt(cne2700.param("Limit"));
          cne2700Relation = cne2700.param("Relation");
        }


        StringBuilder commentChain = new StringBuilder();
        LanguageResult result = null;
        int commentChainStartLine = 1;
        LanguageDetector detector=null;
        String lastLine = null;
        
        while ((currentLine = bufRead.readLine()) != null) { // Browse file line by line
          
          lineNumber++;
          if (inBlockComment) {
            commentedLines++;
          }

          if (std2700!=null) { // Language check
            detector = new OptimaizeLangDetector().loadModels();

            if (!inBlockComment && !inHeader) {

              String lineBeforeComment;
              String lineAfterComment;
              int startCommmentIndex = currentLine.indexOf("--");
              int startBlockCommmentIndex = currentLine.indexOf("/*");
              int startOfComment;
              if (startCommmentIndex!=-1 && startBlockCommmentIndex==-1) {
                startOfComment = startCommmentIndex;
              }
              else if (startCommmentIndex==-1 && startBlockCommmentIndex!=-1) {
                startOfComment = startBlockCommmentIndex;
              }
              else {
                startOfComment = Math.min(currentLine.indexOf("--"),currentLine.indexOf("/*"));
              }
              boolean commentChainExisted = (commentChain.toString().length()>0);
              if (startOfComment==-1) {
                lineBeforeComment = currentLine;
                lineAfterComment = "";
              }
              else
              {
                if (!commentChainExisted) {
                  commentChainStartLine = lineNumber;
                }
                lineBeforeComment = currentLine.subSequence(0, startOfComment).toString();
                lineAfterComment = currentLine.subSequence(startOfComment+1, currentLine.length()).toString();
              }

              
              if (lineBeforeComment.length()>0) { // End of comment chain (if there was one)
                if (commentChainExisted) {
                  result = detector.detect(commentChain.toString());
                  if (result.getLanguage().length()>0 && !result.getLanguage().equalsIgnoreCase(defaultLanguage) && result.isReasonablyCertain() && commentChain.toString().trim().split("\\s+").length>10) {
                    if (commentChainStartLine!=lineNumber) {
                      addNewIssue("STD_02700", inputFile, commentChainStartLine, 0, lineNumber, 0, "English language should be preferred in comments : "+result.getLanguage());
                    } 
                    else {
                      addNewIssue("STD_02700", inputFile, lineNumber, "English language should be preferred in comments : "+result.getLanguage());
                    }                    
                  }
                }
                if (lineAfterComment.length()!=0) {
                  commentChainStartLine = lineNumber;
                }
                commentChain = new StringBuilder(lineAfterComment);
              }
              else if (lineAfterComment.length()!=0) {
                commentChain.append(lineAfterComment).append(" ");
              }
              
            }

            else { // Header and block comments

              String lineBeforeCode;
              String lineAfterCode;
              int startOfCode =currentLine.indexOf("*/");
              if (startOfCode==-1) {
                lineBeforeCode = currentLine;
                lineAfterCode = "";
              }
              else
              {
                lineBeforeCode = currentLine.subSequence(0, startOfCode).toString();
                lineAfterCode = currentLine.subSequence(startOfCode+1, currentLine.length()).toString();
              }
              if (lineBeforeCode.length()>0 && !(commentChain.toString().length()>0)) {
                commentChainStartLine = lineNumber;
              }
              if (lineBeforeCode.length()!=0) {
                commentChain.append(lineBeforeCode).append(" ");
              }
              if (lineAfterCode.length()>0) { // End of comments chain
                result = detector.detect(commentChain.toString());
                if (result.getLanguage().length()>0 && !result.getLanguage().equalsIgnoreCase(defaultLanguage) && result.isReasonablyCertain() && commentChain.toString().trim().split("\\s+").length>10) {
                  if (commentChainStartLine!=lineNumber) {
                    addNewIssue("STD_02700", inputFile, commentChainStartLine, 0, lineNumber, 0, "English language should be preferred in comments : "+result.getLanguage());
                  }
                  else {
                    addNewIssue("STD_02700", inputFile, lineNumber, "English language should be preferred in comments : "+result.getLanguage());
                  }
                }
                commentChain = new StringBuilder();
              }
              // Too many false positives
              /*result = detector.detect(lineAfterCode);
              if (lineAfterCode.length()>0 && result.getLanguage().length()>0 && !result.getLanguage().equalsIgnoreCase(defaultLanguage)) {
                addNewIssue("STD_02700", inputFile, lineNumber, "English language should be preferred in source code");
              }*/
            }
          }

          if (inHeader) {
            
            if (std2200Regex!=null && (currentLine.matches(std2200Regex))) {
              std2200issue = false;   
            }

            if (std2300Regex!=null && (currentLine.matches(std2300Regex))) {
              std2300issue = false;   
            }

            if (std2400Regex!=null && (currentLine.matches(std2400Regex))) {
              std2400issue = false;   
            }

            if (std2500Regex!=null && (currentLine.matches(std2500Regex))) {
              std2500issue = false;   
            }

            if (cne2800Regex!=null && (currentLine.matches(cne2800Regex))) {
              cne2800issue = false;   
            }

            if (cne2900Regex!=null && (currentLine.matches(cne2900Regex))) {
              cne2900issue = false;   
            }

            if (cne3000Regex!=null && (currentLine.matches(cne3000Regex))) {
              cne3000issue = false;   
            }

            if (cne3100Regex!=null && (currentLine.matches(cne3100Regex))) {
              cne3100issue = false;   
            }

            if (cne3200Regex!=null && (currentLine.matches(cne3200Regex))) {
              cne3200issue = false;   
            }

            if (cne3300Regex!=null && (currentLine.matches(cne3300Regex))) {
              cne3300issue = false;   
            }

            if (cne3400Regex!=null && (currentLine.matches(cne3400Regex))) {
              cne3400issue = false;   
            }

            if (cne3500Regex!=null && (currentLine.matches(cne3500Regex))) {
              cne3500issue = false;   
            }

            if (cne3600Regex!=null && (currentLine.matches(cne3600Regex))) {
              cne3600issue = false;   
            }

            if (cne3700Regex!=null && (currentLine.matches(cne3700Regex))) {
              cne3700issue = false;   
            }

            if (cne3800Regex!=null && (currentLine.matches(cne3800Regex))) {
              cne3800issue = false;   
            }

            if (cne3900Regex!=null && (currentLine.matches(cne3900Regex))) {
              cne3900issue = false;   
            }

            if (cne4000Regex!=null && (currentLine.matches(cne4000Regex))) {
              cne4000issue = false;   
            }

            if (cne4100Regex!=null && (currentLine.matches(cne4100Regex))) {
              cne4100issue = false;   
            }

            if (cne4200Regex!=null && (currentLine.matches(cne4200Regex))) {
              cne4200issue = false;   
            }
            
            if (cne4300Regex!=null && (currentLine.matches(cne4300Regex))) {
              cne4300issue = false;   
            }

            if (cne4400Regex!=null && (currentLine.matches(cne4400Regex))) {
              cne4400issue = false;   
            }
            
          }
          
          boolean inComment=false;
          Scanner input = new Scanner(currentLine);
          input.useDelimiter("((\\p{javaWhitespace})|;|,|\\.|\\(|\\))+");
          boolean emptyLine = true;
          while (input.hasNext()) {

            String currentToken = input.next();

            if (inHeader && inComment) {
              // Browse header line word by word
            }
            else if (!inComment) { // Browse uncommented line
              emptyLine=false;
              if (inHeader && currentToken.equalsIgnoreCase("library")) {
                inHeader = false;
                if (detector!=null) {          
                  result = detector.detect(commentChain.toString());
                  if (result.getLanguage().length()>0 && !result.getLanguage().equalsIgnoreCase(defaultLanguage) && result.isReasonablyCertain() && commentChain.toString().trim().split("\\s+").length>10) {
                    if (commentChainStartLine!=lineNumber) {
                      addNewIssue("STD_02700", inputFile, commentChainStartLine, 0, lineNumber, 0, "English language should be preferred in comments : code "+result.getLanguage());
                    }
                    else {
                      addNewIssue("STD_02700", inputFile, lineNumber, "English language should be preferred in comments : code "+result.getLanguage());
                    }
                  }
                }
                commentChain = new StringBuilder();
              }
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
                if (packageDeclaration) {
                  packageDeclaration = false;
                  vhdlPackage.setPackageName(currentToken.toLowerCase());
                }
                if (useClause1) {
                  useClause1 = false;
                  useClause2 = true;
                  if (!notAPackageName.contains(currentToken.toLowerCase())) {
                    vhdlPackage.getUsedPackages().add(currentToken.toLowerCase());
                  }
                }
                if (useClause2) {
                  useClause2 = false;
                  if (!notAPackageName.contains(currentToken.toLowerCase())) {
                    vhdlPackage.getUsedPackages().add(currentToken.toLowerCase());
                  }
                }
                if (std6900!=null && (currentToken.equalsIgnoreCase("procedure") || currentToken.equalsIgnoreCase("function"))) {
                  addNewIssue("STD_06900", inputFile, lineNumber, "Procedures and functions should not be used in RTL design");
                }
                else if (std3300!=null && currentToken.equalsIgnoreCase("buffer")) {
                  addNewIssue("STD_03300", inputFile, lineNumber, "Buffer port type is not recommended for synthesis");
                }
                else if (std6700!=null && currentToken.equalsIgnoreCase("wait")) {
                  addNewIssue("STD_06700", inputFile, lineNumber, "Wait instruction is not synthesizable");
                }
                else if (std2600!=null && (currentToken.equalsIgnoreCase("std_logic_arith") || currentToken.equalsIgnoreCase("std_logic_signed") || currentToken.equalsIgnoreCase("std_logic_unsigned"))) {
                  addNewIssue("STD_02600", inputFile, lineNumber, "\"std_logic_arith\", \"std_logic_signed\" and \"std_logic_unsigned\" libraries are not standardized and should not be used");   
                }
                else if (!cne300Issue && cne300!=null && inTopFile && currentToken.equalsIgnoreCase("entity")) {
                  cne300Issue = true;
                  addNewIssue("CNE_00300", inputFile, lineNumber, "Check that signal names do not contain FPGA/ASIC pin number value");
                }
                else if (std6000!=null && currentToken.equalsIgnoreCase("array")) {
                  arrayDeclaration = true;   
                }
                else if (std6100!=null && currentToken.equalsIgnoreCase("std_logic_vector")) {
                  stdDeclaration = true;   
                }
                else if (currentToken.equalsIgnoreCase("to")) {
                  arrayDeclaration = false;
                  if (stdDeclaration) {
                    stdDeclaration = false;
                    addNewIssue("STD_06100", inputFile, lineNumber, "Decreasing index should be preferred when declaring std_logic_vector");
                  }
                }
                else if (currentToken.equalsIgnoreCase("downto")) {
                  stdDeclaration = false;
                  if (arrayDeclaration) {
                    arrayDeclaration = false;
                    addNewIssue("STD_06000", inputFile, lineNumber, "Increasing index should be preferred when declaring array");
                  }
                }
                else if (!inTopFile && std5400!=null && (currentToken.equalsIgnoreCase("\'Z\'") || currentToken.equalsIgnoreCase("Z...Z"))) {
                  addNewIssue("STD_05400", inputFile, lineNumber, "Internal tristates should be avoided");
                }
                else if (cne5400!=null && currentToken.equalsIgnoreCase("package")) {
                  inPackageFile = true;
                  packageDeclaration = true;
                  vhdlPackage.setPackageFile(inputFile);
                }
                else if (cne5400!=null && inPackageFile && currentToken.equalsIgnoreCase("use")) {
                  useClause1 = true;   
                }
              }             
            }
          }

          if (!emptyLine && std2000!=null && currentLine.startsWith("\t")) { // Check indentation
            addNewIssue("STD_02000", inputFile, lineNumber, "Tabulation characters should be avoided for indentation");
          }

          lastLine = currentLine;
          input.close();
        }
        bufRead.close();

        if (detector!=null) {          
          result = detector.detect(commentChain.toString());
          if (result.getLanguage().length()>0 && !result.getLanguage().equalsIgnoreCase(defaultLanguage) && result.isReasonablyCertain() && commentChain.toString().trim().split("\\s+").length>10) {
            if (commentChainStartLine!=lineNumber && !lastLine.equals("")) {
              addNewIssue("STD_02700", inputFile, commentChainStartLine, 0, lineNumber, lastLine.length()-1, "English language should be preferred in comments : code "+result.getLanguage());
            }
            else {
              addNewIssue("STD_02700", inputFile, lineNumber, "English language should be preferred in comments : code "+result.getLanguage());
            }
          }
        }

        totalComments+=commentedLines;
        context.<Integer>newMeasure().forMetric(CustomMetrics.COMMENT_LINES_STD_02800).on(inputFile).withValue(commentedLines).save(); // Count comments
        Integer std2800Limit = null;
        String std2800Relation = null;
        if (std2800!=null) {
          std2800Limit = Integer.parseInt(std2800.param("Limit"));
          std2800Relation = std2800.param("Relation");
        }
        if (lineNumber!=0 && std2800Limit!=null && !intParamRelation(std2800Relation, (commentedLines*100)/lineNumber, std2800Limit)) { // Check if maximum percent of commented lines is exceeded
          addNewIssue("STD_02800", inputFile, "Comments percentage should be : " + std2800Relation +std2800Limit+". Actual value : "+ (commentedLines*100)/lineNumber);
        }

        if (cne2700Limit!=null && !intParamRelation(cne2700Relation, lineNumber, cne2700Limit)) { // Check number of lines in file
          addNewIssue("CNE_02700", inputFile, "File size should be : " + cne2700Relation +cne2700Limit+". Actual value : "+ lineNumber);
        }

        // Add issues related to missing info in header
        if (std2200Regex!=null && std2200issue) {
          addNewIssue("STD_02200", inputFile, "File header should include version control");
        }
        if (std2300Regex!=null && std2300issue) {
          addNewIssue("STD_02300", inputFile, "File header should include copyright information");
        }
        if (std2400Regex!=null && std2400issue) {
          addNewIssue("STD_02400", inputFile, "File header should include creation information");
        }
        if (std2500Regex!=null && std2500issue) {
          addNewIssue("STD_02500", inputFile, "File header should include functional information");
        }
        if (cne2800Regex!=null && cne2800issue) {
          addNewIssue("CNE_02800", inputFile, "File header should include software VHDL generator");
        }
        if (cne2900Regex!=null && cne2900issue) {
          addNewIssue("CNE_02900", inputFile, "File header should include file name");
        }
        if (cne3000Regex!=null && cne3000issue) {
          addNewIssue("CNE_03000", inputFile, "File header should include creation date");
        }
        if (cne3100Regex!=null && cne3100issue) {
          addNewIssue("CNE_03100", inputFile, "File header should include project name");
        }
        if (cne3200Regex!=null && cne3200issue) {
          addNewIssue("CNE_03200", inputFile, "File header should include author");
        }
        if (cne3300Regex!=null && cne3300issue) {
          addNewIssue("CNE_03300", inputFile, "File header should include functional description");
        }
        if (cne3400Regex!=null && cne3400issue) {
          addNewIssue("CNE_03400", inputFile, "File header should include naming convention");
        }
        if (cne3500Regex!=null && cne3500issue) {
          addNewIssue("CNE_03500", inputFile, "File header should include functional limitation");
        }
        if (cne3600Regex!=null && cne3600issue) {
          addNewIssue("CNE_03600", inputFile, "File header should include current version number");
        }
        if (cne3700Regex!=null && cne3700issue) {
          addNewIssue("CNE_03700", inputFile, "File header should include author of modification(s)");
        }
        if (cne3800Regex!=null && cne3800issue) {
          addNewIssue("CNE_03800", inputFile, "File header should include version history");
        }
        if (cne3900Regex!=null && cne3900issue) {
          addNewIssue("CNE_03900", inputFile, "File header should include reason(s) of modification(s)");
        }
        if (cne4000Regex!=null && cne4000issue) {
          addNewIssue("CNE_04000", inputFile, "File header should include functional impact(s) of modifications");
        }
        if (cne4100Regex!=null && cne4100issue) {
          addNewIssue("CNE_04100", inputFile, "File header should include functional description of modifications");
        }
        if (cne4200Regex!=null && cne4200issue) {
          addNewIssue("CNE_04200", inputFile, "File header should include applicable license");
        }
        if (cne4300Regex!=null && cne4300issue) {
          addNewIssue("CNE_04300", inputFile, "File header should include company coding");
        }
        if (cne4400Regex!=null && cne4400issue) {
          addNewIssue("CNE_04400", inputFile, "File header should include company owner of code");
        }

        // Add package-related informations
        if (!vhdlPackage.getPackageName().equals("")) {
          allVhdlPackages.add(vhdlPackage);
        }

        fReader.close();
        
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

  private void addNewIssue(String ruleId, InputFile inputFile, int startLine, int startLineOffset, int endLine, int endLineOffset, String msg) {
    NewIssue ni = context.newIssue()
      .forRule(RuleKey.of(repo,ruleId));
    NewIssueLocation issueLocation = ni.newLocation()
      .on(inputFile)  
      .at(inputFile.newRange(startLine, startLineOffset, endLine, endLineOffset))
      .message(msg);
    ni.at(issueLocation);
    ni.save(); 
  }

  private void addNewIssue(String ruleId, InputFile inputFile, String msg) {
    NewIssue ni = context.newIssue()
      .forRule(RuleKey.of(repo,ruleId));
    NewIssueLocation issueLocation = ni.newLocation()
      .on(inputFile)
      .message(msg);
    ni.at(issueLocation);
    ni.save(); 
  }
  
  private static boolean intParamRelation(String relation, int value, int limit) {
    boolean result;
    switch (relation) {
    case "=":
      result = value == limit;
    break;
    case "==":
      result = value == limit;
    break;
    case "<":
      result = value < limit;
    break;
    case "<=":
      result = value <= limit;
    break;
    case ">":
      result = value > limit;
    break;
    case ">=":
      result = value >= limit;
    break;
    default:
      LOG.warn("Could not parse relation : "+relation);
      result = value == limit;
    break;
    }
    return result;
  }

}