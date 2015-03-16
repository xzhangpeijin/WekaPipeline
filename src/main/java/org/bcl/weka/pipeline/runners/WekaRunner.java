package org.bcl.weka.pipeline.runners;

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

import org.bcl.weka.pipeline.util.SortFile;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;

/**
 * Runs classifiers for MIMIC experiments
 */
public class WekaRunner
{
	public static void main(String[] args) throws Exception
	{
		File dir = new File("I:\\Documents\\Dropbox\\PRIMES\\PRIMES Shared");
		ArrayList<SortFile> torun = new ArrayList<SortFile>();
		File[] files = dir.listFiles();
		for(int x = 0; x < files.length; x++)
		{	
			if(files[x].getName().contains("temporal.extract.14.2") && 
			   files[x].getName().contains(".hours.category.") && files[x].getName().contains("csv")) {
				torun.add(new SortFile(files[x].getPath()));
			}
		}
		Collections.sort(torun);
		SortFile[] run = new SortFile[torun.size()];
		run = torun.toArray(run);
		for(int x = 0; x < run.length; x++)
			System.out.println(run[x].getName());
		new WekaRunner(run, dir, "");
	}
	
	public static class ClassifierPair {
	  private final Classifier classifier;
	  private final String description;
	  
	  public ClassifierPair(Classifier classifier, String description) {
	    this.classifier = classifier;
	    this.description = description;
	  }
	  
	  public Classifier getClassifier() {
	    return classifier;
	  }
	  
	  public String getDescription() {
	    return description;
	  }
	}
	
	public static List<ClassifierPair> getClassifiers() throws Exception {
		List<ClassifierPair> result = new ArrayList<ClassifierPair>();

		// Bayes Net w/ TAN Search
		String[] bayesOptions = {"-D", "-Q", "weka.classifiers.bayes.net.search.local.TAN", "--", 
				"-S", "BAYES", "-E", "weka.classifiers.bayes.net.estimate.SimpleEstimator",
				"--", "-A", "0.5"};
		BayesNet bayes = new BayesNet();
		bayes.setOptions(bayesOptions);
		result.add(new ClassifierPair(bayes, "TAN Bayes"));

//		// Bayes Net with K2 search
//		for (int p = 1; p <= 4; p*= 2) {
//			bayesOptions = "-D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5".split(" ");
//			bayesOptions[5] = String.valueOf(p);
//			bayes = new BayesNet();
//			bayes.setOptions(bayesOptions);
//			result.add(new ClassifierPair(bayes, "K2 Bayes P" + p));
//		}
//
//		for (int k = 1; k <= 2; k++) {
//			for (double c = 1; c <= 10; c *= 10) {
//				// SVM 
//				String[] svmOptions = "-S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -model dirname -seed 1".split(" ");
//				svmOptions[21] = "C:\\\\Program Files\\\\Weka-3-7";
//				svmOptions[3] = String.valueOf(k);
//				LibSVM svm = new LibSVM();
//				svm.setOptions(svmOptions);
//				svm.setCost(c);
//				result.add(new ClassifierPair(svm, "SVM K" + k + " C" + c));
//			}
//		}
//
//		String[] logisticOptions = "-R 1.0E-8 -M -1".split(" ");
//		Logistic logistic = new Logistic();
//		logistic.setOptions(logisticOptions);
//		result.add(new ClassifierPair(logistic, "Ridge Logistic Regression"));
//
//		String[] simpleLogisticOptions = "-I 0 -M 500 -H 50 -W 0.0".split(" ");
//		SimpleLogistic simpleLogistic = new SimpleLogistic();
//		simpleLogistic.setOptions(simpleLogisticOptions);
//		result.add(new ClassifierPair(logistic, "Boosted Linear Logistic Regression"));
//
//		String[] kStarOptions = "-B 20 -M a".split(" ");
//		KStar kstar = new KStar();
//		kstar.setOptions(kStarOptions);
//		result.add(new ClassifierPair(kstar, "K Star"));
//
//		for (int k = 1; k <= 256; k *= 2) {
//			String[] kNNOptions = "-K 128 -W 0".split(" ");
//			IBk ibk = new IBk();
//			ibk.setOptions(kNNOptions);
//			ibk.setKNN(k);
//
//			String[] searchOptions = "-A distance".split(" ");
//			searchOptions[1] = "weka.core.EuclideanDistance -R first-last";
//			LinearNNSearch search = new LinearNNSearch();
//			search.setOptions(searchOptions);
//			ibk.setNearestNeighbourSearchAlgorithm(search);
//
//			result.add(new ClassifierPair(ibk, "KNN K" + k));
//		}
//
//
//		String[] j48Options = "-C 0.25 -M 2".split(" ");
//		J48 j48 = new J48();
//		j48.setOptions(j48Options);
//		result.add(new ClassifierPair(j48, "J48"));
//
//		DecisionStump decisionstump = new DecisionStump();
//		result.add(new ClassifierPair(decisionstump, "Decision Stump"));
//
//		String[] reptreeOptions = "-M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0".split(" ");
//		REPTree reptree = new REPTree();
//		reptree.setOptions(reptreeOptions);
//		result.add(new ClassifierPair(reptree, "REP Decision Tree"));
//
//		String[] hoeffdingtreeOptions = "-L 2 -S 1 -E 1.0E-7 -H 0.05 -M 0.01 -G 200.0 -N 0.0".split(" ");
//		HoeffdingTree hoeffdingTree = new HoeffdingTree();
//		hoeffdingTree.setOptions(hoeffdingtreeOptions);
//		result.add(new ClassifierPair(hoeffdingTree, "Hoeffding Tree"));
//
//		String[] treeOptions = "-K 0 -M 1.0 -V 0.001 -S 1".split(" ");
//		RandomTree tree = new RandomTree();
//		tree.setOptions(treeOptions);
//		result.add(new ClassifierPair(tree, "Random Tree"));
//
//		for (int t = 10; t <= 160; t *= 2) {
//			String[] forestOptions = "-I 10 -K 0 -S 1 -num-slots 1".split(" ");
//			RandomForest forest = new RandomForest();
//			forest.setOptions(forestOptions);
//			forest.setNumTrees(t);
//			result.add(new ClassifierPair(forest, "Random Forest T" + t));
//		}
//
//		String[] boostOptions = "-P 100 -F 0 -R 1 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -S 1 -I 10".split(" ");
//
//		LogitBoost boost = new LogitBoost();
//
//		boost = new LogitBoost();
//		boost.setOptions(boostOptions);
//		boost.setClassifier(tree);
//		result.add(new ClassifierPair(boost, "Boosted Random Tree"));
//
//		boost = new LogitBoost();
//		boost.setOptions(boostOptions);
//		boost.setClassifier(decisionstump);
//		result.add(new ClassifierPair(boost, "Boosted Decision Stump"));
//
//		boost = new LogitBoost();
//		boost.setOptions(boostOptions);
//		boost.setClassifier(reptree);
//		result.add(new ClassifierPair(boost, "Boosted REP Tree"));

		System.out.println(result.size());
		return result;
	}

