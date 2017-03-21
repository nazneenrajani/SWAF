package edu.utaustin.ensemble;

import weka.classifiers.*;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SVMClassifier {
	String[] prediction;
	
	public void classify(String train_file, String test_file) throws Exception{
		DataSource source = new DataSource(train_file);
		Instances data = source.getDataSet();
		DataSource test = new DataSource(test_file);
		Instances data_test = test.getDataSet();
		if (data.classIndex() == -1)
			data.setClassIndex(data.numAttributes() - 1);
		if (data_test.classIndex() == -1)
			data_test.setClassIndex(data.numAttributes() - 1);
		//InputMappedClassifier = new InputMappedClassifier();
		LibSVM svm = new LibSVM();
		//svm_parameter pre= new svm_parameter();
	   // pre.kernel_type= svm_parameter.POLY;
	   // pre.gamma= 3;
	    //pre.degree=1;
		svm.buildClassifier(data);
		prediction = new String[data_test.numInstances()];
		for (int i = 0; i < data_test.numInstances(); i++) {
			double pred = svm.classifyInstance(data_test.instance(i));
			System.out.print("ID: " + data_test.instance(i).value(0));
			System.out.print(", actual: " + data_test.classAttribute().value((int) data_test.instance(i).classValue()));
			System.out.println(", predicted: " + data_test.classAttribute().value((int) pred));
			prediction[i] = data_test.classAttribute().value((int) pred);
		}
	}
}
