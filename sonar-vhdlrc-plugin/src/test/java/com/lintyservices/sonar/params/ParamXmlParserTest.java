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
package com.lintyservices.sonar.params;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMInputCursor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ParamXmlParserTest {

  public List<ZamiaParam> params;
  ParamXmlParser paramXmlParser;

  @Before
  public void init() {
    params = new ArrayList<>();
  }

  @Test
  public void test_parsing_string_params() throws XMLStreamException {
    parseXmlParam("src/test/parameters/string_param.xml");
    assertThat(params.size()).isEqualTo(3);
    checkXmlParam(0, ZamiaStringParam.class, "P1", "Contain", "reset");
    checkXmlParam(1, ZamiaStringParam.class, "P2", "Prefix", "rst");
    checkXmlParam(2, ZamiaStringParam.class, "P3", "Suffix", "clr");
  }

  @Test
  public void test_parsing_int_params() throws XMLStreamException {
    parseXmlParam("src/test/parameters/int_param.xml");
    assertThat(params.size()).isEqualTo(2);
    checkXmlParam(0, ZamiaIntParam.class, "P1", "GT", "8");
    checkXmlParam(1, ZamiaIntParam.class, "P2", "LET", "-5");
  }

  @Test
  public void test_parsing_range_params() throws XMLStreamException {
    parseXmlParam("src/test/parameters/range_param.xml");
    assertThat(params.size()).isEqualTo(1);
    checkXmlParam(0, ZamiaRangeParam.class, "P1", "LET_GT", "-5:150");
  }

  @Test
  public void test_parsing_mixed_param_type_should_pass() throws XMLStreamException {
    parseXmlParam("src/test/parameters/mixed_param.xml");
    assertThat(params.size()).isEqualTo(3);
    checkXmlParam(0, ZamiaStringParam.class, "P1", "Contain", "reset");
    checkXmlParam(1, ZamiaRangeParam.class, "P1", "LET_GT", "-5:10050");
    checkXmlParam(2, ZamiaIntParam.class, "P2", "LET", "-5");
  }

  @Test
  public void test_parsing_unknown_param_type_should_throw_excpetion() {
    try {
      parseXmlParam("src/test/parameters/unknown_param.xml");
      fail();
    } catch (XMLStreamException e) {
      assertThat(e.getMessage()).contains("Unknown parameter type : UnknownParam");
      assertThat(e.getLocation().getLineNumber()).isEqualTo(4);
    }

  }


  public <T> void checkXmlParam(int index, Class<T> classType, String paramId, String field, String value) {
    assertThat(params.get(index)).isExactlyInstanceOf(classType);
    assertThat(params.get(index).hbParamId).isEqualTo(paramId);
    assertThat(params.get(index).field).isEqualTo(field);
    if (classType.isInstance(ZamiaStringParam.class)) {
      assertThat(((ZamiaStringParam) params.get(index)).value).isEqualTo(value);
    }
    if (classType.isInstance(ZamiaIntParam.class)) {
      assertThat(((ZamiaIntParam) params.get(index)).value).isEqualTo(value);
    }
    if (classType.isInstance(ZamiaRangeParam.class)) {
      String v = String.valueOf(((ZamiaRangeParam) params.get(index)).min) +
        ":" +
        String.valueOf(((ZamiaRangeParam) params.get(index)).max);
      assertThat(v).isEqualTo(value);
    }
  }

  public void parseXmlParam(String testFile) throws XMLStreamException {
    Path path = Paths.get(testFile);
    XMLInputFactory factory = XMLInputFactory.newInstance();
    // disable external entities
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

    SMInputFactory xmlFactory = new SMInputFactory(factory);
    SMInputCursor rootCursor;
    SMInputCursor ruleParamsCursor;

    try {
      rootCursor = xmlFactory.rootElementCursor(path.toFile()).advance();
      ruleParamsCursor = rootCursor.childElementCursor(new QName("HANDBOOK", "RuleParams")).advance();
      ruleParamsCursor.asEvent();
    } catch (XMLStreamException e) {
      throw new IllegalArgumentException("Error when trying to read ressource test file:" + testFile);
    }
    ParamXmlParser.collectParameters(params, ruleParamsCursor);
  }

}
