package org.bcl.weka.pipeline.mimic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bcl.weka.pipeline.util.SortFile;

public class MimicMain {
  public static final String TO_REMOVE = "1,2,5,6";
  
  public static void main(String[] args) throws Exception {
    File dir = new File("/Users/Peijin/Documents/PRIMES");
    
    Map<String, Integer> complications = null;
    List<File> runfiles = new ArrayList<File>();
    File[] subdir = dir.listFiles();
    for(int x = 0; x < subdir.length; x++)
    { 
      if(subdir[x].getName().contains("temporal.extract.14.3") && 
          subdir[x].getName().contains("csv"))
        runfiles.add(subdir[x]);
      if(subdir[x].getName().equals("complication.candidates.expanded.csv")) {
        complications = loadComplications(subdir[x]);
      }
    }
    
    assert(complications != null);
    
    runSingleLayer(runfiles, dir, complications);
    runDoubleLayer(runfiles, dir, complications);
  }
  
  public static Map<String, Integer> loadComplications(File complication) throws IOException {
    Map<String, Integer> complications = new HashMap<String, Integer>();

    BufferedReader in = new BufferedReader(new FileReader(complication));
    String nextline = in.readLine();
    while ((nextline = in.readLine()) != null) {
      String[] data = nextline.split(",");
      if (data.length == 0 || data[0].length() == 0) {
        break;
      }
      complications.put(data[0], Integer.parseInt(data[2]));
    }
    in.close();
    
    return complications;
  }
  
  public static void runSingleLayer(List<File> runfiles, 
      File outdir, Map<String, Integer> complications) {
    Collections.sort(runfiles);
    SortFile[] run = new SortFile[runfiles.size()];
    run = runfiles.toArray(run);
    new MimicRunner(run, dir);
    new MimicRanker(run, dir);
  }
  
  public static void runDoubleLayer(List<File> runfiles, 
      File outdir, Map<String, Integer> complications) {
    
  }
}
