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

import com.lintyservices.sonar.plugins.vhdlrc.Vhdl;

import java.nio.file.Path;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SensorTestUtils {

  public static InputFile getInputFile(Path baseDir, String relativeFilePath, String module) {
    return new TestInputFileBuilder(module, relativeFilePath)
      .setModuleBaseDir(baseDir)
      .setLanguage(Vhdl.KEY)
      .setCharset(UTF_8)
      .build();
  }
}
