/*
 *    The Broad Institute
 *    SOFTWARE COPYRIGHT NOTICE AGREEMENT
 *    This software and its documentation are copyright (2003-2006) by the
 *    Broad Institute/Massachusetts Institute of Technology. All rights are
 *    reserved.
 *
 *    This software is supplied without any warranty or guaranteed support
 *    whatsoever. Neither the Broad Institute nor MIT can be responsible for its
 *    use, misuse, or functionality.
 */

package org.genepattern.clustering.hierarchical;

import java.awt.Dimension;
import java.io.File;
import java.util.Collections;

import javax.swing.JFrame;

import org.genepattern.gui.OS;
import org.genepattern.gui.SystemPropertyManager;
import org.genepattern.gui.UIUtil;
import org.genepattern.heatmap.HeatMap;
import org.genepattern.heatmap.HeatMapElementPanel;
import org.genepattern.heatmap.HeatMapMenuBar;
import org.genepattern.io.DefaultDatasetCreator;
import org.genepattern.io.ParseException;
import org.genepattern.io.stanford.CdtParser;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.DatasetConstants;
import org.genepattern.module.VisualizerUtil;

/**
 * Hierarchical clustering application.
 * 
 * @author Joshua Gould
 * 
 */
public class HCLApp {
    public static void main(String[] args) {
	SystemPropertyManager.load();
	OS.setLookAndFeel();

	try {

	    parseCmdLine(args);

	} catch (ParseException e) {
	    UIUtil.showErrorDialog(null, e.getMessage());
	    System.exit(0);
	} catch (OutOfMemoryError e) {
	    UIUtil
		    .showErrorDialog(null,
			    "Not enough memory available to launch the viewer with the specified dataset.");
	    System.exit(0);
	} catch (Throwable t) {
	    t.printStackTrace();
	}

    }

    private static void parseCmdLine(String[] args) throws ParseException {
	String cdtFile = args[0];
	String gtrFile = null;
	String atrFile = null;
	for (int i = 1; i < args.length; i++) {
	    String flag = args[i].substring(0, 2);
	    String value = args[i].substring(2, args[i].length());
	    value = value.trim();
	    if (value.equals("")) {
		continue;
	    }
	    if (flag.equals("-a")) {
		atrFile = value;
	    } else if (flag.equals("-g")) {
		gtrFile = value;
	    } else {
		System.err.println("Unknown option: " + flag);
		System.exit(1);
	    }
	}

	createViewer(cdtFile, gtrFile, atrFile);
    }

    private static void createViewer(String cdtFile, String gtrFile, String atrFile) throws ParseException {
	CdtParser parser = new CdtParser();
	DefaultDatasetCreator creator = new DefaultDatasetCreator();
	Dataset data = (Dataset) VisualizerUtil.readDataset(null, parser, cdtFile, creator);
	if (data == null) {
	    System.exit(1);
	}
	AtrGtrReader gtrReader = null;
	if (gtrFile != null) {
	    try {
		String[] geneIds = new String[data.getRowCount()];
		for (int i = 0, rows = data.getRowCount(); i < rows; i++) {
		    geneIds[i] = data.getRowMetadata(i, DatasetConstants.GENE_ID);
		}
		gtrReader = new AtrGtrReader(geneIds, gtrFile);
	    } catch (Exception e) {
		throw new ParseException("An error occurred while reading the file " + gtrFile);
	    }
	}
	AtrGtrReader atrReader = null;
	if (atrFile != null) {
	    try {
		String[] arrayIds = new String[data.getColumnCount()];
		for (int j = 0, columns = data.getColumnCount(); j < columns; j++) {
		    arrayIds[j] = data.getColumnMetadata(j, DatasetConstants.ARRAY_ID);
		}
		atrReader = new AtrGtrReader(arrayIds, atrFile);
	    } catch (Exception e) {
		throw new ParseException("An error occurred while reading the file " + atrFile);
	    }
	}

	FeatureTreePanel geneTree = null;
	int geneTreeWidth = 150;
	int arrayTreeHeight = 150;
	int elementSize = HeatMapElementPanel.DEFAULT_ELEMENT_SIZE;
	if (gtrReader != null) {
	    geneTree = new FeatureTreePanel(gtrReader);
	    geneTree.setElementHeight(elementSize);
	    Dimension size = new Dimension(geneTreeWidth, geneTree.getPreferredSize().height);
	    geneTree.setPreferredSize(size);
	    geneTree.setSize(size);
	}
	SampleTreePanel arrayTree = null;
	if (atrReader != null) {
	    arrayTree = new SampleTreePanel(atrReader);
	    arrayTree.setElementWidth(elementSize);
	    Dimension size = new Dimension(arrayTree.getPreferredSize().width, arrayTreeHeight);
	    arrayTree.setPreferredSize(size);
	    arrayTree.setSize(size);
	}
	JFrame frame = new JFrame("Hierarchical Clustering Viewer - " + new File(cdtFile).getName());
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	final HeatMap c = new HeatMap(frame, data, geneTree, arrayTree);
	c.loadPreferences();
	Runtime.getRuntime().addShutdownHook(new Thread() {

	    @Override
	    public void run() {
		c.savePreferences();
	    }
	});

	frame.setJMenuBar(new HeatMapMenuBar(c, Collections.EMPTY_MAP));
	frame.setContentPane(c);
	// The addition of the next line is the only change from the v8 code.
	frame.setPreferredSize(new Dimension(1024, 768));
	UIUtil.showFrame(frame);
    }
}
