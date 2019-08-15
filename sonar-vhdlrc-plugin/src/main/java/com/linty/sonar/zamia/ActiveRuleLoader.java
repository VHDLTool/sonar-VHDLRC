package com.linty.sonar.zamia;

import com.linty.sonar.plugins.vhdlrc.rules.VhdlRulesDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ActiveRuleLoader {
  
  private static final String RC_HANDBOOK_PARAMETER_XML = "rc_handbook_parameters.xml";
  private static final String RC_HANDBOOK_PARAMETERS_PATH = "/" + ZamiaRunner.CONFIGURATION + "/" + RC_HANDBOOK_PARAMETER_XML; 
  
  private Collection<ActiveRule> sonarActiveRules;
  private List<String> activeRuleKeys;
  private static final Logger LOG = Loggers.get(ActiveRuleLoader.class);

  public ActiveRuleLoader(ActiveRules activeRules) {
    PRINT("\n_______________START__________________\n");//TODO
    this.sonarActiveRules = activeRules.findByRepository(VhdlRulesDefinition.VHDLRC_REPOSITORY_KEY);
    PRINT("size of activeRulesList = " + this.sonarActiveRules.size());//TODO   
  }

  public Path makeRcHandbookParameters() {
    activeRuleKeys = new ArrayList<>();
    try { 
      InputStream source = BuildPathMaker.class.getResourceAsStream(RC_HANDBOOK_PARAMETERS_PATH);
      return writeParametersInXml(source);
    } catch (IOException e) {
      //TODO
    } catch (ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  protected Path writeParametersInXml(InputStream source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
    //Create a Temporary xml file with a random name
    Path target = Files.createTempFile("target",".xml");
    target.toFile().deleteOnExit();
    // write the content into xml file
    Document doc = DocumentBuilderFactory
      .newInstance()
      .newDocumentBuilder()
      .parse(source);
    
    //for each node
    
    // write the content into xml file
    TransformerFactory
    .newInstance()
    .newTransformer()
    .transform(new DOMSource(doc), new StreamResult(target.toFile()));

    return target.toAbsolutePath();
  }

  private void writeParam(Document doc, ActiveRule r) {
    
  }

  public List<String> activeRuleKeys() {
    if(activeRuleKeys != null) {
      if(activeRuleKeys.isEmpty()) {
        LOG.warn("List of actives rules from rc_handbook_parameter.xml is empty" );
      }
      return activeRuleKeys;
    } else {
      /*Should not be used before makeRcHandbookParameters()
        has been called to load the list with RuleKeys parsed
        in rc_handbook_parameters.xml first
       */
      throw new IllegalStateException("activeRuleKeys() was called before makeRcHandbookParameters()");
    }
  }
  
  //TODO: Delete this function meant only for testing
  public static void PRINT(String s) {
    System.out.println(s);
  }

}
