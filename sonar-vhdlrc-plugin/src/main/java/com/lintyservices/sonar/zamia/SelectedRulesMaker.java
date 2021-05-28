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
package com.lintyservices.sonar.zamia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

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

  protected SelectedRulesMaker(List<String> ruleKeys) {
    this.ruleKeys = ruleKeys;
  }

  public static Path makeWith(List<String> ruleKeys) {
    LOG.debug("Generating " + ZamiaRunner.RC_CONFIG_SELECTED_RULES);
    if (ruleKeys.isEmpty()) {
      LOG.warn("No rules to load in " + ZamiaRunner.RC_CONFIG_SELECTED_RULES);
    }
    InputStream ressource = BuildPathMaker.class.getResourceAsStream(RC_CONFIG_SELECTED_RULES_PATH);
    return new SelectedRulesMaker(ruleKeys).make(ressource);

  }

  @VisibleForTesting
  protected Path make(InputStream ressource) {
    try {
      Path target = Files.createTempFile("tempFile", ".xml");
      target.toFile().deleteOnExit();
      Files.copy(ressource, target, StandardCopyOption.REPLACE_EXISTING);
      return appendRules(target.toAbsolutePath());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to generate " + ZamiaRunner.RC_CONFIG_SELECTED_RULES, e);
    }

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
