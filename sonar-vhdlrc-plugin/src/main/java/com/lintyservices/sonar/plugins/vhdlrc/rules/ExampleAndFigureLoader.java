/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2020 Linty Services
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
package com.lintyservices.sonar.plugins.vhdlrc.rules;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.annotations.VisibleForTesting;

public class ExampleAndFigureLoader {
  private static final String EXAMPLES_SUBPATH = "/Extras/VHDL/";
  private static final String IMAGES_SUBPATH = "/Extras/Images/";
  public static final String CODE_BALISE = "--CODE";
  public static final String NOT_FOUND_EXAMPLE_MSG = "Example empty or not found : ";
  public static final String NOT_FOUND_IAMGE_MSG = "Image empty or not found : ";

  public final String EXAMPLES_PATH;
  public final String IMAGES_PATH;

  ExampleAndFigureLoader(String dir) {
    EXAMPLES_PATH = dir.concat(EXAMPLES_SUBPATH);
    IMAGES_PATH = dir.concat(IMAGES_SUBPATH);
  }

  public void load(List<Rule> rules) {
    for (Rule r : rules) {
      if (!StringUtils.isEmpty(r.goodExampleRef)) {
        r.goodExampleCode = collectExample(r.goodExampleRef);
      }
      if (!StringUtils.isEmpty(r.badExampleRef)) {
        r.badExampleCode = collectExample(r.badExampleRef);
      }
      if (r.figure != null) {
        r.figure.figureCode = collectImage(r.figure.figureRef);
      }
    }
  }

  // FIXME: @VisibleForTesting
  protected String collectExample(String fileRef) {
    StringBuilder codeExample = new StringBuilder();
    String fileName = fileRef.concat(".vhd");
    try (BufferedReader reader = new BufferedReader(getRessource(EXAMPLES_PATH.concat(fileName)))) {

      String line = reader.readLine();
      while (line != null && !line.contains(CODE_BALISE)) {//waiting for 1rst --CODE to start
        line = reader.readLine();
      }
      while ((line = reader.readLine()) != null && !line.contains(CODE_BALISE)) {//waiting for 2nd --CODE to stop
        codeExample.append(line).append("\r\n");
      }
    } catch (IOException e) {
      return NOT_FOUND_EXAMPLE_MSG + fileName;
    }
    return String.valueOf(codeExample);
  }

  protected String collectImage(String fileName) {
    StringBuilder figureCode = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(getRessource(IMAGES_PATH.concat(fileName)))) {
      String line = reader.readLine();
      while (line != null && !line.contains("<svg")) {
        line = reader.readLine();
      }
      if (line != null) {
        figureCode.append(line).append("\r\n");
      }
      while ((line = reader.readLine()) != null) {
        figureCode.append(line).append("\r\n");
      }
    } catch (IOException e) {
      return NOT_FOUND_IAMGE_MSG + fileName;
    }
    return String.valueOf(figureCode);
  }

  public InputStreamReader getRessource(String ressourcePath) throws IOException {

    InputStream is = ExampleAndFigureLoader.class.getResourceAsStream(ressourcePath);
    if (is == null) {
      throw new IOException();
    }
    return new InputStreamReader(is);
  }
}
