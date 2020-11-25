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
package com.lintyservices.sonar.zamia;

import com.lintyservices.sonar.params.ParamTranslator;
import com.lintyservices.sonar.params.ZamiaIntParam;
import com.lintyservices.sonar.params.ZamiaRangeParam;
import com.lintyservices.sonar.params.ZamiaStringParam;
import com.lintyservices.sonar.plugins.vhdlrc.rules.HandbookYosysRulesXmlParser;
import com.lintyservices.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static com.lintyservices.sonar.params.ParamTranslator.*;

public class ActiveRuleLoader {

  private static final String NAME_SPACE = "hb:";
  private static final String RULE = "Rule";
  private static final String RULE_PARAMS = "RuleParams";
  private static final String PARAM_ID = "ParamID";
  private static final String VALUE = "Value";
  private static final String VALUE_MIN = "ValueMin";
  private static final String VALUE_MAX = "ValueMax";
  private static final String UID = "UID";
  private static final String REPO_KEY = VhdlRulesDefinition.VHDLRC_REPOSITORY_KEY;

  private Collection<ActiveRule> sonarActiveRules;
  private List<String> selectedRuleKeys;
  private Set<String> yosysRules;
  private final String ressource;
  private Document doc;

  private static final Logger LOG = Loggers.get(ActiveRuleLoader.class);

  public ActiveRuleLoader(ActiveRules activeRules, String ressource) {
    this.sonarActiveRules = activeRules.findByRepository(REPO_KEY);
    this.ressource = ressource;
  }

