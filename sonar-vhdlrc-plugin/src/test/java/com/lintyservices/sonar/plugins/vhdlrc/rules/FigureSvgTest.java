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
package com.lintyservices.sonar.plugins.vhdlrc.rules;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FigureSvgTest {

  @Test
  public void test_get_original_dim_decimal() {
    FigureSvg figure = new FigureSvg("", "", "");
    figure.figureCode =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" +
        "<!-- Created with Inkscape (http://www.inkscape.org/) -->\r\n" +
        "\r\n" +
        "<svg\r\n" +
        "   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" +
        "   width=\"722.12146\"\r\n" +
        "   height=\"971.87701\"\r\n" +
        "   id=\"svg3507\"\r\n"
    ;
    figure.loadOriginialDim();
    assertEquals("722.12146", figure.originalWidth());
    assertEquals("971.87701", figure.originalHeight());
  }

  @Test
  public void test_get_original_dim_round_number() {
    FigureSvg figure = new FigureSvg("", null, null);
    figure.figureCode =
      "xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" +
        "   width=\"450\"\r\n" +
        "   height=\"650\"\r\n" +
        "   viewBox=\"0 0 449.99998 649.99999\""
    ;
    figure.loadOriginialDim();
    assertEquals("450", figure.originalWidth());
    assertEquals("650", figure.originalHeight());
  }

  @Test
  public void test_get_original_dim_with_unit() {
    FigureSvg figure = new FigureSvg();
    figure.figureCode =
      " xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" +
        "   width=\"165mm\"\r\n" +
        "   height=\"137px\"\r\n" +
        "   viewBox=\"0 0 584.64567 485.43307\"\r\n" +
        "   id=\"svg2\"\r\n" +
        "   version=\"1.1\""
    ;
    figure.loadOriginialDim();
    assertEquals("165", figure.originalWidth());
    assertEquals("137", figure.originalHeight());
  }

  @Test
  public void test_get_original_dim_with_no_dims() {
    FigureSvg figure = new FigureSvg();
    figure.figureCode =
      " xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\r\n" +
        "   viewBox=\"0 0 584.64567 485.43307\"\r\n" +
        "   id=\"svg2\"\r\n" +
        "   version=\"1.1\""
    ;
    figure.loadOriginialDim();
    assertEquals("300", figure.originalWidth());
    assertEquals("300", figure.originalHeight());
  }

}
