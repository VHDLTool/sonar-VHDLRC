/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2019 Maxime Facquet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.linty.sonar.plugins.vhdlrc.metrics;


import com.linty.sonar.plugins.vhdlrc.VHDLRcPlugin;
import com.linty.sonar.plugins.vhdlrc.Vhdl;
import com.linty.sonar.test.utils.SensorTestUtils;
import java.io.File;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.measures.CoreMetrics;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricSensorTest {
  private SensorContextTester context;
  private File baseDir;
  private DefaultFileSystem fs;
  private static final SonarRuntime SQRT = SonarRuntimeImpl.forSonarQube(VHDLRcPlugin.SQ_6_7, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
  
  private static final String COMMENT_LINE = CoreMetrics.COMMENT_LINES.getKey();
  private static final String NCLOC = CoreMetrics.NCLOC.getKey();
  
  @Before
  public void setUp() throws Exception {
    baseDir = new File("src/test/Metrics");
    context = SensorContextTester
      .create(baseDir)
      .setRuntime(SQRT);
    fs = context.fileSystem();
    addFileToContext("empty.vhd");
    addFileToContext("File1.vhd");
    addFileToContext("commented_out_file.vhd");
  }

  @Test
  public void test_line_metrics() {
    MetricSensor sensor = new MetricSensor();
    sensor.execute(context);

    checkMetric("commented_out_file.vhd", COMMENT_LINE, 0);// Comments are a header
    
    checkMetric("empty.vhd", NCLOC, 0);
    checkMetric("empty.vhd", COMMENT_LINE, 0);
    
    checkMetric("File1.vhd", NCLOC, 45);
    checkMetric("File1.vhd", COMMENT_LINE, 19);
    
  }
  
  @Test
  public void metric_sensor_should_only_execute_when_authories_in_properties() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    MetricSensor sensor = new MetricSensor();

    MapSettings settings1 = new MapSettings()
      .setProperty(MetricSensor.SKIP_METRICS_KEY, true); //will skip
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.configurationPredicate().test(settings1.asConfig())).isEqualTo(false);
    
    MapSettings settings2 = new MapSettings()
      .setProperty(MetricSensor.SKIP_METRICS_KEY, false); //will execute 
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.configurationPredicate().test(settings2.asConfig())).isEqualTo(true);
    
  }
  
  
  @Test
  public void descriptor_test() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    MetricSensor sensor = new MetricSensor();
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.languages().size()).isEqualTo(1);
    assertThat(sensorDescriptor.languages()).containsOnly((Vhdl.KEY));
    
  }
  
  private void addFileToContext(String filename) {
    InputFile inputFile = SensorTestUtils.getInputFile(fs.baseDirPath(), filename, context.module().key());
    fs.add(inputFile);
  }
  
  private void checkMetric(String filename, String metric, @Nullable Number expectedValue) {
    InputFile inputFile = fs.inputFile(fs.predicates().hasRelativePath(filename));
    Measure<Serializable> measure = context.measure(inputFile.key(), metric);
    if (expectedValue == null) {
      assertThat(measure).isNull();
    } else {
      assertThat(measure.value()).isEqualTo(expectedValue);
    }
  }
  

}
