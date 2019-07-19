package com.linty.sonar.plugins.vhdlrc.metrics;

import com.linty.sonar.plugins.vhdlrc.Vhdl;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;

public class MetricSensor implements Sensor {

  private Map<Metric<Integer>, Integer> measures;

  public MetricSensor() {
    measures = new HashMap<>();
  }
  
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
    .name("VhdlRcMetricSensor")
    .onlyOnLanguage(Vhdl.KEY);  
    //add a check for linty sslr presence 
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

  private <T extends Serializable> void saveMetricOnFile(SensorContext context, InputFile InputFile, Metric<T> metric, T value) {
    context.<T>newMeasure().forMetric(metric).on(InputFile).withValue(value).save();    
  }
  
}
