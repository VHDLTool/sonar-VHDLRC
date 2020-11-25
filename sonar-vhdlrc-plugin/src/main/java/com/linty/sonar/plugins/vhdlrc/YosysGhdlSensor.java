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

package com.linty.sonar.plugins.vhdlrc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.apache.commons.io.FilenameUtils;
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
import com.linty.sonar.zamia.BuildPathMaker;

public class YosysGhdlSensor implements Sensor {

  public static final String SCANNER_HOME_KEY ="sonar.vhdlrc.scanner.home";
  public static final String      PROJECT_DIR = "rc/Data/workspace/project";
  public static final String   REPORTING_PATH = PROJECT_DIR + "/rule_checker/reporting/rule";
  public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
  public static final String RC_SYNTH_REPORT_PATH = IS_WINDOWS ? ".\\report_" : "./report_";
  public static final String SOURCES_DIR = "vhdl";
  public static final String REPORTING_RULE = "rule_checker/reporting/rule";
  private static final String repo="vhdlrc-repository";
  private static final String fexplicit=" -fexplicit";
  private static final String fsynopsys=" -fsynopsys";
  private static final String yosysFsmCmd1="yosys -m ghdl -p \"ghdl";
  private static final String yosysFsmCmd2="; setattr -set fsm_encoding \\\"auto\\\"; fsm -norecode -nomap -export\"";
  private static final Logger LOG = Loggers.get(YosysGhdlSensor.class);
  private String fsmRegex;
  private SensorContext context;
  private FilePredicates predicates;
  private String baseProjDir;
  private String workdir;
  private String topFile="";
  private int topLineNumber=0;


  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
    .name("Import of issues using yosys-ghdl analysis")
    .onlyOnLanguage(Vhdl.KEY)
    .name("yosysGhdlSensor")
    .onlyWhenConfiguration(conf -> conf.hasKey(SCANNER_HOME_KEY));
  }

  @Override
  public void execute(SensorContext context) {
    this.context=context;
    this.predicates = context.fileSystem().predicates();
    Configuration config = context.config();
    baseProjDir=System.getProperty("user.dir");
    String top=BuildPathMaker.getTopEntities(config);
    workdir = null;

    if(BuildPathMaker.getAutoexec(config)) {			
      String fileList=BuildPathMaker.getFileList(config);
      String rcSynth = BuildPathMaker.getRcSynthPath(config);
      String ghdlParams=((BuildPathMaker.getFexplicit(config)) ? fexplicit : "")+((BuildPathMaker.getFsynopsys(config)) ? fsynopsys : "");	
      String yosysFsmCmd = yosysFsmCmd1+ghdlParams+" "+top+" "+yosysFsmCmd2;
      workdir=BuildPathMaker.getWorkdir(config);
      if(IS_WINDOWS) {
        System.out.println(executeCommand(new String[] {"cmd.exe","/c","ubuntu1804 run "+rcSynth+" "+top+" \""+ghdlParams+"\""+" \""+fileList+"\""})); // Still needs work
        //System.out.println(executeCommand(new String[] {"cmd.exe","/c","cd "+BuildPathMaker.getWorkdir(config)+"; ubuntu1804 run "+yosysFsmCmd}));
        try {
          Runtime.getRuntime().exec("cmd.exe /c cd "+workdir+"; ubuntu1804 run \"+yosysFsmCmd").waitFor();
        } catch (IOException | InterruptedException e) {
          LOG.warn("Ubuntu thread interrupted");
          Thread.currentThread().interrupt();
        }
      }
      else {
        String[] cmd = new String[] {"sh","-c","bash "+rcSynth+" "+top+" \""+ghdlParams+"\""+" \""+fileList+"\""};
        System.out.println(executeCommand(cmd));
        System.out.println(executeCommand(new String[] {"sh","-c","cd "+workdir+"; "+yosysFsmCmd}));
        System.out.println(executeCommand(new String[] {"sh","-c","cd "+workdir+"; "+yosysFsmCmd1+ghdlParams+" "+top+" ; select o:* -module "+top+"; dump -o outputlist;\""}));
        List<String> outputNames = getOutputs(workdir+"/outputlist");
        StringBuilder builder= new StringBuilder();
        for (String outputName:outputNames)
          builder.append("select "+top+"/"+outputName+" %xe5; tee -q -o "+outputName+".statlog stat; select -clear; ");
        String yosysCheckOutputs=builder.toString();
        System.out.println(executeCommand(new String[] {"sh","-c","cd "+workdir+"; "+yosysFsmCmd1+ghdlParams+" "+top+" ; synth; "+yosysCheckOutputs+"\""}));

        //outputNames.forEach(o->System.out.println(o));
      }
    }

    try { // Get top entity information (file path and line number) from cf file (ie work-obj93.cf) generated with ghdl -a
      Files.walk(Paths.get(context.fileSystem().baseDir().getAbsolutePath())).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(".cf")).forEach(o1->getTopLocation(o1,top));    
    } catch (IOException e) {
      LOG.warn("Could not find any .cf file in build directory");
    }
    
    Set<String> outputs=new HashSet<>();
    
    try { // Parse files containing dumped "stat" command results. If "Number of cells"/=0 then an issue is created (rule STD_5200)
      Files.walk(Paths.get(context.fileSystem().baseDir().getAbsolutePath())).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(".statlog")).forEach(o1->outputs.add(parseStatLog(o1)));    
    } catch (IOException e) {
      LOG.warn("Could not find any .statlog file in build directory");
    }

    outputs.remove("");
    findOutputs(Paths.get(workdir+"/"+topFile),topLineNumber,outputs); // Add issues on output ports declarations (rule STD_5200)
    
    try { // Parse generated .kiss2 files and add the corresponding issues
      Files.walk(Paths.get(context.fileSystem().baseDir().getAbsolutePath())).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(".kiss2")).forEach(o1->addYosysIssues(o1)); //could use workdir
    } catch (IOException e) {
      LOG.warn("Could not find any .kiss2 file in build directory");
    }

  }


  private void addYosysIssues(Path kiss2Path) {
    fsmRegex = null;
    ActiveRule cne_02000 = context.activeRules().findByInternalKey(repo, "CNE_02000");
    if (cne_02000!=null) {
      String format = cne_02000.param("Format");
      if(format!=null) {
        if(!format.startsWith("*"))
          format="^"+format;
        fsmRegex=format.trim().replace("*", ".*");
      }
    }


    String[] kiss2FileName =kiss2Path.getFileName().toString().split("-\\$fsm\\$.");
    String vhdlFilePath=kiss2Path.toString().split("-\\$fsm")[0];	
    String sourceFileName=vhdlFilePath.substring(vhdlFilePath.lastIndexOf("/"))+".vhd";

    Optional<Path> oPath = Optional.empty();
    try {
      oPath = Files.walk(Paths.get(baseProjDir)).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(sourceFileName)||o.toString().toLowerCase().endsWith(sourceFileName+"l")).findFirst();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    InputFile inputFile=null;
    File file=null;
    if(oPath.isPresent()) {
      inputFile = context.fileSystem().inputFile(predicates.hasPath(oPath.get().toString()));
      file = new File(oPath.get().toString());
    }

    if (inputFile!=null) {
      String stateName=kiss2FileName[1].split("\\$")[0];			
      String stateType="";
      int sigDecLine=1;
      try (FileReader fReader = new FileReader(file)){
        BufferedReader bufRead = new BufferedReader(fReader);
        String currentLine = null;
        int lineNumber=0;
        boolean foundStateType=false;
        while ((currentLine = bufRead.readLine()) != null && !foundStateType) {    												
          lineNumber++;
          Scanner input = new Scanner(currentLine);
          boolean sigDec=false;
          boolean sigType=false;
          while(input.hasNext()&&!foundStateType) {
            String currentToken = input.next();
            if (currentToken.equalsIgnoreCase("signal"))
              sigDec=true;
            else if(sigDec&&currentToken.equalsIgnoreCase(stateName))
              sigDecLine=lineNumber;								
            else if(sigDec&&currentToken.equalsIgnoreCase(":")) {
              sigDec=false;
              sigType=true;
            }
            else if(sigType) {
              stateType=currentToken.toLowerCase();
              foundStateType=true;
            }
          }
          input.close();
        }
      } catch (IOException e) {
        LOG.warn("Could not read source file");
      }


      if(fsmRegex!=null && !stateName.matches(fsmRegex)) 
        addNewIssue("CNE_02000",inputFile,sigDecLine,"State machine signal "+stateName+" is miswritten.");				
      if(context.activeRules().findByInternalKey(repo, "STD_03900")!=null && (stateType.startsWith("std_")||(stateType.startsWith("ieee_"))))
        addNewIssue("STD_03900",inputFile,sigDecLine,"State machine signal "+stateName+" uses wrong type.");

    }

    kiss2Path.toFile().deleteOnExit();		
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

  private List<String> getOutputs(String path){
    List<String> result=new ArrayList<>();
    File file=new File(path);
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      boolean connect=false;
      while ((currentLine = bufRead.readLine()) != null && !connect) {                           
        Scanner input = new Scanner(currentLine);
        boolean wire=false;
        boolean output=false;
        while(input.hasNext()&&!connect) {
          String currentToken = input.next();
          if (currentToken.equalsIgnoreCase("wire"))
            wire=true;
          else if(currentToken.equalsIgnoreCase("output"))
            output=true;                
          else if(wire&&output&&currentToken.startsWith("\\"))
            result.add(currentToken.substring(1));
          else if(currentToken.equalsIgnoreCase("connect"))
            connect=true; 
        }
        wire=false;
        output=false;
        input.close();
      }
    } catch (IOException e) {
      LOG.warn("Could not read source file");
    }
    return result;
  }

  private void getTopLocation(Path path, String top){
    File file=path.toFile();
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      String currentFile="";
      boolean finished=false;
      while ((currentLine = bufRead.readLine()) != null && !finished) {                           
        Scanner input = new Scanner(currentLine);
        boolean fileLine=false;
        boolean entityLine=false;
        boolean architectureLine=false;
        while(input.hasNext()&&!architectureLine && !finished) {
          String currentToken = input.next();
          if (currentToken.equalsIgnoreCase("architecture"))
            architectureLine=true;
          else if (currentToken.equalsIgnoreCase("file"))
            fileLine=true;
          else if(fileLine && (currentToken.endsWith(".vhd\"")||currentToken.endsWith(".vhdl\"")))
            currentFile=currentToken.substring(1,currentToken.length()-1);              
          else if(currentToken.equalsIgnoreCase("entity"))
            entityLine=true;
          else if(entityLine && currentToken.equalsIgnoreCase(top)) {
            topFile=currentFile; 
            while(input.hasNext() && !finished) {
              try {
                String next=input.next();
                if(next.endsWith("(") && next.length()>1)
                  next=next.substring(0,next.length()-1);
                topLineNumber=Integer.parseInt(next);
                finished=true;
              }
              catch(NumberFormatException e) {}              
            }
          }
        }
        fileLine=false;
        entityLine=false;
        input.close();
      }
    } catch (IOException e) {
      LOG.warn("Could not read source file");
    }
  }
  
  private String parseStatLog(Path path){
    File file=path.toFile();
    String output="";
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      boolean foundNumberOfCells=false;
      while ((currentLine = bufRead.readLine()) != null && !foundNumberOfCells) {                           
        Scanner input = new Scanner(currentLine);
        while(input.hasNext()&&!foundNumberOfCells) {
          String currentToken = input.next();
          if (currentToken.equalsIgnoreCase("Number") && input.hasNext() && input.next().equalsIgnoreCase("of") && input.hasNext() && input.next().startsWith("cells") && input.hasNext()) {
              try {
                if(Integer.parseInt(input.next())!=0)
                  output=FilenameUtils.removeExtension(path.getFileName().toString().toLowerCase());
                foundNumberOfCells=true;
              }
              catch(NumberFormatException e) {}               
          } 
        }
        input.close();
      }
    } catch (IOException e) {
      LOG.warn("Could not read source file");
    }
    return output;
  }
  
  private void findOutputs(Path path, int startLine, Set<String> outputs){
    File file=path.toFile();
    int currentLineNumber=0;
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      boolean inPortDecl=false;
      while ((currentLine = bufRead.readLine()) != null) {
        currentLineNumber++;
        if (currentLineNumber>=startLine) {
          Scanner input = new Scanner(currentLine);
          input.useDelimiter("((\\p{javaWhitespace})|;|,|:|\\.|\\(|\\))+");
          while(input.hasNext()) {
            String currentToken = input.next();
            if (currentToken.toLowerCase().startsWith("port")) {
              inPortDecl=true;
            }
            else if (inPortDecl && outputs.contains(currentToken.toLowerCase())) {
              InputFile inputFile = context.fileSystem().inputFile(predicates.hasPath(workdir+"/"+topFile));
              if(inputFile!=null)
                addNewIssue("STD_05200",inputFile,currentLineNumber,"Combinational outputs at top level should be avoided");
            }
          }
          input.close();
        }
      }
    } catch (IOException e) {
      LOG.warn("Could not read source file");
    }
  }


  private String executeCommand(String[] cmd) {
    StringBuffer theRun = new StringBuffer();
    try {
      Process process = Runtime.getRuntime().exec(cmd);

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));
      int read;
      char[] buffer = new char[4096];
      StringBuffer output = new StringBuffer();
      while ((read = reader.read(buffer)) > 0) {
        theRun = output.append(buffer, 0, read);
      }
      reader.close();
      process.waitFor();

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      LOG.warn("Command thread interrupted");
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
    return theRun.toString().trim();
  }

}
