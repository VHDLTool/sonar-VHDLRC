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
import java.util.ArrayList;
import java.util.List;

public class HandbookXmlParser {
	
	private static final Logger LOG = Loggers.get(HandbookXmlParser.class);
	private static final String NAMESPACE_HANDBOOX = "HANDBOOK";
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
			LOG.error("Unable to parse xml file: {}",file.getPath());
			if (LOG.isDebugEnabled()) {
				LOG.debug("XML file parsing failed because of : {}", ExceptionUtils.getFullStackTrace(e));
			}
			throw new IllegalStateException(e);
		}
		return NULL;
	}

	private void collectRules(File file, List<Rule> rules) throws XMLStreamException {
		SMInputFactory xmlFactory = new SMInputFactory(XMLInputFactory.newInstance());
		SMInputCursor cursor = xmlFactory.rootElementCursor(file).advance();
		SMInputCursor ruleCursor = cursor.childElementCursor(new QName(NAMESPACE_HANDBOOX, "Rule")).advance();
		Rule r = new Rule();
		rules.add(r);		
	}


	
	
	

}
