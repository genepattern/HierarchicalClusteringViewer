package edu.mit.genome.gp.ui.hclviewer;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import edu.mit.genome.annotation.*;
import edu.mit.genome.math.*;

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
	FloatMatrix matrix;
	TreeNode geneTreeRoot;
	TreeNode arrayTreeRoot;

	public TreeNode parseAtr_Gtr(String file) throws IOException {
		TreeNode root = null;

		BufferedReader br = new BufferedReader(new FileReader(file));
		Map availableNodes = new HashMap();
		String s = null;
		while((s = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(s, "\t");
			String nodeId = st.nextToken().trim();

			String leftChildId = st.nextToken().trim();
			String rightChildId = st.nextToken().trim();

			double correlation = Double.parseDouble(st.nextToken());

			double height = correlation;

			TreeNode leftNode = (TreeNode) availableNodes.remove(leftChildId);
			TreeNode rightNode = (TreeNode) availableNodes.remove(rightChildId);
			if(leftNode == null) {
				leftNode = new TreeNode(null, null, 1, leftChildId);
			}

			if(rightNode == null) {
				rightNode = new TreeNode(null, null, 1, rightChildId);
			}

			root = new TreeNode(leftNode, rightNode, height, nodeId);
			availableNodes.put(nodeId, root);
		}
		br.close();
		return root;
	}


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
			if(s.endsWith(".atr")) {
				atrFileName = s;
			} else {
				gtrFileName = s;
			}
		} else if(args.length==3) {
			String s = args[1];	
			if(s.endsWith(".atr")) {
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
		int Columns = 0;
		// Parse First Line
		String[] headers = split(br.readLine());
		int numHeaders = headers.length;
		boolean[] IsData = new boolean[numHeaders];
		//System.out.println("numHeaders " + numHeaders);
		Arrays.fill(IsData, true);

		int GIDIndex = findIndexOf(headers, "GID");
		int UniqueIDIndex = 0;
		boolean LoadGeneTree = false;
		if(GIDIndex > -1) {
			LoadGeneTree = true;
			UniqueIDIndex = GIDIndex + 1;
			IsData[GIDIndex] = false;
			//System.out.println("GIDIndex " + GIDIndex);
		}
		//System.out.println("UniqueIDIndex " + UniqueIDIndex);

		String UniqueID = headers[UniqueIDIndex];
		IsData[UniqueIDIndex] = false;

		int NameIndex = findIndexOf(headers, "NAME");

		if(NameIndex > -1) {
			//	System.out.println("NameIndex " + NameIndex);
			IsData[NameIndex] = false;
		}

		int GeneWeightIndex = findIndexOf(headers, "GWEIGHT");
		//System.out.println("GeneWeightIndex " + GeneWeightIndex);
		if(GeneWeightIndex > -1) {
			IsData[GeneWeightIndex] = false;
		}
		matrix = new FloatMatrix(1000, headers.length / 2);

		for(int i = 0; i < headers.length; i++) {
			if(IsData[i]) {
				String array = headers[i];
				Annotation colAnnot = new Annotation(array);
				matrix.setAnnotationForColumn(Columns, colAnnot);
				Columns++;
				//System.out.println("Columns " + Columns);
				// Arrays->Add(headers[i]);
			}
		}

		int Rows = 0;
		String line = null;
		boolean LoadArrayTree = false;
		String ArrayWeightString = null;
		String ArrayHeaderString = null;
		while((line = br.readLine()) != null) {

			String[] tokens = split(line);// toArray(line);
			//	for(int i = 0; i < tokens.length; i++) {
			//		System.out.println(tokens[i]);
			//	}
			if(tokens[0].equals("AID")) {
				LoadArrayTree = true;
				ArrayHeaderString = tokens[0];
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
					/*
					    for (int Pos=Name.Length();Pos>=0;Pos--)
					    {
					    if (Name.SubString(Pos,1) == "\"")
					    {
					    Name.Delete(Pos,1);
					    }
					    }
					  */
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
				Annotation rowAnnot = new Annotation(ID);
				rowAnnot.add("NAME", Name);
				//rowAnnot.add("GID", GID);
				matrix.setAnnotationForRow(Rows, rowAnnot);

				//   index = Genes->Add(ID);

				/*
				    if(LoadGeneTree) {
				    try {
				    nGID = (GID.SubString(5, GID.Length() - 5)).ToInt();
				    ListIndex[nGID] = index;
				    } catch(NumberFom e) {
				    }
				    }
				  */
				int columnIndex = 0;

				for(int j = 0; j < min(tokens.length, headers.length); j++) {
					if(IsData[j]) {
						try {
							float Val = Float.parseFloat(tokens[j]);
							minValue = Val < minValue ? Val : minValue;
							maxValue = Val > maxValue ? Val : maxValue;
							matrix.setElement(Rows, columnIndex, Val);

						} catch(NumberFormatException nfe) {

							matrix.setElement(Rows, columnIndex, Float.NaN);// missing value
						}
						//  GeneTreeNode->Data->Data[index] = Val;
						//GeneTreeNode->Data->Mask[index] = true;
						//  Sum += Val;
						// Sum2 += pow(Val,2.0);
						// DCount += 1.0;
						//  }
						// catch (EConvertError &E)
						//{
						//   GeneTreeNode->Data->Mask[index] = false;
						//}
						// index++;
						//System.out.println("columnIndex " + columnIndex);
						columnIndex++;
					}
				}

				/*
				    Handle case where there are too few columns of data
				  */
				for(int j = columnIndex; j < Columns; j++) {
					matrix.setElement(Rows, j, Float.NaN);
					// GeneTreeNode->Data->Mask[j] = false;
				}

				Rows++;
			}
		}

		matrix.trimToSize();
		String base = file.substring(0, file.lastIndexOf("."));
		//System.out.println("base " + base);
		if(gtrFileName!=null) {
			geneTreeRoot = parseGtrOrAtr(gtrFileName);
		}
		if(atrFileName!=null) {
			arrayTreeRoot = parseGtrOrAtr(atrFileName);
		}
	}

	TreeNode parseGtrOrAtr(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		Map availableNodes = new HashMap();
		String line = null;
		TreeNode root = null;
		while((line = br.readLine()) != null) {
			String[] tokens = split(line);
			String nodeId = tokens[0];
			String leftChildId = tokens[1];
			String rightChildId = tokens[2];
			double correlation = Double.parseDouble(tokens[3]);

			// nGID1 =  (Child1.SubString(5,Child1.Length()-5)).ToInt();
			//nGID2 =  (Child2.SubString(5,Child2.Length()-5)).ToInt();


			TreeNode leftNode = (TreeNode) availableNodes.remove(leftChildId);
			TreeNode rightNode = (TreeNode) availableNodes.remove(rightChildId);
			if(leftNode == null) {
				leftNode = new TreeNode(null, null, 1, leftChildId);
			}

			if(rightNode == null) {
				rightNode = new TreeNode(null, null, 1, rightChildId);
			}

			root = new TreeNode(leftNode, rightNode, correlation, nodeId);
			availableNodes.put(nodeId, root);
		}
		return root;
	}

}

