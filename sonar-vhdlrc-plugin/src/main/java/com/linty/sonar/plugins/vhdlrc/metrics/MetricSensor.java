package com.linty.sonar.plugins.vhdlrc.metrics;

import com.linty.sonar.plugins.vhdlrc.Vhdl;
import java.io.Serializable;
import java.util.Arrays;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class MetricSensor implements Sensor {

  public static final String SSLR_FILE_SUFFIXES = "sonar.vhdl.file.suffixes";
  private static final Logger LOG = Loggers.get(MetricSensor.class);

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
    .name("VhdlRcMetricSensor")
    .onlyOnLanguage(Vhdl.KEY)
    .onlyWhenConfiguration(this::noSslrConficts);   
  }

  private boolean noSslrConficts(Configuration conf) {
    if(conf.hasKey(SSLR_FILE_SUFFIXES)) {
      for(String suffix : conf.getStringArray(Vhdl.FILE_SUFFIXES_KEY)) {
        if(Arrays.asList(conf.getStringArray(SSLR_FILE_SUFFIXES)).contains(suffix)){
          LOG.info("Linty plugin detected: [" + SSLR_FILE_SUFFIXES + "]");
          LOG.info("Metrics will not be computed by vhdlrc to avoid conficts");
          return false;
        }
      }
      return true;
    }
    return true;
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
