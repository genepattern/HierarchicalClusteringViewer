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
package org.genepattern.heatmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.genepattern.annotation.Annotation;
import org.genepattern.annotation.AnnotationsLegend;
import org.genepattern.annotation.AnnotatorColorModel;
import org.genepattern.annotation.AnnotatorModel;
import org.genepattern.annotation.AnnotatorModelEvent;
import org.genepattern.annotation.AnnotatorModelListener;
import org.genepattern.annotation.AnnotatorPanel;
import org.genepattern.annotation.DefaultAnnotation;
import org.genepattern.annotation.DefaultAnnotatorColorModel;
import org.genepattern.annotation.DefaultAnnotatorModel;
import org.genepattern.clustering.hierarchical.DendrogramColorEditor;
import org.genepattern.clustering.hierarchical.FeatureTreePanel;
import org.genepattern.clustering.hierarchical.Node;
import org.genepattern.clustering.hierarchical.SampleTreePanel;
import org.genepattern.genecruiser.ExtendedTable;
import org.genepattern.genecruiser.GeneCruiserModelEvent;
import org.genepattern.genecruiser.GeneCruiserModelListener;
import org.genepattern.gui.BrowserLauncher;
import org.genepattern.gui.CenteredDialog;
import org.genepattern.gui.Drawable;
import org.genepattern.gui.FileChooser;
import org.genepattern.gui.GPResourceBundleManager;
import org.genepattern.gui.KeyboardShortcutDialog;
import org.genepattern.gui.UIUtil;
import org.genepattern.gui.VerticalLabelUI;
import org.genepattern.heatmap.DatasetAction.CentroidPlotAction;
import org.genepattern.heatmap.DatasetAction.GeneNeighborsAction;
import org.genepattern.heatmap.DatasetAction.HistogramAction;
import org.genepattern.heatmap.DatasetAction.ProfileAction;
import org.genepattern.heatmap.DatasetAction.SaveDatasetAction;
import org.genepattern.heatmap.DatasetAction.ScatterPlotAction;
import org.genepattern.io.ImageUtil;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.DatasetConstants;
import org.genepattern.matrix.DatasetUtil;
import org.genepattern.matrix.DefaultClassVector;
import org.genepattern.menu.MenuItemAction;
import org.genepattern.plot.DatasetHistogramPlot;
import org.genepattern.table.TableDrawer;
import org.genepattern.table.VerticalTableDrawer;
import org.jdesktop.swingx.icon.ColumnControlIcon;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.TableColumnExt;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints.Alignment;

/** @author Joshua Gould */
public class HeatMap extends JComponent implements Drawable {

    /** Dialog that allows users to change options */
    protected HeatMapOptions optionsDialog;
    Container parentComponent;
    /** Dialog that shows info about heat map */
    private JDialog aboutDialog;
    private Component accessoryComponent;
    /** optional component placed below column keys */
    private Component belowColumnKeysComponent;
    /** holds belowColumnKeysComponent and columnAnnotatorPanel */
    private JPanel belowColumnKeysPanel;
    private JPanel bottomPanel;

    private AnnotationsLegend columnAnnotationsLegend;
    private AnnotatorModel columnAnnotatorModel;
    private AnnotatorPanel columnAnnotatorPanel;

    /** Component that renders column dendrogram */
    private SampleTreePanel columnDendrogram;
    private DendrogramColorEditor columnDendrogramColorEditor;
    private boolean columnNamesVisible = true;
    /** Table for column names */
    private ExtendedTable columnTable;
    /** Table model for <tt>columnTable</tt> */
    private AbstractTableModel columnTableModel;
    /** Dataset that is being displayed */
    private Dataset dataset;
    /** Displays histogram of selected rows and columns */
    private DatasetHistogramPlot datasetHistogramPlot;
    private String fontName = null;
    /** Model for <tt>rowTable</tt> */
    private RowTableModel rowTableModel;
    private JPanel heatMapAndRowAnnotatorPanel;
    /** Panel that draws heat map elements */
    private HeatMapElementPanel heatMapPanel;
    private KeyboardShortcutDialog keyBoardShortcutDialog;
    private String[] optionsDialogHiddenKeys;
    /** Popup menu when right-click on row or column table */
    private JPopupMenu popupMenu;
    /** Whether <tt>popupMenu</tt> is visible */
    private boolean popupMenuVisible = true;
    private int profileInTableColumnSize = 60;

    private JMenuItem quickLabelSamplesMenuItem;
    private GPResourceBundleManager resourceBundle;
    private AnnotationsLegend rowAnnotationsLegend;
    private AnnotatorModel rowAnnotatorModel;
    private AnnotatorPanel rowAnnotatorPanel;
    /** Component that renders row dendrogram */
    private FeatureTreePanel rowDendrogram;
    private DendrogramColorEditor rowDendrogramColorEditor;
    /** Table for row names and descriptions */
    private ExtendedTable rowTable;

    private JScrollPane scrollPane;
    private boolean squareAspect = true;
    private JLabel statusLabel = new JLabel("");
    private JPanel topPanel;
    /** The original dataset loaded. Used to restore view. */
    private final Dataset originalDataset;
    private Map<KeyStroke, Action> keyBoardShortcuts = new LinkedHashMap<KeyStroke, Action>();
    private DatasetAction datasetAction;
    private MenuItemAction profileAction;
    private MenuItemAction centroidPlotAction;
    private MenuItemAction histogramAction;
    private MenuItemAction scatterPlotAction;
    private MenuItemAction saveDatasetAction;
    private MenuItemAction geneNeighborsAction;

    /**
     * Creates a new instance of <tt>HeatMap</tt>
     *
     * @param parent
     *                The parent of this component.
     * @param dataset
     *                The dataset to display.
     */
    public HeatMap(Container parent, Dataset dataset) {
	this(parent, dataset, null, null);
    }

    /**
     * Creates a new instance of <tt>HeatMap</tt>
     *
     * @param parent
     *                The parent of this component.
     * @param dataset
     *                The dataset to display.
     * @param featureTree
     *                The feature tree to display or <tt>null</tt>.
     * @param sampleTree
     *                The sample tree to display or <tt>null</tt>.
     */
    public HeatMap(Container parent, Dataset dataset, FeatureTreePanel featureTree, SampleTreePanel sampleTree) {
	this(parent, dataset, featureTree, sampleTree, null);
    }

    /**
     * Creates a new instance of <tt>HeatMap</tt>
     *
     * @param parent
     *                The parent of this component.
     * @param dataset
     *                The dataset to display.
     * @param featureTree
     *                The feature tree to display or <tt>null</tt>.
     * @param sampleTree
     *                The sample tree to display or <tt>null</tt>.
     * @param accessoryComponent
     */
    public HeatMap(Container parent, Dataset dataset, FeatureTreePanel featureTree, SampleTreePanel sampleTree,
	    Component accessoryComponent) {
	this(parent, dataset, featureTree, sampleTree, accessoryComponent, null);
    }

