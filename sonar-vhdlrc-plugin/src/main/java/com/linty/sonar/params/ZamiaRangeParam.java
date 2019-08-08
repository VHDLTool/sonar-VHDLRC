package com.linty.sonar.params;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static com.linty.sonar.params.ParamTranslator.*;

public class ZamiaRangeParam extends ZamiaParam {
  
  static {
    FIELDS = new ArrayList<>(RANGE_FIELDS_MAP.keySet());
  }

  public ZamiaRangeParam(String hbParamId) {
    super(hbParamId);
  }

  @Override
  public void setSonarParams(List<ZamiaParam> params, NewRule nr, String ruleKey) {
    // TODO Auto-generated method stub
    
  }





}
