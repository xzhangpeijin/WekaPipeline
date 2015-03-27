package org.bcl.project.mimic;

import java.io.*;
import java.util.*;


public class RankCount
{
	public RankCount() throws Exception
	{
		int[] hours = {1,2,3,6,12,18,24,48,72,96,120};
		HashMap<Integer, Integer> hourmap = new HashMap<Integer, Integer>();
		for(int x = 0; x < hours.length; x++)
			hourmap.put(hours[x], x);
		
		ArrayList<String> attributes = new ArrayList<String>();
		ArrayList<int[]> rankings = new ArrayList<int[]>();
		BufferedReader in = new BufferedReader(new FileReader(new File("AttributeRanks.txt")));
		String nextline = "";
		int hour = Integer.MIN_VALUE;
		while((nextline = in.readLine()) != null)
		{
			if(nextline.contains("temporal.extract.v14") && nextline.contains("hours.csv"))
			{
				String filename = nextline.substring(6);
				hour = Integer.parseInt(filename.substring(27, filename.indexOf(".hours")));
				
				int rank = 1;
				nextline = in.readLine();
				nextline = in.readLine();
				while(!(nextline = in.readLine()).contains("Case"))
				{
					String attribute = nextline.split(": ")[0].trim();
					if(attributes.contains(attribute))
					{
						int[] ranking = rankings.get(attributes.indexOf(attribute));
						ranking[hourmap.get(hour)] = rank;
					}
					else
					{
						attributes.add(attribute);
						int[] ranking = new int[hours.length];
						for(int x = 0; x < hours.length; x++)
							ranking[x] = Integer.MIN_VALUE;
						ranking[hourmap.get(hour)] = rank;
						rankings.add(ranking);
					}
					rank++;
				}
			}
		}
		in.close();
		
		ArrayList<int[]> diffs = new ArrayList<int[]>();
		for(int x = 0; x < attributes.size(); x++)
		{
			int[] ranking = rankings.get(x);
			int[] diff = new int[ranking.length - 1];
			int cur = ranking[0];
			for(int y = 1; y < ranking.length; y++)
			{
				if(cur != Integer.MIN_VALUE && ranking[y] != Integer.MIN_VALUE)
					diff[y - 1] = ranking[y] - cur;
				else
					diff[y - 1] = Integer.MIN_VALUE;
				cur = ranking[y];
			}
			diffs.add(diff);
		}
		
		ArrayList<double[]> stats = new ArrayList<double[]>();
		for(int x = 0; x < attributes.size(); x++)
		{
			int[] diff = diffs.get(x);
			int max = Integer.MIN_VALUE;
			int min = Integer.MAX_VALUE;
			int count = 0;
			int tot = 0;
			int squared = 0;
			for(int y = 0; y < diff.length; y++)
			{
				if(diff[y] != Integer.MIN_VALUE)
				{
					tot += diff[y];
					squared += diff[y] * diff[y];
					count++;
					max = Math.max(max, diff[y]);
					min = Math.min(min, diff[y]);
				}
			}
			int[] a = diff.clone();
			Arrays.sort(a);
			double[] stat = new double[7];
			if(count != 0)
			{
				stat[0] = (double)tot / count;
				stat[2] = (double)squared / count;
			}
			else
			{
				stat[0] = Integer.MIN_VALUE;
				stat[2] = Integer.MIN_VALUE;
			}
			stat[1] = median(a);
			
			stat[3] = max;
			stat[4] = min;
			
			tot = 0;
			count = 0;
			int[] rank = rankings.get(x);
			for(int y = 0; y < rank.length; y++)
			{
				if(rank[y] != Integer.MIN_VALUE)
				{
					tot += rank[y];
					count++;
				}
			}
			a = rank.clone();
			Arrays.sort(a);
			stat[5] = (double)tot / count;
			stat[6] = median(a);
		
//			System.out.print(attributes.get(x) + " ");
//			for(int y = 0; y < diff.length; y++)
//				System.out.print(diff[y] + " ");
//			System.out.println();
//			for(int y = 0; y < rank.length; y++)
//				System.out.print(rank[y] + " ");
//			System.out.println();
			stats.add(stat);
		}
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File("AttributeRanks.csv"))));
		out.println("Attribute,Hour 1,Hour 2, Hour 3,Hour 6,Hour 12,Hour 18,"
				+ "Hour 24,Hour 48,Hour 72,Hour 96,Hour 120,Average change,"
				+ "Median change,Average squared change,Max change,Min change,Average rank,Median rank");
		for(int x = 0; x < attributes.size(); x++)
		{
			int[] ranking = rankings.get(x);
			double[] stat = stats.get(x);
			out.print(attributes.get(x));
			for(int y = 0; y < ranking.length; y++)
				if(ranking[y] != Integer.MIN_VALUE)
					out.print("," + ranking[y]);
				else
					out.print(",");
			for(int y = 0; y < stat.length; y++)
				if(stat[y] != Integer.MIN_VALUE && stat[y] != Integer.MAX_VALUE)
					out.print("," + stat[y]);
				else
					out.print(",");
			out.println();
		}
		out.flush();
		out.close();
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
