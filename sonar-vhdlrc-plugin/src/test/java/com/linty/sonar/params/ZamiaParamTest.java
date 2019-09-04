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

package com.linty.sonar.params;

import com.linty.sonar.plugins.vhdlrc.Vhdl;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.junit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ZamiaParamTest {
  
  public RulesDefinition.Context context;
  public NewRepository newRepo;
  public NewRule nr;
  public Repository repo;
  public Rule r;
  public List<ZamiaParam> params;
  public static final String RULE_KEY = "STD_1";
  
  
  @Before
  public void init() {
    context = new RulesDefinition.Context();
    newRepo = context.createRepository("repo-test", Vhdl.KEY);
    nr = newRepo
      .createRule(RULE_KEY)
      .setName("A name")
      .setHtmlDescription("A description");
    params = new ArrayList<>();
  }

  @Test
  public void test_string_param() {

    params.add(createStringParam("P1", "Prefix", "clk"));
    params.add(createStringParam("P2", "Contain", "i"));
    params.add(createStringParam("P3", "Suffix", "_out"));
    params.add(createStringParam("P4", "Equal", "coco"));
    //Attach params to NewRule
    this.testBuildingRule(); 
    
    assertThat(r.params().size()).isEqualTo(1);
    checkParamIs(r.params().get(0), 
      "Format", //key
      "P4Format", //name : ParamID + NAME
      "Comma seperated pattern to match, use only * symbol e.g. : *prefix** , **suffix* , **contains** , *equal*",//description
      RuleParamType.STRING.type(), //type
      "clk*,*i*,*_out,coco" //value
      );
  }
  
  @Test
  public void test_int_param() {
    params.add(createIntParam("P1", "LET", 2));//must be ignored
    params.add(createIntParam("P2", "E", 9));  //must be ignored
    params.add(createIntParam("P3", "GET", 123));
    //Attach params to NewRule
    this.testBuildingRule();

    assertThat(r.params().size()).isEqualTo(2);  
    checkParamIs(r.params().get(0), 
      "Relation", //key
      "Relation",  //name
      "Relation with the limit", //description
      "SINGLE_SELECT_LIST", //type
      ">=" //value
      );
    assertThat(r.params().get(0).type().multiple()).isFalse();
    assertThat(r.params().get(0).type().values()).containsExactlyInAnyOrder("<", "<=", "=", ">=", ">");
    checkParamIs(r.params().get(1), 
      "Limit", 
      "LimitP3", 
      "Value to be compared with ex: a < limit value",
      RuleParamType.INTEGER.type(), 
      "123"
      );
  }
  
  @Test
  public void test_range_param() {
    params.add(createRangeParam("P1", "LT_GT", 2, 7));//must be ignored
    params.add(createRangeParam("P2", "LET_GT", -5, 26));
    //Attach params to NewRule
    this.testBuildingRule();
    assertThat(r.params().size()).isEqualTo(3);
    //min param
    checkParamIs(r.params().get(0), 
      "Min  ",
      "MinP2",
      "Minimum value to respect",
      RuleParamType.INTEGER.type(),
      "-5"
      );
    //relation param
    assertThat(r.params().get(1).type().multiple()).isFalse();
    checkParamIs(r.params().get(1),
      "Range",
      "Range",
      "Inclusive and exclusive options",
      "SINGLE_SELECT_LIST",
      "<=_<"
      );
     //max param
     checkParamIs(r.params().get(2),
      "Max  ",
      "MaxP2",
      "Maximum value to respect",
      RuleParamType.INTEGER.type(),
      "26"
      );
  }
  
  @Test
  public void test_bad_string_param() {   
    try {
      params.add(createStringParam("P1", "COCORICO", "clk"));
      fail();
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isNotNull().isNotEmpty();
    }
  }
  
  //If there are non StringParam in a StringParamList, they should be ignored silently 
  @Test
  public void test_heterogeneous_list_of_params() {
    params.add(createIntParam("P1", "GT", 99));//Should be ignored
    params.add(createStringParam("P2", "Prefix", "a"));
    params.add(createStringParam("P3", "Contain", "bb"));
    params.add(createRangeParam("P4", "LT_GT", 3, 7));//should be ignored
    params.add(createStringParam("P5", "Suffix", "cc"));
    
    this.testBuildingRule();
    
    assertThat(r.params().size()).isEqualTo(1);
    checkParamIs(r.params().get(0), 
      "Format", 
      "P5Format", //ParamID + NAME
      "Comma seperated pattern to match, use only * symbol e.g. : *prefix** , **suffix* , **contains** , *equal*",
      RuleParamType.STRING.type(), 
      "a*,*bb*,*cc"
      );    
  }
  

  public ZamiaStringParam createStringParam(String hbParamId, String position, String value){
    ZamiaStringParam sp = new ZamiaStringParam()
      .setHbParamId(hbParamId)
      .setField(position);
    sp.setValue(value);
    return sp;
  }
  
  public ZamiaIntParam createIntParam(String hbParamId, String relation, int value) {
    ZamiaIntParam ip = new ZamiaIntParam()
      .setHbParamId(hbParamId)
      .setField(relation);
    ip.setValue(value);
    return ip;
  }
  
  public ZamiaRangeParam createRangeParam(String hbParamId, String range, int min, int max) {
    ZamiaRangeParam rp = new ZamiaRangeParam()
      .setHbParamId(hbParamId)
      .setField(range);
    rp
    .setMin(min)
    .setMax(max);
    return rp;
  }
    
  public void testBuildingRule() {
    params.get(params.size()-1).setSonarParams(params, nr, nr.key());
    newRepo.done();
    repo = context.repository("repo-test");
    r = repo.rule(RULE_KEY);
  }
  
  public void checkParamIs(Param p, String key, String name, String desc, String type, String value) {
    checkParamIs(p, key, name, desc, type);
    assertThat(p.defaultValue()).isEqualTo(value);
  }
    
  public void checkParamIs(Param p, String key, String name, String desc, String type) {
    assertThat(p.key()).isEqualTo(key);
    assertThat(p.name()).isEqualTo(name);
    assertThat(p.description()).isEqualTo(desc);
    assertThat(p.type().type()).isEqualTo(type);
  }
  
  

}