	public WekaRunner(SortFile[] inputfiles, File outputdirectory, String removeop) throws Exception
	{
	    List<ClassifierPair> classifiers = getClassifiers();
	    
		if(removeop.equals(""))
			removeop = "1,2,5,6";

		File outputfile = new File(outputdirectory.getPath() + File.separator + "WekaResults.csv");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile, true)));

		out.println("Filename,Algorithm,PctCorrect,PctIncorrect,AUC,TPRate,TNRate,Precision,Recall,FScore");

		Arrays.sort(inputfiles);
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
			
			String[] removeOptions = {"-R", removeop};
			Remove remove = new Remove();
			remove.setOptions(removeOptions);
			remove.setInputFormat(data); 
			data = Filter.useFilter(data, remove);

			//Discretize into three equal frequency bins
			String[] discretizeOptions = {"-F", "-B", "3", "-M", "-1.0", "-R", "first-last"};
			Discretize discretize = new Discretize();
			discretize.setOptions(discretizeOptions);
			discretize.setInputFormat(data);
			data = Filter.useFilter(data, discretize);

			String[] replaceOptions = {"-A", "first-last", "-N", "NA", };
			ReplaceMissingWithUserConstant replace = new ReplaceMissingWithUserConstant();
			replace.setOptions(replaceOptions);
			replace.setInputFormat(data);
			data = Filter.useFilter(data, replace);

			Evaluation eval;
			
			for (int i = 0; i < classifiers.size(); i++) {
			  ClassifierPair pair = classifiers.get(i);
			  String desc = pair.getDescription();
			  Classifier classifier = pair.getClassifier();
			  
			  System.out.println("Starting " + desc);
			  
			  eval = new Evaluation(data);
			  eval.crossValidateModel(classifier, data, 10, new Random(1));
			  out.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%n", inputfiles[x].getName(), desc,
			      Utils.doubleToString(eval.pctCorrect(), 4),
			      Utils.doubleToString(eval.pctIncorrect(), 4),
			      Utils.doubleToString(eval.weightedAreaUnderROC(), 4),
			      Utils.doubleToString(eval.truePositiveRate(yesindex), 4),
			      Utils.doubleToString(eval.truePositiveRate(noindex), 4),
			      Utils.doubleToString(eval.weightedPrecision(), 4),
			      Utils.doubleToString(eval.weightedRecall(), 4),
			      Utils.doubleToString(eval.weightedFMeasure(), 4));
			  out.flush();
			  
			  System.out.println("Done with " + desc);
			}
		}
		out.flush();
		out.close();
	}
}
