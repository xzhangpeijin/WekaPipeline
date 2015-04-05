package org.bcl.project.mimic;

import java.io.*;
import java.util.*;

import org.apache.commons.math3.stat.regression.SimpleRegression;


public class RankCount
{
  private static final String RANK_FILE = "I:\\Documents\\PRIMES\\SingleLayerRankings.txt";
  private static final int[] HOURS = {1,2,3,6,12,18,24,48,72,96};
  private static final int NUM_CATEGORIES = 5;
  
	public RankCount() throws Exception
	{
		Map<Integer, Integer> hourmap = new HashMap<Integer, Integer>();
		for(int x = 0; x < HOURS.length; x++)
			hourmap.put(HOURS[x], x);
		
		Map<String, int[][]> rankings = new HashMap<String, int[][]>();
		BufferedReader in = new BufferedReader(new FileReader(new File(RANK_FILE)));
		String nextline = "";
		while((nextline = in.readLine()) != null)
		{
			if(nextline.contains("temporal.extract.14") && nextline.contains("none.discretized"))
			{
				String filename = nextline.substring("File: ".length());
				int hour = Integer.parseInt(filename.substring(
				    filename.indexOf("first.") + "first.".length(), filename.indexOf(".hours")));
				int category = Integer.parseInt(filename.substring(
				    filename.indexOf("category.") + "category.".length(), filename.indexOf(".csv")));
				
				int rank = 1;
				// Skip two lines
				in.readLine(); in.readLine();
				while(!(nextline = in.readLine()).contains("Case"))
				{
					String attribute = nextline.split(": ")[0].trim();
					if (!rankings.containsKey(attribute)) {
					  rankings.put(attribute, new int[NUM_CATEGORIES][HOURS.length]);
					}
					rankings.get(attribute)[category - 1][hourmap.get(hour)] = rank++;
				}
			}
		}
		in.close();
		
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File("AttributeRanks.csv"))));
    StringBuffer title = new StringBuffer();
    title.append("Attribute");
    for (int x = 0; x < NUM_CATEGORIES; x++) {
      for (int y = 0; y < HOURS.length; y++) {
        title.append(String.format(",Category %d Hour %d", x + 1, HOURS[y]));
      }
      title.append(String.format(",Category %d Median Rank", x + 1));
      title.append(String.format(",Category %d Mean Rank", x + 1));
      title.append(String.format(",Category %d Max Rank", x + 1));
      title.append(String.format(",Category %d Min Rank", x + 1));
      title.append(String.format(",Category %d Median Change", x + 1));
      title.append(String.format(",Category %d Mean Change", x + 1));
      title.append(String.format(",Category %d Max Change", x + 1));
      title.append(String.format(",Category %d Min Change", x + 1));
      title.append(String.format(",Category %d Regression Slope", x + 1));
    }
    out.println(title.toString());
    
    for (String attribute : rankings.keySet()) {
      StringBuffer line = new StringBuffer();
      line.append(attribute);
      int[][] rank = rankings.get(attribute);
      for (int x = 0; x < NUM_CATEGORIES; x++) {
        int count = 0;
        double sum = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        
        List<Integer> diffs = new ArrayList<Integer>();
        double diffSum = 0;
        int diffMax = Integer.MIN_VALUE;
        int diffMin = Integer.MAX_VALUE;
        
        SimpleRegression reg = new SimpleRegression();
        for (int y = 0; y < HOURS.length; y++) {
          line.append(",");
          if (rank[x][y] != 0) {
            line.append(rank[x][y]);
            count++;
            sum += rank[x][y];
            reg.addData(HOURS[y], rank[x][y]);
          }
          max = Math.max(max, rank[x][y]);
          min = Math.min(min, (rank[x][y] == 0) ? min : rank[x][y]);
          
          if (y > 0 && rank[x][y] != 0 && rank[x][y - 1] != 0) {
            int diff = Math.abs(rank[x][y] - rank[x][y - 1]);
            diffs.add(diff);
            diffMax = Math.max(diffMax, diff);
            diffMin = Math.min(diffMin, diff);
            diffSum += diff;
          }
        }
        
        double median = median(rank[x]);
        double mean = sum / count;
        
        if (diffs.size() > 0) {
          double diffMedian = median(diffs);
          double diffMean = diffSum / diffs.size();
          line.append(String.format(",%.3f,%.3f,%d,%d,%.3f,%.3f,%d,%d", 
              median, mean, max, min, diffMedian, diffMean, diffMax, diffMin));
        } else {
          line.append(String.format(",%.3f,%.3f,%d,%d,,,,", median, mean, max, min));
        }
        
        if (Double.isNaN(reg.getSlope())) {
          line.append(",");
        } else {
          line.append(String.format(",%.3f", reg.getSlope()));
        }
      }
      out.println(line.toString());
    }
    out.flush();
    out.close();
	}
	
	private double median(List<Integer> input) {
	  int[] arr = new int[input.size()];
    for (int x = 0; x < input.size(); x++) 
      arr[x] = input.get(x);
    return median(arr);
	}
	
	private double median(int[] input) {
	  int[] arr = input.clone();
	  Arrays.sort(arr);
	  
		int x;
		for(x = 0; x < arr.length; x++)
			if(arr[x] != 0)
				break;
		int start = x;
		int end = arr.length - 1;
		while(start <= end)
		{
			if(start + 1 == end)
				return (double)(arr[start] + arr[end]) / 2;
			if(start == end)
				return arr[start];
			
			start++;
			end--;
		}
		return -1;
	}
	
	public static void main(String[] args) throws Exception
	{
		new RankCount();
	}
}
