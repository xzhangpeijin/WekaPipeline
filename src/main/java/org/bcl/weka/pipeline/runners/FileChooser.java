package org.bcl.weka.pipeline.runners;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.bcl.weka.pipeline.util.ClassificationRunner;
import org.bcl.weka.pipeline.util.WekaFilter;
import org.bcl.weka.pipeline.util.SortFile;

import java.io.File;

public class FileChooser extends JPanel implements ActionListener 
{
	private JButton openButton, compile;
    private JFileChooser open;
    private JTextField indices;
    private SortFile[] inputfiles = null;
    private File outputdirectory = null;
    
    public FileChooser() 
    {
    	 super(new BorderLayout());
    	 
         open = new JFileChooser();
         open.setFileSelectionMode(JFileChooser.FILES_ONLY);
         open.setAcceptAllFileFilterUsed(false);
         open.setFileFilter(new WekaFilter());
         open.setMultiSelectionEnabled(true);
         
         
         openButton = new JButton("Select input files");
         openButton.setPreferredSize(new Dimension(240,60));
         openButton.addActionListener(this);
         
         JPanel compilePanel = new JPanel();
         compile = new JButton("Run");
         compile.setPreferredSize(new Dimension(240,60));
         compile.addActionListener(this);
         compilePanel.add(compile);
         
         
         JPanel top = new JPanel();
         JLabel in1 = new JLabel("Instructions to run:");
         JLabel in2 = new JLabel("1. Select input files (csv/arff format)");
         JLabel in3 = new JLabel("2. List indices you want skipped in text box (Format: 0, 1, 2, 5)");
         JLabel in4 = new JLabel("3. Press run");
         
         indices = new JTextField();
         top.setLayout(new GridLayout(5, 1));
         top.add(in1);
         top.add(in2);
         top.add(in3);
         top.add(in4);
         top.add(indices);

         add(top, BorderLayout.NORTH);
         add(openButton, BorderLayout.CENTER);
         add(compilePanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) 
    {
		if (e.getSource() == openButton) 
        {
            int returnVal = open.showOpenDialog(FileChooser.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
            	File[] tempfiles = open.getSelectedFiles();
            	inputfiles = new SortFile[tempfiles.length];
            	for(int x = 0; x < tempfiles.length; x++)
            		inputfiles[x] = new SortFile(tempfiles[x].getPath());
            }
        } 

        else if(e.getSource() == compile)
        {
        	if(inputfiles == null)
        		JOptionPane.showMessageDialog(new JFrame(), "Please select input files", "Error", JOptionPane.ERROR_MESSAGE);
        	else
        	{
        		outputdirectory = new File(inputfiles[0].getPath().substring(0, inputfiles[0].getPath().lastIndexOf(File.separator)));
        		try
				{
					new ClassificationRunner(inputfiles, outputdirectory, indices.getText());
				} 
        		catch (Exception er)
				{
					er.printStackTrace();
				}
        		JOptionPane.showMessageDialog(new JFrame(), "Evaluation Complete", "Message", JOptionPane.PLAIN_MESSAGE);
        	}
        }
    }

    private static void createAndShowGUI() 
    {
        JFrame frame = new JFrame("MIMIC Weka Pipeline");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300,200);
        frame.setResizable(false);
        frame.add(new FileChooser());
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}