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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;

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

import com.lintyservices.sonar.zamia.ActiveRuleLoader;
import com.lintyservices.sonar.zamia.BuildPathMaker;

public class YosysGhdlSensor implements Sensor {

  public static final String SCANNER_HOME_KEY ="sonar.vhdlrc.scanner.home";
  public static final String      PROJECT_DIR = "rc/Data/workspace/project";
  public static final String   REPORTING_PATH = PROJECT_DIR + "/rule_checker/reporting/rule";
  public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
  public static final String RC_SYNTH_REPORT_PATH = IS_WINDOWS ? ".\\report_" : "./report_";
  public static final String SOURCES_DIR = "vhdl";
  public static final String REPORTING_RULE = "rule_checker/reporting/rule";
  private static final String repo="vhdlrc-repository";
  private static final String yosysFsmDetectCmd="; tee -q -o fsmdetect fsm_detect; fsm_extract; fsm_export\"";
  private static final Logger LOG = Loggers.get(YosysGhdlSensor.class);
  private static String yosysGhdlCmd;
  private String fsmRegex;
  private SensorContext context;
  private FilePredicates predicates;
  private String baseProjDir;
  private String workdir;
  private String topFile="";
  private int topLineNumber=0;
  private Map<String, Set<String>> outputs = new HashMap<>(); //Output ports raising an issue according to rule STD_05200 (key = component name, value = port name)
  private Map<String, Set<String>> inputs = new HashMap<>(); //Input ports raising an issue according to rule STD_05100 (key = component name, value = port name)


  private ActiveRule cne2000;
  private ActiveRule cne4600;
  private ActiveRule std3900;
  private ActiveRule std5200;
  private ActiveRule std4000;
  private ActiveRule std5500;
  private ActiveRule std4400;
  private ActiveRule cne200;
  private ActiveRule std5100;
  private ActiveRule ghdlMessages;

  private int std5100Limit;


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

    if(executeCommand(new String[] {"bash","-c","yosys -H | grep -c ghdl"}).equals("1")) { // Check if ghdl module has been included in yosys at build time 
      yosysGhdlCmd="yosys -p \"ghdl";
    }
    else {
      yosysGhdlCmd="yosys -m ghdl -p \"ghdl";
    }

