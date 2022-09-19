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
package com.lintyservices.sonar.test.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.xmlunit.builder.Input;
import org.xmlunit.matchers.CompareMatcher;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;

public class fileTestUtils {

  private static final Logger LOG = Loggers.get(fileTestUtils.class);

  public void compareFileLines(Path f1, Path f2, boolean printLines) {

    BufferedReader br1 = null;
    BufferedReader br2 = null;
    try {
      br1 = Files.newBufferedReader(f1, UTF_8);
      br2 = Files.newBufferedReader(f2, UTF_8);
      String l1;
      String l2;
      int lineNum = 0;
      while ((l1 = br1.readLine()) != null && (l2 = br2.readLine()) != null) {
        if (printLines == true) {
          System.out.println(lineNum + "-1|" + l1);
        }
        assertThat(l1).isEqualTo(l2);
        lineNum++;
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read file", e);
    } finally {
      try {
        if (br1 != null)
          br1.close();
      } catch (IOException e) {
        LOG.warn("Could not close stream");
      }
      try {
        if (br2 != null)
          br2.close();
      } catch (IOException e) {
        LOG.warn("Could not close stream");
      }
    }
  }

  public static void compareFiles(Path f1, Path f2, boolean printLines) {
    new fileTestUtils().compareFileLines(f1, f2, printLines);
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
      LOG.warn("Could not find file");
    } catch (IOException e) {
      LOG.warn("Could not find file");

    }
  }
}

