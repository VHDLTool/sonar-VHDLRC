package com.linty.sonar.zamia;

import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import com.linty.sonar.plugins.vhdlrc.VhdlRcSensor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import org.junit.Test;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;

import static java.nio.charset.StandardCharsets.UTF_8;
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
  
  private static final String SCANNER_HOME = "src/test/files/scanner-home";
  private static final String PROJECT_HOME = "src/test/files/vhdl";
  
  @Test
  public void test_uploading_config() throws IOException {
    SensorContextTester context = createContext();
    FilePredicates predicates = context.fileSystem().predicates();
    //InputFile inputFile = context.fileSystem().inputFile(predicates.hasFilename("mux.vhd"));
    
    
  }
  
  public static SensorContextTester createContext() {
    return SensorContextTester.create(Paths.get(PROJECT_HOME))
      .setSettings(new MapSettings()
        .setProperty(VhdlRcSensor.SCANNER_HOME_KEY, SCANNER_HOME)
        .setProperty(BuildPathMaker.TOP_ENTITY_KEY, "TOP"))
      .setRuntime(SonarRuntimeImpl.forSonarQube(VHDLRcPlugin.SQ_6_7, SonarQubeSide.SCANNER));
  }
  

}
