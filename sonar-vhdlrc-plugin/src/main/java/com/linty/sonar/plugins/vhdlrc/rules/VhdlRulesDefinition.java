package com.linty.sonar.plugins.vhdlrc.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import com.linty.sonar.plugins.vhdlrc.rules.Rule;
import com.linty.sonar.plugins.vhdlrc.Vhdl;
import java.io.FilenameFilter;
import org.sonar.api.config.Configuration;
import org.sonar.api.internal.apachecommons.io.FilenameUtils;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;


@ServerSide
public class VhdlRulesDefinition implements RulesDefinition {

	
	public static final String HANDBOOK_PATH_KEY = "sonar.vhdlrc.handbook.path";
    public static final String DEFAULT_HANDBOOK_PATH = "rulechecker/default/VHDL_Handbook_STD-master";
    public static final String HANDBOOK_PATH_DESC = "Path to the handbook directory. The path may be absolute or relative to the SonarQube server base directory.";
	
    private static final String RULE_SETS_PATH = "RuleSets";
    private static final String EXTRAS_PATH = "Extras";
    private static List<com.linty.sonar.plugins.vhdlrc.rules.Rule> rules;
    private final ServerFileSystem serverFileSystem;
    private final Configuration configuration;
	
	public VhdlRulesDefinition (Configuration configuration,ServerFileSystem serverFileSystem) {
		this.configuration = configuration;
		this.serverFileSystem = serverFileSystem;
	}
	
	@Override
	public void define(Context context) {
		
		NewRepository repository = context
				.createRepository("vhdl-repository", Vhdl.KEY)
				.setName("VhdlRulecker");
		try {
			File hb = getHandbook();
			List<com.linty.sonar.plugins.vhdlrc.rules.Rule> rules = new HandbookXmlParser().parseXML(hb);
		}
		catch (FileNotFoundException e) {
			
		}
		
		
	}

	private File getHandbook() throws FileNotFoundException {
		String dirPath = FilenameUtils.separatorsToUnix(configuration.get(HANDBOOK_PATH_KEY).orElse(""));
		if(!dirPath.endsWith("/")) {
			dirPath = dirPath.concat("/");
		}
		dirPath = dirPath.concat(RULE_SETS_PATH);
		File dir = new File(dirPath);
		if(dir.exists()){
			return findHandbookin(dir);
		}
		else throw new FileNotFoundException("Wrong path to handbook : " + dirPath +" ; Check parameter " + VhdlRulesDefinition.HANDBOOK_PATH_KEY);
	}

	private File findHandbookin(File dir) throws FileNotFoundException {
		
        File[] fileList = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.contains("header") && name.matches(".*handbook.*\\.xml");
            }
        });
        if(fileList.length != 0) {
        	return fileList[0];
        }
        else throw new FileNotFoundException("No handbook found in : " + dir.getPath());
	}

	
}
