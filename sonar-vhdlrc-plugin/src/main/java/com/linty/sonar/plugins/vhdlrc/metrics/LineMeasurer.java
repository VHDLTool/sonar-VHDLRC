package com.linty.sonar.plugins.vhdlrc.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import static java.nio.charset.StandardCharsets.UTF_8;

public class LineMeasurer {
  private static final Logger LOG = Loggers.get(LineMeasurer.class);
  
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
          if(!line.startsWith(COMMENT_START)) {
            outOfHeader=true;
            numberLineOfCode++;
          }
          if(line.contains(COMMENT_START) && outOfHeader) {
            numberLineComment++;
          } 
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read: " + file.filename(), e);
    }
   
    
  }

  public int getNumberLineOfCode() {
    return numberLineOfCode;
  }

  public int getNumberLineComment() {
    return numberLineComment;
  }

  
}
