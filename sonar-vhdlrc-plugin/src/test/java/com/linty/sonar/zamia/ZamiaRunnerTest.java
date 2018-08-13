package com.linty.sonar.zamia;

import org.junit.Test;

import static org.junit.Assert.*;

public class ZamiaRunnerTest {
  
  /*ZamiaRunner specification:
   * 1-Make a buildPath.txt from a virgin one and append top entities from config()
   * If not present:
   * => Error + Execution Failure 
   * 
   * 2-Copy BuildPath, handbook, rc_config_selected_rule, rc_handbook_parameters 
   * to Zamia destination workspace, means replace present files
   * If missing conf file (Except BuildPath):
   * =>load default files + Warn
   * 
   * 3-Empty Zamia inputFile folder and fill it with current Sensor's vhdl InputFile
   * 
   * 4-Run shell Zamia cmd line for executing analysis
   * Output must be logged into vhdlrc-log.txt and available into debug mod
   * 
   * 5-Check if Excution is successful??
   * */

  @Test
  public void test() {
   
  }

}
