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


import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMFilter;
import org.codehaus.staxmate.in.SMInputCursor;

import static com.lintyservices.sonar.params.ParamTranslator.*;

public class ParamXmlParser {

  //Filter is mandatory for SMInputCursor to advance
  private SMFilter filter = new IgnoreSomeRuleElements();

  /*Static method called if parameters exist and are not empty
   * Entry point is as section SMInputCursor at <hb:RuleParams>
   */
  public static void collectParameters(List<ZamiaParam> params, SMInputCursor ruleParamsCursor) throws XMLStreamException {
    new ParamXmlParser().parseParameters(params, ruleParamsCursor);
  }

  //Collect all parameters into the given List<ZamiaParam> for a rule
  private void parseParameters(List<ZamiaParam> params, SMInputCursor ruleParamsCursor) throws XMLStreamException {
    SMInputCursor paramCursor = ruleParamsCursor.childCursor(filter).advance();
    while (paramCursor.asEvent() != null) {
      params.add(collectParameter(paramCursor));
      paramCursor.advance();
    }
  }

  //Parse one parameter section and returns a ZamiaParam filled with it
  private ZamiaParam collectParameter(SMInputCursor paramCursor) throws XMLStreamException {
    String paramType = paramCursor.getLocalName();
    Location l = paramCursor.getCursorLocation();
    SMInputCursor contentCursor = paramCursor.childCursor(filter).advance();
    if (STRING_PARAM.equals(paramType)) {
      return collectStringParam(contentCursor);
    } else if (INT_PARAM.equals(paramType)) {
      return collectIntParam(contentCursor);
    } else if (RANGE_PARAM.equals(paramType)) {
      return collectRangeParam(contentCursor);
    } else {
      throw new XMLStreamException("Unknown parameter type : " + paramType, l);
    }
  }

  //For String parameters
  private ZamiaParam collectStringParam(SMInputCursor contentCursor) throws XMLStreamException {
    ZamiaStringParam sp = new ZamiaStringParam();
    return collectContent(sp, contentCursor)
      .setValue(contentCursor.advance().getElemStringValue());
  }

  //For In parameters
  private ZamiaParam collectIntParam(SMInputCursor contentCursor) throws XMLStreamException {
    ZamiaIntParam ip = new ZamiaIntParam();
    return collectContent(ip, contentCursor)
      .setValue(contentCursor.advance().getElemIntValue());
  }

  //For Range parameters
  private ZamiaParam collectRangeParam(SMInputCursor contentCursor) throws XMLStreamException {
    ZamiaRangeParam rp = new ZamiaRangeParam();
    return collectContent(rp, contentCursor)
      .setMin(contentCursor.advance().getElemIntValue())
      .setMax(contentCursor.advance().getElemIntValue());
  }

  //Fills the paramID and field
  @SuppressWarnings("unchecked")
  private <T extends ZamiaParam> T collectContent(T zp, SMInputCursor contentCursor) throws XMLStreamException {
    return (T) zp
      .setHbParamId(contentCursor.getElemStringValue())
      .setField(contentCursor.advance().getElemStringValue());
  }

  private class IgnoreSomeRuleElements extends SMFilter {
    @Override
    public boolean accept(SMEvent evt, SMInputCursor caller) throws XMLStreamException {
      return evt.hasLocalName();
    }
  }


}
