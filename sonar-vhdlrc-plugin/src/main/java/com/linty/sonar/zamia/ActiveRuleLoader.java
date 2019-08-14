package com.linty.sonar.zamia;

import com.linty.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;

public class ActiveRuleLoader {
  
  private Collection<ActiveRule> activeRules;
  private List<String> activeRuleKeys;

  public ActiveRuleLoader(ActiveRules activeRules) {
    //this.activeRules = activeRules.findByRepository(VhdlRulesDefinition.VHDLRC_REPOSITORY_KEY);
  }

  public Path makeRcHandbookParameters() {
    // TODO Auto-generated method stub
    return Paths.get("src/test/parameters/rc_parameters/source.xml");
  }

  public Object activeRuleKeys() {
    return activeRuleKeys;
  }

}
