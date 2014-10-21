package org.bcl.weka.pipeline.util;

import java.io.File;

/**
 * Sort method for sorting files in numerical order based on their filenames
 * Assumes that file information is standardized and delimited by periods
 * 
 * Comparisons between files will be done by looking at filenames. Each filename will be separated
 * into period blocks and blocks will be compared sequentially. If both blocks can be interpreted
 * as integers, then sort order will be by integer value. Otherwise it will be lexicographic order.
 */

@SuppressWarnings("serial")
public class SortFile extends File implements Comparable<File>
{
	private String filename;

	public SortFile(String arg0) 
	{
		super(arg0);
		filename = arg0.substring(arg0.lastIndexOf(File.separator) + 1);
	}

	public int compareTo(File a)
	{
		String afilename = a.getPath();
		afilename = afilename.substring(afilename.lastIndexOf(File.separator) + 1);

		String[] aArgs = afilename.split("\\.");
		String[] tArgs = filename.split("\\.");

		for (int i = 0; i < Math.min(aArgs.length, tArgs.length); i++) {
			if (aArgs[i].equals(tArgs[i])) {
				continue;
			}
			if (isInteger(aArgs[i]) && isInteger(tArgs[i])) {
				return Integer.parseInt(tArgs[i]) - Integer.parseInt(aArgs[i]); 
			} else {
				return tArgs[i].compareTo(aArgs[i]);
			}
		}
		
		return tArgs.length - aArgs.length;
	}

	private static boolean isInteger(String s) {
		try { 
			Integer.parseInt(s); 
		} catch(NumberFormatException e) { 
			return false; 
		}
		return true;
	}
}
