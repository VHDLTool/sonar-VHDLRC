/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class Vhdl extends AbstractLanguage {

  public static final String KEY = "vhdl";

  public static final String NAME = "VHDL";
  
  public static final String VHDL_CATEGORY = "VHDL";

  public static final String FILE_SUFFIXES_KEY = "sonar.vhdl.file.suffixes";

  public static final String DEFAULT_FILE_SUFFIXES = ".vhdl,.vhd";

  private final Configuration config;

  public Vhdl(Configuration config) {
    super(KEY, NAME);
    this.config = config;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(config.getStringArray(Vhdl.FILE_SUFFIXES_KEY)).filter(s -> s != null && !s.trim().isEmpty()).toArray(String[]::new);
    if (suffixes.length == 0) {
      suffixes = Iterables.toArray(Splitter.on(',').split(DEFAULT_FILE_SUFFIXES), String.class);
    }
    return suffixes;
  }

}
