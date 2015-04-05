package org.bcl.project.mimic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import org.bcl.pipeline.util.SortFile;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;

/**
 * Abstract class for running generic Weka pipelines
 */
public class MimicRunner
{
  public MimicRunner(List<SortFile> inputfiles, File outputfile) throws Exception {
    this(inputfiles, outputfile, null);
  }
  
	public MimicRunner(List<SortFile> inputfiles, File outputfile, String removeop) throws Exception
	{    
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile, false)));

		out.println(MimicMain.CLASSIFIER_HEADER);

		for(File file : inputfiles)
		{
			System.err.println(file.getName());
			Instances data;
			if(file.getPath().substring(file.getPath().length() - 3).equals("csv"))
			{
				CSVLoader loader = new CSVLoader();
				loader.setSource(file);
				data = loader.getDataSet();
			}
			else
			{
				BufferedReader reader = new BufferedReader(new FileReader(file));
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
			
			if (removeop != null) {
			  data = MimicMain.preprocessData(data, removeop);
			} else {
        data = MimicMain.preprocessData(data);
			}

			String[] bayesOptions = {"-D", "-Q", "weka.classifiers.bayes.net.search.local.TAN", "--", 
			    "-S", "BAYES", "-E", "weka.classifiers.bayes.net.estimate.SimpleEstimator",
			    "--", "-A", "0.5"};
			BayesNet bayes = new BayesNet();
			bayes.setOptions(bayesOptions);
			
//			String[] logisticOptions = {"-R", "1.0E-8", "-M", "-1"};
//			Logistic logistic = new Logistic();
//			logistic.setOptions(logisticOptions);
			
	    Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(bayes, data, 10, new Random(1));
			out.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%n", file.getName(), "TAN Bayes",
			    Utils.doubleToString(eval.pctCorrect(), 4),
			    Utils.doubleToString(eval.pctIncorrect(), 4),
			    Utils.doubleToString(eval.weightedAreaUnderROC(), 4),
			    Utils.doubleToString(eval.truePositiveRate(yesindex), 4),
			    Utils.doubleToString(eval.truePositiveRate(noindex), 4),
			    Utils.doubleToString(eval.weightedPrecision(), 4),
			    Utils.doubleToString(eval.weightedRecall(), 4),
			    Utils.doubleToString(eval.weightedFMeasure(), 4));
			out.flush();
		}
		out.flush();
		out.close();
	}
}
