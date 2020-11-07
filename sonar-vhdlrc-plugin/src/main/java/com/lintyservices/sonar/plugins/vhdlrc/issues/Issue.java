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
package com.lintyservices.sonar.plugins.vhdlrc.issues;


import java.nio.file.Path;

public class Issue {
  String ruleKey;
  String remediationMsg;
  String errorMsg;
  Path file;
  int line;

  public String ruleKey() {
    return ruleKey;
  }

  public String remediationMsg() {
    return remediationMsg;
  }

  public String errorMsg() {
    return errorMsg;
  }

  public Path file() {
    return file;
  }

  public int line() {
    return line;
  }


}
