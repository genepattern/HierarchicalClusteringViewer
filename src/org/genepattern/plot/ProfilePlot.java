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

package org.genepattern.plot;

import java.awt.Container;

import javax.swing.JMenuBar;

import org.genepattern.gui.GPResourceBundleManager;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.DatasetUtil;
import org.genepattern.menu.MenuBar;
import org.genepattern.menu.jfree.JFreeUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

public class ProfilePlot implements DataSource<Dataset, ChartPanel> {

    private JFreeChart jfreeChart;

    private ChartPanel chartPanel;

    private boolean centroidPlot;

    private boolean xAxisLabelsVisible = true;

    private boolean legendVisible = true;

    private boolean shapesVisible = true;

    private Dataset dataset;

    private GPResourceBundleManager resourceManager;

    /**
     * Creates a new instance
     *
     *
     * @param centroidPlot
     *                if <tt>true</tt> plot data centroids
     */
    public ProfilePlot(boolean centroidPlot, GPResourceBundleManager resourceManager) {
	this.resourceManager = resourceManager;
	jfreeChart = ChartFactory.createScatterPlot("",
	// title
		resourceManager.getString("Column"),
		// x-axis label
		resourceManager.getString("Profile.Plot.Value.Label"),
		// y-axis label
		null,
		// data
		PlotOrientation.VERTICAL, false,
		// create legend?
		false,
		// generate tooltips?
		false
	// generate URLs?
		);

	chartPanel = ChartUtil.createChartPanel(jfreeChart);
	this.centroidPlot = centroidPlot;
	if (centroidPlot) {
	    jfreeChart.getXYPlot().setRenderer(new XYErrorBarRenderer());
	} else {
	    XYLineAndShapeRenderer lineRenderer = (XYLineAndShapeRenderer) jfreeChart.getXYPlot().getRenderer();
	    lineRenderer.setLinesVisible(true);
	    setShapesVisible(shapesVisible);
	}
    }

    /**
     * Sets the dataset to plot
     *
     * @param data
     *                a <tt>Dataset</tt> instance
     */
    public void setData(Dataset data) {
	this.dataset = data;
	String[] columnKeys = DatasetUtil.getColumnNames(dataset);
	if (xAxisLabelsVisible) {
	    SymbolAxis xAxis = new SymbolAxis(resourceManager.getString("Column"), columnKeys);
	    xAxis.setVerticalTickLabels(true);
	    xAxis.setGridBandsVisible(false);
	    jfreeChart.getXYPlot().setDomainAxis(xAxis);
	}
	if (centroidPlot) {
	    doCentroidPlot();
	} else {
	    doProfilePlot();
	}

    }

    private void doCentroidPlot() {
	XYErrorBarDataset coll = new XYErrorBarDataset();
	XYErrorBarSeries series = new XYErrorBarSeries(resourceManager.getString("Centroid.Plot.Menu.Item"));
	coll.addSeries(series);
	// calculate mean and stdev for each column
	for (int c = 0, columns = dataset.getColumnCount(); c < columns; c++) {
	    int n = 0;
	    double[] rowValues = new double[dataset.getRowCount()];
        for (int r = 0, rows = dataset.getRowCount(); r < rows; r++)
        {
            rowValues[r]= dataset.getValue(r, c);
	    }
        Mean m = new Mean();
        double mean = m.evaluate(rowValues);

        StandardDeviation sd = new StandardDeviation();
        double stdDev = sd.evaluate(rowValues, mean);

	    series.add(c, mean, mean - stdDev, mean + stdDev);
	}
	jfreeChart.getXYPlot().setDataset(coll);
	jfreeChart.clearSubtitles();
    }

    private void doProfilePlot() {
	XYSeriesCollection coll = new XYSeriesCollection();
	for (int i = 0, rows = dataset.getRowCount(); i < rows; i++) {

	    XYSeries series = new XYSeries(dataset.getRowName(i));
	    for (int j = 0, columns = dataset.getColumnCount(); j < columns; j++) {
		series.add(j, dataset.getValue(i, j), false);
	    }
	    coll.addSeries(series);
	}
	jfreeChart.getXYPlot().setDataset(coll);
	setLegendVisible(legendVisible);
    }

    public ChartPanel getComponent() {
	return chartPanel;
    }

    public String getName() {
	return centroidPlot ? resourceManager.getString("Centroid.Plot.Menu.Item") : resourceManager
		.getString("Profile.Menu.Item");
    }

    public boolean isXAxisLabelsVisible() {
	return xAxisLabelsVisible;
    }

    public void setXAxisLabelsVisible(boolean axisLabelsVisible) {
	xAxisLabelsVisible = axisLabelsVisible;
	jfreeChart.getXYPlot().getDomainAxis().setVisible(xAxisLabelsVisible);
    }

    public boolean isLegendVisible() {
	return legendVisible;
    }

    public void setLegendVisible(boolean legendVisible) {
	this.legendVisible = legendVisible;
	if (legendVisible && dataset.getRowCount() <= 5) { // only show
	    // legend if
	    // showing 5 genes or less
	    JFreeUtil.createLegend(jfreeChart);
	} else {
	    jfreeChart.clearSubtitles();
	}
    }

    public boolean isShapesVisible() {
	return shapesVisible;
    }

    public void setShapesVisible(boolean shapesVisible) {
	this.shapesVisible = shapesVisible;
	XYLineAndShapeRenderer lineRenderer = (XYLineAndShapeRenderer) jfreeChart.getXYPlot().getRenderer();
	lineRenderer.setShapesVisible(shapesVisible);
    }

    public JMenuBar getMenuBar(Container parentComponent) {
	return new MenuBar(chartPanel, parentComponent);
    }
}
