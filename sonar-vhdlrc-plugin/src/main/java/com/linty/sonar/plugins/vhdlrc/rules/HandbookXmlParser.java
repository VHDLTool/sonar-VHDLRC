package com.linty.sonar.plugins.vhdlrc.rules;

import org.sonar.api.internal.apachecommons.lang.exception.ExceptionUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


import com.google.common.collect.ImmutableList;


import com.linty.sonar.plugins.vhdlrc.rules.Rule;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMFilter;
import org.codehaus.staxmate.in.SMFilterFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.codehaus.staxmate.in.SimpleFilter;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HandbookXmlParser {
	
	private static final String RULE_CONTENT = "RuleContent"; 	
	private static final String SONARQUBE = "Sonarqube"; 
	private static final String RULE_DESC = "RuleDesc"; 

	
	private static final ImmutableList<String> IGNORE = ImmutableList.of(
			"RuleUID",
			"RuleHist",
			"Status",
			"Technology",
			"ApplicationFields",
			"Revision",
			"Modified",
			"Creation",
			"Version",
			"ParentUID"
			);
	private SMFilter filter = new IgnoreSomeRuleElements();
	
	private static final Logger LOG = Loggers.get(HandbookXmlParser.class);
	private static final String NAMESPACE_HANDBOOX = "HANDBOOK";//TODO : get this from configuration
	private static final List<Rule> NULL = null;
	
	private static boolean fileExists(@Nullable File file) {
		return file != null && file.exists() && file.isFile();
	}
	
	public List<Rule> parseXML(File file) {
		try {
			if (fileExists(file)) {
				if (file.length() == 0) {
					LOG.warn("File {} is empty and won't be analyzed.", file.getPath());
					return NULL;
				}
				else {
					List<Rule> rules = new ArrayList<>();
					collectRules(file,rules);					
					LOG.info("Parsing {}", file.getPath());
					return rules;
				}
			}
			else if(file==null)
				throw new NullPointerException();			
			else if(!file.exists() || !file.isFile()) {
				LOG.warn("File {} was not found or is not a file and won't be analysed", file.getPath());
				return NULL;
			}
			
		} catch (NullPointerException e) {
			LOG.error("Null argument in parseXML()");
			if (LOG.isDebugEnabled()) {
				LOG.debug(ExceptionUtils.getFullStackTrace(e));
			}
			throw new IllegalStateException(e);
			
		} catch ( XMLStreamException e) {
			LOG.error("Error when parsing xml file: {}",file.getPath());
			if (LOG.isDebugEnabled()) {
				LOG.debug("{}\nXML file parsing failed because of :{}\n{}",ExceptionUtils.getFullStackTrace(e),e.getMessage());
			}
			throw new IllegalStateException(e);
		}
		return NULL;
	}

	private void collectRules(File file, List<Rule> rules) throws XMLStreamException {		
		SMInputFactory xmlFactory = new SMInputFactory(XMLInputFactory.newInstance());
		SMInputCursor cursor = xmlFactory.rootElementCursor(file).advance();
		SMInputCursor ruleCursor = cursor.childElementCursor(new QName(NAMESPACE_HANDBOOX, "Rule")).advance();
		
		while (ruleCursor.asEvent() != null) {
			Rule r = new Rule();
			r.ruleKey = ruleCursor.getAttrValue("UID");	
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
				collectRuleContent(r, sectionCursor.childElementCursor());
				break;
			case SONARQUBE:
				collectRuleSQ(r, sectionCursor.childElementCursor());
				break;
			case RULE_DESC:
				collectRuleDesc(r, sectionCursor.childElementCursor());
				break;
			}
		}

			
		//catch {("Missing <RuleContent> at line" + sectionCursor.getCursorLocation().getLineNumber());			
			
	}

	private void collectRuleContent(Rule r, SMInputCursor contentCursor) throws XMLStreamException {
		try {
			while (contentCursor.asEvent() != null) {
				switch (contentCursor.getLocalName()) {
				case "Name":
					r.name = contentCursor.getElemStringValue();
					break;
				case "Category":
					r.category = contentCursor.getElemStringValue();
					break;
				case "SubCategory":
					r.subCategoty = contentCursor.getElemStringValue();
					break;
				case "Rationale":
					r.rationale = contentCursor.getElemStringValue();
					break;
				case "ShortDesc":
					r.shortDescription = contentCursor.getElemStringValue();
					break;
				case "LongDesc":
					r.longDescription = contentCursor.getElemStringValue();
					break;
				default:
					// Nothing. Ignore other element names.
				}
				contentCursor.advance();
			}
		} catch (XMLStreamException e) {		
			throw new XMLStreamException(errorMessage(contentCursor));
		}
	}

	private void collectRuleSQ(Rule r, SMInputCursor contentCursor) throws XMLStreamException {

	}
	private void collectRuleDesc(Rule r, SMInputCursor contentCursor) throws XMLStreamException {

	}
	
	
	private void errorMessage(String message) throws XMLStreamException {
		LOG.error(message);
		throw new XMLStreamException(message);
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
