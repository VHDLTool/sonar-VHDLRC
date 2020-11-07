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
package com.lintyservices.sonar.plugins.vhdlrc.metrics;

import com.lintyservices.sonar.plugins.vhdlrc.Vhdl;

import java.io.Serializable;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;

public class MetricSensor implements Sensor {

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("VhdlRcMetricSensor")
      .onlyOnLanguage(Vhdl.KEY);
  }

  @Override
  public void execute(SensorContext context) {
    FilePredicates p = context.fileSystem().predicates();
    Iterable<InputFile> files = context.fileSystem().inputFiles(p.hasLanguage(Vhdl.KEY));
    files.forEach(file -> {
      final LineMeasurer measurer = new LineMeasurer(file);
      measurer.analyseFile();
      saveMetricOnFile(context, file, CoreMetrics.NCLOC, measurer.getNumberLineOfCode());
      saveMetricOnFile(context, file, CoreMetrics.COMMENT_LINES, measurer.getNumberLineComment());
    });
  }

  private <T extends Serializable> void saveMetricOnFile(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    context.<T>newMeasure().forMetric(metric).on(inputFile).withValue(value).save();
  }

}
