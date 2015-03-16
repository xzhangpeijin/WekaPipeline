package org.bcl.oneoff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Selects patients for MIMIC experiment
 */
public class MIMICSelection {
  public static void makeTrainingData(File dir) throws IOException {
    for (File data : dir.listFiles()) {
      if (data.getName().contains("temporal.extract.14.2.first") && 
          data.getName().contains(".hours.csv")) {
        String name = data.getName().substring(0, data.getName().lastIndexOf("."));
        System.out.println(name);
        
        for (int cat : new int[]{1,2,3,4}) {
          String outputname = String.format("%s.category.%d.healthy.controls.csv", name, cat);

          Set<String> caseIDs = new HashSet<String>();
          
          List<String> cases = new ArrayList<String>();
          List<String> controls = new ArrayList<String>();

          BufferedReader br = new BufferedReader(new FileReader(data));
          
          String[] headers = br.readLine().split(",");
          StringBuffer outputHeader = new StringBuffer();
          int hadmIndex = 0, categoryIndex = 0;
          for (int x = 0; x < headers.length; x++) {
            if (x != 0) {
              outputHeader.append(",");
            }
            if (headers[x].contains("hadm_id")) {
              hadmIndex = x;
              outputHeader.append(headers[x]);
            } else if (headers[x].contains("category")) {
              categoryIndex = x;
              outputHeader.append("\"Case\"");
            } else {
              outputHeader.append(headers[x]);
            }
          }
          
          // Add cases and controls and format output string
          String nextline;
          while ((nextline = br.readLine()) != null) {
            String[] dat = nextline.split(",");
            String hadm = dat[hadmIndex];
            int category = Integer.parseInt(dat[categoryIndex].replaceAll("\"", ""));
            
            StringBuffer output = new StringBuffer();
            for (int x = 0; x < dat.length; x++) {
              if (x != 0) {
                output.append(",");
              }
              if (x != categoryIndex) {
                output.append(dat[x]);
              } else {
                output.append((category == cat) ? "Y" : "N");
              }
            }
            
            if (category == cat && !caseIDs.contains(hadm)) {
              caseIDs.add(hadm);
              cases.add(output.toString());
            } else if (category == 0) {
              controls.add(output.toString());
            } 
          }
          br.close();
          
          // Remove invalid controls
          for (int x = 0; x < controls.size(); x++) {
            String hadm = controls.get(x).split(",")[hadmIndex];
            if (caseIDs.contains(hadm)) {
              controls.remove(x);
              x--;
            }
          }
          System.out.format("Category %d - Cases: %d Controls: %d%n", 
              cat, cases.size(), controls.size());
          
          // Write to file
          if (cases.size() > controls.size()) {
            System.err.println("More cases than controls!");
          } else {
            Random rand = new Random(System.currentTimeMillis());
            while (controls.size() > cases.size()) {
              controls.remove(rand.nextInt(controls.size()));
            }
            
            PrintWriter out = new PrintWriter(new FileWriter(new File(dir, outputname)));
            out.println(outputHeader.toString());
            for (String line : cases) {
              out.println(line);
            }
            for (String line : controls) {
              out.println(line);
            }
            out.flush();
            out.close();
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    File dir = new File("I:\\Documents\\Dropbox\\primes\\PRIMES Shared");
    MIMICSelection.makeTrainingData(dir);
  }
}
