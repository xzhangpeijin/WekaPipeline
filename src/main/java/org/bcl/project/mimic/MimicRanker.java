package org.bcl.project.mimic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.bcl.pipeline.util.SortFile;

import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

public class MimicRanker
{
  public MimicRanker(List<SortFile> inputfiles, File outputfile) throws Exception {
    this(inputfiles, outputfile, null);
  }
  
	public MimicRanker(List<SortFile> inputfiles, File outputfile, String removeop) throws Exception
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));

		out.println("MIMIC Attribute Rankings");
		out.println("Files Selected: " + inputfiles.size());
		out.println("Selection: BestFirst Bidirectional with Search Termination 5");
		out.println("Ranking: Gain Ratio (values printed)");
		out.println("\n");
		out.println("============================");
		out.println("\n");
		
		for (File file : inputfiles)
		{
			out.println("File: " + file.getName() + "\n");
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

			if (removeop != null) {
			  data = MimicMain.preprocessData(data, removeop);
			} else {
		    data = MimicMain.preprocessData(data);
			}
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
