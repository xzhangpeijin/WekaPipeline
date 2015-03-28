package org.bcl.project.mimic;

import java.io.*;
import java.util.*;


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
    }
    out.println(title.toString());
    
    for (String attribute : rankings.keySet()) {
      StringBuffer line = new StringBuffer();
      line.append(attribute);
      int[][] rank = rankings.get(attribute);
      for (int x = 0; x < NUM_CATEGORIES; x++) {
        for (int y = 0; y < HOURS.length; y++) {
          line.append(",");
          if (rank[x][y] != 0) {
            line.append(rank[x][y]);
          }
        }
      }
      out.println(line.toString());
    }
    out.flush();
    out.close();
		
//		ArrayList<int[]> diffs = new ArrayList<int[]>();
//		for(int x = 0; x < attributes.size(); x++)
//		{
//			int[] ranking = rankings.get(x);
//			int[] diff = new int[ranking.length - 1];
//			int cur = ranking[0];
//			for(int y = 1; y < ranking.length; y++)
//			{
//				if(cur != Integer.MIN_VALUE && ranking[y] != Integer.MIN_VALUE)
//					diff[y - 1] = ranking[y] - cur;
//				else
//					diff[y - 1] = Integer.MIN_VALUE;
//				cur = ranking[y];
//			}
//			diffs.add(diff);
//		}
//		
//		ArrayList<double[]> stats = new ArrayList<double[]>();
//		for(int x = 0; x < attributes.size(); x++)
//		{
//			int[] diff = diffs.get(x);
//			int max = Integer.MIN_VALUE;
//			int min = Integer.MAX_VALUE;
//			int count = 0;
//			int tot = 0;
//			int squared = 0;
//			for(int y = 0; y < diff.length; y++)
//			{
//				if(diff[y] != Integer.MIN_VALUE)
//				{
//					tot += diff[y];
//					squared += diff[y] * diff[y];
//					count++;
//					max = Math.max(max, diff[y]);
//					min = Math.min(min, diff[y]);
//				}
//			}
//			int[] a = diff.clone();
//			Arrays.sort(a);
//			double[] stat = new double[7];
//			if(count != 0)
//			{
//				stat[0] = (double)tot / count;
//				stat[2] = (double)squared / count;
//			}
//			else
//			{
//				stat[0] = Integer.MIN_VALUE;
//				stat[2] = Integer.MIN_VALUE;
//			}
//			stat[1] = median(a);
//			
//			stat[3] = max;
//			stat[4] = min;
//			
//			tot = 0;
//			count = 0;
//			int[] rank = rankings.get(x);
//			for(int y = 0; y < rank.length; y++)
//			{
//				if(rank[y] != Integer.MIN_VALUE)
//				{
//					tot += rank[y];
//					count++;
//				}
//			}
//			a = rank.clone();
//			Arrays.sort(a);
//			stat[5] = (double)tot / count;
//			stat[6] = median(a);
//		
//			stats.add(stat);
//		}
//		
	}
	
	double median(int[] arr)
	{
		int x;
		for(x = 0; x < arr.length; x++)
			if(arr[x] != Integer.MIN_VALUE)
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
		return Integer.MIN_VALUE;
	}
	
	public static void main(String[] args) throws Exception
	{
		new RankCount();
	}
}
