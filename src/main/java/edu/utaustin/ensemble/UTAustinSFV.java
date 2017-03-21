package edu.utaustin.ensemble;

public class UTAustinSFV {

	public static void main(String[] args) throws Exception {
		String inputDir1 = "src/main/resources/2013"; //cs_2014
		String inputDir2 = "src/main/resources/2014"; //cs_2015 //2015_unsupervised
		Integer nsys = 10;//38
		String year1= "2013";
		String year2 = "2014";
		String key1 = "src/main/resources/keys/key_file_2013";
		String key2 = "src/main/resources/keys/key_file_2014";
		//String query1="src/main/resources/q_2013";
		//String query2="src/main/resources/q_2014";
		String out_file1 = "src/main/resources/2013_out";
		String out_file2 = "src/main/resources/2014_out";
		String feature_file1 = "src/main/resources/2013.arff";
		String feature_file2 = "src/main/resources/2014.arff";
		String prob_file ="src/main/resources/prob";
		String outFile=new String("src/main/resources/"+year2+"_final_");
		String aliasFlag = "false";
		
		FeatureExtractor fe1 = new FeatureExtractor(nsys);
		fe1.getFiles(inputDir1);
		for(int sys=0; sys<nsys;sys++){
			fe1.getFeatures(year1, nsys, key1, fe1.REOutputs[sys]);
		}
		for(int i=0;i<nsys;i++){
			String[] nargs=new String[3];
			nargs[0]=fe1.REOutputs[i];
			nargs[1]=key1;
			nargs[2]= new String("anydoc");
			fe1.scorers_2013[i].run(nargs);	
		}
		fe1.getSlotsAndConfidences(year1);
		fe1.writeOutput(nsys,year1,feature_file1,out_file1);
	
		FeatureExtractor fe2 = new FeatureExtractor(nsys);
		fe2.getFiles(inputDir2);
		for(int sys=0; sys<nsys;sys++){
			fe2.getFeatures(year2, nsys, key2, fe2.REOutputs[sys]);
		}
		for(int i=0;i<nsys;i++){
			String[] nargs=new String[3];
			nargs[0]=fe2.REOutputs[i];
			nargs[1]=key2;
			nargs[2]= new String("anydoc");
			fe2.scorers_2014[i].run(nargs);	
		}
		fe2.getSlotsAndConfidences(year2);
		fe2.writeOutput(nsys,year2,feature_file2,out_file2);

		SVMClassifier sc = new SVMClassifier();
		sc.classify(feature_file1,feature_file2);
		
		PostProcessor pp = new PostProcessor(aliasFlag,"","");
		pp.populateSingleValuedSlots();
		pp.populateSlotFills();
		pp.processClassifierOutput(out_file2,year2,prob_file,sc.prediction);
		pp.writeOutputFile(outFile);
		
		String[] nargs=new String[3];
		nargs[0] = outFile;
		nargs[1] = key2;
		nargs[2] = new String("anydoc");
		SFScorer sf = new SFScorer();
		sf.run(nargs);
	}
}
