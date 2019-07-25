package com.linty.sonar.plugins.vhdlrc.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.sonar.api.batch.fs.InputFile;
import static java.nio.charset.StandardCharsets.UTF_8;

public class LineMeasurer {

  private int numberLineOfCode = 0;
  private int numberLineComment = 0;
  private final InputFile file;
  private static final String COMMENT_START = "--";

  public LineMeasurer(InputFile f) {
    file = f;
  }
  
  public void analyseFile() {
    
    try (final BufferedReader br = new BufferedReader(
      new InputStreamReader(file.inputStream(), UTF_8))) {
      
      String line;
      boolean outOfHeader = false;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if(!line.isEmpty()) {
          if(!outOfHeader && !line.startsWith(COMMENT_START)) { //skip the header until first occurence of a loc
            outOfHeader=true;
          }
          if(!line.startsWith(COMMENT_START)) {
            numberLineOfCode++;
          }
          if(containsCommentContent(line) && outOfHeader) {
            numberLineComment++;
          } 
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read: " + file.filename(), e);
    }
   
    
  }

  private boolean containsCommentContent(String line) {
    if(line.contains(COMMENT_START)) {
      return (line.substring(line.indexOf(COMMENT_START))).matches("\\" + COMMENT_START + ".*[a-zA-Z0-9].*");
    }
    return false;
  }

  public int getNumberLineOfCode() {
    return numberLineOfCode;
  }

  public int getNumberLineComment() {
    return numberLineComment;
  }

  
}