package org.genepattern.plot;

/*
 The Broad Institute
 SOFTWARE COPYRIGHT NOTICE AGREEMENT
 This software and its documentation are copyright (2003-2006) by the
 Broad Institute/Massachusetts Institute of Technology. All rights are
 reserved.

 This software is supplied without any warranty or guaranteed support
 whatsoever. Neither the Broad Institute nor MIT can be responsible for its
 use, misuse, or functionality.
 */

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.genepattern.genecruiser.ExtendedTable;
import org.genepattern.gui.UIUtil;
import org.genepattern.matrix.Dataset;
import org.genepattern.menu.MenuBar;
import org.genepattern.stats.Sorting;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * X axis is gene, y axis is score.
 *
 * @author Joshua Gould
 */

public class GeneListChart implements DataSource2<GeneListChart.GeneListChartModel, JComponent, ChartPanel> {
    private ChartPanel chartPanel;
    private SelectionRenderer renderer = new SelectionRenderer();
    private JSplitPane splitPane;
    private JFreeChart chart;
    private ExtendedTable table;
    private JTextField numNeighborsTextField;
    private XYSeriesCollection coll;
    private JPopupMenu popupMenu;
    private Dataset dataset;

    public JMenuBar getMenuBar(Container parentComponent) {
	return new MenuBar(table, chartPanel, parentComponent, true, false);
    }

    /**
     * Creates a new instance
     *
     */

    public GeneListChart() {
	table = new ExtendedTable() {
	    @Override
	    protected void showPopup(MouseEvent e) {
		if (popupMenu != null) {
		    popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	    }
	};
	table.setSortable(true);

	chart = ChartFactory.createScatterPlot(null, // title
		"Feature", "", null, // data
		PlotOrientation.VERTICAL, true, // create legend
		true, // generate tooltips
		false// generate URLs
		);

	renderer.setLinesVisible(false);
	chartPanel = new ChartPanel(chart, false, false, false, false, false);
	chart.getXYPlot().setRenderer(renderer);
	chartPanel.setMouseZoomable(true, false);

	ChartMouseListener listener = new ChartMouseListener() {

	    public void chartMouseClicked(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();
		if (entity instanceof XYItemEntity) {
		    XYItemEntity xyItemEntity = (XYItemEntity) entity;
		    int selectedItem = xyItemEntity.getItem();
		    int selectedSeries = xyItemEntity.getSeriesIndex();

		    // Number x = coll.getSeries(selectedSeries).getX(index);
		    // Number y = coll.getSeries(selectedSeries).getY(index);
		    renderer.toggleSelection(selectedSeries, selectedItem);
		    int rank = xyItemEntity.getDataset().getX(selectedSeries, selectedItem).intValue();
		    table.changeSelection(table.convertRowIndexToView(rank), 0, false, false);
		} else {
		    renderer.clear();
		    table.clearSelection();
		}
		chartPanel.repaint();
	    }

	    public void chartMouseMoved(ChartMouseEvent event) {
	    }
	};
	chartPanel.addChartMouseListener(listener);

	JPanel numNeighborsPanel = new JPanel(new FormLayout("4dlu, pref, 4dlu, pref", "2dlu, pref, 2dlu"));
	CellConstraints cc = new CellConstraints();
	numNeighborsPanel.add(new JLabel("Neighbors"), cc.xy(2, 2));
	numNeighborsTextField = new JTextField(10);
	numNeighborsTextField.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		try {
		    int neighbors = Integer.parseInt(numNeighborsTextField.getText().trim());
		} catch (NumberFormatException x) {
		    UIUtil.showErrorDialog(UIUtil.getWindowForComponent(chartPanel),
			    "Number of neighbors is not a number.");
		    return;
		}
	    }

	});
	numNeighborsPanel.add(numNeighborsTextField, cc.xy(4, 2));
	JScrollPane sp = new JScrollPane(table);

	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, sp);
	splitPane.setDividerLocation(chartPanel.getPreferredSize().height);

	table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
		    return;
		}
		int[] rows = table.getSelectedRows();
		int[] indices = new int[rows.length];
		for (int i = 0; i < rows.length; i++) {
		    indices[i] = table.convertRowIndexToModel(rows[i]);

		}
		renderer.clear();
		renderer.select(0, indices);
		chartPanel.repaint();
	    }
	});

    }

    public JComponent getChartPanel() {
	return chartPanel;
    }

    public JComponent getComponent() {
	return splitPane;
    }

    public ChartPanel getJComponent() {
	return chartPanel;
    }

    public String getName() {
	return "Nearest Neighbors";
    }

    public ExtendedTable getTable() {
	return table;
    }

    public void setData(GeneListChartModel model) {
	table.resetSortOrder();
	final double[] scores = model.getScores();
	final int[] indices = Sorting.index(scores, Sorting.DESCENDING);
	this.dataset = model.getDataset();
	final String metric = model.getDistanceMetric();

	table.setModel(new AbstractTableModel() {

	    public int getColumnCount() {
		return 2;
	    }

	    @Override
	    public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
		    return "Feature";
		case 1:
		    return metric;
		}
		return null;
	    }

	    public int getRowCount() {
		return scores.length;
	    }

	    public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
		    return dataset.getRowName(indices[rowIndex]);
		case 1:
		    return scores[indices[rowIndex]];
		}
		return null;
	    }

	});
	numNeighborsTextField.setText("" + scores.length);
	String geneName = model.getGeneName();
	XYSeries series = new XYSeries("Neighbors of " + geneName);

	coll = new XYSeriesCollection();
	coll.addSeries(series);

	String[] names = new String[scores.length];
	for (int i = 0, length = scores.length; i < length; i++) {
	    series.add(i, scores[indices[i]], false);
	    names[i] = dataset.getRowName(indices[i]);
	}
	chart.getXYPlot().getRangeAxis().setLabel(model.getDistanceMetric());
	chart.getXYPlot().setDataset(coll);

	SymbolAxis xAxis = new SymbolAxis("Feature", names);
	xAxis.setGridBandsVisible(false);
	xAxis.setVerticalTickLabels(true);
	// chart.getXYPlot().getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

	chart.getXYPlot().setDomainAxis(xAxis);

    }

    public static class GeneListChartModel {
	private String geneName;
	private double[] scores;
	private String distanceMetric;
	private Dataset dataset;

	public Dataset getDataset() {
	    return dataset;
	}

	public String getDistanceMetric() {
	    return distanceMetric;
	}

	public String getGeneName() {
	    return geneName;
	}

	public double[] getScores() {
	    return scores;
	}

	public void setDataset(Dataset dataset) {
	    this.dataset = dataset;
	}

	public void setDistanceMetric(String distanceMetric) {
	    this.distanceMetric = distanceMetric;
	}

	public void setGeneName(String geneName) {
	    this.geneName = geneName;
	}

	public void setScores(double[] scores) {
	    this.scores = scores;
	}
    }

}
