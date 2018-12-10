package com.linty.sonar.zamia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class CmdSandbox {
  
  public String home = "P:\\";
  public String notePadDir = "Tools/notepad++";
  public String notePad = "notepad++.exe";
  public String arguments = "-alwaysOnTop -notabbar";
  public String fileDir = "dev/eclipse_test.bat";
  
  public void execute(String program, String args, String target) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder();
    Process process;
    boolean isWindows = System.getProperty("os.name")
      .toLowerCase().startsWith("windows");
    ArrayList<String> fullCmd = new ArrayList<>();
    fullCmd.add(program);
    fullCmd.addAll(Arrays.asList(args.split(" ")));
    fullCmd.add(target);
    
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
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
}
