/*
 * SonarQube Linty VHDLRC :: Plugin
 * Copyright (C) 2018-2020 Linty Services
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
package com.lintyservices.sonar.plugins.vhdlrc.rules;

import com.lintyservices.sonar.params.ZamiaIntParam;
import com.lintyservices.sonar.params.ZamiaStringParam;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleTest {

  @Test
  public void test_rule_values() {
    Rule r = new Rule();
    r.name = "A rule name";
    r.remediationEffort = "Easy";
    r.sonarSeverity = "Major";
    r.type = "Code_Smell";
    r.applicationFields = "General";
    r.parentUid = "STD_00800";
    r.category = "Traceability";
    r.subCategory = "Naming";
    r.rationale = "Labels improve readability of simulations, VHDL source code and synthesis logs.";
    r.shortDescription = "Processes are identified by a label.";
    r.longDescription = "A long description";
    r.goodExDesc = "Extracted from STD_00400_good.vhd";
    r.goodExampleRef = "STD_00400_good";
    r.goodExampleCode = "if() \n then \n else";
    r.badExDesc = "Extracted from STD_00400_bad.vhd";
    r.badExampleRef = "STD_00400_bad";
    r.parameters().add(new ZamiaIntParam().setHbParamId("1"));
    r.parameters().add(new ZamiaIntParam().setHbParamId("2"));
    r.parameters().add(new ZamiaStringParam().setHbParamId("3"));
    ((ZamiaStringParam) r.parameters().get(2)).setValue("lolo");

    assertThat(r.figureDesc).isNotNull();
    String htmlDesc = r.buildHtmlDescritpion();
    assertThat(htmlDesc).contains("Category :");
    assertThat(htmlDesc).contains("SubCategory :");
    assertThat(htmlDesc).contains("Technology :");
    assertThat(htmlDesc).contains("Application Fields :");
    assertThat(htmlDesc).contains("Parent Rule :");
    assertThat(htmlDesc).contains("Short Description");
    assertThat(htmlDesc).contains("Description");
    assertThat(htmlDesc).contains("Rational");

    assertThat(r.parameters().size()).isEqualTo(3);
    assertThat(r.parameters().get(0)).isInstanceOf(ZamiaIntParam.class);
    //System.out.println(htmlDesc);
  }

  @Test
  public void test_no_parent_rule_should_not_be_displayed() {
    Rule r = new Rule();
    r.name = "rule with no parent rule";
    String htmlDesc = r.buildHtmlDescritpion();
    assertThat(htmlDesc).doesNotContain("Parend Rule :");
    //System.out.println(htmlDesc);
  }

  @Test
  public void no_additionnal_info_should_not_be_a_desc() {
    Rule r = new Rule();
    r.name = "A other rule name";
    r.remediationEffort = "Easy";
    r.sonarSeverity = "Major";
    r.type = "BUG";
    r.category = "Traceability";
    r.subCategory = "Naming";
    r.rationale = "Labels improve readability of simulations, VHDL source code and synthesis logs.";
    r.shortDescription = "Processes are identified by a label.";
    r.longDescription = "No additional information.";
    r.goodExDesc = "Extracted from STD_00400_good.vhd";
    r.goodExampleRef = "STD_00400_good";
    r.goodExampleCode = "";
    r.badExDesc = "Extracted from STD_00400_bad.vhd";
    r.badExampleRef = "STD_00400_bad";
    r.figure = new FigureSvg();

    assertThat(r.figureDesc).isNotNull();
    assertThat(r.figure.hasImage()).isFalse();

    String htmlDesc = r.buildHtmlDescritpion();
  }

  @Test
  public void with_bad_Example_code_and_figure() {
    Rule r = new Rule();
    r.name = "A other rule name";
    r.longDescription = "No additional information.";
    r.goodExDesc = "Extracted from STD_00400_good.vhd";
    r.goodExampleRef = "STD_00400_good";
    r.goodExampleCode = "";
    r.badExDesc = "Extracted from STD_00400_bad.vhd";
    r.badExampleRef = "STD_00400_bad";
    r.badExampleCode = "code code code";
    r.figure = new FigureSvg();
    r.figure.figureRef = "STD_04200.svg";
    r.figure.figureCode = "< image>";
    r.figureDesc = "this is a figure";

    assertThat(r.figureDesc).isNotNull();
    assertThat(r.figure.height).isEqualTo("300");
    assertThat(r.figure.figureCode).isNotNull();
    String htmlDesc = r.buildHtmlDescritpion();
    //System.out.print(htmlDesc);
  }
}
