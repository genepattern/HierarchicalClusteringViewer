package edu.mit.genome.gp.ui.hclviewer;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import edu.mit.genome.dataobj.jg.*;

/**
 *  see http://microarray.ccgb.umn.edu/smd/html/MicroArray/help/formats.shtml
 *
 * @author     jgould
 * @created    July 31, 2003
 */

public class CdtParser {
	/**  min value in cdt file */
	float minValue = Integer.MAX_VALUE;
	/**  max value in cdt file */
	float maxValue = Integer.MIN_VALUE;
	/**  matrix of values in cdt file */
	Dataset matrix;
	TreeNode geneTreeRoot;
	TreeNode arrayTreeRoot;
	private Map availableColumnNodes = new HashMap();
	private Map availableRowNodes = new HashMap();
	

	
	public static void main(String[] args) {

	/*	CmdLineParser parser = new CmdLineParser();
		// NOTE we check for .gtr file and .atr file even if not specified on command line
		CmdLineParser.Option cdtOption = parser.addStringOption('f', "cdt_filename");
		CmdLineParser.Option gtrOption = parser.addStringOption('r', "gtr_filename");
		CmdLineParser.Option atrOption = parser.addStringOption('c', "atr_filename");
		try {
			parser.parse(args);
		} catch(CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			System.exit(2);
		}

		String cdtFileName = (String) parser.getOptionValue(cdtOption);
		CdtParser cdtParser = new CdtParser();
		if(cdtFileName == null) {
			System.err.println("Usage: java CdtParser -f cdt_file [-r gtr_file] [-c atr_file]");
			return;
		}*/
		
		String cdtFileName = args[0];
		String atrFileName = null;
		String gtrFileName = null;
		
		if(args.length==2) {
			String s = args[1];	
			if(s.toLowerCase().endsWith(".atr")) {
				atrFileName = s;
			} else {
				gtrFileName = s;
			}
		} else if(args.length==3) {
			String s = args[1];	
			if(s.toLowerCase().endsWith(".atr")) {
				atrFileName = s;
				gtrFileName = args[2];
			} else {
				gtrFileName = s;
				atrFileName = args[2];
			}
		}
		
		
		CdtParser cdtParser = new CdtParser();
		try {
			cdtParser.parse(cdtFileName, atrFileName, gtrFileName);
		} catch(Exception e) {
			System.err.println("Parsing of cdt file failed.");
			e.printStackTrace();
			System.exit(2);
		}

		// String gtrFileName = (String) parser.getOptionValue(gtrOption); ignored
		Dendrogram geneTree = null;
		if(cdtParser.geneTreeRoot != null) {
			try {
				geneTree = new Dendrogram(cdtParser.geneTreeRoot, Dendrogram.LEFT_ORIENTATION);
				geneTree.setLeafNodeSpacing(0, 4);
			} catch(Exception e) {
				System.err.println("Parsing of gtr file failed.");
				e.printStackTrace();
				System.exit(2);
			}
		}
		//	String atrFileName = (String) parser.getOptionValue(atrOption); ignored
		Dendrogram sampleTree = null;
		if(cdtParser.arrayTreeRoot != null) {
			try {
				sampleTree = new Dendrogram(cdtParser.arrayTreeRoot, Dendrogram.TOP_ORIENTATION);
				sampleTree.setLeafNodeSpacing(0, 4);
			} catch(Exception e) {
				System.err.println("Parsing of atr file failed.");
				e.printStackTrace();
				System.exit(2);
			}
		}
		HCL p = new HCL(cdtParser.matrix, cdtParser.minValue, cdtParser.maxValue, sampleTree, geneTree);
		HCLFrame f = new HCLFrame(p);
	}

	static int findIndexOf(String[] tokens, String key) {
		for(int i = 0, length = tokens.length; i < length; i++) {
			if(tokens[i].equals(key)) {
				return i;
			}
		}
		return -1;
	}


	/**
	a temporary hack for 1.3 to replace String.split
	*/
	static String[] split(String line) {
		java.util.List temp = new java.util.LinkedList();
		// if line contained 5\t\t3, an empty string would not be returned between 5 and 3 unless we do the following
		StringTokenizer st = new StringTokenizer(line, "\t", true);
		boolean previousWasDelim = false;
		while(st.hasMoreTokens()) {
			String s = st.nextToken();
			if(s.equals("\t")) {
				if(previousWasDelim) {
					temp.add("");	
				}
				previousWasDelim = true;
			} else {
				temp.add(s);
				previousWasDelim = false;
			}
		}
		String[] tokens = new String[temp.size()];
		return (String[]) temp.toArray(tokens);
	}

	static int min(int i, int j) {
		return ((i < j) ? i : j);
	}

	public void parse(String file, String atrFileName, String gtrFileName) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(file));
		
		// Remove Comment Lines

