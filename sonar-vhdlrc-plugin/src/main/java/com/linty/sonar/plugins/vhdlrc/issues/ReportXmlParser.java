/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc.issues;


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
		SMInputFactory xmlFactory = new SMInputFactory(XMLInputFactory.newInstance());
		SMInputCursor rootCursor = xmlFactory.rootElementCursor(reportPath.toFile()).advance();
		SMInputCursor cursor = rootCursor.childCursor(filter).advance();
		if("RuleName".equals(cursor.getLocalName())){
			this.ruleKey = cursor.getElemStringValue();
			collectIssues(cursor.advance());//<rc:RuleFailure>
		} else {
			LOG.error("No RuleKey found in {}. No issues will not be imported from this report", this.reportPath.getFileName());
		}
		return this.issues;
	}

	private void collectIssues(SMInputCursor cursor) throws XMLStreamException {
		issues = new ArrayList<>();
		while(cursor.asEvent() != null){
			collectIssue(cursor.childCursor(filter).advance());
			cursor.advance();
		}
		
	}


	private void collectIssue(SMInputCursor cursor) throws XMLStreamException {
		String localName;
		Issue i = new Issue();
		i.ruleKey = this.ruleKey;
		while(cursor.asEvent() != null) {
			localName = cursor.getLocalName();
			if("File".equals(localName)) {
				i.file = Paths.get(cursor.getElemStringValue());
			} else if ("Line".equals(localName)) {
				i.line = Integer.parseInt(cursor.getElemStringValue());
			} else if (ruleKey.equals(localName)) {
				i.errorMsg = collectSonarMsg(cursor.childElementCursor().advance());
			}
			cursor.advance();
		}
		issues.add(i);
	}

	private String collectSonarMsg(SMInputCursor cursor) throws XMLStreamException {
		return cursor.childCursor(filter).advance().getElemStringValue();
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
