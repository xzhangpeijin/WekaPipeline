package org.bcl.weka.pipeline.mimic;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;
import weka.core.neighboursearch.LinearNNSearch;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bcl.weka.pipeline.util.ClassificationRunner;
import org.bcl.weka.pipeline.util.SortFile;

/**
 * Abstract class for running generic Weka pipelines
 * 
 *     String[] bayesOptions = {"-D", "-Q", "weka.classifiers.bayes.net.search.local.TAN", "--", 
        "-S", "BAYES", "-E", "weka.classifiers.bayes.net.estimate.SimpleEstimator",
        "--", "-A", "0.5"};
    BayesNet bayes = new BayesNet();
    bayes.setOptions(bayesOptions);
    result.add(new ClassifierPair(bayes, "TAN Bayes"));
 */
public class MimicRunner
{
	public MimicRunner(File[] inputfiles, File outputdirectory) throws Exception
	{    
		File outputfile = new File(outputdirectory.getPath() + File.separator + "WekaResults.csv");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile, true)));

		out.println("Filename,Algorithm,PctCorrect,PctIncorrect,AUC,TPRate,TNRate,Precision,Recall,FScore");

		for(int x = 0; x < inputfiles.length; x++)
		{
			System.err.println(inputfiles[x].getName());
			Instances data;
			if(inputfiles[x].getPath().substring(inputfiles[x].getPath().length() - 3).equals("csv"))
			{
				CSVLoader loader = new CSVLoader();
				loader.setSource(inputfiles[x]);
				data = loader.getDataSet();
			}
			else
			{
				BufferedReader reader = new BufferedReader(new FileReader(inputfiles[x]));
				data = new Instances(reader);
				reader.close();
			}
			data.setClassIndex(data.numAttributes() - 1);
			
			int yesindex, noindex;
			if (data.classAttribute().value(0).equals("Y")) {
			  yesindex = 0;
			  noindex = 1;
			} else {
			  yesindex = 1;
			  noindex = 0;
			}
			
			String[] removeOptions = {"-R", MimicMain.TO_REMOVE};
			Remove remove = new Remove();
			remove.setOptions(removeOptions);
			remove.setInputFormat(data); 
			data = Filter.useFilter(data, remove);

			// Discretize into three equal frequency bins
			String[] discretizeOptions = {"-F", "-B", "3", "-M", "-1.0", "-R", "first-last"};
			Discretize discretize = new Discretize();
			discretize.setOptions(discretizeOptions);
			discretize.setInputFormat(data);
			data = Filter.useFilter(data, discretize);

			// Replace all missing values with NA bin
			String[] replaceOptions = {"-A", "first-last", "-N", "NA", };
			ReplaceMissingWithUserConstant replace = new ReplaceMissingWithUserConstant();
			replace.setOptions(replaceOptions);
			replace.setInputFormat(data);
			data = Filter.useFilter(data, replace);

			Evaluation eval;
			
//			for (int i = 0; i < classifiers.size(); i++) {
//			  ClassifierPair pair = classifiers.get(i);
//			  String desc = pair.getDescription();
//			  Classifier classifier = pair.getClassifier();
//			  
//			  System.out.println("Starting " + desc);
//			  
//			  eval = new Evaluation(data);
//			  eval.crossValidateModel(classifier, data, 10, new Random(1));
//			  out.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%n", inputfiles[x].getName(), desc,
//			      Utils.doubleToString(eval.pctCorrect(), 4),
//			      Utils.doubleToString(eval.pctIncorrect(), 4),
//			      Utils.doubleToString(eval.weightedAreaUnderROC(), 4),
//			      Utils.doubleToString(eval.truePositiveRate(yesindex), 4),
//			      Utils.doubleToString(eval.truePositiveRate(noindex), 4),
//			      Utils.doubleToString(eval.weightedPrecision(), 4),
//			      Utils.doubleToString(eval.weightedRecall(), 4),
//			      Utils.doubleToString(eval.weightedFMeasure(), 4));
//			  out.flush();
//			  
//			  System.out.println("Done with " + desc);
//			}
		}
		out.flush();
		out.close();
	}
}
