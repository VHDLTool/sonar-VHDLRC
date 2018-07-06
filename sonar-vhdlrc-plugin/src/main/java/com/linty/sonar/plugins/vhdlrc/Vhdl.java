package com.linty.sonar.plugins.vhdlrc;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

/**
 * VHDL language implementation
 *
 * @since 1.3
 */
public class Vhdl extends AbstractLanguage {

  /**
   * VHDL key
   */
  public static final String KEY = "vhdl";

  /**
   * VHDL name
   */
  public static final String NAME = "VHDL";

  /**
   * Key of the file suffix parameter
   */
  public static final String FILE_SUFFIXES_KEY = "sonar.vhdl.file.suffixes";

  /**
   * Default VHDL files knows suffixes
   */
  public static final String DEFAULT_FILE_SUFFIXES = ".vhdl,.vhd";

  /**
   * Configuration of the plugin
   */
  private final Configuration config;

  /**
   * Default constructor
   */
  public Vhdl(Configuration config) {
    super(KEY, NAME);
    this.config = config;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.sonar.api.resources.AbstractLanguage#getFileSuffixes()
   */
  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(config.getStringArray(Vhdl.FILE_SUFFIXES_KEY)).filter(s -> s != null && !s.trim().isEmpty()).toArray(String[]::new);
    if (suffixes.length == 0) {
      suffixes = Iterables.toArray(Splitter.on(',').split(DEFAULT_FILE_SUFFIXES), String.class);
    }
    return suffixes;
  }

}
