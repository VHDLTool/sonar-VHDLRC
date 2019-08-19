package com.linty.sonar.zamia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SelectedRulesMaker {
  
  private static final String RC_CONFIG_SELECTED_RULES_PATH = "/" + ZamiaRunner.VIRGIN_CONF + "/" + ZamiaRunner.RC_CONFIG_SELECTED_RULES;
  
  private static final String LINE_PREFIX = "  <hb:Rule ParameterSource=\"RULE_CHECKER\" UID=\"";
  private static final String LINE_SUFFIX = "\"/>";
  private static final String CLOSING_TAG = "</config_selected_rules>";
  
  private List<String> ruleKeys;
  
  private static final Logger LOG = Loggers.get(SelectedRulesMaker.class);
  
  public SelectedRulesMaker(List<String> ruleKeys) {
    this.ruleKeys = ruleKeys;
  }

  public static Path makeWith(List<String> ruleKeys) {
    try {
      if(ruleKeys.isEmpty()) {
        LOG.warn("No rules to load in " + ZamiaRunner.RC_CONFIG_SELECTED_RULES);
      }
      return new SelectedRulesMaker(ruleKeys).make();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to generate " + ZamiaRunner.RC_CONFIG_SELECTED_RULES, e);
    }
  }

  private Path make() throws IOException {
    Path target = Files.createTempFile("tempFile", ".xml");
    target.toFile().deleteOnExit();
    if(LOG.isDebugEnabled()) {
      LOG.debug("TempFile created by SelectedRulesMaker : " + target);
    }
    InputStream source = BuildPathMaker.class.getResourceAsStream(RC_CONFIG_SELECTED_RULES_PATH);
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    return appendRules(target.toAbsolutePath());
  }

  private Path appendRules(Path target) throws IOException {
    StringBuilder builder = new StringBuilder();
    this.ruleKeys.forEach(s -> builder
      .append(LINE_PREFIX)
      .append(s)
      .append(LINE_SUFFIX)
      .append("\r\n")
      );
    builder
    .append(CLOSING_TAG);
    return Files.write(target, builder.toString().getBytes(UTF_8), StandardOpenOption.APPEND);
  }

}
