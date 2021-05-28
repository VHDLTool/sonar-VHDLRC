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
package com.lintyservices.sonar.plugins.vhdlrc;


import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import com.lintyservices.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;

public class VhdlRcProfile implements BuiltInQualityProfilesDefinition {

  public static final String BUILT_IN_PROFILE_NAME = "VHDL-RC";
  private NewBuiltInQualityProfile vhdlRcQP;

  @Override
  public void define(Context context) {
    vhdlRcQP = context.createBuiltInQualityProfile(BUILT_IN_PROFILE_NAME, Vhdl.KEY);
    InputStream handbook = getClass().getClassLoader().getResourceAsStream("configuration/HANDBOOK/Rulesets/handbook.xml");
    try {
      SMInputFactory inputFactory = initStax();
      SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(handbook);
      while (rootCursor.getNext() != null) {
        addImplementedRules(rootCursor.descendantElementCursor("hb:Rule"));
      }
      rootCursor.getStreamReader().closeCompletely();
    } catch (XMLStreamException e) {
      throw new IllegalStateException("XML is not valid", e);
    }
    vhdlRcQP.setDefault(!VhdlRcPlugin.withoutVhdl).done();

  }

  private void addImplementedRules(SMInputCursor rule) throws XMLStreamException {
    while (rule.getNext() != null) {
      String ruleID = rule.getAttrValue("UID");
      SMInputCursor ruleHist = rule.descendantElementCursor("hb:RuleHist");
      if (ruleHist.getNext() != null) {
        SMInputCursor ruleStatus = ruleHist.descendantElementCursor("hb:Status");
        if (ruleStatus.getNext() != null && ruleStatus.getElemStringValue().equalsIgnoreCase("Implemented"))
          vhdlRcQP.activateRule(VhdlRulesDefinition.VHDLRC_REPOSITORY_KEY, ruleID);
      }
    }
  }


  private static SMInputFactory initStax() {
    final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    return new SMInputFactory(xmlFactory);
  }
}
