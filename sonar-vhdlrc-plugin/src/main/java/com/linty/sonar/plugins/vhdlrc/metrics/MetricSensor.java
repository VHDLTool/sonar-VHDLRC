package com.linty.sonar.plugins.vhdlrc.metrics;

import com.linty.sonar.plugins.vhdlrc.Vhdl;
import java.io.Serializable;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;

public class MetricSensor implements Sensor {

  public static final String SKIP_METRICS_KEY = "sonar.vhdlrc.skipMetrics";
  public static final boolean SKIP_METRICS_DEFAULT = false;

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
    .name("VhdlRcMetricSensor")
    .onlyOnLanguage(Vhdl.KEY)
    .onlyWhenConfiguration(c -> !c.getBoolean(SKIP_METRICS_KEY).orElse(SKIP_METRICS_DEFAULT));   
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
