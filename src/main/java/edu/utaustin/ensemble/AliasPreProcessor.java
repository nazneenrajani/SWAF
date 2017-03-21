
package edu.utaustin.ensemble;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * AliasPreProcessor Class:
 * 
 * Used to take data extractor output and eliminate 
 * alias extractions
 * 
 * 
 */
public class AliasPreProcessor {

	/**
	 * @param args
	 */
	
	Set<String> fillsSet=new HashSet<String>();
	Set<String> perSlots = new HashSet<String>();
	Set<String> orgSlots = new HashSet<String>();
	Set<String> specialAliasSlots = new HashSet<String>(); //slots for which strong equals should be used 
	Set<String> citySlots = new HashSet<String>();
	Set<String> countrySlots = new HashSet<String>();
	String runid = new String("");
	AliasWrapper aw = null;
	boolean doAlias = false;
	String year = new String("");
	
	public AliasPreProcessor(String yr, String wikiFilePath, String orgSuffixPath) throws IOException{
		aw = new AliasWrapper(wikiFilePath,orgSuffixPath,10);
		year = yr;
	}
	
	/*
	 *  this function is keep track of what slots have fills or some entry
	 *  from classifier result. The KBP mandates to have NIL slot fill if
	 *  no suitable fill is found. So, using this book keeping, we can append
	 *  NIL slot fills for slots with no fills from classifier results.
	 *  
	 */
	public void populateSlotFills(){
		
		//add per slots
		perSlots.add("per:alternate_names");
		perSlots.add("per:date_of_birth");
		perSlots.add("per:age");
		perSlots.add("per:country_of_birth");
		perSlots.add("per:stateorprovince_of_birth");
		
		perSlots.add("per:city_of_birth");
		perSlots.add("per:origin");
		perSlots.add("per:date_of_death");
		perSlots.add("per:country_of_death");
		perSlots.add("per:stateorprovince_of_death");
		
		perSlots.add("per:city_of_death");
		perSlots.add("per:cause_of_death");
		perSlots.add("per:countries_of_residence");
		perSlots.add("per:statesorprovinces_of_residence");
		perSlots.add("per:cities_of_residence");
		
		perSlots.add("per:schools_attended");
		perSlots.add("per:title");
		perSlots.add("per:employee_or_member_of");
		perSlots.add("per:religion");
		perSlots.add("per:spouse");
		
		perSlots.add("per:children");
		perSlots.add("per:parents");
		perSlots.add("per:siblings");
		perSlots.add("per:other_family");
		perSlots.add("per:charges");
		
		
		//add org slots
		orgSlots.add("org:alternate_names");
		orgSlots.add("org:political_religious_affiliation");
		orgSlots.add("org:top_members_employees");
		orgSlots.add("org:number_of_employees_members");
		orgSlots.add("org:members");
		
		orgSlots.add("org:member_of");
		orgSlots.add("org:subsidiaries");
		orgSlots.add("org:parents");
		orgSlots.add("org:founded_by");
		orgSlots.add("org:date_founded");
		
		orgSlots.add("org:date_dissolved");
		orgSlots.add("org:country_of_headquarters");
		orgSlots.add("org:stateorprovince_of_headquarters");
		orgSlots.add("org:city_of_headquarters");
		orgSlots.add("org:shareholders");
		orgSlots.add("org:website");
	
		//these slots have names of person or orgs as fills
		specialAliasSlots.add("org:top_members_employees");
		//specialAliasSlots.add("per:title");		
		specialAliasSlots.add("org:alternate_names");
		specialAliasSlots.add("per:employee_or_member_of");
		//specialAliasSlots.add("per:alternate_names");
		specialAliasSlots.add("org:parents");
		//specialAliasSlots.add("per:spouse");
		//specialAliasSlots.add("per:parents");
		specialAliasSlots.add("org:founded_by");
		specialAliasSlots.add("org:subsidiaries");
		//specialAliasSlots.add("per:siblings");
		//specialAliasSlots.add("per:children");
		//specialAliasSlots.add("per:other_family");
		specialAliasSlots.add("org:members");
		specialAliasSlots.add("");
		specialAliasSlots.add("");
		
		//these slots have cities as fills
		citySlots.add("per:cities_of_residence");
		citySlots.add("per:city_of_birth");
		citySlots.add("per:city_of_death");
		citySlots.add("org:city_of_headquarters");	
		
		//these slots have countries as fills
		countrySlots.add("per:countries_of_residence");
		countrySlots.add("per:country_of_birth");
		countrySlots.add("per:country_of_death");
		countrySlots.add("org:country_of_headquarters");	
	}

