package org.bcl.project.mimic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import weka.core.Instance;

public class MimicData {
  private static final int HADM_INDEX = 1;
  private static final int ICD9_INDEX = 6;
  
  public final int hadm_id;
  public final String data;
  public final Set<Integer> categories;
  public final Set<String> icd9codes;
  
  public String classFeat;
  public Instance instance;
  
  private MimicData(int hadm_id, String data,
      Set<Integer> categories, Set<String> icd9codes) {
    this.hadm_id = hadm_id;
    this.data = data;
    this.categories = categories;
    this.icd9codes = icd9codes;
    
    this.classFeat = null;
    this.instance = null;
  }
  
  public void addCategory(int cat) {
    this.categories.add(cat);
  }
  
  public void addInstance(Instance instance) {
    this.instance = instance;
  }
  
  public void setClass(String classFeat) {
    this.classFeat = classFeat;
  }
  
  public String toString() {
    if (classFeat != null) {
      return data + "," + classFeat;
    } else {
      return data;
    }
  }
  
  public static Map<Integer, MimicData> makeData(File inputfile) throws IOException {
    Map<Integer, MimicData> data = new HashMap<Integer, MimicData>();
    
    CSVParser parser = CSVParser.parse(inputfile, 
        Charset.defaultCharset(), CSVFormat.RFC4180.withHeader());
    for (CSVRecord record : parser) {
      int hadm_id = Integer.parseInt(record.get(HADM_INDEX));
      int category = Integer.parseInt(record.get(record.size() - 1));
      if (data.containsKey(hadm_id)) {
        data.get(hadm_id).addCategory(category);
      } else {
        Set<Integer> categories = new HashSet<Integer>();
        categories.add(category);

        Set<String> icd9codes = new HashSet<String>();
        String icd9 = record.get(ICD9_INDEX);
        String[] codes = icd9.substring(0, icd9.length() - 1).split(",");
        for (String code : codes) {
          icd9codes.add(code);
        }
        
        data.put(hadm_id, new MimicData(hadm_id, genData(record), categories, icd9codes));
      }
    }
    
    return data;
  }
  
  private static String genData(CSVRecord record) {
    StringBuilder buf = new StringBuilder();
    // NOTE: Category is NOT written
    for (int x = 0; x < record.size() - 1; x++) {
      if (x != 0) {
        buf.append(",");
      }
      buf.append("\"");
      buf.append(record.get(x));
      buf.append("\"");
    }
    return buf.toString();
  }
}