/*    for (i=DataFileList->Count-1;i>=0;i--) //FIXME
    {
        if (DataFileList->Strings[i].SubString(1,6) == "REMARK")
        {
            DataFileList->Delete(i);
        }
        if (DataFileList->Strings[i].Length() < 3)
        {
            DataFileList->Delete(i);
        }
    }*/
	 
		int Columns = 0;
		// Parse First Line
		java.util.List names = new ArrayList();
		java.util.List rowNames = new ArrayList();
		java.util.List rowDescriptions = new ArrayList();
		
		String[] headers = split(br.readLine());
		int numHeaders = headers.length;
		boolean[] IsData = new boolean[numHeaders];
		
		Arrays.fill(IsData, true);

		int GIDIndex = findIndexOf(headers, "GID");
		int UniqueIDIndex = 0;
		boolean LoadGeneTree = false;
		if(GIDIndex > -1) {
			LoadGeneTree = true;
			UniqueIDIndex = GIDIndex + 1;
			IsData[GIDIndex] = false;
			
		}


		String UniqueID = headers[UniqueIDIndex];
		IsData[UniqueIDIndex] = false;

		int NameIndex = findIndexOf(headers, "NAME");

		if(NameIndex > -1) {
		
			IsData[NameIndex] = false;
		}

		int GeneWeightIndex = findIndexOf(headers, "GWEIGHT");
		
		if(GeneWeightIndex > -1) {
			IsData[GeneWeightIndex] = false;
		}
		
	 
		matrix =  DatasetFactory.createDataset(1, headers.length / 2);

		for(int i = 0; i < headers.length; i++) {
			if(IsData[i]) {
				String array = headers[i];
				names.add(array);
				Columns++;
			}
		}
		
		int Rows = 0;
		String line = null;
		boolean LoadArrayTree = false;
		String ArrayWeightString = null;
		String ArrayHeaderString = null;
		while((line = br.readLine()) != null) {
			String[] tokens = split(line);// toArray(line);
	
			if(tokens[0].equals("AID")) {
				LoadArrayTree = true;
				ArrayHeaderString = line;
				
			} else if(tokens[0].equals("EWEIGHT")) {
				ArrayWeightString = tokens[0];
			} else {
				String GID = null;
				if((GIDIndex > -1) && (GIDIndex < tokens.length)) {
					GID = tokens[GIDIndex];
				}

				String Name = null;
				if((NameIndex > -1) && (NameIndex < tokens.length)) {
					Name = tokens[NameIndex];
				}

				String ID = null;
				if((UniqueIDIndex > -1) && (UniqueIDIndex < tokens.length)) {
					ID = tokens[UniqueIDIndex];
				}

				double Weight = 1.0;
				if((GeneWeightIndex > -1) && (GeneWeightIndex < tokens.length)) {
					try {
						Weight = Double.parseDouble(tokens[GeneWeightIndex]);
					} catch(NumberFormatException nfe) {
					}
				}
				if (LoadGeneTree) {
                try {
                  //  int nGID =  Integer.parseInt(GID.substring("GENE".length(),GID.length()-1)); 
						  TreeNode node = new TreeNode(null, null, 1, ID);
						  node.position = Rows;
						  availableRowNodes.put(GID, node);
						//  System.out.println("created " + GID);
                }
                catch (NumberFormatException e) {
						 e.printStackTrace();
                }
            }
				
				rowNames.add(ID);
				rowDescriptions.add(Name);
				
				int columnIndex = 0;

				for(int j = 0; j < min(tokens.length, headers.length); j++) {
					if(IsData[j]) {
						try {
							float Val = Float.parseFloat(tokens[j]);
							minValue = Val < minValue ? Val : minValue;
							maxValue = Val > maxValue ? Val : maxValue;
							matrix.set(Rows, columnIndex, Val);
						} catch(NumberFormatException nfe) {
							matrix.set(Rows, columnIndex, Float.NaN);// missing value
						}
						
						columnIndex++;
					}
				}

				/*
				    Handle case where there are too few columns of data
				  */
				for(int j = columnIndex; j < Columns; j++) {
					matrix.set(Rows, j, Float.NaN);
					// GeneTreeNode->Data->Mask[j] = false;
				}

				Rows++;
			}
		}
	
		
		if(LoadArrayTree) {
			StringTokenizer st = new StringTokenizer(ArrayHeaderString);
			st.nextToken(); // skip 'AID'
			
			for(int i = 0, size = matrix.getColumnDimension(); i < size; i++) {
				String name = matrix.getName(i);
				TreeNode node = new TreeNode(null, null, 1, name);
				node.position = i;
				String aid = st.nextToken();
			//	System.out.println("created " + aid);
				availableColumnNodes.put(aid, node);
			}
		}
		  
		
		matrix.setNames(names);
		matrix.setRowNames(rowNames);
		matrix.setRowDescriptions(rowDescriptions);
	
		if(gtrFileName!=null) {
			geneTreeRoot = parseGtrOrAtr(gtrFileName, true, availableRowNodes);
		}
		if(atrFileName!=null) {
			arrayTreeRoot = parseGtrOrAtr(atrFileName, false, availableColumnNodes);
		}
	}

	TreeNode parseGtrOrAtr(String file, boolean isGeneTree, Map availableNodes) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
	
		String line = null;
		TreeNode root = null;
		while((line = br.readLine()) != null) {
			String[] tokens = split(line);
			String nodeId = tokens[0];
			String leftChildId = tokens[1];
			String rightChildId = tokens[2];
			//System.out.println("nodeId " + nodeId + " leftChildId " + leftChildId + " rightChildId " + rightChildId);
			double correlation = Double.parseDouble(tokens[3]);

			// nGID1 =  (Child1.SubString(5,Child1.Length()-5)).ToInt();
			//nGID2 =  (Child2.SubString(5,Child2.Length()-5)).ToInt();


			TreeNode leftNode = (TreeNode) availableNodes.remove(leftChildId);
			TreeNode rightNode = (TreeNode) availableNodes.remove(rightChildId);
			if(leftNode == null) {
				leftNode = new TreeNode(null, null, 1, leftChildId);
				//System.out.println("retrieved leftChildId " + leftChildId);
			}

			if(rightNode == null) {
				rightNode = new TreeNode(null, null, 1, rightChildId);
				//System.out.println("retrieved rightChildId " + rightChildId);
			}

			root = new TreeNode(leftNode, rightNode, correlation, nodeId);
			availableNodes.put(nodeId, root);
		}
		return root;
	}

}

