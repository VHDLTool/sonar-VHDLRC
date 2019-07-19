package com.linty.sonar.plugins.vhdlrc.metrics;

import com.linty.sonar.test.utils.SensorTestUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;

import static org.assertj.core.api.Assertions.assertThat;

public class LineMeasurerTest {
  
  @Rule
  public LogTester logTester = new LogTester();
  
  public Path baseDir = Paths.get("src/test/Metrics");
  
  @Test
  public void test() {
    checkLocMeasure(baseDir,"File1.vhd",45);
    checkCommentLineMeasure(baseDir,"File1.vhd",19);  
    
    checkLocMeasure(baseDir,"empty.vhd",0);
    checkCommentLineMeasure(baseDir,"empty.vhd",0);
    
    checkLocMeasure(baseDir,"commented_out_file.vhd",0);
    checkCommentLineMeasure(baseDir,"commented_out_file.vhd",0);//Should consider header
    
    
  }
  
  @Test
  public void testExpetion() {
      try{
        LineMeasurer measurer = new LineMeasurer(SensorTestUtils.getInputFile(baseDir,"Not existing.vhd", "module"));
        measurer.analyseFile();      
      } catch (IllegalStateException ise) {
        assertThat(ise).hasMessageStartingWith("Failed to read: Not existing.vhd");
      }
  }
  
  public static void checkLocMeasure(Path baseDir, String relativeFilePath, int expected) {
    LineMeasurer measurer = new LineMeasurer(SensorTestUtils.getInputFile(baseDir,relativeFilePath, "module"));
    measurer.analyseFile();
    assertThat(measurer.getNumberLineOfCode()).isNotNull();
    assertThat(measurer.getNumberLineOfCode()).isEqualTo(expected);
  }
  
  public static void checkCommentLineMeasure(Path baseDir, String relativeFilePath, int expected) {
    LineMeasurer measurer = new LineMeasurer(SensorTestUtils.getInputFile(baseDir,relativeFilePath, "module"));
    measurer.analyseFile();
    assertThat(measurer.getNumberLineComment()).isNotNull();
    assertThat(measurer.getNumberLineComment()).isEqualTo(expected);    
  }
  
}
