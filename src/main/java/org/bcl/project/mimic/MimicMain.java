package org.bcl.project.mimic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bcl.pipeline.util.SortFile;

import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;

public class MimicMain {
  public static final String TO_REMOVE = "1,2,6,7";
  public static final int NUM_CATEGORIES = 5;
  public static final String TEMP_DIR = "temp";
  public static final String ICD9_DIR = "icd9files";
  public static final String CLASSIFIER_HEADER = 
      "Filename,Algorithm,PctCorrect,PctIncorrect,AUC,TPRate,TNRate,Precision,Recall,FScore";
  
  public static void main(String[] args) throws Exception {
    File dir = new File("I:\\Documents\\PRIMES");
    
    Map<String, Integer> complications = null;
    List<File> runfiles = new ArrayList<File>();
    File[] subdir = dir.listFiles();
    for(int x = 0; x < subdir.length; x++)
    { 
      if(subdir[x].getName().contains("temporal.extract.14.3") && 
          subdir[x].getName().contains("none.discretized.csv"))
        runfiles.add(subdir[x]);
      if(subdir[x].getName().equals("complication.candidates.expanded.csv")) {
        complications = loadComplications(subdir[x]);
      }
    }
    
    assert(complications != null);
    
    //runSingleLayer(runfiles, dir);
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
  
  public static void runSingleLayer(List<File> runfiles, File outdir) throws Exception {
    List<SortFile> outputs = new ArrayList<SortFile>();
    for (File data : runfiles) {
      String name = data.getName();
      Map<Integer, MimicData> patients = MimicData.makeData(data);
      String header = getHeader(data);
      header = header.substring(0, header.lastIndexOf(",")) + ",Case";
      for (int i = 1; i <= NUM_CATEGORIES; i++) {
        outputs.add(new SortFile(writeFile(header, patients, i, outdir, name)));
      }
    }
    Collections.sort(outputs);
    new MimicRunner(outputs, new File(outdir, "SingleLayerResults.csv"));
    new MimicRanker(outputs, new File(outdir, "SingleLayerRankings.txt"));
  }
  
  public static void runDoubleLayer(List<File> runfiles, 
      File outdir, Map<String, Integer> complications) throws Exception {
    List<SortFile> outputs = new ArrayList<SortFile>();
    Set<Integer> cases = new HashSet<Integer>();
    for (File data : runfiles) {
      String name = data.getName();
      Map<Integer, MimicData> patientmap = MimicData.makeData(data);
      for (int hadm_id : patientmap.keySet()) {
        if (!patientmap.get(hadm_id).categories.contains(0) || patientmap.get(hadm_id).categories.size() > 1) {
          cases.add(hadm_id);
        }
      }
//      
//      File temp = File.createTempFile("icd9temp", ".csv");
//      PrintWriter out = new PrintWriter(new FileWriter(temp));
//      String header = getHeader(data);
//      header = header.substring(0, header.lastIndexOf(","));
//      out.println(header);
//      for (int hadm_id : patientmap.keySet()) {
//        MimicData info = patientmap.get(hadm_id);
//        info.setClass(null);
//        out.println(info.toString());
//      }
//      out.flush();
//      out.close();
//      
//      CSVLoader loader = new CSVLoader();
//      loader.setSource(temp);
//      Instances patients = loader.getDataSet();
//      patients = preprocessData(patients, "1,6,7");
//      
//      for (Instance instance : patients) {
//        patientmap.get((int)instance.value(0)).addInstance(instance);
//      }
//      
//      temp.delete();
//      
//      List<String> yn = new ArrayList<String>();
//      yn.add("Y");
//      yn.add("N");
//      
//      Map<String, BayesNet> classifiers = new HashMap<String, BayesNet>();
//      for (String code : complications.keySet()) {
//        Instances instances = new Instances(patients);
//        instances.delete();
//        
//        List<Instance> controls = new ArrayList<Instance>();
//        Set<Integer> cases = new HashSet<Integer>();
//        for (int hadm_id : patientmap.keySet()) {
//          MimicData info = patientmap.get(hadm_id);
//          if (info.icd9codes.contains(code)) {
//            instances.add(info.instance);
//            cases.add(hadm_id);
//          } else {
//            controls.add(info.instance);
//          }
//        }
//        
//        if (cases.size() > 0) {
//          // Randomly sample controls
//          Random rand = new Random(System.currentTimeMillis());
//          for (int x = 0; x < cases.size(); x++) {
//            instances.add(controls.remove(rand.nextInt(controls.size())));
//          }
//          
//          Attribute att = new Attribute("Case", yn);
//          instances.insertAttributeAt(att, instances.numAttributes());
//          for (Instance instance : instances) {
//            if (cases.contains((int)instance.value(0))) {
//              instance.setValue(instances.numAttributes() - 1, "Y");
//            } else {
//              instance.setValue(instances.numAttributes() - 1, "N");
//            }
//          }
//          
//          instances.deleteAttributeAt(0);
//          
//          instances.setClassIndex(instances.numAttributes() - 1);
//
//          String[] bayesOptions = {"-D", "-Q", "weka.classifiers.bayes.net.search.local.TAN", "--", 
//              "-S", "BAYES", "-E", "weka.classifiers.bayes.net.estimate.SimpleEstimator",
//              "--", "-A", "0.5"};
//          BayesNet bayes = new BayesNet();
//          bayes.setOptions(bayesOptions);
//          bayes.buildClassifier(instances);
//          classifiers.put(code, bayes);
//        }
//      }
//      
//      patients.deleteAttributeAt(0);

//      for (int category = 1; category <= NUM_CATEGORIES; category++) {
//        String outputname = name.substring(0, name.lastIndexOf(".")) + 
//            ".category." + category + ".icd9.csv";
//        System.out.println(outputname);
//        File icd9out = new File(new File(outdir, ICD9_DIR), outputname);
//        outputs.add(new SortFile(icd9out));
//        out = new PrintWriter(new FileWriter(icd9out));
//
//        StringBuilder outheader = new StringBuilder();
//        outheader.append("hadm_id");
//        for (String code : complications.keySet()) {
//          outheader.append(",");
//          outheader.append(code);
//        }
//        outheader.append(",Case");
//        out.println(outheader.toString());
//
//        List<String> controls = new ArrayList<String>();
//        int cases = 0;
//        for (int hadm_id : patientmap.keySet()) {
//          Instance instance = patientmap.get(hadm_id).instance;
//          
//          StringBuilder buf = new StringBuilder();
//          buf.append(hadm_id);
//          for (String code : complications.keySet()) {
//            buf.append(",");
//            if (classifiers.containsKey(code)) {
//              BayesNet bayes = classifiers.get(code);
//              double[] est = bayes.getEstimator().distributionForInstance(bayes, instance);
//              buf.append(est[0]);
//            } else {
//              buf.append(0);
//            }
//          }
//          
//          if (patientmap.get(hadm_id).categories.contains(category)) {
//            cases++;
//            buf.append(",Y");
//            out.println(buf.toString());
//          } else {
//            buf.append(",N");
//            controls.add(buf.toString());
//          }
//        }
//        
//        if (cases == 0) {
//          System.out.format("%s category %d has no cases!%n", name, category);
//        }
//        
//        Random rand = new Random(System.currentTimeMillis());
//        for (int x = 0; x < cases; x++) {
//          out.println(controls.remove(rand.nextInt(controls.size())));
//        }
//
//        out.flush();
//        out.close();
//      }
//
    }
    System.out.println(cases.size());
//    
//    Collections.sort(outputs);
//    new MimicRunner(outputs, new File(outdir, "DoubleLayerResults5.csv"), "1");
//    new MimicRanker(outputs, new File(outdir, "DoubleLayerRankings.txt"), "1");
  }
  
  public static Instances preprocessData(Instances data) throws Exception {
    return preprocessData(data, TO_REMOVE);
  }
  
  public static Instances preprocessData(Instances data, String removeop) throws Exception {
    String[] removeOptions = {"-R", removeop};
    Remove remove = new Remove();
    remove.setOptions(removeOptions);
    remove.setInputFormat(data); 
    data = Filter.useFilter(data, remove);
    
    if (removeop.equals("1")) {
      // Discretize into three equal frequency bins
      String[] discretizeOptions = {"-F", "-B", "3", "-M", "-1.0", "-R", "first-last"};
      Discretize discretize = new Discretize();
      discretize.setOptions(discretizeOptions);
      discretize.setInputFormat(data);
      data = Filter.useFilter(data, discretize);
      return data;
    }

    boolean hasMissing = false;
    StringBuilder buf = new StringBuilder();
    for (int x = 0; x < data.numAttributes(); x++) {
      if (data.attributeStats(x).missingCount > 0) {
        hasMissing = true;
      }
      if (data.attribute(x).isNumeric() && !data.attribute(x).name().equals("hadm_id")) {
        if (buf.length() > 0) {
          buf.append(",");
        }
        buf.append(x + 1);
      }
      if (data.attribute(x).isString()) {
        System.err.println("STRING: " + data.attribute(x).name());
      }
    }
    
    if (buf.length() > 0) {
      // Discretize into three equal frequency bins
      String[] discretizeOptions = {"-F", "-B", "3", "-M", "-1.0", "-R", buf.toString()};
      Discretize discretize = new Discretize();
      discretize.setOptions(discretizeOptions);
      discretize.setInputFormat(data);
      data = Filter.useFilter(data, discretize);
    }
    
    if (hasMissing) {
      // Replace all missing values with NA bin
      String[] replaceOptions = {"-A", "first-last", "-N", "NA", };
      ReplaceMissingWithUserConstant replace = new ReplaceMissingWithUserConstant();
      replace.setOptions(replaceOptions);
      replace.setInputFormat(data);
      data = Filter.useFilter(data, replace);
    }
    
    return data;
  }
  
  public static String getHeader(File input) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(input));
    String header = br.readLine();
    br.close();
    return header;
  }
  
  public static File writeFile(String header, Map<Integer, MimicData> patients, 
      int category, File outdir, String name) throws IOException {
    File tempdir = new File(outdir, TEMP_DIR);
    tempdir.mkdir();
    String outputname = name.substring(0, name.lastIndexOf(".")) + ".category." + category + ".csv";
    File output = new File(tempdir, outputname);
    
    PrintWriter out = new PrintWriter(new FileWriter(output));
    out.println(header);
    // Randomly sample equal case.
    List<MimicData> controls = new ArrayList<MimicData>();
    int cases = 0;
    for (int hadm_id : patients.keySet()) {
      MimicData info = patients.get(hadm_id);
      if (info.categories.contains(category)) {
        info.setClass("Y");
        out.println(info.toString());
        cases++;
      } else {
        info.setClass("N");
        controls.add(info);
      }
    }
    
    System.out.format("%s - Cases: %d Controls: %d%n", outputname, cases, controls.size());
    
    Random rand = new Random(System.currentTimeMillis());
    for (int x = 0; x < cases; x++) {
      MimicData data = controls.remove(rand.nextInt(controls.size()));
      out.println(data.toString());
    }
    
    out.flush();
    out.close();
    return output;
  }
  
  public static File writeICD9(String header, Map<Integer, MimicData> patients, 
      String code, File outdir, String name) throws IOException {
    File tempdir = new File(outdir, ICD9_DIR);
    tempdir.mkdir();
    String outputname = name.substring(0, name.lastIndexOf(".")) + ".code." + code + ".csv";
    File output = new File(tempdir, outputname);
    
    PrintWriter out = null;
    
    List<MimicData> controls = new ArrayList<MimicData>();
    int cases = 0;
    for (int hadm_id : patients.keySet()) {
      MimicData info = patients.get(hadm_id);
      if (info.icd9codes.contains(code)) {
        info.setClass("Y");
        if (out == null) {
          out = new PrintWriter(new FileWriter(output));
          out.println(header);
        }
        out.println(info.toString());
        cases++;
      } else {
        info.setClass("N");
        controls.add(info);
      }
    }
    
    System.out.format("%s - Cases: %d Controls: %d%n", outputname, cases, controls.size());
    
    if (cases == 0) {
      return null;
    }
    
    Random rand = new Random(System.currentTimeMillis());
    for (int x = 0; x < cases; x++) {
      MimicData data = controls.remove(rand.nextInt(controls.size()));
      out.println(data.toString());
    }
    
    out.flush();
    out.close();
    
    return output;
  }
}