	/*
	 * a strict alias equal function
	 * that considers two fills to be alias 
	 * if even a single word in both fills match
	 */
	public boolean aliasEquals(String fill1, String fill2){
		boolean result = false;
		Set<String> fill1Parts = new HashSet<String>();
		Set<String> fill2Parts = new HashSet<String>();
		int commonCount=0;
		for(String s: fill1.split(" ")){
			fill1Parts.add(s);
		}
		for(String s: fill2.split(" ")){
			fill2Parts.add(s);
		}
		
		for(String s : fill1Parts){
			if(fill2Parts.contains(s)){
				commonCount++;
			}				
		}
		
		if(commonCount>fill1Parts.size()/2 && commonCount>fill2Parts.size()/2){
			result=true;
		}
		
		return result;
		
	}
	/*
	 * a function to decide which type of
	 * equals must be used based on slot type
	 */
	public boolean myEquals(String fill1, String fill2, String slotType){
		if(specialAliasSlots.contains(slotType) || slotType.equals("per:title")){
			return aliasEquals(fill1,fill2);			
		}
		else return (fill1.equalsIgnoreCase(fill2));
	}

	public void processExtractorOutput(String infile, String outfile, String year) throws IOException{
		int nLines=0, nSkipped=0, nAliasFillsSkipped=0;
		int nCityFillsSkipped=0, nCountryFillsSkipped=0;
		BufferedReader csv = null;
		csv = new BufferedReader(new FileReader(infile));
		String line;
		csv.readLine(); //skip header
		int fillIndex=4;
		
		BufferedWriter out = null;
		out = new BufferedWriter(new FileWriter(outfile));
	
		if(!year.equals("2013") && !year.equals("2014")){
			System.out.println("ERR: Invalid year");
		}
		while ((line = csv.readLine()) != null) {
			nLines++;
			String[] data = line.split("\t",6);
			runid=data[2];		
		
			String query_id = data[0] + "~" + data[1] + "~" + data[fillIndex];
			String key = data[0] + "~" + data[1];
			String slotType = new String(data[1]);

			//check for alias
			String newfill = new String(data[fillIndex]);
			List<String> existingFills = new ArrayList<String>();
			boolean aliasFound = false;

			for(String k : fillsSet){
				if(k.contains(key)){
					String[] parts = k.split("~");
					existingFills.add(parts[2]);
				}
			}

			for(String oldFill : existingFills){

				List<String> aliases = null;
				if(slotType.equals("per:title")==false)
					aliases = aw.getAliases(oldFill);
				else
					aliases = new ArrayList<String>();

				aliases.add(oldFill);
				for(String a : aliases){
					if(myEquals(newfill,a,slotType)){
						System.out.println("alias found for slottype-"+slotType+" fill- "+newfill+" -from oldfill -"+oldFill);
						aliasFound = true;
						break;
					}
				}
				if(aliasFound==true){
					break;
				}
			}

			if(aliasFound==true){
				//skip the fill
				nAliasFillsSkipped++;
				continue;
			}
			else{
				fillsSet.add(query_id);
				out.write(line);
			}
		}
		
		

		csv.close();
		out.close();
		System.out.println("Total lines: "+nLines);
		System.out.println("Skipped lines: "+nSkipped);
		System.out.println("Alias fills skipped: "+nAliasFillsSkipped);
		System.out.println("City fills skipped: "+nCityFillsSkipped);
		System.out.println("Country fills skipped: "+nCountryFillsSkipped);
	}
	/*
	 * args[0]	-	input file comprising of the slot fills and classifier result 
	 * args[1]	-	output file to write slot fills in KBP format based on classifier result				
	 * args[2]  -	year
	 * args[3]  -   wiki links path
	 * args[4]  -   org suffixes path
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fname=new String(args[0]);
		String outFile=new String(args[1]);
		String year = new String(args[2]);
		AliasPreProcessor app = new AliasPreProcessor(year,args[3],args[4]);
		app.populateSlotFills();
		app.processExtractorOutput(fname,outFile,year);
	}

}

