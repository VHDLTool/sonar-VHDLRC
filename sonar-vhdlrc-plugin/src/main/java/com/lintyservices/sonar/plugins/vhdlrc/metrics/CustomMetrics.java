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
package com.lintyservices.sonar.plugins.vhdlrc.metrics;

import java.util.List;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import static java.util.Arrays.asList;

public class CustomMetrics implements Metrics {

  public static final Metric<Integer> COMMENT_LINES_STD_02800 = new Metric.Builder("comment_02800", "Commented lines", Metric.ValueType.INT)
    .setDescription("Number of lines including a commented part")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(CoreMetrics.DOMAIN_SIZE)
    .create();

  @Override
  public List<Metric> getMetrics() {
    return asList(COMMENT_LINES_STD_02800);
  }
}
