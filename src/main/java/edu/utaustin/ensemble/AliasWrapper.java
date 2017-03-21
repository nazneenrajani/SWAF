package edu.utaustin.ensemble;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AliasWrapper {

	/**
	 * @param args
	 */
	
	TopNLinkEntityExpander expander = null;
	String wikiExpansionsFile = null;
	String orgSuffixFile = null;
	int maxCount;
	public AliasWrapper(String expansionsFile, String orgFile, int maxN) throws IOException{
		wikiExpansionsFile = new String(expansionsFile);
		orgSuffixFile = new String(orgFile);
		maxCount = maxN;
		System.out.println("creating wiki expander!");
		expander =  new TopNLinkEntityExpander(expansionsFile, maxN, true);
		System.out.println("wiki expander ready!");
	}
	
	public List<String> loadOrgSuffixes() throws IOException{
		List<String> orgSuffixes = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(orgSuffixFile));
		for (String suffix; (suffix = br.readLine()) != null;) {
			orgSuffixes.add(suffix);
		}
		br.close();
		
		return orgSuffixes;
		
	}
	public List<String> getAliases(String text) throws IOException{
		
		List<String> orgSuffixes = loadOrgSuffixes();  
	    
	    
		List<String> ruleExpansions = addRuleExpansions(text,true,orgSuffixes);
		List<String> wikiExpansions =  expander.expand(text);
		
		for(String rExp : ruleExpansions){
			if(wikiExpansions.contains(rExp) == false){
				wikiExpansions.add(rExp);
			}
		}
		
		return wikiExpansions;
	}
	 
	 
	private Collection<String> suffixExpand(String name, Collection<String> orgSuffixes) {
    List<String> alternateNames = new ArrayList<String>();
    String baseForm = name;
    // base form is shortest stripped off suffix form.
    for (String suffix : orgSuffixes) {
      if (name.endsWith(suffix) && 
          baseForm.length() > (name.length() - suffix.length())) {
        baseForm = name.substring(0, name.length() - suffix.length());
      }
    }
    if (baseForm.isEmpty()) {
      return alternateNames;
    }
    if (!baseForm.equals(name)) {
      alternateNames.add(baseForm);
    }
    for (String suffix : orgSuffixes) {
      String alternateName = baseForm + suffix;
      if (!alternateName.equals(name)) {
        alternateNames.add(alternateName);
      }
    }
    return alternateNames;
  }
	
	 public List<String> addRuleExpansions(String fill, boolean addLastName, Collection<String> orgSuffixes) 
      throws IOException {

		List<String> ruleAliases = new ArrayList<String>();
		
        String[] parts = fill.split(" ");
        String lastName = parts[parts.length - 1];
        ruleAliases.add(lastName);
       
     
        for (String expansion : suffixExpand(fill, orgSuffixes)) {
          if (!expansion.equals(fill) && !expansion.isEmpty() &&
              Character.isUpperCase(expansion.charAt(0)) &&
              !ruleAliases.contains(expansion)) { // TODO: this is quadratic.
        	  ruleAliases.add(expansion);
          }
        }
        
        return ruleAliases;
      
    
  }
  
 
	
	/*
	 * args[0]:	path to wiki links file
	 * args[1]:	path to orgSuffixes links file
	 */
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//"/home/vidhoonv/workspace/RE_ensemble/alias-data/wikiLinksData"
		String wikiFilePath = new String(args[0]);
		String orgSuffixFilePath = new String(args[1]);
		AliasWrapper aw = new AliasWrapper(wikiFilePath,orgSuffixFilePath,10);
		//System.out.println("helloworld!");
		for(String s : aw.getAliases("Bollard")){
			System.out.println(s);
		}

	}

}
