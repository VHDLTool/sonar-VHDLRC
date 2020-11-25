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
package com.lintyservices.sonar.zamia;

import com.lintyservices.sonar.test.utils.fileTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildPathMakerTest {

  @Rule
  public LogTester logTester = new LogTester();

  @Test
  public void test_top_entity() throws IOException, URISyntaxException {
    logTester.setLevel(LoggerLevel.DEBUG);
    MapSettings settings = new MapSettings();
    settings.setProperty(BuildPathMaker.TOP_ENTITY_KEY, "work.my_entity(rtl)");

    Path ComputedBuildPath = BuildPathMaker.make(settings.asConfig());

    assertThat(Files.exists(ComputedBuildPath)).isTrue();
    assertThat(Files.isWritable(ComputedBuildPath)).isTrue();
    assertThat(getLineOf(ComputedBuildPath, 69)).isEqualTo("toplevel WORK.MY_ENTITY(RTL)");
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0)).isNotEmpty();
  }

  @Test
  public void test_custom_cmd() throws IOException {
    MapSettings settings = new MapSettings();
    settings
      .setProperty(BuildPathMaker.TOP_ENTITY_KEY, "rtl")
      .setProperty(BuildPathMaker.CUSTOM_CMD_KEY,
        "extern GRLIB            \"$LEON_SRC/lib/grlib\"\r\n" +
          " extern TECHMAP          \"$LEON_SRC/lib/techmap\"\r\n" +
          "\r\n" +
          "# by default, extern declarations are recursive\r\n" +
          "# use the 'nonrecursive' keyword otherwise\r\n" +
          " extern nonrecursive FOO \"$LEON_SRC/lib/foo\"");

    Path ComputedBuildPath = BuildPathMaker.make(settings.asConfig());

    assertThat(Files.exists(ComputedBuildPath)).isTrue();
    assertThat(Files.isWritable(ComputedBuildPath)).isTrue();
    new fileTestUtils().compareFileLines(ComputedBuildPath, Paths.get("src/test/files/config/ExpectedBuildPath.txt"), false);
  }


  @Test
  public void test_description() {
    String desc = BuildPathMaker.customCmdDescription();
    assertThat(desc).isNotNull();
    assertThat(desc).isNotEmpty();
  }

  public static String getLineOf(Path p, int index) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(p, UTF_8)) {
      String line = reader.readLine();
      int lineNumber = 1;
      while (lineNumber < (index)) {
        line = reader.readLine();
        lineNumber++;
      }
      return line;
    }
  }

}