    /**
     * Creates a new instance of <tt>HeatMap</tt>
     *
     * @param parent
     *                The parent of this component.
     * @param dataset
     *                The dataset to display.
     * @param featureTree
     *                The feature tree to display or <tt>null</tt>.
     * @param sampleTree
     *                The sample tree to display or <tt>null</tt>.
     * @param accessoryComponent
     * @param resourceBundle
     *                The resource bundle used to display strings.
     */
    public HeatMap(Container parent, Dataset dataset, FeatureTreePanel featureTree, SampleTreePanel sampleTree,
	    Component accessoryComponent, GPResourceBundleManager resourceBundle) {
	this(parent, dataset, featureTree, sampleTree, accessoryComponent, resourceBundle, false);
    }

    /**
     * Creates a new instance of <tt>HeatMap</tt>
     *
     * @param parent
     *                The parent of this component.
     * @param dataset
     *                The dataset to display.
     * @param featureTree
     *                The feature tree to display or <tt>null</tt>.
     * @param sampleTree
     *                The sample tree to display or <tt>null</tt>.
     * @param accessoryComponent
     * @param resourceBundle
     *                The resource bundle used to display strings.
     * @param variableRowSize
     *                Whether to allow variable row sizes.
     */
    public HeatMap(Container parent, Dataset dataset, FeatureTreePanel featureTree, SampleTreePanel sampleTree,
	    Component accessoryComponent, GPResourceBundleManager resourceBundle, boolean variableRowSize) {
	setDoubleBuffered(false);

	heatMapPanel = new HeatMapElementPanel(dataset, null, variableRowSize);
	squareAspect = !variableRowSize;
	this.accessoryComponent = accessoryComponent;
	this.originalDataset = dataset;
	this.parentComponent = parent;
	this.rowDendrogram = featureTree;
	this.resourceBundle = resourceBundle == null ? new GPResourceBundleManager() : resourceBundle;
	this.columnDendrogram = sampleTree;
	this.dataset = dataset;
	init(featureTree, sampleTree);
	installKeyBindings();
    }

    public void savePreferences() {
	Preferences prefs = Preferences.userNodeForPackage(getClass());
	prefs.putBoolean("color.scheme.gradient.color.map", getColorScheme().isGradientColorMap());
	prefs.putBoolean("color.scheme.relative", getColorScheme().isRelative());
	prefs.put("color.scheme.colors", ImageUtil.toString(getColorScheme().getColors()));
	prefs.putInt("row.size", getRowSize(0));
	prefs.putInt("column.size", getColumnSize());
	prefs.putBoolean("column.names.visible", isColumnNamesVisible());
	prefs.putBoolean("row.names.visible", isRowNamesVisible());
	prefs.putBoolean("row.descriptions.visible", isRowDescriptionsVisible());
	prefs.putBoolean("profile.in.tablevisible", isProfileInTableVisible());
	prefs.putBoolean("square.aspect", isSquareAspect());
	prefs.putBoolean("draw.grid", heatMapPanel.isDrawGrid());
	if (columnDendrogram != null) {
	    prefs.putInt("sample.dendrogram.height", columnDendrogram.getPreferredSize().height);
	    prefs.putInt("sample.dendrogram.line.thickness", columnDendrogram.getDendrogramLineThickness());
	}
	if (rowDendrogram != null) {
	    prefs.putInt("row.dendrogram.height", rowDendrogram.getPreferredSize().width);
	    prefs.putInt("row.dendrogram.line.thickness", rowDendrogram.getDendrogramLineThickness());
	}
	try {
	    prefs.sync();
	} catch (BackingStoreException e2) {
	    e2.printStackTrace();
	}
    }

    public void loadPreferences() {
	Preferences prefs = Preferences.userNodeForPackage(getClass());
	try {
	    prefs.sync();
	} catch (BackingStoreException e2) {
	    e2.printStackTrace();
	}
	boolean gradientColorMap = prefs.getBoolean("color.scheme.gradient.color.map", false);
	boolean relative = prefs.getBoolean("color.scheme.relative", true);
	ColorScheme cs = DefaultColorScheme.getInstance(relative, gradientColorMap);
	Color[] colors = ImageUtil.parseString(prefs.get("color.scheme.colors", ""));
	if (colors.length > 0) {
	    cs.setColors(colors);
	}
	setColorScheme(cs);
	setRowSize(prefs.getInt("row.size", HeatMapElementPanel.DEFAULT_ELEMENT_SIZE));
	setColumnSize(prefs.getInt("column.size", HeatMapElementPanel.DEFAULT_ELEMENT_SIZE));
	setColumnNamesVisible(prefs.getBoolean("column.names.visible", true));
	setRowNamesVisible(prefs.getBoolean("row.names.visible", true));
	setRowDescriptionsVisible(prefs.getBoolean("row.descriptions.visible", true));
	setProfileInTableVisible(prefs.getBoolean("profile.in.tablevisible", true));
	setSquareAspect(prefs.getBoolean("square.aspect", true));
	heatMapPanel.setDrawGrid(prefs.getBoolean("draw.grid", true));
	if (columnDendrogram != null) {
	    columnDendrogram.setPreferredSize(new Dimension(columnDendrogram.getPreferredSize().width, prefs.getInt(
		    "sample.dendrogram.height", 150)));
	    columnDendrogram.setDendrogramLineThickness(prefs.getInt("sample.dendrogram.line.thickness", 1));
	}
	if (rowDendrogram != null) {
	    rowDendrogram.setPreferredSize(new Dimension(prefs.getInt("row.dendrogram.height", 150), rowDendrogram
		    .getPreferredSize().height));
	    rowDendrogram.setDendrogramLineThickness(prefs.getInt("row.dendrogram.line.thickness", 1));
	}

    }