  public Path makeRcHandbookParameters() {
    try {
      InputStream sourceIs = BuildPathMaker.class.getResourceAsStream(this.ressource);
      if (sourceIs != null) {
        return writeParametersInXml(sourceIs);
      }
      throw new FileNotFoundException("rc_handbook_parameters.xml not found");
    } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
      throw new IllegalStateException(e);
    }
  }


  protected Path writeParametersInXml(InputStream source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
    //Initiates the list of active rules to put in rc_selected_rules.xml later
    selectedRuleKeys = new ArrayList<>();
    yosysRules = new HandbookYosysRulesXmlParser().parseXML(getClass().getClassLoader().getResourceAsStream("configuration/HANDBOOK/Rulesets/handbook.xml"));
    //Create a Temporary xml file with a random name
    Path target = Files.createTempFile("target", ".xml");
    target.toFile().deleteOnExit();

    // write the content into xml file
    DocumentBuilderFactory dbf = DocumentBuilderFactory
      .newInstance();
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    this.doc = dbf
      .newDocumentBuilder()
      .parse(source);

    NodeList ruleNodes = doc.getElementsByTagName(hb(RULE));

    for (int i = 0; i < ruleNodes.getLength(); i++) {
      treatRuleNode(ruleNodes.item(i));
    }

    // write the content into xml file
    TransformerFactory tf = TransformerFactory
      .newInstance();
    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    Transformer transfo = tf
      .newTransformer();

    transfo.setOutputProperty(OutputKeys.INDENT, "yes");
    transfo.transform(new DOMSource(this.doc), new StreamResult(target.toFile()));

    return target.toAbsolutePath();
  }

  private void treatRuleNode(Node ruleNode) {
    String nodeUid = ruleNode.getAttributes().getNamedItem(UID).getNodeValue();
    //Try to get the matching rule of UID in sonar activeRules
    ActiveRule sonarRule = sonarActiveRules.stream()
      .filter(r -> r.ruleKey().equals(RuleKey.of(REPO_KEY, nodeUid)))
      .findFirst()
      .orElse(null);
    //If present
    if (sonarRule != null && !yosysRules.contains(nodeUid)) {
      selectedRuleKeys.add(nodeUid); //Add it to the list of selected rules
      if (!sonarRule.params().isEmpty() && ParamTranslator.hasZamiaParam(sonarRule)) {
        writeParam(ruleNode, sonarRule);
      }
    }

  }

  private void writeParam(Node ruleNode, ActiveRule sonarRule) {
    //hb:RuleParam
    Node paramNode = initNode(ruleNode, hb(RULE_PARAMS));
    if (ParamTranslator.hasStringParam(sonarRule)) {
      writeStringParams(paramNode, sonarRule);
    } else if (ParamTranslator.hasIntParam(sonarRule)) {
      writeIntParam(paramNode, sonarRule);
    } else {
      writeRangeParam(paramNode, sonarRule);
    }
    //Don't write anything if param is not type String, Int or Range
  }

  //Receives <hb:RuleParams><> to fill
  private void writeStringParams(Node paramNode, ActiveRule sonarRule) {

    // "*a,*b*,c" -> List{"*a" , "*b*" , "c"} each one leads to a param 
    List<String> ls = Stream.of(StringUtils.split(sonarRule.param(ZamiaStringParam.PARAM_KEY), ","))
      .collect(Collectors.toList());
    String position;
    String value;
    int i = 1;
    for (String s : ls) {
      //Translates the string expression "[?*][value][?*]
      position = ParamTranslator.positionOf(s);
      value = ParamTranslator.stringValueOf(s);
      //hb:StringParam
      Node strParamNode = paramNode.appendChild(this.doc.createElement(hb(STRING_PARAM)));

      //hb:ParamID : P1, P2, P3, ..., P[i]
      strParamNode.appendChild(paramLine(hb(PARAM_ID), "P" + i++));
      //hb:Relation
      strParamNode.appendChild(paramLine(hb(POSITION), position));
      //hb:Value
      strParamNode.appendChild(paramLine(hb(VALUE), value));
    }
  }

  //Receives <hb:RuleParams><> to fill
  private void writeIntParam(Node paramNode, ActiveRule sonarRule) {

    String relation = ParamTranslator.relationOf(sonarRule.param(ZamiaIntParam.RE_KEY));
    String value = sonarRule.param(ZamiaIntParam.LI_KEY);

    //hb:IntParam
    Node intParamNode = paramNode.appendChild(this.doc.createElement(hb(INT_PARAM)));

    //hb:ParamID
    intParamNode.appendChild(paramLine(hb(PARAM_ID), "P1"));
    //hb:Relation
    intParamNode.appendChild(paramLine(hb(RELATION), relation));
    //hb:Value
    intParamNode.appendChild(paramLine(hb(VALUE), value));
  }

  //Receives <hb:RuleParams><> to fill
  private void writeRangeParam(Node paramNode, ActiveRule sonarRule) {

    String range = ParamTranslator.rangeOf(sonarRule.param(ZamiaRangeParam.RANGE_KEY));
    String valueMin = sonarRule.param(ZamiaRangeParam.MIN_KEY);
    String valueMax = sonarRule.param(ZamiaRangeParam.MAX_KEY);

    //hb:RangeParam
    Node intParamNode = paramNode.appendChild(this.doc.createElement(hb(RANGE_PARAM)));

    //hb:ParamID
    intParamNode.appendChild(paramLine(hb(PARAM_ID), "P1"));
    //hb:Range
    intParamNode.appendChild(paramLine(hb(RANGE), range));
    //hb:ValueMin
    intParamNode.appendChild(paramLine(hb(VALUE_MIN), valueMin));
    //hb:ValueMax
    intParamNode.appendChild(paramLine(hb(VALUE_MAX), valueMax));
  }

  private Element paramLine(String element, String content) {
    Element e = doc.createElement(element);
    e.appendChild(doc.createTextNode(content));
    return e;
  }

  private Node initNode(Node node, String nodeName) {
    //clear <hb:RuleParams><>
    clearOldParams(node);
    //adds new empty node <hb:RuleParams><>
    return node.insertBefore(this.doc.createElement(nodeName), node.getLastChild());//
  }

  //If present deletes old <hb:RuleParams><> section
  private void clearOldParams(Node ruleNode) {
    NodeList nodes = ruleNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (hb(RULE_PARAMS).equals(node.getNodeName())) {
        ruleNode.removeChild(node);
      }
    }
  }

  public List<String> activeRuleKeys() {
    if (selectedRuleKeys != null) {
      if (selectedRuleKeys.isEmpty()) {
        LOG.warn("List of actives rules from rc_handbook_parameter.xml is empty");
      }
      return selectedRuleKeys;
    } else {
      /*Should not be used before makeRcHandbookParameters()
        has been called to load the list with RuleKeys parsed
        in rc_handbook_parameters.xml first
       */
      throw new IllegalStateException("activeRuleKeys() was called before makeRcHandbookParameters()");
    }
  }

  public static String hb(String element) {
    return NAME_SPACE + element;
  }

}
