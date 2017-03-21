package edu.utaustin.ensemble;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
 * PostProcessor Class:
 * 
 * Used to create output file in KBP format for evaluation.
 * Feed the output predictions from classifier and postProcessor
 * throws an output file in KBP format that can be run against
 * their provided scorer. 
 * 
 * 
 */
public class PostProcessor {
	Map<String,String> mpOutput=new HashMap<String,String>();
	Map<String,Double> mpConfidence=new HashMap<String,Double>();
	Map<Integer,Double>prob_estimate=new HashMap<Integer,Double>();
	Set<String> singleValuedSlots= new HashSet<String>();
	Set<String> filledSlots = new HashSet<String>(); //only tracks single valued slots
	Map<String,Boolean> slotfills = new HashMap<String,Boolean>();
	Map<String,String> nilfills = new HashMap<String,String>();
	Set<String> perSlots = new HashSet<String>();
	Set<String> orgSlots = new HashSet<String>();
	String runid = new String("");
	AliasWrapper aw = null;
	boolean doAlias = false;
	public PostProcessor(String aliasFlag, String wikiFilePath, String orgSuffixPath) throws IOException{
		if(aliasFlag.equals("true")){
			aw = new AliasWrapper(wikiFilePath,orgSuffixPath,10);
			doAlias=true;
		}

	}

	public void populateSlotFills(){
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

		String queryPrefix = new String("SF14_ENG_");
		for(int i=1;i<=100;i++){
			String queryID = queryPrefix+String.format("%03d", i);

			if(i<=50){
				//per type
				for(String slotName : perSlots){
					String key = new String(queryID+"~"+slotName);
					slotfills.put(key, false);
				}
			}
			else{
				//org type
				for(String slotName : orgSlots){
					String key = new String(queryID+"~"+slotName);
					slotfills.put(key, false);
				}
			}

		}

	}
	public void populateSingleValuedSlots(){
		singleValuedSlots.add("per:date_of_birth");
		singleValuedSlots.add("per:age");
		singleValuedSlots.add("per:country_of_birth");
		singleValuedSlots.add("per:stateorprovince_of_birth");
		singleValuedSlots.add("per:city_of_birth");
		singleValuedSlots.add("per:date_of_death");
		singleValuedSlots.add("per:country_of_death");
		singleValuedSlots.add("per:stateorprovince_of_death");
		singleValuedSlots.add("per:city_of_death");
		singleValuedSlots.add("per:cause_of_death");
		singleValuedSlots.add("per:religion");

		singleValuedSlots.add("org:number_of_employees_members");
		singleValuedSlots.add("org:website");
		singleValuedSlots.add("org:city_of_headquarters");
		singleValuedSlots.add("org:stateorprovince_of_headquarters");
		singleValuedSlots.add("org:country_of_headquarters");
		singleValuedSlots.add("org:date_dissolved");
		singleValuedSlots.add("org:date_founded");
	}


