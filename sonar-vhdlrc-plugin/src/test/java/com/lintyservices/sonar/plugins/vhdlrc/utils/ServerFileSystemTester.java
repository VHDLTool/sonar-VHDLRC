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
package com.lintyservices.sonar.plugins.vhdlrc.utils;


import java.io.File;

import org.sonar.api.platform.ServerFileSystem;


public class ServerFileSystemTester implements ServerFileSystem {

  private File serverHome;


  public ServerFileSystemTester(File serverHome) {
    this.serverHome = serverHome;
  }

  @Override
  public File getHomeDir() {
    return serverHome;
  }

  @Override
  public File getTempDir() {
    return serverHome;
  }


}