    if(ActiveRuleLoader.getEnableYosys()) {			
      String buildCmdParams=BuildPathMaker.getFileList(config);
      String rcSynth = BuildPathMaker.getRcSynthPath(config);
      String ghdlParams=" "+BuildPathMaker.getGhdlOptions(config);	
      String yosysFsmCmd = yosysGhdlCmd+ghdlParams+" "+top+" "+yosysFsmDetectCmd;
      workdir=BuildPathMaker.getWorkdir(config);

      cne2000 = context.activeRules().findByInternalKey(repo, "CNE_02000");
      cne4600 = context.activeRules().findByInternalKey(repo, "CNE_04600");
      std3900 = context.activeRules().findByInternalKey(repo, "STD_03900");
      std5200 = context.activeRules().findByInternalKey(repo, "STD_05200");
      std4000 = context.activeRules().findByInternalKey(repo, "STD_04000");
      std5500 = context.activeRules().findByInternalKey(repo, "STD_05500");
      std4400 = context.activeRules().findByInternalKey(repo, "STD_04400");
      cne200 = context.activeRules().findByInternalKey(repo, "CNE_00200");
      std5100 = context.activeRules().findByInternalKey(repo, "STD_05100");
      ghdlMessages = context.activeRules().findByInternalKey(repo, "CNE_06000");

      if(!IS_WINDOWS && (ghdlMessages !=null || std4000!=null || std5500!=null)) { // Analyse each file separately with ghdl -s command
        Iterable<InputFile> files = context.fileSystem().inputFiles(predicates.hasLanguage(Vhdl.KEY));
        files.forEach(file->checkGhdlLog(file, "ghdl -a \""+(new File(file.uri())).getPath()+"\"; ghdl --synth"));
      }

      if(!IS_WINDOWS && (cne2000!=null || std3900!=null || std5200!=null || cne4600!=null)) {        

        // Execute ghdl
        //System.out.println("bash "+rcSynth+" "+top+" \""+ghdlParams+"\""+" \""+buildCmdParams+"\"");
        System.out.println(executeCommand(new String[] {"bash","-c","bash "+rcSynth+" "+top+" \""+ghdlParams+"\""+" \""+buildCmdParams+"\""}));

        // Detect fsm with yosys and dump results in fsmdetect file
        System.out.println(executeCommand(new String[] {"bash","-c","cd "+workdir+"; "+yosysFsmCmd}));

        // Get the names of ignored fsms from fsmdetect file
        List<String> ignoredFsmNames=getIgnoredFsms();
        StringBuilder builder= new StringBuilder();
        for (String fsmName:ignoredFsmNames) {
          builder.append("setattr -set fsm_encoding \\\"auto\\\" "+fsmName+"; ");
        }

        // Generate kiss2 files
        String yosysFsmExtractExport=builder.toString()+"fsm_extract ; fsm_export";
        System.out.println(executeCommand(new String[] {"bash","-c","cd "+workdir+"; "+yosysGhdlCmd+ghdlParams+" "+top+" ; "+yosysFsmExtractExport+"\""}));

        //System.out.println(executeCommand(new String[] {"bash","-c","cd "+workdir+"; "+yosysGhdlCmd+ghdlParams+" "+top+" ; select o:* -module "+top+"; dump -o outputlist;\""}));
        // Generate input, output and clock lists
        System.out.println(executeCommand(new String[] {"bash","-c","cd "+workdir+"; "+yosysGhdlCmd+ghdlParams+" "+top+" ; synth ; tee -q -o outputlist select o:* -module "+top+" -list; select -clear ; tee -q -o inputlist select i:* -module "+top+" -list; select -clear ;  tee -q -o clocklist select t:* %x:+[C] t:* %d -list; select -clear ; tee -q -a clocklist select t:* %x:+[CLK] t:* %d -list\""}));

        // Parse output list file
        File listFile=new File(workdir+"/outputlist");
        try (FileReader fReader = new FileReader(listFile)){
          BufferedReader bufRead = new BufferedReader(fReader);
          String currentLine;
          while ((currentLine = bufRead.readLine()) != null) {
            String currentLineAsName = currentLine.replaceAll("/", "\\*");
            builder.append("select "+currentLine+" %cie*; tee -q -o "+currentLineAsName+".statlog stat; select -clear; ");
          }
        }catch (IOException e) {
          LOG.warn("Could not read outputlist file");
        }
        String yosysCheckOutputs=builder.toString();
        System.out.println(executeCommand(new String[] {"bash","-c","cd "+workdir+"; "+yosysGhdlCmd+ghdlParams+" "+top+" ; synth; "+yosysCheckOutputs+"\""}));
        executeCommand(new String[] {"bash", "-c", "ghdl --remove"});

        /*List<String> outputNames = getOutputs(workdir+"/outputlist");
        builder= new StringBuilder();
        for (String outputName:outputNames)
          builder.append("select "+top+"/"+outputName+" %cie*; tee -q -o "+outputName+".statlog stat; select -clear; ");*/

        // Parse input list file
        Map <String,Set<String>> inputs = new HashMap<>();

        listFile=new File(workdir+"/inputlist");
        builder= new StringBuilder();
        try{
          std5100Limit = Integer.parseInt(std5100.param("Limit"));
        }
        catch(NumberFormatException e) {
          LOG.warn("Could not parse rule STD_05100 limit as an integer");
        }
        try (FileReader fReader = new FileReader(listFile)) {
          BufferedReader bufRead = new BufferedReader(fReader);
          String currentLine;
          while ((currentLine = bufRead.readLine()) != null) {

            // Generate the hashmap <module name, set of the module's inputs>
            int lastSlash = currentLine.lastIndexOf("/");
            int len = currentLine.length();
            if (lastSlash!=-1) {
              String moduleName = currentLine.substring(0, lastSlash).toLowerCase();
              String inputName = currentLine.substring(lastSlash+1, len).toLowerCase();
              Set<String> clocksSet = inputs.get(moduleName);
              if (clocksSet!=null) {
                clocksSet.add(inputName);
              }
              else {
                inputs.put(moduleName, new HashSet<String>(Arrays.asList(inputName)));
              }
            }

            // Generate command line for rule STD_05100
            String currentLineAsName = currentLine.replaceAll("/", "\\*");
            builder.append("select "+currentLine+" %co"+(std5100Limit+1)+"; tee -q -o "+currentLineAsName+".istatlog stat; select -clear; select "+currentLine+" %co"+(std5100Limit+1)+"  t:*ff* %i; tee -q -a "+currentLineAsName+".istatlog stat; select -clear; ");

          }
        } catch (IOException e) {
          LOG.warn("Could not read inputlist file");
        }

        // Check rule STD_05100
        if (std5100!=null) {
          String yosysCheckInputs=builder.toString();
          System.out.println(executeCommand(new String[] {"bash","-c","cd "+workdir+"; "+yosysGhdlCmd+ghdlParams+" "+top+" ; "+yosysCheckInputs+"\""}));
        }

        // Parse clock list file
        Map <String,Set<String>> clocks = new HashMap<>();
        File clocklistfile=new File(workdir+"/clocklist");
        try (FileReader fReader = new FileReader(clocklistfile)){
          BufferedReader bufRead = new BufferedReader(fReader);
          String currentLine;
          while ((currentLine = bufRead.readLine()) != null) {
            int lastSlash = currentLine.lastIndexOf("/");
            int len = currentLine.length();
            if (lastSlash!=-1) {
              String fileName = currentLine.substring(0, lastSlash).toLowerCase();
              String clockName = currentLine.substring(lastSlash+1, len).toLowerCase();
              Set<String> clocksSet = clocks.get(fileName);
              if (clocksSet!=null) {
                clocksSet.add(clockName);
              }
              else {
                clocks.put(fileName, new HashSet<String>(Arrays.asList(clockName)));
              }
            }
          }

          boolean clocksInTopOnly = false;
          if (clocks.containsKey(top) && clocks.size()==1) {
            clocksInTopOnly = true;
          }

          for (Map.Entry<String,Set<String>> pair: clocks.entrySet()) {
            String fileName = pair.getKey();
            Set<String> clockNames = pair.getValue();
            Set<String> inputNames = inputs.get(fileName);
            if (inputNames!=null) {
              clockNames.removeAll(inputNames); // Exclude input ports
            }
            InputFile inputFile = context.fileSystem().inputFile(predicates.hasPath(fileName+".vhd"));
            if (inputFile == null) {
              inputFile = context.fileSystem().inputFile(predicates.hasPath(fileName+".vhdl"));
            }              
            if (inputFile != null ) {
              List<Integer> clockLines = getClockLocation(new File(inputFile.uri()), clockNames);
              boolean clocksInMultipleModules = false;
              if (clockLines.remove(Integer.valueOf(-1))) {
                clocksInMultipleModules = true;
              }
              boolean inTop = fileName.startsWith(top.toLowerCase());
              for (int clockLine : clockLines) {
                if (clocksInMultipleModules && std4400!=null) {
                  addNewIssue("STD_04400", inputFile, clockLine ,"All clocks should be declared in a dedicated module");
                }
                if (inTop) {
                  if (cne200!=null) {
                    addNewIssue("CNE_00200", inputFile, clockLine ,"The clock signal name does not contain the clock frequency value (Warning, to be verified manually)");
                  }
                  if (clocksInTopOnly && std4400!=null) {
                    addNewIssue("STD_04400", inputFile, clockLine ,"Clocks should be generated in a dedicated module and not in the top level entity");
                  }
                }
              }
            }  
          }
        }catch (IOException e) {
          LOG.warn("Could not read source file");
        }

      }

      try { // Get top entity information (file path and line number) from cf file (ie work-obj93.cf) generated with ghdl -a
        Files.walk(Paths.get(workdir)).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(".cf")).forEach(o1->getTopLocation(o1,top));    
      } catch (IOException e) {
        LOG.warn("Could not find any .cf file in build directory");
      }