	public void writeOutputFile(String file) throws IOException{
		BufferedWriter out = null;
		out = new BufferedWriter(new FileWriter(file));
		for(String key : mpOutput.keySet()){
			String output_str = mpOutput.get(key);
			String line = output_str +"\n";
			out.write(line);
		}
		int slots = 0;
		for(String key : slotfills.keySet()){
			if(slotfills.get(key)==false){	
				if(!singleValuedSlots.contains(key.split("~")[1]) && nilfills.containsKey(key)){
					slots++;
					System.out.println(nilfills.get(key));
				}
				String output_string = new String("");
				String[] data= key.split("~");
				output_string =  data [0] + "\t" + data [1] + "\t" + runid + "\t" + "NIL" + "\n";
				out.write(output_string);
			}
		}
		System.out.println(slots);
		out.close();
	}
	public void processClassifierOutput(String infile, String year, String prob, String[] pred) throws IOException{
		int nLines=0, nSkipped=0, nAliasFillsSkipped=0;
		BufferedReader csv = null;
		csv = new BufferedReader(new FileReader(infile));
		String line;
		int expectedNumFields=10,predictedTargetFieldIndex=8,fillIndex=4;
		BufferedReader br = new BufferedReader(new FileReader(prob));
		int count = 0;
		while((line = br.readLine()) != null){
			count++;
			prob_estimate.put(count, Double.parseDouble(line.trim()));
		}
		br.close();
		if(year.equals("2013")){
			expectedNumFields=12;
			predictedTargetFieldIndex=10;
			fillIndex=4;
		}
		else if(year.equals("2014")){
			expectedNumFields=6;
			predictedTargetFieldIndex=6;
			fillIndex=4;
		}
		else{
			System.out.println("ERR: Invalid year");
		}
		int nil_counter=0;
		count=-1;
		while ((line = csv.readLine()) != null) {
			nLines++; count++;
			String[] data = line.split("\t");
			runid=data[2];

			if(data.length<expectedNumFields)
				continue;
			if(pred[count].trim().equals("w")){
				if(!data[fillIndex].equals("NIL") && singleValuedSlots.contains(data[1])){
					nil_counter++;
				}
				else if (!data[fillIndex].equals("NIL")){
					nilfills.put(data[0]+"~"+data[1],data [0] + "\t" + data [1] + "\t" + data [2] + "\t" + data [3]+"\t" + data [fillIndex] + "\t" + data [5]);
				}
				nSkipped++;
				continue;
			}
			String query_id = data[0] + "~" + data[1] + "~" + data[fillIndex];
			String output_string = new String("");
			if(data[fillIndex].equals("NIL")){
				output_string +=  data [0] + "\t" + data [1] + "\t" + data [2] + "\t" + data [fillIndex];
			}
			else{
				output_string +=  data [0] + "\t" + data [1] + "\t" + data [2] + "\t" + data [3];
				output_string += "\t" + data [fillIndex] + "\t" + data [5] ;
			}
			String key = data[0] + "~" + data[1];
			if(singleValuedSlots.contains(data[1])){		
				if(filledSlots.contains(key)){
					if(data[fillIndex].equals("NIL")==true)
						continue;
					Double econf = new Double("0.0");
					String oldkey = new String("");
					for(String k : mpConfidence.keySet()){
						if(k.contains(key)){
							econf = mpConfidence.get(k);
							oldkey = k;
							break;
						}
					}
					if(oldkey.contains("NIL"))
						continue;
					if(Double.compare(econf, prob_estimate.get(nLines))>0){
						mpConfidence.remove(oldkey);
						mpOutput.remove(oldkey);
						mpConfidence.put(query_id,prob_estimate.get(nLines));
						mpOutput.put(query_id, output_string);
					}
				}
				else{
					if(data[fillIndex].equals("NIL")==false)
						output_string += "\t" + prob_estimate.get(nLines);
					mpConfidence.put(query_id,new Double(prob_estimate.get(nLines)));
					mpOutput.put(query_id, output_string);
					filledSlots.add(key);
					if(slotfills.containsKey(key)){
						slotfills.remove(key);
						slotfills.put(key,true);
					}

				}
			}
			else{
				if(doAlias == true){
					String newfill = new String(data[fillIndex]);
					List<String> existingFills = new ArrayList<String>();
					boolean aliasFound = false;

					for(String k : mpConfidence.keySet()){
						if(k.contains(key)){
							String[] parts = k.split("~");
							existingFills.add(parts[2]);
						}
					}

					for(String oldFill : existingFills){
						List<String> aliases = aw.getAliases(oldFill);
						for(String a : aliases){
							if(newfill.equals(a)){
								System.out.println("alias found for -"+newfill+" -from oldfill -"+oldFill);
								aliasFound = true;
							}
						}
					}

					if(aliasFound==true){
						nAliasFillsSkipped++;
						continue;
					}
				}

				if(data[fillIndex].equals("NIL")==false)
					output_string += "\t" + prob_estimate.get(nLines);
				mpConfidence.put(query_id,new Double(prob_estimate.get(nLines)));
				mpOutput.put(query_id, output_string);
				filledSlots.add(key);
				if(slotfills.containsKey(key)){
					slotfills.remove(key);
					slotfills.put(key,true);
				}

			}
		}
		csv.close();
		System.out.println(nil_counter+" nil counter");
		System.out.println("Total lines: "+nLines);
		System.out.println("Skipped lines: "+nSkipped);
		System.out.println("Alias fills skipped: "+nAliasFillsSkipped);
	}
}
