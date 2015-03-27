package org.bcl.pipeline.util;

import java.io.File;

import weka.core.Instances;


/**
 * Abstract class for running generic Weka pipelines
 */
public abstract class WekaBase
{
	private final SortFile[] files;
	private final File outputDirectory;
	
	abstract Instances preprocessData(Instances data);
	
	public WekaBase(File[] inputFiles, File outputDirectory) throws Exception
	{
		this.files = new SortFile[inputFiles.length];
		for (File file : inputFiles) {
			
		}
		this.outputDirectory = outputDirectory;
	}
}
