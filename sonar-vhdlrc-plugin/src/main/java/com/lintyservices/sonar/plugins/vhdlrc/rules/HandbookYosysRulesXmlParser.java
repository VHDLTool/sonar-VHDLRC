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


import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.google.common.collect.ImmutableList;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMFilter;
import org.codehaus.staxmate.in.SMInputCursor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class HandbookYosysRulesXmlParser {

  private static final String RULE_HIST = "RuleHist";


  private static final ImmutableList<String> IGNORE = ImmutableList.of(
    "Status",
    "Version",
    "Creation",
    "Modified",
    "Revision",
    "RuleContent",
    "Sonarqube",
    "RuleDesc",
    "RuleParams"
  );
  private SMFilter filter = new IgnoreSomeRuleElements();
  private String currentUID = "";
  private Set<String> yosysrules = new HashSet<>();

  private static final Logger LOG = Loggers.get(HandbookYosysRulesXmlParser.class);
  private static final String NAMESPACE_HANDBOOX = "HANDBOOK";
  private static final Set<String> NULL = null;


  public Set<String> parseXML(InputStream hbStream) {
    try {
      if (hbStream.available() == 0) {
        LOG.warn("Handbook.xml is empty, no rules will be loaded");
        return NULL;
      }

      collectRules(hbStream);
      return yosysrules;

    } catch (XMLStreamException e) {
      throw new IllegalStateException("Error when parsing rules in " + VhdlRulesDefinition.RULESET_PATH + " line " + e.getLocation().getLineNumber(), e);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read handbook.xml in jar ressources", e);
    }
  }

  private void collectRules(InputStream hbStream) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    // disable external entities
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

    SMInputFactory xmlFactory = new SMInputFactory(factory);
    SMInputCursor cursor = xmlFactory.rootElementCursor(hbStream).advance();
    SMInputCursor ruleCursor = cursor.childElementCursor(new QName(NAMESPACE_HANDBOOX, "Rule")).advance();

    while (ruleCursor.asEvent() != null) {
      currentUID = ruleCursor.getAttrValue("UID");
      collectRule(ruleCursor.childCursor(filter).advance());
      ruleCursor.advance();
    }
    cursor.getStreamReader().closeCompletely();
  }

  private void collectRule(SMInputCursor sectionCursor) throws XMLStreamException {
    boolean found = false;
    while (sectionCursor.asEvent() != null && !found) {
      if (sectionCursor.getLocalName().equals(RULE_HIST)) {
        collectRuleHist(sectionCursor.childCursor(filter).advance());
        found = true;
      }
      sectionCursor.advance();
    }
  }


  private void collectRuleHist(SMInputCursor cursor) throws XMLStreamException {
    while (cursor.asEvent() != null) {
      if (cursor.getLocalName().equalsIgnoreCase("Engine") && cursor.getElemStringValue().equalsIgnoreCase("Yosys-ghdl")) {
        yosysrules.add(currentUID);
      }
      cursor.advance();
    }

  }


  private class IgnoreSomeRuleElements extends SMFilter {

    @Override
    public boolean accept(SMEvent evt, SMInputCursor caller) throws XMLStreamException {
      if (!evt.hasLocalName()) {
        return false;
      }
      return !IGNORE.contains(caller.getLocalName());
    }
  }


}
