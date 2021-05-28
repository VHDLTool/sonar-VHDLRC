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
package com.lintyservices.sonar.plugins.vhdlrc.issues;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMFilter;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.google.common.collect.ImmutableList;

public class ReportXmlParser {

  private final Path reportPath;
  protected String ruleKey = null;
  private List<Issue> issues;

  private static final Logger LOG = Loggers.get(ReportXmlParser.class);

  private static final ImmutableList<String> IGNORE = ImmutableList.of(
    "Entity",
    "Architecture",
    "Library",
    "Clock",
    "SignalType",
    "Process",
    "Sensitivity",
    "RuleCheckerVersion",
    "ExecutionDate",
    "SonarRemediationMsg"
  );

  private SMFilter filter = new IgnoreSomeRuleElements();

  public ReportXmlParser(Path reportPath) {
    this.reportPath = reportPath;
  }

  public static List<Issue> getIssues(Path reportsPath) throws XMLStreamException {
    return new ReportXmlParser(reportsPath).parseXML();
  }

  public List<Issue> parseXML() throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    // disable external entities
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

    SMInputFactory xmlFactory = new SMInputFactory(factory);
    SMInputCursor rootCursor = xmlFactory.rootElementCursor(reportPath.toFile()).advance();
    SMInputCursor cursor = rootCursor.childCursor(filter).advance();
    if ("RuleName".equals(cursor.getLocalName())) {
      this.ruleKey = cursor.getElemStringValue();
      collectIssues(cursor.advance());//<rc:RuleFailure>
    } else {
      LOG.error("No RuleKey found in {}. No issues will not be imported from this report", this.reportPath.getFileName());
    }
    return this.issues;
  }

  private void collectIssues(SMInputCursor cursor) throws XMLStreamException {
    issues = new ArrayList<>();
    while (cursor.asEvent() != null) {
      collectIssue(cursor.childCursor(filter).advance());
      cursor.advance();
    }
  }

  private void collectIssue(SMInputCursor cursor) throws XMLStreamException {
    String localName;
    Issue i = new Issue();
    i.ruleKey = this.ruleKey;
    while (cursor.asEvent() != null) {
      localName = cursor.getLocalName();
      if ("File".equals(localName)) {
        i.file = Paths.get(cursor.getElemStringValue());
      } else if ("Line".equals(localName)) {
        i.line = Integer.parseInt(cursor.getElemStringValue());
      } else if (localName.startsWith(ruleKey)) {
        i.errorMsg = collectSonarMsg(cursor.childElementCursor().advance());
      }
      cursor.advance();
    }
    issues.add(i);
  }

  private String collectSonarMsg(SMInputCursor cursor) throws XMLStreamException {
    while (!"SonarQubeMsg".equals(cursor.getLocalName())) {
      cursor.advance();
    }
    return cursor.childCursor(filter).advance().getElemStringValue();
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
