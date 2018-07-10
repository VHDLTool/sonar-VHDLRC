package com.linty.sonar.plugins.vhdlrc.rules;

import org.sonar.api.internal.apachecommons.lang.exception.ExceptionUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMFilterFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.codehaus.staxmate.in.SimpleFilter;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class HandbookXmlParser {
	
	private static final Logger LOG = Loggers.get(HandbookXmlParser.class);
	private static final String NAMESPACE_HANDBOOX = "HANDBOOK";
	
	private List<Rule> rules;
	
	private static boolean fileExists(@Nullable File file) {
		return file != null && file.exists() && file.isFile();
	}
	
	public void parseXML(File file) {
		try {
			if (fileExists(file)) {
				if (file.length() == 0) {
					LOG.warn("File {} is empty and won't be analyzed.", file.getPath());
				}
				else {
					SMInputFactory xmlFactory = new SMInputFactory(XMLInputFactory.newInstance());
					SMInputCursor cursor = xmlFactory.rootElementCursor(file).advance();
					collectRules(cursor);					
					LOG.info("Parsing " + file.getName());					
				}
			}
			else LOG.error("File was not found");

		} catch ( XMLStreamException e) {
			LOG.error("Unable to parse "+file.getName(), e);
			if (LOG.isDebugEnabled()) {
				LOG.debug("XML file parsing failed because of : {}", ExceptionUtils.getFullStackTrace(e));
			}
		}
	}

	private void collectRules(SMInputCursor cursor) throws XMLStreamException {
		SMInputCursor ruleCursor = cursor.childElementCursor(new QName(NAMESPACE_HANDBOOX, "Rule")).advance();
		
	}

	public List<Rule> getRules(){
		return rules;
	}
	
	
	

}
