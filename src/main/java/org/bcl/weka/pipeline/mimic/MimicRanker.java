package org.bcl.weka.pipeline.mimic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bcl.weka.pipeline.util.SortFile;

import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;

public class MimicRanker
{
	public MimicRanker(SortFile[] inputfiles, File outputdirectory) throws Exception
	{
		File outputfile = new File(outputdirectory.getPath() + File.separator + "AttributeRanks.txt");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));

		out.println("MIMIC Attribute Rankings");
		out.println("Files Selected: " + inputfiles.length);
		out.println("Selection: BestFirst Bidirectional with Search Termination 5");
		out.println("Ranking: Gain Ratio (values printed)");
		out.println("\n");
		out.println("============================");
		out.println("\n");
		
		for(int x = 0; x < inputfiles.length; x++)
		{
			out.println("File: " + inputfiles[x].getName() + "\n");
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

			String[] removeOptions = {"-R", MimicMain.TO_REMOVE};
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

			//Attribute Selection
//			CfsSubsetEval evaluator = new CfsSubsetEval();
//			String[] evalOptions = {"-P", "3", "-E", "3"};
//			evaluator.setOptions(evalOptions);
//			BestFirst search = new BestFirst();
//			String[] searchOptions = {"-D", "2", "-N", "5"};
//			search.setOptions(searchOptions);
			
			GainRatioAttributeEval info = new GainRatioAttributeEval();
			Ranker rank = new Ranker();
			String[] rankOptions = {"-T", "-1"};
			rank.setOptions(rankOptions);

//			AttributeSelection select = new AttributeSelection();
//			select.setEvaluator(evaluator);
//			select.setSearch(search);
//			select.setInputFormat(data);
//			Instances reduce = Filter.useFilter(data, select);

			AttributeSelection ranker = new AttributeSelection();
			ranker.setEvaluator(info);
			ranker.setSearch(rank);
			ranker.setInputFormat(data);
			data = Filter.useFilter(data, ranker);
//			ranker.setInputFormat(reduce);
//			reduce = Filter.useFilter(reduce, ranker);
			
			out.println("Full Data");
			info.buildEvaluator(data);
			for(int y = 0; y < data.numAttributes(); y++)
			{
				out.println("   " + data.attribute(y).name() + ": " + info.evaluateAttribute(y));
			}
			
//			out.println("Reduced Data");
//			info.buildEvaluator(reduce);
//			for(int y = 0; y < reduce.numAttributes(); y++)
//			{
//				out.println("   " + reduce.attribute(y).name() + ": " + info.evaluateAttribute(y));
//			}
			
			out.println();
			out.flush();
		}
		out.flush();
		out.close();
	}
}
