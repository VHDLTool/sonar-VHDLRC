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

package com.lintyservices.sonar.plugins.vhdlrc;

import java.util.HashSet;
import java.util.Set;

import org.sonar.api.batch.fs.InputFile;

public class VhdlPackage {
  
  private InputFile packageFile;
  private String packageName;
  private Set<String> usedPackages;
  
  public VhdlPackage(String packageName) {
    this.setPackageName(packageName);
    this.usedPackages = new HashSet<String>();
  }
  
  public String getPackageName() {
    return packageName;
  }
  
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
  
  public Set<String> getUsedPackages() {
    return usedPackages;
  }
  
  public void setUsedPackages(Set<String> usedPackages) {
    this.usedPackages = usedPackages;
  }

  public InputFile getPackageFile() {
    return packageFile;
  }

  public void setPackageFile(InputFile packageFile) {
    this.packageFile = packageFile;
  }

  
}
