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



import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.annotations.VisibleForTesting;
import com.linty.sonar.plugins.vhdlrc.Vhdl;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Configuration;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RuleTagFormat;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


@ServerSide
public class VhdlRulesDefinition implements RulesDefinition {
  
  public static class HbRessourceContext {
    protected InputStream getRuleset() {
      return VhdlRulesDefinition.class.getResourceAsStream(RULESET_PATH);
    }
  }
  
  public static final String VHDLRC_REPOSITORY_KEY = "vhdlrc-repository";

  public static final String  HANDBOOK_DIR = "/configuration/HANDBOOK";
  public static final String  RULESET_PATH = HANDBOOK_DIR + "/Rulesets/handbook.xml";

  private final Configuration configuration;

  private static final Logger LOG = Loggers.get(VhdlRulesDefinition.class);

  private static final Map<String,Severity> SEVERITY_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private static final Map<String,RuleType> TYPE_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private static final Map<String,String> DEBT_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			static {
				SEVERITY_MAP.put("BLOCKER",  Severity.BLOCKER);
				SEVERITY_MAP.put("CRITICAL", Severity.CRITICAL);
				SEVERITY_MAP.put("MAJOR",    Severity.MAJOR);
				SEVERITY_MAP.put("MINOR",    Severity.MINOR);//Default
				SEVERITY_MAP.put("INFO",     Severity.INFO);
				
				TYPE_MAP.put("BUG",           RuleType.BUG);
				TYPE_MAP.put("VULNERABILITY", RuleType.VULNERABILITY); 
				TYPE_MAP.put("CODE_SMELL",    RuleType.CODE_SMELL);//Default
				
				DEBT_MAP.put("Trivial", "5min");
				DEBT_MAP.put("Easy"   , "10min");//Default
				DEBT_MAP.put("Medium" , "20min");
				DEBT_MAP.put("Major"  , "1h");
				DEBT_MAP.put("High"   , "3h");
				DEBT_MAP.put("Complex", "1d");
			}
			
	
	public VhdlRulesDefinition (Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void define(Context context) {
	  defineFromRessources(context, new HbRessourceContext());
	}

	@VisibleForTesting
	public void defineFromRessources(Context context, HbRessourceContext ressourceContext){
	  NewRepository repository = context
	    .createRepository(VHDLRC_REPOSITORY_KEY, Vhdl.KEY)
	    .setName("VhdlRuleChecker");
	  InputStream rulesetXML = ressourceContext.getRuleset();
	  if(rulesetXML != null) {
	    List<com.linty.sonar.plugins.vhdlrc.rules.Rule> rules = new HandbookXmlParser().parseXML(rulesetXML); 
	    if(rules != null) {
	      new ExampleAndFigureLoader(HANDBOOK_DIR).load(rules);
	      for(com.linty.sonar.plugins.vhdlrc.rules.Rule r : rules) {
	        newRule(r,repository);
	      }
	      repository.done();
	    } else { LOG.warn("No VHDL RuleCheker rules loaded!");}
	  } else {LOG.error("handbook not found in jar ressources, re-build with {}", RULESET_PATH);}	  
	}

	@VisibleForTesting
	protected void newRule(com.linty.sonar.plugins.vhdlrc.rules.Rule r, NewRepository repository) {
		NewRule nr = repository.createRule(r.ruleKey);
		nr.setHtmlDescription(r.buildHtmlDescritpion());		
		addMetadataTo(nr,r);
		addParameters(nr,r);
	}

	private void addParameters(NewRule nr, com.linty.sonar.plugins.vhdlrc.rules.Rule r) {
    if(!r.parameters().isEmpty()) {
      r
      .parameters()
      .get(r.parameters().size()-1) //Sets sonar params according to the last parameter
      .setSonarParams(r.parameters(), nr, nr.key());
    }
    
  }

  private void addMetadataTo(NewRule nr, com.linty.sonar.plugins.vhdlrc.rules.Rule r) {
		nr
		.setInternalKey(r.ruleKey)
		.setName(r.name)
		.setSeverity(SEVERITY_MAP.getOrDefault(r.sonarSeverity, Severity.MINOR).toString())
		.setType(TYPE_MAP.getOrDefault(r.type, RuleType.CODE_SMELL))
		.setActivatedByDefault(true)
		.setDebtRemediationFunction(nr.debtRemediationFunctions().constantPerIssue(DEBT_MAP.getOrDefault(r.remediationEffort, DEBT_MAP.get("easy"))))
		;
		addTags(nr,r.tag);
		addTags(nr,r.category);
		addTags(nr,r.subCategoty);
	}

	private void addTags(NewRule nr, String tag) {
		if(!tag.isEmpty() && !"tbd".equalsIgnoreCase(tag) ) {
			 nr.addTags(RuleTagFormat.isValid(tag) ? tag : tag.toLowerCase().replaceAll("[^a-z0-9\\\\+#\\\\-\\\\.]", "-"));
		}
	}



	
}
