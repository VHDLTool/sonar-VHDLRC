/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2019 Maxime Facquet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.linty.sonar.plugins.vhdlrc.rules;


import org.apache.commons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.google.common.collect.ImmutableList;
import com.linty.sonar.plugins.vhdlrc.rules.Rule;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMFilter;
import org.codehaus.staxmate.in.SMInputCursor;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HandbookXmlParser {
	
	private static final String RULE_CONTENT = "RuleContent"; 	
	private static final String SONARQUBE = "Sonarqube"; 
	private static final String RULE_DESC = "RuleDesc"; 

	
	private static final ImmutableList<String> IGNORE = ImmutableList.of(
			"RuleUID",
			"RuleHist",
			"IsParent",
			"IsSon",
			"Severity"
			);
	private SMFilter filter = new IgnoreSomeRuleElements();
	
	private static final Logger LOG = Loggers.get(HandbookXmlParser.class);
	private static final String NAMESPACE_HANDBOOX = "HANDBOOK";
	private static final List<Rule> NULL = null;
	

	public List<Rule> parseXML(InputStream hbStream) {
	  try {
	    if(hbStream.available() == 0) {
	      LOG.warn("Handbook.xml is empty, no rules will be loaded");
	      return NULL;
	    }
	    List<Rule> rules = new ArrayList<>();
	    collectRules(hbStream, rules);					
	    return rules;
	    
	  } catch (XMLStreamException e) {
	    if (LOG.isDebugEnabled()) {
	      LOG.debug("Error when parsing handbook.xml file at line: {}\n{}",e.getLocation().getLineNumber(),e.getMessage());
	    }
	    throw new IllegalStateException(e);
	  } catch (IOException e) {
	    throw new IllegalStateException("Unable to read handbook.xml in jar ressources",e);
    }
	}

	private void collectRules(InputStream hbStream, List<Rule> rules) throws XMLStreamException {		
		SMInputFactory xmlFactory = new SMInputFactory(XMLInputFactory.newInstance());
		SMInputCursor cursor = xmlFactory.rootElementCursor(hbStream).advance();
		SMInputCursor ruleCursor = cursor.childElementCursor(new QName(NAMESPACE_HANDBOOX, "Rule")).advance();
		
		while (ruleCursor.asEvent() != null) {
			Rule r = new Rule();
			r.ruleKey = ruleCursor.getAttrValue("UID");	
			if (StringUtils.isEmpty(r.ruleKey)) {
				throw new XMLStreamException("No mandatory RuleUID is defined", ruleCursor.getCursorLocation());
			}
			collectRule(r, ruleCursor.childCursor(filter).advance());
			rules.add(r);
			ruleCursor.advance();
		}
		cursor.getStreamReader().closeCompletely();		
	}
	
	private void collectRule(Rule r, SMInputCursor sectionCursor) throws XMLStreamException {
		while(sectionCursor.asEvent() != null) {
			switch(sectionCursor.getLocalName()) {
			case RULE_CONTENT:
				collectRuleContent(r, sectionCursor.childCursor(filter).advance());
				break;
			case SONARQUBE:
				collectRuleSQ(r, sectionCursor.childCursor(filter).advance());
				break;
			case RULE_DESC:
				collectRuleDesc(r, sectionCursor.childCursor(filter).advance());
				break;
			default:
				// Nothing, ignore other tags
			}
			sectionCursor.advance();
		}
	}

	private void collectRuleContent(Rule r, SMInputCursor cursor) throws XMLStreamException {
		while (cursor.asEvent() != null) {
			switch (cursor.getLocalName()) {
			case "Name":
				r.name = cursor.getElemStringValue();
				break;
			case "ParentUID":
			  r.parentUid = cursor.getElemStringValue();
			  break;
			case "Technology":
        r.technology = cursor.getElemStringValue();
        break;
			case "ApplicationFields":
        r.applicationFields = cursor.getElemStringValue();
        break;
			case "Category":
				r.category = cursor.getElemStringValue();
				break;
			case "SubCategory":
				r.subCategoty = cursor.getElemStringValue();
				break;
			case "Rationale":
				r.rationale = cursor.getElemStringValue();
				break;
			case "ShortDesc":
				r.shortDescription = cursor.getElemStringValue();
				break;
			case "LongDesc":
				r.longDescription = cursor.getElemStringValue();
				break;
			default:
				// Nothing. Ignore other element names.
			}
			cursor.advance();
		}
	}

	private void collectRuleSQ(Rule r, SMInputCursor cursor) throws XMLStreamException {
		while (cursor.asEvent() != null) {
			switch (cursor.getLocalName()) {
			case "SonarType":
				r.type = cursor.getElemStringValue();
				break;
			case "SonarSeverity":
				r.sonarSeverity = cursor.getElemStringValue();
				break;
			case "RemediationEffort":
				r.remediationEffort = cursor.getElemStringValue();
				break;
			case "SonarTag":
				r.tag = cursor.getElemStringValue();
				break;			
			default:
				// Nothing. Ignore other element names.
			}
			cursor.advance();
		}

	}
	private void collectRuleDesc(Rule r, SMInputCursor cursor) throws XMLStreamException {
		while (cursor.asEvent() != null) {
			switch (cursor.getLocalName()) {
			case "GoodExDesc":
				r.goodExDesc = cursor.getElemStringValue();
				break;
			case "GoodExample":
				r.goodExampleRef = cursor.getElemStringValue();
				break;
			case "BadExDesc":
				r.badExDesc = cursor.getElemStringValue();
				break;
			case "BadExample":
				r.badExampleRef = cursor.getElemStringValue();
				break;
			case "FigureDesc":
				r.figureDesc = cursor.getElemStringValue();
				break;
			case "Figure":
				r.figure = collectFigureRef(cursor);
				break;
			default:
				// Nothing. Ignore other element names.
			}
			cursor.advance();
		}
	}
	
	private FigureSvg collectFigureRef(SMInputCursor cursor) throws XMLStreamException {
		FigureSvg f = null;
		String fileRef=cursor.getAttrValue("fileref");
		if(fileRef!=null) {
			return new FigureSvg(fileRef,cursor.getAttrValue("height"),cursor.getAttrValue("width"));			
		}
		return f;
	}

	private class IgnoreSomeRuleElements extends SMFilter {
		
		@Override
		public boolean accept(SMEvent evt, SMInputCursor caller) throws XMLStreamException {
			if (! evt.hasLocalName()) {
				return false;
			}
			return ! IGNORE.contains(caller.getLocalName());
		}
	}
	
	
	

}
