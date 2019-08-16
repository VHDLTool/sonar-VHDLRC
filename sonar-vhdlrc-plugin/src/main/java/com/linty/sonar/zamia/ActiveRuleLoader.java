package com.linty.sonar.zamia;

import com.linty.sonar.params.ParamTranslator;
import com.linty.sonar.params.ZamiaIntParam;
import com.linty.sonar.params.ZamiaRangeParam;
import com.linty.sonar.params.ZamiaStringParam;
import com.linty.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
import static com.linty.sonar.params.ParamTranslator.*;

public class ActiveRuleLoader {
    
  private static final String NAME_SPACE  = "hb:";
  private static final String RULE        = "Rule";
  private static final String RULE_PARAMS = "RuleParams";
  private static final String PARAM_ID    = "ParamID";
  private static final String VALUE       = "Value";
  private static final String VALUE_MIN   = "ValueMin";
  private static final String VALUE_MAX   = "ValueMax";
  private static final String UID = "UID";
  private static final String REPO_KEY = VhdlRulesDefinition.VHDLRC_REPOSITORY_KEY;  
  
  private Collection<ActiveRule> sonarActiveRules;
  private List<String> selectedRuleKeys;
  private final String RESSOURCE;
  private Document doc;
  
  private static final Logger LOG = Loggers.get(ActiveRuleLoader.class);

  public ActiveRuleLoader(ActiveRules activeRules, String ressource) {
    PRINT("\n_______________START__________________\n");//TODO
    this.sonarActiveRules = activeRules.findByRepository(REPO_KEY);
    this.RESSOURCE = ressource;
    PRINT("size of activeRulesList = " + this.sonarActiveRules.size());//TODO   
  }

  public Path makeRcHandbookParameters() {
    try { 
      InputStream sourceIs = BuildPathMaker.class.getResourceAsStream(this.RESSOURCE);
      //If null throw expection
      return writeParametersInXml(sourceIs);
    } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
      throw new IllegalStateException(e);
    }
  }

  protected Path writeParametersInXml(InputStream source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
    //Initiates the list of active rules to put in rc_selected_rules.xml later
    selectedRuleKeys = new ArrayList<>();
    
    //Create a Temporary xml file with a random name
    Path target = Files.createTempFile("target",".xml");
    target.toFile().deleteOnExit();
    
    // write the content into xml file
    this.doc = DocumentBuilderFactory
      .newInstance()
      .newDocumentBuilder()
      .parse(source);
    
    NodeList ruleNodes = doc.getElementsByTagName(hb(RULE));
    //PRINT("NodeList zise: " + String.valueOf(ruleNodes.getLength()));
    
    for(int i = 0; i < ruleNodes.getLength(); i++) {
      treatRuleNode(ruleNodes.item(i));
    }
    
    // write the content into xml file
    Transformer transfo = TransformerFactory
    .newInstance()
    .newTransformer();
    transfo.setOutputProperty(OutputKeys.INDENT, "yes");
    transfo.transform(new DOMSource(this.doc), new StreamResult(target.toFile()));

    return target.toAbsolutePath();
  }

  private void treatRuleNode(Node ruleNode) {
    String nodeUid = ruleNode.getAttributes().getNamedItem(UID).getNodeValue();
    PRINT(nodeUid);
    //Try to get the matching rule of UID in sonar activeRules
    ActiveRule sonarRule = sonarActiveRules.stream()
      .filter(r -> r.ruleKey().equals(RuleKey.of(REPO_KEY, nodeUid)))
      .findFirst()
      .orElse(null);
    //If present
    if(sonarRule != null) {
      PRINT("> Active in sonar");//TODO
      selectedRuleKeys.add(nodeUid); //Add it to the list of selected rules
      if(!sonarRule.params().isEmpty() && ParamTranslator.hasZamiaParam(sonarRule)) {
        PRINT("> has param in sonar");//TODO
        writeParam(ruleNode, sonarRule);
      }
    }
    
  }

  private void writeParam(Node ruleNode, ActiveRule sonarRule) {
    //printNodes(ruleNode.getChildNodes());//TODO
    Node paramNode = initNode(ruleNode, hb(RULE_PARAMS));
    if (ParamTranslator.hasStringParam(sonarRule)) {
      writeStringParams(paramNode, sonarRule);
    } else if (ParamTranslator.hasIntParam(sonarRule)) {
      writeIntParam(paramNode, sonarRule);
    } else {
      writeRangeParam(paramNode, sonarRule);     
    }
    //Don't write anything if param is not type anything
   // printNodes(ruleNode.getChildNodes());//TODO
  }


  private void writeStringParams(Node paramNode, ActiveRule sonarRule) {
   // printNodes(paramNode.getChildNodes());//TODO
//    PRINT("Writing string param");
//    Stream.of(sonarRule.param(ZamiaStringParam.PARAM_KEY).split(","))
//    .collect(Collectors.toList())
//    .forEach(action);
//    String position = ParamTranslator.positionOf("");
//    String value = ParamTranslator.stringValueOf( "");
//    
//    //hb:StringParam
//    Node strParamNode = paramNode.appendChild(this.doc.createElement(hb(STRING_PARAM)));
//    
//    //hb:ParamID
//    strParamNode.appendChild(paramLine(hb(PARAM_ID), "P1"));    
//    //hb:Relation
//    strParamNode.appendChild(paramLine(hb(POSITION),""));    
//    //hb:Value
//    strParamNode.appendChild(paramLine(hb(VALUE), value));    
  }

  private void writeIntParam(Node paramNode, ActiveRule sonarRule) {
    // TODO Auto-generated method stub
    PRINT("Writing int param");
    String relation = ParamTranslator.relationOf(sonarRule.param(ZamiaIntParam.RE_KEY));
    String value = sonarRule.param(ZamiaIntParam.LI_KEY);
    
    //hb:IntParam
    Node intParamNode = paramNode.appendChild(this.doc.createElement(hb(INT_PARAM)));
    
    //hb:ParamID
    intParamNode.appendChild(paramLine(hb(PARAM_ID), "P1"));    
    //hb:Relation
    intParamNode.appendChild(paramLine(hb(RELATION),relation));    
    //hb:Value
    intParamNode.appendChild(paramLine(hb(VALUE), value));
  }

  private void writeRangeParam(Node paramNode, ActiveRule sonarRule) {
    // TODO Auto-generated method stub
    PRINT("Writing range param");
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
    clearOldParams(node);
    return node.insertBefore(this.doc.createElement(nodeName), node.getLastChild());
  }

  private void clearOldParams(Node ruleNode) {
    NodeList nodes = ruleNode.getChildNodes();
    for(int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if(hb(RULE_PARAMS).equals(node.getNodeName())) {
        ruleNode.removeChild(node);
      }
    }   
  }

  public List<String> activeRuleKeys() {
    if(selectedRuleKeys != null) {
      if(selectedRuleKeys.isEmpty()) {
        LOG.warn("List of actives rules from rc_handbook_parameter.xml is empty" );
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
  
  //TODO: Delete this function meant only for testing
  public static void PRINT(String s) {
    System.out.println(s);
  }
  
  //TODO delete
  public static void printNodes(NodeList nl) {
    System.out.println("----Node list------");
    for(int i = 0; i < nl.getLength(); i++) {
      System.out.println(i + ")" + nl.item(i).getNodeName() + " : " +  nl.item(i).getTextContent());
    }
    System.out.println("----end------\n");
  }

}
