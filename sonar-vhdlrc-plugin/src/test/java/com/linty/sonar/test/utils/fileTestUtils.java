package com.linty.sonar.test.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.xmlunit.builder.Input;
import org.xmlunit.matchers.CompareMatcher;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;

public class fileTestUtils {

  public void compareFileLines(Path f1, Path f2, boolean printLines) {
  
    try {
      BufferedReader br1 = Files.newBufferedReader(f1,UTF_8);
      BufferedReader br2 = Files.newBufferedReader(f2,UTF_8);
      String l1;
      String l2;
      int lineNum = 0;
      while((l1 = br1.readLine()) != null && (l2 = br2.readLine()) != null) {
        if(printLines == true) {
          System.out.println(lineNum + "-1|" + l1);
        }
        assertThat(l1).isEqualTo(l2);
        lineNum++;
      } 
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read file", e);
    }
  }

  public static void compareXml(Path result, Path expected) {
    assertThat(
      Input.fromFile(result.toFile()), 
      CompareMatcher
      .isIdenticalTo(Input.fromFile(expected.toFile()))
      .ignoreComments()
      .ignoreWhitespace()
      );
  }
  
  public static void printFile(Path p) {
    try (BufferedReader br = new BufferedReader(new FileReader(p.toFile()))) {
      String line = null;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
