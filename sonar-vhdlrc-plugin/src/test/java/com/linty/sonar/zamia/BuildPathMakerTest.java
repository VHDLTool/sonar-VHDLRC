package com.linty.sonar.zamia;


import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;


public class BuildPathMakerTest {
  
  @Test
  public void test() throws IOException, URISyntaxException {
    MapSettings settings = new MapSettings();
    settings.setProperty(BuildPathMaker.TOP_ENTITY_KEY, "work.my_entity(rtl)");
    BuildPathMaker.build(settings.asConfig());
    Path ComputedBuidPath = Paths.get(BuildPathMakerTest.class.getResource("/computed_conf/BuildPath.txt").toURI());    
    System.out.println("looking in " + ComputedBuidPath);
    BuildPathMaker.printFile(ComputedBuidPath);
    assertThat(Files.exists(ComputedBuidPath)).isTrue();
    assertThat(Files.isWritable(ComputedBuidPath)).isTrue();
    assertThat(getLineOf(ComputedBuidPath,69)).isEqualTo("toplevel WORK.MY_ENTITY(RTL)");
  }
  
  @Test
  public void test_multiple_entities() throws IOException, URISyntaxException {
      MapSettings settings = new MapSettings();
      settings.setProperty(BuildPathMaker.TOP_ENTITY_KEY, "top, top1(rtl), work.my_entity(rtl)");
      BuildPathMaker.build(settings.asConfig());
      Path ComputedBuidPath = Paths.get(BuildPathMakerTest.class.getResource("/computed_conf/BuildPath.txt").toURI());    
      System.out.println("looking in " + ComputedBuidPath);
      BuildPathMaker.printFile(ComputedBuidPath);
      assertThat(Files.exists(ComputedBuidPath)).isTrue();
      assertThat(Files.isWritable(ComputedBuidPath)).isTrue();
      assertThat(getLineOf(ComputedBuidPath,69)).isEqualTo("toplevel TOP");
      assertThat(getLineOf(ComputedBuidPath,70)).isEqualTo("toplevel TOP1(RTL)");
      assertThat(getLineOf(ComputedBuidPath,71)).isEqualTo("toplevel WORK.MY_ENTITY(RTL)");
  }


  public static String getLineOf(Path p, int index) throws IOException {
    try(BufferedReader reader = Files.newBufferedReader(p,UTF_8)){
      String line=reader.readLine();
      int lineNumber = 1;
      while(lineNumber < (index)) {        
        line = reader.readLine();
        lineNumber++;
      }
      return line;
    } 

  }

}