    protected void init(FeatureTreePanel featureTree, SampleTreePanel sampleTree) {
	rowAnnotatorModel = new DefaultAnnotatorModel();
	columnAnnotatorModel = new DefaultAnnotatorModel();

	quickLabelSamplesMenuItem = new JMenuItem(this.resourceBundle.getString("Label.Columns.Menu.Item"));
	quickLabelSamplesMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		// ExtendedTable table = new ExtendedTable(columnTableModel);
		// ((JLabel) table.getDefaultRenderer(String.class)).setUI(new
		// VerticalLabelUI(false));
		// table.setRowSelectionAllowed(false);
		// table.setGridColor(new Color(239, 239, 255));
		// table.setColumnSelectionAllowed(true);
		String classZeroLabel = UIUtil
			.showInputDialog(parentComponent, "Enter class name for selected columns");
		if (classZeroLabel != null && !classZeroLabel.equals("")) {
		    int[] columnIndices = columnTable.getSelectedColumns();
		    if (columnIndices.length == 0) {
			UIUtil.showErrorDialog(parentComponent, "Please select at least one column.");
			return;
		    }
		    String[] x = new String[HeatMap.this.dataset.getColumnCount()];
		    String not = "not ";
		    if (Character.isUpperCase(classZeroLabel.charAt(0))) {
			not = "Not ";
		    }
		    String classOneLabel = not + classZeroLabel;
		    Arrays.fill(x, classOneLabel);
		    for (int j = 0, columns = columnIndices.length; j < columns; j++) {
			x[columnIndices[j]] = classZeroLabel;
		    }
		    DefaultClassVector classVector = new DefaultClassVector(x);
		    DefaultAnnotation a = new DefaultAnnotation(DatasetUtil.getColumnNames(HeatMap.this.dataset),
			    classVector);
		    a.setName(classZeroLabel + " vs. " + classOneLabel);
		    ((DefaultAnnotatorModel) columnAnnotatorModel).addAnnotation(0, a);
		}
	    }
	});
	// popupMenu.add(quickLabelSamplesMenuItem);

	if (rowDendrogram != null) {
	    addPropertyChangeListener("rowSize", rowDendrogram);
	    rowDendrogram.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    if (e.getFirstIndex() != -1) {
			rowTable.setRowSelectionInterval(e.getFirstIndex(), e.getLastIndex());
		    }
		}
	    });
	    if (!GraphicsEnvironment.isHeadless()) {
		rowDendrogramColorEditor = new DendrogramColorEditor(parentComponent, rowDendrogram);
	    }
	}
	if (sampleTree != null) {
	    addPropertyChangeListener("columnSize", columnDendrogram);
	    sampleTree.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    if (e.getFirstIndex() != -1) {
			columnTable.setColumnSelectionInterval(e.getFirstIndex(), e.getLastIndex());
		    }
		}
	    });
	    if (!GraphicsEnvironment.isHeadless()) {
		columnDendrogramColorEditor = new DendrogramColorEditor(parentComponent, sampleTree);
	    }
	}

	rowTableModel = new RowTableModel();
	rowTable = new ExtendedTable(rowTableModel) {
	    @Override
	    public void processMouseEvent(MouseEvent e) {
		if (e.isPopupTrigger() && popupMenuVisible) {
		    quickLabelSamplesMenuItem.setEnabled(false);
		    popupMenu.show(e.getComponent(), e.getX(), e.getY());
		} else {
		    super.processMouseEvent(e);
		}
	    }
	};
	rowTable.setFixedColumnSize(this.resourceBundle.getString("Profile"), profileInTableColumnSize);
	rowTable.setFeatureColumn(this.resourceBundle.getString("Feature"));
	ProfileTableCellRenderer profileTableCellRenderer = new ProfileTableCellRenderer();
	rowTable.setDefaultRenderer(Dataset.class, profileTableCellRenderer);
	rowTable.setDefaultRenderer(String.class, new RowHeightTableCellRenderer());
	rowTable.setColumnSelectionAllowed(false);
	rowTable.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent event) {
		if (isProfileInTableVisible()
			&& rowTable.convertColumnIndexToModel(rowTable.columnAtPoint(event.getPoint())) == 2) {
		    profileAction.actionPerformed(new ActionEvent(event.getComponent(), ActionEvent.ACTION_PERFORMED,
			    "profile", event.getWhen(), event.getModifiers()));
		}
	    }
	});
	columnTableModel = new AbstractTableModel() {
	    @Override
	    public Class<?> getColumnClass(int column) {
		return String.class;
	    }

	    public int getColumnCount() {
		return HeatMap.this.dataset.getColumnCount();
	    }

	    public int getRowCount() {
		return columnNamesVisible ? 1 : 0;
	    }

	    public Object getValueAt(int rowIndex, int columnIndex) {
		return HeatMap.this.dataset.getColumnName(columnIndex);
	    }
	};
	columnAnnotatorPanel = new AnnotatorPanel(false, columnAnnotatorModel, dataset, parentComponent, heatMapPanel
		.getPixelConverter(), new DefaultAnnotatorColorModel());
	columnAnnotatorModel.addAnnotatorModelListener(new AnnotatorModelListener() {
	    public void annotatorModelChanged(AnnotatorModelEvent e) {
		revalidate();
		repaint();
		columnAnnotatorPanel.revalidate();
		columnAnnotatorPanel.repaint();
	    }
	});
	rowAnnotatorPanel = new AnnotatorPanel(true, rowAnnotatorModel, dataset, parentComponent, heatMapPanel
		.getPixelConverter(), new DefaultAnnotatorColorModel());
	rowAnnotatorModel.addAnnotatorModelListener(new AnnotatorModelListener() {
	    public void annotatorModelChanged(AnnotatorModelEvent e) {
		revalidate();
		repaint();
		rowAnnotatorPanel.revalidate();
		rowAnnotatorPanel.repaint();
		((FormLayout) topPanel.getLayout()).setColumnSpec(2, new ColumnSpec(rowAnnotatorPanel
			.getPreferredSize().width
			+ "px"));
	    }
	});
	if (!GraphicsEnvironment.isHeadless()) {
	    columnAnnotationsLegend = new AnnotationsLegend(parentComponent, columnAnnotatorPanel
		    .getAnnotatorColorModel(), true, getResourceBundleManager());
	    columnAnnotationsLegend.setAnnotatorModel(columnAnnotatorModel);
	    rowAnnotationsLegend = new AnnotationsLegend(parentComponent, rowAnnotatorPanel.getAnnotatorColorModel(),
		    false, getResourceBundleManager());
	    rowAnnotationsLegend.setAnnotatorModel(rowAnnotatorModel);
	}
	columnTable = new ExtendedTable(null) {
	    @Override
	    public void addColumn(TableColumn column) {
		super.addColumn(column);
		layoutSampleTableColumn(column, heatMapPanel.getColumnSize(0));
	    }

	    @Override
	    public void processMouseEvent(MouseEvent e) {
		if (e.isPopupTrigger() && popupMenuVisible) {
		    quickLabelSamplesMenuItem.setEnabled(true);
		    popupMenu.show(e.getComponent(), e.getX(), e.getY());
		} else {
		    super.processMouseEvent(e);
		}
	    }
	};
	columnTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
	((JLabel) columnTable.getDefaultRenderer(String.class)).setUI(new VerticalLabelUI(false));
	columnTable.setRowSelectionAllowed(false);
	columnTable.setModel(columnTableModel);
	columnTable.setGridColor(new Color(239, 239, 255));
	columnTable.setColumnSelectionAllowed(true);

	rowTable.addGeneCruiserModelListener(new GeneCruiserModelListener() {
	    public void update(GeneCruiserModelEvent e) {
		switch (e.getCode()) {
		case GeneCruiserModelEvent.BEFORE_UPDATE:
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    statusLabel.setText("Retrieving information from GeneCruiser\u2026");
			}
		    });
		    break;
		case GeneCruiserModelEvent.AFTER_UPDATE:
		    statusLabel.setText("");
		    break;
		}
	    }
	});
	FormLayout fl = new FormLayout("pref, pref, pref:g(1)", "pref");
	CellConstraints cc = new CellConstraints();
	bottomPanel = new JPanel(fl);
	bottomPanel.setBackground(Color.WHITE);
	if (featureTree != null) {
	    bottomPanel.add(featureTree, cc.xy(1, 1, CellConstraints.LEFT, CellConstraints.TOP));
	}
	heatMapAndRowAnnotatorPanel = new JPanel(new FormLayout("pref, pref", "pref"));
	heatMapAndRowAnnotatorPanel.add(heatMapPanel, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.TOP));
	heatMapAndRowAnnotatorPanel.add(rowAnnotatorPanel, cc.xy(2, 1, CellConstraints.LEFT, CellConstraints.TOP));
	bottomPanel.add(heatMapAndRowAnnotatorPanel, cc.xy(2, 1, CellConstraints.LEFT, CellConstraints.TOP));
	JPanel temp = new JPanel(new BorderLayout());
	temp.setBorder(BorderFactory.createEmptyBorder(2, 1, 0, 0));
	temp.add(rowTable);
	bottomPanel.add(temp, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.TOP));
	topPanel = new JPanel(new FormLayout("pref, 0px, pref:g", "pref, pref, pref, pref"));
	topPanel.setBackground(Color.WHITE);
	// topPanel layout layed out as:
	// accessoryPanel or sampleTree
	// column table, spacer, row table header
	// columnAnnotatorPanel
	// belowColumnKeysPanel
	topPanel.setBorder(BorderFactory.createEmptyBorder(0,
		featureTree != null ? (featureTree.getPreferredSize().width + 2) : 2, 0, 0));
	topPanel.add(columnTable, cc.xy(1, 2));
	topPanel.add(rowTable.getTableHeader(), cc.xy(3, 3, CellConstraints.FILL, CellConstraints.BOTTOM));
	if (sampleTree != null) {
	    topPanel.add(sampleTree, cc.xywh(1, 1, 2, 1, CellConstraints.LEFT, CellConstraints.TOP));
	}
	setAccessoryComponent(accessoryComponent, true);
	belowColumnKeysPanel = new JPanel(new FormLayout("pref", "pref, pref"));
	belowColumnKeysPanel.add(columnAnnotatorPanel, cc.xy(1, 1));
	topPanel.add(belowColumnKeysPanel, cc.xywh(1, 3, 2, 1, CellConstraints.LEFT, CellConstraints.TOP));
	scrollPane = new JScrollPane(bottomPanel);
	JViewport v = new JViewport();
	v.setView(topPanel);
	scrollPane.setColumnHeader(v);
	ColumnControlButton columnControlButton = new ColumnControlButton(rowTable, new ColumnControlIcon());
	JPanel p = new JPanel(new FormLayout("right:pref:none", "pref:g, bottom:pref:none"));
	p.add(new JPanel(), cc.xy(1, 1));
	p.add(columnControlButton, cc.xy(1, 2));
	scrollPane.setCorner(JScrollPane.UPPER_TRAILING_CORNER, p);
	setColumnSize(heatMapPanel.getColumnSize(0));
	layoutSampleTable(heatMapPanel.getColumnSize(0));
	if (!heatMapPanel.isVariableRowSize()) {
	    rowTable.setRowHeight(heatMapPanel.getRowSize(0));
	} else {
	    for (int i = 0, rows = heatMapPanel.getRowCount(); i < rows; i++) {
		int size = heatMapPanel.getRowSize(i);
		if (size < 1) {
		    throw new IllegalArgumentException("Row height less than 1.");
		}
		rowTable.setRowHeight(i, size);
	    }
	}
	setLayout(new BorderLayout());
	add(scrollPane, BorderLayout.CENTER);
	add(statusLabel, BorderLayout.SOUTH);

	if (!GraphicsEnvironment.isHeadless()) {
	    datasetAction = new DatasetAction(parentComponent, getResourceBundleManager()) {
		@Override
		protected int[] getColumnIndices() {
		    return getColumnTable().getSelectedColumns();
		}

		@Override
		protected int[] getRowIndices() {
		    return getRowTable().getSelectedRows();
		}

	    };
	    datasetAction.setDataset(getDataset());
	    addPropertyChangeListener("dataset", new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent evt) {
		    datasetAction.setDataset((Dataset) evt.getNewValue());
		}

	    });

	    profileAction = new ProfileAction(datasetAction);
	    datasetAction.add(profileAction);
	    centroidPlotAction = new CentroidPlotAction(datasetAction);
	    datasetAction.add(centroidPlotAction);
	    histogramAction = new HistogramAction(datasetAction);
	    datasetAction.add(histogramAction);
	    geneNeighborsAction = new GeneNeighborsAction(datasetAction);
	    datasetAction.add(geneNeighborsAction);
	    scatterPlotAction = new ScatterPlotAction(datasetAction);
	    datasetAction.add(scatterPlotAction);

	    try {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    sm.checkPropertiesAccess();
		}
		saveDatasetAction = new SaveDatasetAction(datasetAction);
		datasetAction.add(saveDatasetAction);
	    } catch (SecurityException se) {
	    }

	    setPopupMenu(datasetAction.createPopupMenu());
	}
    }

    /**
     * Adds a PropertyChangeListener to the listener list. The listener is registered for all bound properties of this
     * class, including the following:
     * <ul>
     * <li>this Component's row size ("rowSize")</li>
     * <li>this Component's column size ("columnSize")</li>
     * <li>this Component's dataset ("dataset")</li>
     * <li>this Component's row dendrogram height ("rowDendrogramHeight")</li>
     * <li>this Component's column dendrogram height ("columnDendrogramHeight")</li>
     * <li>this Component's show column names ("showColumnNames")</li>
     * <li>this Component's show row descriptions ("showRowDescriptions")</li>
     * <li>this Component's show row names ("showRowNames")</li>
     * <p/>
     * </ul>
     * Note that if this <code>Component</code> is inheriting a bound property, then no event will be fired in
     * response to a change in the inherited property. <p/> If <code>listener</code> is <code>null</code>, no
     * exception is thrown and no action is performed.
     *
     * @param listener
     *                the property change listener to be added
     * @see javax.swing.JComponent#removePropertyChangeListener(PropertyChangeListener)
     * @see javax.swing.JComponent#getPropertyChangeListeners()
     * @see javax.swing.JComponent#addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
	super.addPropertyChangeListener(listener);
    }

    public void showSortFeaturesDialog() {
	new SortFeaturesDialog(this);
    }

    public void showSortColumnsDialog() {
	new SortColumnsDialog(this);
    }

    /** Flips the column dendrogram at the selected node or at the root if no node is selected. */
    public void flipColumnDendrogram() {
	Node node = columnDendrogram.getSelectedNode();
	if (node == null) {
	    node = columnDendrogram.getRootNode();
	}
	int[] order = new int[dataset.getColumnCount()];
	int min = node.getMinIndex();
	int max = node.getMaxIndex();
	for (int i = 0; i < min; i++) {
	    order[i] = i;
	}
	int index = max;
	for (int i = min; i <= max; i++) {
	    order[i] = index--;
	}
	for (int i = max + 1; i < dataset.getColumnCount(); i++) {
	    order[i] = i;
	}
	columnDendrogram.flip();
	this.setDataset(DatasetUtil.sliceView(dataset, null, order));
	columnDendrogram.repaint();
    }

    /** Flips the row dendrogram at the selected node or at the root if no node is selected. */
    public void flipRowDendrogram() {
	Node node = rowDendrogram.getSelectedNode();
	if (node == null) {
	    node = rowDendrogram.getRootNode();
	}
	int[] order = new int[dataset.getRowCount()];
	int min = node.getMinIndex();
	int max = node.getMaxIndex();
	for (int i = 0; i < min; i++) {
	    order[i] = i;
	}
	int index = max;
	for (int i = min; i <= max; i++) {
	    order[i] = index--;
	}
	for (int i = max + 1; i < dataset.getRowCount(); i++) {
	    order[i] = i;
	}
	rowDendrogram.flip();
	rowTableModel.fireTableStructureChanged();
	this.setDataset(DatasetUtil.sliceView(dataset, order, null));
	rowDendrogram.repaint();
    }

    public Component getBelowColumnKeysComponent() {
	return belowColumnKeysComponent;
    }

    /**
     * Gets the color scheme for the component.
     *
     * @return The color scheme.
     */
    public ColorScheme getColorScheme() {
	return heatMapPanel.getColorScheme();
    }

    /**
     * Gets the color scheme legend for the component.
     *
     * @return The color scheme legend.
     */
    public Component getColorSchemeLegend() {
	return heatMapPanel.getColorScheme().getLegend();
    }

    /**
     * Gets the column annotations legend for the component.
     *
     * @return The column annotations legend.
     */
    public AnnotationsLegend getColumnAnnotationsLegend() {
	return columnAnnotationsLegend;
    }

    /**
     * Gets the model for annotating columns.
     *
     * @return The column annotator model.
     */
    public AnnotatorModel getColumnAnnotatorModel() {
	return columnAnnotatorModel;
    }

    /**
     * Gets the panel used to draw column annotations.
     *
     * @return The column annotator panel.
     */
    public AnnotatorPanel getColumnAnnotatorPanel() {
	return columnAnnotatorPanel;
    }

    /**
     * Gets the column dendrogram component.
     *
     * @return The column dendrogram component.
     */
    public SampleTreePanel getColumnDendrogram() {
	return columnDendrogram;
    }

    /**
     * Gets the object that it used to edit the column dendrogram.
     *
     * @return The column dendrogram editor.
     */
    public DendrogramColorEditor getColumnDendrogramColorEditor() {
	return columnDendrogramColorEditor;
    }

    /**
     * Returns the height of the column dendrogram.
     *
     * @return The column dendrogram height.
     */
    public int getColumnDendrogramHeight() {
	return columnDendrogram != null ? columnDendrogram.getPreferredSize().height : 0;
    }

    /**
     * Gets the column size of an element in the heat map.
     *
     * @return The column size.
     */
    public int getColumnSize() {
	return heatMapPanel.getColumnSize(0);
    }

    /**
     * Gets the column table
     *
     * @return the column table
     */
    public ExtendedTable getColumnTable() {
	return columnTable;
    }

    /**
     * Gets the dataset.
     *
     * @return The dataset.
     */
    public Dataset getDataset() {
	return this.dataset;
    }

    /**
     * Gets the histogram plot.
     *
     * @return The histogram plot.
     */
    public DatasetHistogramPlot getDatasetHistogramPlot() {
	return datasetHistogramPlot;
    }

    /**
     * Gets the component used to render elements in the heat map.
     *
     * @return The heat map panel.
     */
    public HeatMapElementPanel getHeatMapPanel() {
	return heatMapPanel;
    }

    /**
     * Gets the popup menu.
     *
     * @return The popup menu.
     */
    public JPopupMenu getPopupMenu() {
	return popupMenu;
    }

    /**
     * Gets the row annotations legend.
     *
     * @return The row annotations legend.
     */
    public AnnotationsLegend getRowAnnotationsLegend() {
	return rowAnnotationsLegend;
    }

    /**
     * Gets the row annotator model.
     *
     * @return The row annotator model.
     */
    public AnnotatorModel getRowAnnotatorModel() {
	return rowAnnotatorModel;
    }

    /**
     * Gets the row annotator color model.
     *
     * @return The row annotator color model.
     */
    public AnnotatorColorModel getRowAnnotatorColorModel() {
	return rowAnnotatorPanel.getAnnotatorColorModel();
    }

    /**
     * Gets the column annotator color model.
     *
     * @return The column annotator color model.
     */
    public AnnotatorColorModel getColumnAnnotatorColorModel() {
	return columnAnnotatorPanel.getAnnotatorColorModel();
    }

    /**
     * Gets the row annotator panel.
     *
     * @return The row annotator panel.
     */
    public AnnotatorPanel getRowAnnotatorPanel() {
	return rowAnnotatorPanel;
    }

    /**
     * Gets the row dendrogram.
     *
     * @return The row dendrogram.
     */
    public FeatureTreePanel getRowDendrogram() {
	return rowDendrogram;
    }

    public DendrogramColorEditor getRowDendrogramColorEditor() {
	return rowDendrogramColorEditor;
    }

    public int getRowDendrogramHeight() {
	return rowDendrogram != null ? rowDendrogram.getPreferredSize().width : 0;
    }

    public int getRowSize(int index) {
	return heatMapPanel.getRowSize(index);
    }

    public ExtendedTable getRowTable() {
	return rowTable;
    }

    public boolean isColumnNamesVisible() {
	return columnNamesVisible;
    }

    public boolean isPopupMenuVisible() {
	return popupMenuVisible;
    }

    public boolean isSquareAspect() {
	return this.squareAspect;
    }

    @Override
    public void revalidate() {
	super.revalidate();
	rowTable.invalidate();
	rowTable.validate();
	heatMapPanel.invalidate();
	heatMapPanel.validate();
	heatMapPanel.repaint();
    }

    /**
     * Adds a component either aligned above sample keys or above row keys
     *
     * @param c
     *                the component
     * @param alignWithSampleKeys
     *                true to align with sample keys, false to align with row keys
     */
    public void setAccessoryComponent(Component c, boolean alignWithSampleKeys) {
	if (accessoryComponent != null) {
	    topPanel.remove(accessoryComponent);
	}
	this.accessoryComponent = c;
	if (accessoryComponent != null) {
	    CellConstraints cc = new CellConstraints();
	    int gridWidth = alignWithSampleKeys ? 2 : 1;
	    Alignment alignment = alignWithSampleKeys ? CellConstraints.TOP : CellConstraints.BOTTOM;
	    int column = alignWithSampleKeys ? 1 : 2;
	    topPanel.add(accessoryComponent, cc.xywh(column, 1, gridWidth, 1, CellConstraints.LEFT, alignment));
	    // FIXME
	    // can't
	    // have
	    // sampleTree
	    // and
	    // accessoryPanel
	}
    }

    public void setBelowColumnKeysComponent(Component belowColumnKeysComponent) {
	if (this.belowColumnKeysComponent != null) {
	    belowColumnKeysPanel.remove(this.belowColumnKeysComponent);
	}
	CellConstraints cc = new CellConstraints();
	belowColumnKeysPanel.add(belowColumnKeysComponent, cc.xy(1, 2));
	topPanel.revalidate();
	this.belowColumnKeysComponent = belowColumnKeysComponent;
    }

    public void setColorScheme(ColorScheme colorScheme) {
	heatMapPanel.setColorScheme(colorScheme);
    }

    public void setColumnDendrogramHeight(int height) {
	if (height < 1) {
	    throw new IllegalArgumentException();
	}
	int oldHeight = columnDendrogram.getPreferredSize().height;
	columnDendrogram.setPreferredSize(new Dimension(columnDendrogram.getPreferredSize().width, height));
	topPanel.revalidate();
	scrollPane.revalidate();
	firePropertyChange("columnDendrogramHeight", oldHeight, height);
    }

    public void setColumnNamesVisible(boolean showColumnNames) {
	if (this.columnNamesVisible == showColumnNames) {
	    return;
	}
	boolean old = this.columnNamesVisible;
	this.columnNamesVisible = showColumnNames;
	columnTableModel.fireTableStructureChanged();
    setColumnSize(heatMapPanel.getColumnSize(0));    
	firePropertyChange("showColumnNames", old, showColumnNames);
    }

    public void setColumnSize(int columnSize) {
	int oldColumnSize = heatMapPanel.getColumnSize(0);
	if (columnSize < 1) {
	    throw new IllegalArgumentException("Column size (" + columnSize + ") less than one.");
	}
	heatMapPanel.setColumnSize(columnSize);
	layoutSampleTable(columnSize);
	columnTable.setFont(new Font(fontName != null ? fontName : columnTable.getFont().getName(), Font.PLAIN,
		columnSize));
	JLabel test = new JLabel();
	test.setFont(new Font(fontName != null ? fontName : columnTable.getFont().getName(), Font.PLAIN, columnSize));
	int size = 0;
	for (int j = 0; j < dataset.getColumnCount(); j++) {
	    test.setText(dataset.getColumnName(j));
	    size = Math.max(size, test.getPreferredSize().width);
	}
	columnAnnotatorPanel.repaint();
	columnTable.setRowHeight(size + 10);
	heatMapAndRowAnnotatorPanel.invalidate();
	heatMapAndRowAnnotatorPanel.validate();
	heatMapPanel.invalidate();
	heatMapPanel.validate();
	firePropertyChange("columnSize", oldColumnSize, columnSize);
	belowColumnKeysPanel.revalidate();
    }

    /**
     * Sets the data to display.
     *
     * @param d
     *                the dataset
     */
    public void setDataset(Dataset d) {
	Dataset old = this.dataset;
	this.dataset = d;
	heatMapPanel.setDataset(d);
	fireFeatureTableChanged();
	columnTableModel.fireTableStructureChanged();
	setColumnSize(heatMapPanel.getColumnSize(0));
	rowAnnotatorPanel.setDataset(dataset);
	columnAnnotatorPanel.setDataset(dataset);
	firePropertyChange("dataset", old, d);
    }

    /**
     * Sets the options to hide in the display options dialog. Supported option keys are rowNames, columnNames,
     * rowDescriptions, columnAnnotationHeight,rowAnnotationWidth, colorScheme.
     *
     * @param keys
     */
    public void setOptionsDialogHidden(String[] keys) {
	this.optionsDialogHiddenKeys = keys;
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
	this.popupMenu = popupMenu;
	if (popupMenu == null) {
	    setPopupMenuVisible(false);
	}
    }

    public void setRowDendrogramHeight(int height) {
	if (height < 1) {
	    throw new IllegalArgumentException();
	}
	int oldValue = rowDendrogram.getPreferredSize().width;
	rowDendrogram.setPreferredSize(new Dimension(height, rowDendrogram.getPreferredSize().height));
	topPanel.setBorder(BorderFactory.createEmptyBorder(0,
		rowDendrogram != null ? (rowDendrogram.getPreferredSize().width + 2) : 2, 0, 0));
	firePropertyChange("rowDendrogramHeight", oldValue, height);
    }

    public void setPopupMenuVisible(boolean showPopupMenu) {
	this.popupMenuVisible = showPopupMenu;
    }

    public boolean isProfileInTableVisible() {
	return rowTable.getColumnExt(resourceBundle.getString("Profile")).isVisible();
    }

    public void setProfileInTableVisible(boolean profileInTableVisible) {
	rowTable.getColumnExt(resourceBundle.getString("Profile")).setVisible(profileInTableVisible);
    }

    public boolean isRowDescriptionsVisible() {
	return rowTable.getColumnExt(resourceBundle.getString("Feature.Description")).isVisible();
    }

    public void setRowDescriptionsVisible(boolean visible) {
	TableColumnExt col = rowTable.getColumnExt(resourceBundle.getString("Feature.Description"));
	boolean old = col.isVisible();
	col.setVisible(visible);
	firePropertyChange("showRowDescriptions", old, visible);
    }

    public boolean isRowNamesVisible() {
	return rowTable.getColumnExt(resourceBundle.getString("Feature")).isVisible();
    }

    public void setRowNamesVisible(boolean visible) {
	TableColumnExt col = rowTable.getColumnExt(resourceBundle.getString("Feature"));
	boolean old = col.isVisible();
	col.setVisible(visible);
	firePropertyChange("showRowDescriptions", old, visible);
    }

    /**
     * Sets the row size when {@link HeatMapElementPanel#isVariableRowSize()} is <tt>true</tt>.
     *
     * @param rowSize
     *                The size in pixels that the value <tt>middle</tt> will be displayed.
     */
    public void setVariableRowSize(int rowSize) {
	if (rowSize < 1) {
	    throw new IllegalArgumentException();
	}
	if (rowDendrogram != null) {
	    rowDendrogram.setElementHeight(rowSize);
	}
	heatMapPanel.setVariableRowSize(rowSize);
	for (int i = 0, rows = dataset.getRowCount(); i < rows; i++) {
	    rowTable.setRowHeight(i, heatMapPanel.getRowSize(i));
	}
	rowAnnotatorPanel.repaint();
	rowTable.invalidate();
	rowTable.validate();
	heatMapPanel.invalidate();
	heatMapPanel.validate();
	firePropertyChange("rowSizes", rowSize, rowSize);
    }

    public void setRowSize(int rowSize) {
	if (rowSize < 1) {
	    throw new IllegalArgumentException();
	}
	int old = heatMapPanel.getRowSize(0);
	rowTable.setRowHeight(rowSize);
	heatMapPanel.setRowSize(rowSize);
	rowAnnotatorPanel.repaint();
	rowTable.invalidate();
	rowTable.validate();
	heatMapPanel.invalidate();
	heatMapPanel.validate();
	firePropertyChange("rowSize", old, rowSize);
    }

    /**
     * Sets whether to display the column header for rows
     *
     * @param show
     */
    public void setShowFeatureTableHeader(boolean show) {
	rowTable.getTableHeader().setVisible(show);
    }

    public void setSquareAspect(boolean b) {
	if (squareAspect != b) {
	    this.squareAspect = b;
	    if (squareAspect) {
		int size = Math.max(heatMapPanel.getRowSize(0), heatMapPanel.getColumnSize(0));
		setRowSize(size);
		setColumnSize(size);
	    }
	}
    }

    public void showAboutDialog() {
	if (aboutDialog == null) {
	    aboutDialog = CenteredDialog.createInstance(parentComponent);
	    aboutDialog.setTitle("About");
	    JLabel text = new JLabel(
		    "<html><p align=\"center\">Created by Joshua Gould.<br><br>Please send questions and comments to gp-help@broad.mit.edu.",
		    SwingConstants.CENTER);
	    aboutDialog.getContentPane().add(text);
	    aboutDialog.setResizable(false);
	    aboutDialog.setSize(new Dimension(250, 150));
	}
	aboutDialog.setVisible(true);
    }

    public void showDocumentation(String docFilename) {
	// SimpleViewer simpleViewer = new SimpleViewer();
	// simpleViewer.setupViewer(docFilename);
	try {
	    BrowserLauncher.openURL(docFilename);
	} catch (Exception e) {
	    UIUtil.showMessageDialog(parentComponent, "Unable to open documentation file");
	}
    }

    public void showKeyboardShortcuts() {
	if (keyBoardShortcutDialog == null) {
	    keyBoardShortcutDialog = new KeyboardShortcutDialog(parentComponent, keyBoardShortcuts.keySet().toArray(
		    new KeyStroke[0]), keyBoardShortcuts.values().toArray(new Action[0]));
	}
	keyBoardShortcutDialog.setVisible(true);
    }

    public void showLegend() {
	JDialog d = CenteredDialog.createInstance(parentComponent);
	d.setTitle(getResourceBundleManager().getString("Color.Scheme.Legend.Menu.Item"));
	Component p = heatMapPanel.getColorScheme().getLegend();
	d.getContentPane().add(p);
	d.pack();
	d.setVisible(true);
    }

    public void showOptionsDialog() {
	if (optionsDialog != null && optionsDialog.isShowing()) {
	    optionsDialog.toFront();
	} else {
	    optionsDialog = new HeatMapOptions(parentComponent, HeatMap.this, heatMapPanel,
		    this.optionsDialogHiddenKeys);
	}
    }

    public void showSaveImageDialog(final String format) {
	final File file = FileChooser.showSaveDialog(parentComponent, null, ImageUtil.getExtensions(format),
		"Save Image");
	if (file != null) {
	    new Thread() {
		@Override
		public void run() {
		    try {
			ImageUtil.saveImage(HeatMap.this, format, file, false);
		    } catch (OutOfMemoryError ome) {
			UIUtil.showErrorDialog(parentComponent, "Not enough memory available to save the image.");
		    } catch (Throwable t) {
			t.printStackTrace();
			UIUtil.showErrorDialog(parentComponent, "An error occurred while saving the image.");
		    }
		}
	    }.start();
	}
    }

    public Dimension getDrawPreferredSize() {
	int width = 0;
	int height = 0;
	if (columnDendrogram != null) {
	    height += columnDendrogram.getPreferredSize().height;
	}
	if (getColumnAnnotatorModel().getSize() > 0) {
	    height += columnAnnotatorPanel.getDrawPreferredSize().height;
	    height += 10;
	}
	if (rowDendrogram != null) {
	    width += rowDendrogram.getPreferredSize().width;
	}
	if (columnTable.isVisible() && columnTable.getRowCount() == 1) {
	    VerticalTableDrawer t = new VerticalTableDrawer(columnTable);
	    height += t.getDrawPreferredSize().height;
	}
	width += heatMapPanel.getDrawPreferredSize().width;
	height += heatMapPanel.getDrawPreferredSize().height;
	if (getRowAnnotatorModel().getSize() > 0) {
	    width += 10;
	    width += rowAnnotatorPanel.getDrawPreferredSize().width;
	}
	if (rowTable.isVisible()) {
	    TableDrawer t = new TableDrawer(rowTable);
	    width += t.getDrawPreferredSize().width;
	}
	return new Dimension(width, height);
    }

    public void draw(Graphics g, Rectangle rect) {
	if (rowDendrogram != null) {
	    g.translate(rowDendrogram.getPreferredSize().width, 0);
	}

	if (columnDendrogram != null) {
	    int x = 0;
	    int y = 0;
	    g.translate(x, y);
	    columnDendrogram.draw(g, rect);
	    g.translate(-x, columnDendrogram.getPreferredSize().height);
	}

	if (getColumnAnnotatorModel().getSize() > 0) {
	    g.translate(0, 1);
	    columnAnnotatorPanel.draw(g, rect);
	    g.translate(0, columnAnnotatorPanel.getDrawPreferredSize().height);
	}

	if (columnTable.isVisible() && columnTable.getRowCount() == 1) {
	    VerticalTableDrawer t = new VerticalTableDrawer(columnTable);
	    t.draw(g, rect);
	    g.translate(0, t.getDrawPreferredSize().height);
	}
	if (rowDendrogram != null) {
	    g.translate(-rowDendrogram.getPreferredSize().width, 0);
	    rowDendrogram.draw(g, rect);
	    g.translate(rowDendrogram.getPreferredSize().width, 0);
	}
	heatMapPanel.draw(g, rect);
	g.translate(heatMapPanel.getDrawPreferredSize().width, 0);
	if (getRowAnnotatorModel().getSize() > 0) {
	    g.translate(1, 0);
	    rowAnnotatorPanel.draw(g, rect);
	    g.translate(rowAnnotatorPanel.getDrawPreferredSize().width, 0);
	}
	if (rowTable.isVisible()) {
	    new TableDrawer(rowTable).draw(g, rect);
	}
    }

    protected void fireFeatureTableChanged() {
	rowTableModel.fireTableStructureChanged();
    }

    GPResourceBundleManager getResourceBundleManager() {
	return resourceBundle;
    }

    private void addAction(KeyStroke accelerator, Action action, InputMap inputMap, ActionMap actionMap) {
	String name = (String) action.getValue(Action.NAME);
	inputMap.put(accelerator, name);
	actionMap.put(name, action);
	keyBoardShortcuts.put(accelerator, action);
    }

    void installKeyBindings() {
	// final InputMap inputMap = new ComponentInputMapUIResource(this);
	// final ActionMap actionMap = new ActionMap();
	if (GraphicsEnvironment.isHeadless()) {
	    return;
	}
	final ActionMap actionMap = getActionMap();
	final InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	int shift = Event.SHIFT_MASK;
	int alt = Event.ALT_MASK;
	int sc = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	AbstractAction zoomInAction = new AbstractAction("Increase Element Size") {
	    public void actionPerformed(ActionEvent e) {
		setColumnSize(getColumnSize() + 1);
		if (!heatMapPanel.isVariableRowSize()) {
		    setRowSize(getRowSize(0) + 1);
		} else {
		    setVariableRowSize(heatMapPanel.getVariableRowSize() + 1);
		}
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, sc), zoomInAction, inputMap, actionMap);
	Action zoomOutAction = new AbstractAction("Decrease Element Size") {
	    public void actionPerformed(ActionEvent e) {
		setColumnSize(getColumnSize() - 1);
		if (!heatMapPanel.isVariableRowSize()) {
		    setRowSize(getRowSize(0) - 1);
		} else {
		    setVariableRowSize(heatMapPanel.getVariableRowSize() - 1);
		}
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke(KeyEvent.VK_UP, sc), zoomOutAction, inputMap, actionMap);
	AbstractAction zoomInColumn = new AbstractAction("Increase Column Size") {
	    public void actionPerformed(ActionEvent e) {
		setSquareAspect(false);
		setColumnSize(getColumnSize() + 1);
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, sc | shift), zoomInColumn, inputMap, actionMap);
	AbstractAction zoomOutColumn = new AbstractAction("Decrease Column Size") {
	    public void actionPerformed(ActionEvent e) {
		setSquareAspect(false);
		setColumnSize(getColumnSize() - 1);
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, sc | shift), zoomOutColumn, inputMap, actionMap);
	AbstractAction zoomInRow = new AbstractAction("Increase Row Size") {
	    public void actionPerformed(ActionEvent e) {
		setSquareAspect(false);
		if (!heatMapPanel.isVariableRowSize()) {
		    setRowSize(getRowSize(0) + 1);
		} else {
		    setVariableRowSize(heatMapPanel.getVariableRowSize() + 1);
		}
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, sc | shift), zoomInRow, inputMap, actionMap);
	AbstractAction zoomOutRow = new AbstractAction("Decrease Row Size") {
	    public void actionPerformed(ActionEvent e) {
		setSquareAspect(false);
		if (!heatMapPanel.isVariableRowSize()) {
		    setRowSize(getRowSize(0) - 1);
		} else {
		    setVariableRowSize(heatMapPanel.getVariableRowSize() - 1);
		}
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke(KeyEvent.VK_UP, sc | shift), zoomOutRow, inputMap, actionMap);
	if (columnDendrogram != null) {
	    Action sampleDendrogamIn = new AbstractAction("Increase Sample Dendrogram Height") {
		public void actionPerformed(ActionEvent e) {
		    setColumnDendrogramHeight(getColumnDendrogramHeight() + 4);
		    revalidate();
		}
	    };
	    addAction(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, alt), sampleDendrogamIn, inputMap, actionMap);
	    Action sampleDendrogamOut = new AbstractAction("Decrease Sample Dendrogram Height") {
		public void actionPerformed(ActionEvent e) {
		    setColumnDendrogramHeight(getColumnDendrogramHeight() - 4);
		    revalidate();
		}
	    };
	    addAction(KeyStroke.getKeyStroke(KeyEvent.VK_UP, alt), sampleDendrogamOut, inputMap, actionMap);
	}
	if (rowDendrogram != null) {
	    Action geneDendrogamIn = new AbstractAction("Increase Feature Dendrogram Width") {
		public void actionPerformed(ActionEvent e) {
		    setRowDendrogramHeight(getRowDendrogramHeight() + 4);
		    revalidate();
		}
	    };
	    addAction(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, alt), geneDendrogamIn, inputMap, actionMap);
	    Action geneDendrogramOut = new AbstractAction("Decrease Feature Dendrogram Width") {
		public void actionPerformed(ActionEvent e) {
		    setRowDendrogramHeight(getRowDendrogramHeight() - 4);
		    revalidate();
		}
	    };
	    addAction(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, alt), geneDendrogramOut, inputMap, actionMap);
	}
	Action rowNormalization = new AbstractAction("Row Normalized Color Scheme") {
	    public void actionPerformed(ActionEvent e) {
		heatMapPanel.getColorScheme().setRelative(true);
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke('R', sc), rowNormalization, inputMap, actionMap);
	Action globalNormalization = new AbstractAction("Global Color Scheme") {
	    public void actionPerformed(ActionEvent e) {
		heatMapPanel.getColorScheme().setRelative(false);
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke('G', sc), globalNormalization, inputMap, actionMap);
	if (!heatMapPanel.isVariableRowSize()) {
	    Action squareAspectAction = new AbstractAction("Toggle Square Aspect") {
		public void actionPerformed(ActionEvent e) {
		    setSquareAspect(!isSquareAspect());
		    revalidate();
		}
	    };
	    addAction(KeyStroke.getKeyStroke('T', sc), squareAspectAction, inputMap, actionMap);
	}
	Action showGrid = new AbstractAction("Toggle Show Grid") {
	    public void actionPerformed(ActionEvent e) {
		heatMapPanel.setDrawGrid(!heatMapPanel.isDrawGrid());
		revalidate();
	    }
	};
	addAction(KeyStroke.getKeyStroke('L', sc), showGrid, inputMap, actionMap);

	// KeyboardFocusManager focusManager =
	// KeyboardFocusManager.getCurrentKeyboardFocusManager();
	// focusManager.addKeyEventDispatcher(new KeyEventDispatcher() {
	//
	// public boolean dispatchKeyEvent(KeyEvent keyevent) {
	// KeyStroke keystroke = KeyStroke.getKeyStrokeForEvent(keyevent);
	// String name = (String) inputMap.get(keystroke);
	// if (name != null) {
	// Action action = actionMap.get(name);
	// SwingUtilities.notifyAction(action, keystroke, keyevent,
	// HeatMap.this, keyevent.getModifiers());
	// return true;
	// }
	// return false;
	// }
	//
	// });
    }

    private void layoutSampleTable(int columnSize) {
	for (int i = 0; i < columnTable.getColumnCount(); i++) {
	    TableColumn c = columnTable.getColumnModel().getColumn(i);
	    c.setMinWidth(columnSize);
	    c.setMaxWidth(columnSize);
	    c.setPreferredWidth(columnSize);
	    c.setWidth(columnSize);
	}
    }

    private void layoutSampleTableColumn(TableColumn c, int columnSize) {
	c.setMinWidth(columnSize);
	c.setMaxWidth(columnSize);
	c.setPreferredWidth(columnSize);
	c.setWidth(columnSize);
    }

    public static Map<String, List<Color>> getFeatureName2ColorsMap(AnnotatorModel model, Dataset data, boolean byRow,
	    AnnotatorColorModel colorGenerator) {
	Map<String, List<Color>> rowName2Colors = new HashMap<String, List<Color>>();
	for (int i = 0, end = byRow ? data.getRowCount() : data.getColumnCount(); i < end; i++) {
	    String key = byRow ? data.getRowName(i) : data.getColumnName(i);
	    List<Color> colors = new ArrayList<Color>();
	    rowName2Colors.put(key, colors);
	    for (int k = 0; k < model.getSize(); k++) {
		Annotation a = model.getAnnotation(k);
		colors.add(colorGenerator.getColor(a.getCategory(key)));
	    }
	}
	return rowName2Colors;
    }

    private class RowTableModel extends AbstractTableModel {

	@Override
	public Class<?> getColumnClass(int col) {
	    switch (col) {
	    case 0:
		return String.class;
	    case 1:
		return String.class;
	    case 2:
		return Dataset.class;
	    }
	    return String.class;
	}

	public int getColumnCount() {
	    int columns = 3;
	    return columns;
	}

	@Override
	public String getColumnName(int col) {
	    switch (col) {
	    case 0:
		return resourceBundle.getString("Feature");
	    case 1:
		return resourceBundle.getString("Feature.Description");
	    case 2:
		return resourceBundle.getString("Profile");
	    }
	    return null;
	}

	public int getRowCount() {
	    return dataset.getRowCount();
	}

	public Object getValueAt(int row, int col) {
	    switch (col) {
	    case 0:
		return dataset.getRowName(row);
	    case 1:
		return dataset.getRowMetadata(row, DatasetConstants.DESCRIPTION);
	    case 2:
		return dataset;
	    }
	    return null;
	}
    }

    public Dataset getOriginalDataset() {
	return originalDataset;
    }

    public void setDrawGrid(boolean drawGrid) {
	heatMapPanel.setDrawGrid(drawGrid);
    }

    public void setGridColor(Color c) {
	heatMapPanel.setGridColor(c);
    }

    public MenuItemAction getProfileAction() {
	return profileAction;
    }

    public MenuItemAction getCentroidPlotAction() {
	return centroidPlotAction;
    }

    public MenuItemAction getHistogramAction() {
	return histogramAction;
    }

    public MenuItemAction getScatterPlotAction() {
	return scatterPlotAction;
    }

    public MenuItemAction getSaveDatasetAction() {
	return saveDatasetAction;
    }

    public MenuItemAction getGeneNeighborsAction() {
	return geneNeighborsAction;
    }

}