      try { // Parse files containing dumped "stat" command results. If "Number of cells"/=0 then an issue should be raised (rule STD_05200)
        Files.walk(Paths.get(workdir)).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(".statlog")).forEach(o1->parseStatLog(o1));    
      } catch (IOException e) {
        LOG.warn("Could not find any .statlog file in build directory");
      }

      try { // Parse files containing dumped "stat" command results. If different numbers of cells are found, an issue should be raised (rule STD_05100)
        Files.walk(Paths.get(workdir)).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(".istatlog")).forEach(o1->parseIStatLog(o1));    
      } catch (IOException e) {
        LOG.warn("Could not find any .istatlog file in build directory");
      }

      outputs.remove("");
      inputs.remove("");
      findPortIssues(Paths.get(workdir+"/"+topFile),topLineNumber); // Add issues on top entity ports (rules STD_05100 and STD_05200)

      try { // Parse generated .kiss2 files and add the corresponding issues
        Files.walk(Paths.get(workdir)).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(".kiss2")).forEach(o1->addYosysIssues(o1)); //could use workdir
      } catch (IOException e) {
        LOG.warn("Could not find any .kiss2 file in build directory");
      }
    }
  }

  private void addYosysIssues(Path kiss2Path) {
    fsmRegex = null;
    if (cne2000!=null) {
      String format = cne2000.param("Format");
      fsmRegex = stringParamToRegex(format);
    }

    String[] kiss2FileName =kiss2Path.getFileName().toString().split("-\\$fsm\\$.");
    String vhdlFilePath=kiss2Path.toString().split("-\\$fsm")[0];	
    int  lastInd = vhdlFilePath.lastIndexOf("/");
    String sourceFileName=null;
    if (lastInd!=-1)
      sourceFileName=vhdlFilePath.substring(lastInd)+".vhd";
    final String innerSourceFileName=sourceFileName;
    Optional<Path> oPath = Optional.empty();
    if(sourceFileName!=null) {
      try {
        oPath = Files.walk(Paths.get(baseProjDir)).filter(Files::isRegularFile).filter(o->o.toString().toLowerCase().endsWith(innerSourceFileName)||o.toString().toLowerCase().endsWith(innerSourceFileName+"l")).findFirst();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
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
          input.useDelimiter("((\\p{javaWhitespace})|;|,|\\.|\\(|\\))+");
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

      if(cne4600!=null) {
        addNewIssue("CNE_04600",inputFile,sigDecLine,"Finite state machine.");
      }
      if(cne2000!=null && fsmRegex!=null && !stateName.matches(fsmRegex)) {
        addNewIssue("CNE_02000",inputFile,sigDecLine,"State machine signal "+stateName+" is miswritten.");
      }
      if(std3900!=null && (stateType.startsWith("std_")||(stateType.startsWith("ieee_")))) {
        addNewIssue("STD_03900",inputFile,sigDecLine,"State machine signal "+stateName+" uses wrong type.");
      }

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

  // Should be deprecated
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

  private List<String> getIgnoredFsms(){
    List<String> result=new ArrayList<>();
    File file=new File(workdir+"/fsmdetect");
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      while ((currentLine = bufRead.readLine()) != null) {                           
        Scanner input = new Scanner(currentLine);
        boolean not=false;
        boolean notmarking=false;
        while(input.hasNext()) {
          String currentToken = input.next();
          if (currentToken.equalsIgnoreCase("Not"))
            not=true;
          else if(not && currentToken.equalsIgnoreCase("marking"))
            notmarking=true;                
          else if(notmarking) {
            result.add(currentToken.replace('.', '/'));
            not=false; 
            notmarking=false;  
          }
          else {
            not=false; 
            notmarking=false;
          }
        }
        not=false;
        notmarking=false;
        input.close();
      }
    } catch (IOException e) {
      LOG.warn("Could not read fsmdetect file");
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

  private List<Integer> getClockLocation(File file, Set<String> clocks){
    List<Integer> clockLines = new ArrayList<>();
    List<String> allClocks = new ArrayList<>(clocks);
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      boolean finished = false;
      boolean portMap = false;
      boolean nextPortMap = false;
      boolean clockAssignedInPortMap = false;
      int currentLineNumber = 0;
      while ((currentLine = bufRead.readLine()) != null && !finished) {
        currentLineNumber++;
        Scanner input = new Scanner(currentLine);
        input.useDelimiter("((\\p{javaWhitespace})|;|,|:|\\.|\\(|\\))+");
        while(input.hasNext() && !finished) {
          String currentToken = input.next();

          if (portMap && currentToken.equalsIgnoreCase("=>") && input.hasNext() && allClocks.contains(input.next())) {
            if (nextPortMap) {
              clockLines.add(-1);  // Clock signals are assigned in different modules
              if (clocks.isEmpty()) {
                finished = true;
              }
            }
            clockAssignedInPortMap = true;
          }

          if (currentToken.equalsIgnoreCase("port") && input.hasNext() && input.next().equalsIgnoreCase("map")) {
            portMap = true;
            if (clockAssignedInPortMap) {
              nextPortMap = true;
            }
          }

          if (clocks.remove(currentToken.toLowerCase())) {            
            if (clocks.isEmpty() && std4400 == null) {
              finished = true;
            }
            clockLines.add(currentLineNumber);
          }

        }
        input.close();
      }
    } catch (IOException e) {
      LOG.warn("Could not read source file");
    }
    return clockLines;
  }

  private void parseStatLog(Path path){
    File file=path.toFile();
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
              int numCells = Integer.parseInt(input.next());
              foundNumberOfCells=true;
              if (numCells!=0) {
                String outputFullName = FilenameUtils.removeExtension(path.getFileName().toString()).toLowerCase();
                int delimitation = outputFullName.lastIndexOf("*");
                int len = outputFullName.length();
                String componentName = outputFullName.substring(0, delimitation);
                String portName = outputFullName.substring(delimitation+1, len);
                Set<String> ports = outputs.get(componentName);
                if (ports!=null) {
                  ports.add(portName);
                }
                else {
                  outputs.put(componentName, new HashSet<String>(Arrays.asList(portName)));
                }
              }
            }
            catch(Exception e) {}               
          } 
        }
        input.close();
      }
    } catch (IOException e) {
      LOG.warn("Could not read statlog file");
    }
  }

  private void parseIStatLog(Path path) {
    File file=path.toFile();
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      boolean foundNumberOfCells1=false;
      boolean foundNumberOfCells2=false;
      int numCells1 = -1;
      int numCells2 = -1;
      while ((currentLine = bufRead.readLine()) != null && !(foundNumberOfCells1 && foundNumberOfCells2)) {    
        Scanner input = new Scanner(currentLine);
        while (input.hasNext() && !(foundNumberOfCells1 && foundNumberOfCells2)) {
          String currentToken = input.next();
          if (currentToken.equalsIgnoreCase("Number") && input.hasNext() && input.next().equalsIgnoreCase("of") && input.hasNext() && input.next().startsWith("cells") && input.hasNext()) {
            try {
              if (!foundNumberOfCells1) {
                numCells1 = Integer.parseInt(input.next());
                foundNumberOfCells1=true;
              }
              else {
                numCells2 = Integer.parseInt(input.next());
                foundNumberOfCells2=true;
              }
              if (foundNumberOfCells1 && foundNumberOfCells2 && numCells1!=numCells2) {
                String inputFullName = FilenameUtils.removeExtension(path.getFileName().toString()).toLowerCase();
                int delimitation = inputFullName.lastIndexOf("*");
                int len = inputFullName.length();
                String componentName = inputFullName.substring(0, delimitation);
                String portName = inputFullName.substring(delimitation+1, len);
                Set<String> ports = inputs.get(componentName);
                if (ports!=null) {
                  ports.add(portName);
                }
                else {
                  inputs.put(componentName, new HashSet<String>(Arrays.asList(portName)));
                }               
              }
            }
            catch(Exception e) {}               
          } 
        }
        input.close();
      }
    } catch (IOException e) {
      LOG.warn("Could not read istatlog file");
    }
  }

  private void findPortIssues(Path path, int startLine){
    File file=path.toFile();
    int currentLineNumber=0;
    try (FileReader fReader = new FileReader(file)){
      BufferedReader bufRead = new BufferedReader(fReader);
      String currentLine = null;
      while ((currentLine = bufRead.readLine()) != null) {
        currentLineNumber++;
        if (currentLineNumber>=startLine) {
          Scanner input = new Scanner(currentLine);
          input.useDelimiter("((\\p{javaWhitespace})|;|,|:|\\.|\\(|\\))+");
          boolean inComment = false;
          while(!inComment && input.hasNext()) {
            String currentToken = input.next();
            if (currentToken.toLowerCase().startsWith("--")) {
              inComment = true;
            }
            else if ((currentToken.toLowerCase().startsWith("component") || currentToken.toLowerCase().startsWith("entity")) && input.hasNext()) {
              currentToken = input.next();
              if (outputs.containsKey(currentToken.toLowerCase())) {
                InputFile inputFile = context.fileSystem().inputFile(predicates.hasPath(workdir+"/"+topFile));
                if(inputFile!=null && std5200!=null) {
                  addNewIssue("STD_05200",inputFile,currentLineNumber,"Output signal "+collectionToString(outputs.get(currentToken.toLowerCase()))+" includes combinatorial elements in its output path.");
                }
                outputs.remove(currentToken.toLowerCase());
              }
              if (inputs.containsKey(currentToken.toLowerCase())) {
                InputFile inputFile = context.fileSystem().inputFile(predicates.hasPath(workdir+"/"+topFile));
                if(inputFile!=null && std5100!=null) {
                  addNewIssue("STD_05100",inputFile,currentLineNumber,"Asynchronous input signals should be synchronized with at least a "+std5100Limit+" Flip-Flops synchronizer : "+collectionToString(inputs.get(currentToken.toLowerCase())));
                }
                inputs.remove(currentToken.toLowerCase());
              }
            }
          }
          input.close();
        }
      }
    } catch (IOException e) {
      LOG.warn("Could not read source file");
    }
  }

  private void checkGhdlLog(InputFile file, String ghdlCmd) {

    String ghdlLog = executeCommand(new String[] {"bash", "-c", ghdlCmd}); // Analyze file
    BufferedReader reader = new BufferedReader(new StringReader(ghdlLog));
    String currentLine;
    reader = new BufferedReader(new StringReader(ghdlLog));
    try {
      while ((currentLine = reader.readLine()) != null) {
        String afterFilename = currentLine.substring(currentLine.lastIndexOf(".vhd") + 1);
        if (afterFilename.length()!=currentLine.length()) {
          int errorLine=-1;
          try {
            errorLine = Integer.parseInt(afterFilename.split(":")[1]);
          }
          catch (Exception e) {
            LOG.warn("Could not parse error line number in ghdl synthesis log");
          }
          String errorMsg = afterFilename.substring(afterFilename.lastIndexOf(":") + 1);
          if (errorMsg.length()!=currentLine.length()) {
            if (errorLine!=-1) {
              if (ghdlMessages!=null) {
                try {  
                  Integer.parseInt(errorMsg);  
                } catch(NumberFormatException e){
                  addNewIssue("CNE_06000", file, errorLine, errorMsg);
                }  
              }
              if (std4000!=null && (errorMsg.startsWith(" no choice for") || errorMsg.startsWith(" no choices for"))) {
                addNewIssue("STD_04000", file, errorLine, "All case statements should be addressed in the VHDL code : "+errorMsg);
              }
              else if (std5500!=null && errorMsg.startsWith(" latch infered for")) {
                addNewIssue("STD_05500", file, errorLine, "Latches should be avoided : "+(currentLine.substring(currentLine.lastIndexOf(" ")+1).replaceAll("\"", "")));
              }
            }
          }
        }
      }
    } catch (IOException e) {
      LOG.warn("Could not read ghdl synthesis log");
    }

    executeCommand(new String[] {"bash", "-c", "ghdl --remove"}); // Remove generated files

  }


  private String executeCommand(String[] cmd) {
    StringBuffer theRun = new StringBuffer();
    try {
      ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
      //LOG.info("Executing: "+pb.command().toString());
      Process process = pb.start();
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

  public static String stringParamToRegex (String param) {
    String regex = null;
    if(param!=null) {
      if(!param.startsWith("*")) {
        param="^"+param;
      }
      regex = param.trim().replace("*", ".*").replace(',', '|');
    }
    return regex;
  }

  public static String collectionToString (Collection<String> collec) {
    StringBuilder builder = new StringBuilder();
    boolean firstElement = true;
    for (String str : collec) {
      if (firstElement ) {
        firstElement = false;
        builder.append(str);
      }
      else {
        builder.append(", ").append(str);
      }
    }
    return builder.toString();
  }

}
