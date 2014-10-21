package org.bcl.weka.pipeline.util;

import java.io.File;
import javax.swing.filechooser.*;

public class WekaFilter extends FileFilter 
{
    public boolean accept(File f) 
    {
    	if(f.isDirectory())
    		return true;
    	String extension = null;
    	String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1)
            extension = s.substring(i+1).toLowerCase();
        if (extension != null) 
        {
            if(extension.equals("csv")) 
                return true;
            else if(extension.equals("arff"))
            	return true;
            else
                return false;
        }
        return false;
    }

    public String getDescription() 
    {
        return "Weka Files (.csv/.arff)";
    }
}
