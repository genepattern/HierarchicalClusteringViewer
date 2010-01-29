package org.genepattern.heatmap;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.genepattern.gui.CenteredDialog;
import org.genepattern.gui.FileChooser;
import org.genepattern.gui.GPResourceBundleManager;
import org.genepattern.gui.UIUtil;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.DatasetConstants;
import org.genepattern.matrix.DatasetUtil;
import org.genepattern.menu.MenuAction;
import org.genepattern.menu.MenuItemAction;
import org.genepattern.module.VisualizerUtil;
import org.genepattern.plot.DatasetHistogramPlot;
import org.genepattern.plot.GeneListChart;
import org.genepattern.plot.PlotDialog;
import org.genepattern.stats.DatasetStats;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class DatasetAction extends MenuAction {

    private Dataset dataset;
    private Container parentComponent;
    private GPResourceBundleManager resourceBundleManager;

    public DatasetAction(Container parentComponent, GPResourceBundleManager resourceBundleManager) {
	super("Dataset");
	this.parentComponent = parentComponent;
	this.resourceBundleManager = resourceBundleManager;
    }

    public Dataset getDataset() {
	return dataset;
    }

    public void setDataset(Dataset dataset) {
	this.dataset = dataset;
    }

    protected abstract int[] getColumnIndices();

    protected abstract int[] getRowIndices();

    public static class GeneNeighborsAction extends DatasetMenuItemAction {
	private PlotDialog geneListDialog;

	public GeneNeighborsAction(DatasetAction datasetAction) {
	    super("Nearest Neighbors", datasetAction);
	    geneListDialog = PlotDialog.createGeneNeighborsPlotInstance(datasetAction.parentComponent,
		    datasetAction.resourceBundleManager);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    final int[] rowIndices = datasetAction.getRowIndices();
	    if (rowIndices.length != 1) {
		UIUtil.showMessageDialog(datasetAction.parentComponent, "Please select one feature.");
		return;
	    }
	    int rowIndex1 = rowIndices[0];
	    double[] dist = new double[datasetAction.dataset.getRowCount()];
	    final String[] choices = new String[] { "Cosine Distance", "Euclidean Distance", "Manhattan Distance",
		    "Pearson Correlation" };
	    String result = (String) JOptionPane.showInputDialog(datasetAction.parentComponent,
		    "Please select a distance metric", "Distance Metric", JOptionPane.QUESTION_MESSAGE, null, choices,
		    choices[3]);

	    if (result != null) {
		if (result.equals(choices[0])) {
		    for (int i = 0, length = dist.length; i < length; i++) {
			dist[i] = DatasetStats.cosineDistance(datasetAction.dataset, rowIndex1, i);
		    }
		} else if (result.equals(choices[1])) {
		    for (int i = 0, length = dist.length; i < length; i++) {
			dist[i] = DatasetStats.euclideanDistance(datasetAction.dataset, rowIndex1, i);
		    }
		} else if (result.equals(choices[2])) {
		    for (int i = 0, length = dist.length; i < length; i++) {
			dist[i] = DatasetStats.manhattanDistance(datasetAction.dataset, rowIndex1, i);
		    }
		} else if (result.equals(choices[3])) {
		    for (int i = 0, length = dist.length; i < length; i++) {
			dist[i] = DatasetStats.pearsonCorrelation(datasetAction.dataset, rowIndex1, i);
		    }
		} else {
		    throw new IllegalArgumentException();
		}

		GeneListChart.GeneListChartModel model = new GeneListChart.GeneListChartModel();
		model.setDataset(datasetAction.dataset);
		model.setDistanceMetric(result);
		model.setGeneName(datasetAction.dataset.getRowName(rowIndex1));
		model.setScores(dist);
		geneListDialog.setData(model);
	    }

	}
    }

    public static class CentroidPlotAction extends DatasetMenuItemAction {
	/** Displays centroid plot of selected rows and columns */
	private PlotDialog centroidPlot;

	public CentroidPlotAction(DatasetAction datasetAction) {
	    super(datasetAction.resourceBundleManager.getString("Centroid.Plot.Menu.Item"), datasetAction);
	    centroidPlot = PlotDialog.createCentroidPlotInstance(datasetAction.parentComponent,
		    datasetAction.resourceBundleManager);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    final int[] rowIndices = datasetAction.getRowIndices();
	    int[] _sampleIndices = datasetAction.getColumnIndices();
	    if (_sampleIndices.length == 0) {
		_sampleIndices = null;
	    }
	    final int[] sampleIndices = _sampleIndices;
	    if (rowIndices.length < 2) {
		UIUtil.showMessageDialog(datasetAction.parentComponent, datasetAction.resourceBundleManager
			.getString("features.select.two.error"));
		return;
	    }
	    centroidPlot.setData(DatasetUtil.sliceView(datasetAction.dataset, rowIndices, sampleIndices));
	}

    }

    public static class HistogramAction extends DatasetMenuItemAction {
	private DatasetHistogramPlot datasetHistogramPlot;

	public HistogramAction(DatasetAction datasetAction) {
	    super(datasetAction.resourceBundleManager.getString("Histogram.Menu.Item"), datasetAction);
	    datasetHistogramPlot = new DatasetHistogramPlot(datasetAction.parentComponent);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    final int[] rowIndices = datasetAction.getRowIndices();
	    int[] _sampleIndices = datasetAction.getColumnIndices();
	    if (_sampleIndices.length == 0) {
		_sampleIndices = null;
	    }
	    final int[] sampleIndices = _sampleIndices;
	    if (rowIndices.length == 0) {
		UIUtil.showMessageDialog(datasetAction.parentComponent, datasetAction.resourceBundleManager
			.getString("features.select.one.error"));
		return;
	    }
	    datasetHistogramPlot.setData(DatasetUtil.sliceView(datasetAction.dataset, rowIndices, sampleIndices));
	}
    }

    public static class ProfileAction extends DatasetMenuItemAction {
	/** Displays profile of selected rows and columns */
	private PlotDialog profilePlot;

	public ProfileAction(DatasetAction datasetAction) {
	    super(datasetAction.resourceBundleManager.getString("Profile.Menu.Item"), datasetAction);
	    profilePlot = PlotDialog.createScatterPlotInstance(datasetAction.parentComponent,
		    datasetAction.resourceBundleManager);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    int[] rowIndices = datasetAction.getRowIndices();
	    int[] sampleIndices = datasetAction.getColumnIndices();
	    if (sampleIndices.length == 0) {
		sampleIndices = null;
	    }
	    if (rowIndices.length == 0) {
		UIUtil.showMessageDialog(datasetAction.parentComponent, datasetAction.resourceBundleManager
			.getString("features.select.one.error"));
		return;
	    }
	    profilePlot.setData(DatasetUtil.sliceView(datasetAction.dataset, rowIndices, sampleIndices));
	}

    }

    public static class SaveDatasetAction extends DatasetMenuItemAction {
	private SaveDataset saveDataset;

	public SaveDatasetAction(DatasetAction datasetAction) {
	    super(datasetAction.resourceBundleManager.getString("Save.Dataset.Menu.Item"), datasetAction);
	    this.saveDataset = new SaveDataset();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    saveDataset.show();
	}

	private class SaveDataset {
	    private JDialog dialog;

	    private SaveDataset() {
		JLabel label = new JLabel("Output File:");
		JPanel filePanel = new JPanel();
		FormLayout f = new FormLayout("left:pref:none, 3dlu, left:pref:none, left:pref:none",
			"pref, 5dlu, pref");
		filePanel.setLayout(f);
		final JTextField input = new JTextField(30);
		JButton btn = new JButton("Browse\u2026");
		btn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			File file = FileChooser.showSaveDialog(datasetAction.parentComponent);
			if (file != null) {
			    try {
				input.setText(file.getCanonicalPath());
			    } catch (IOException e1) {
				input.setText(file.getPath());
				e1.printStackTrace();
			    }
			}
			dialog.toFront();
		    }
		});
		CellConstraints cc = new CellConstraints();
		JLabel instructionsLabel = new JLabel("The selected columns and rows will be included in the dataset.");
		filePanel.add(label, cc.xy(1, 1));
		filePanel.add(input, cc.xy(3, 1));
		filePanel.add(btn, cc.xy(4, 1));
		final JButton cancelBtn = new JButton("Cancel");
		final JButton saveBtn = new JButton("Save");
		JPanel buttonPanel = UIUtil.buildOKCancelBar(saveBtn, cancelBtn);
		if (!GraphicsEnvironment.isHeadless()) {
		    dialog = CenteredDialog.createInstance(datasetAction.parentComponent);
		    dialog.setTitle("Save Dataset");
		    dialog.getContentPane().add(instructionsLabel, BorderLayout.NORTH);
		    dialog.getContentPane().add(filePanel, BorderLayout.CENTER);
		    dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		    dialog.pack();
		}
		ActionListener l = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			if (e.getSource() == saveBtn) {
			    String pathname = input.getText().trim();
			    if (pathname.equals("")) {
				UIUtil.showErrorDialog(datasetAction.parentComponent, "Please enter an output file.");
				return;
			    }
			    String outputFileFormat = DatasetUtil.containsData(datasetAction.dataset,
				    DatasetConstants.ABSENT_PRESENT_CALLS) ? "res" : "gct";
			    if (outputFileFormat.equals("gct") && !pathname.toLowerCase().endsWith(".gct")) {
				pathname += ".gct";
			    } else if (outputFileFormat.equals("res") && !pathname.toLowerCase().endsWith(".res")) {
				pathname += ".res";
			    }
			    if (!FileChooser.overwriteFile(datasetAction.parentComponent, new File(pathname))) {
				return;
			    }
			    int[] columns = datasetAction.getColumnIndices();
			    if (columns.length == 0) {
				columns = null;
			    }
			    int[] rows = datasetAction.getRowIndices();
			    if (rows.length == 0) {
				rows = null;
			    }
			    Dataset slicedData = DatasetUtil.sliceView(datasetAction.dataset, rows, columns);
			    VisualizerUtil.write(datasetAction.parentComponent, slicedData, outputFileFormat, pathname,
				    false);
			    dialog.dispose();
			} else if (e.getSource() == cancelBtn) {
			    dialog.dispose();
			}
		    }
		};
		saveBtn.addActionListener(l);
		cancelBtn.addActionListener(l);
	    }

	    void show() {
		dialog.setVisible(true);
	    }
	}
    }

    public static class ScatterPlotAction extends DatasetMenuItemAction {
	/** Displays column-column plot of selected rows and 2 columns */
	private PlotDialog columnColumnPlot;

	public ScatterPlotAction(DatasetAction datasetAction) {
	    super(datasetAction.resourceBundleManager.getString("Scatter.Plot.Menu.Item"), datasetAction);
	    columnColumnPlot = PlotDialog.createColumnColumnPlotInstance(datasetAction.parentComponent,
		    datasetAction.resourceBundleManager);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    int[] _rowIndices = datasetAction.getRowIndices();
	    if (_rowIndices.length == 0) {
		_rowIndices = null;
	    }
	    final int[] rowIndices = _rowIndices;
	    int[] _sampleIndices = datasetAction.getColumnIndices();
	    if (_sampleIndices.length != 2) {
		UIUtil.showMessageDialog(datasetAction.parentComponent, datasetAction.resourceBundleManager
			.getString("columns.select.two.error"));
		return;
	    }
	    final int[] sampleIndices = _sampleIndices;
	    columnColumnPlot.setData(DatasetUtil.sliceView(datasetAction.dataset, rowIndices, sampleIndices));
	}

    }

    private static class DatasetMenuItemAction extends MenuItemAction {
	protected DatasetAction datasetAction;

	public DatasetMenuItemAction(String text, DatasetAction datasetAction) {
	    super(text);
	    this.datasetAction = datasetAction;
	}

    }
}
