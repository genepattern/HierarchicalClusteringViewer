package edu.mit.genome.gp.ui.hclviewer;

import edu.mit.genome.gp.ui.hclviewer.colorconverter.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.JPEGEncodeParam;
import javax.media.jai.JAI;
import java.awt.image.BufferedImage;

/**
 *@author    Joshua Gould
 */
public class HCLFrame extends JFrame {
	HCL hcl;


	public HCLFrame(HCL hcl) {
		this.hcl = hcl;
		setJMenuBar(new MenuBar());
		Container c = getContentPane();
		c.setBackground(Color.white);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.add(hcl.getComponent(), BorderLayout.CENTER);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (screen.width * .80), (int) (screen.height * .80));
		show();
		hcl.updateSize();
	}


	void showSaveImageDialog() {

		if((Double.parseDouble(System.getProperty("java.specification.version"))) <
				1.4) {
			JOptionPane.showMessageDialog(this, "Java 1.4 is required to save an image.");
			return;
		}

		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new MyFileFilter(new String[]{"bmp"}, "BMP image", "BMP"));
		fc.addChoosableFileFilter(new MyFileFilter(new String[]{"jpeg", "jpg"}, "JPEG image", "JPEG"));
		fc.addChoosableFileFilter(new MyFileFilter(new String[]{"png"}, "PNG image", "PNG"));
		fc.addChoosableFileFilter(new MyFileFilter(new String[]{"tiff"}, "TIFF image", "TIFF"));

		if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File f = fc.getSelectedFile();
			if(f.exists()) {
				String message = "An item named " + f.getName() + " already exists in this location. Do you want to replace it with the one that you are saving?";
				if(JOptionPane.showOptionDialog(this, message, null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Replace", "Cancel"}, "Cancel") != JOptionPane.YES_OPTION) {
					return;
				}
			}
			final String outputFileFormat = ((MyFileFilter) fc.getFileFilter()).getFileFormat();

				new Thread() {
					public void run() {
						BufferedImage bufferedImage = null;
						try {
							bufferedImage = hcl.snapshot();
						} catch(OutOfMemoryError o) {
							JOptionPane.showMessageDialog(HCLFrame.this, "Not enough memory available to create image.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						ImageEncodeParam fParam = null;
						if(outputFileFormat.equalsIgnoreCase("jpeg")) {
							JPEGEncodeParam jpegParam = new JPEGEncodeParam();
							jpegParam.setQuality(1.0f);
							fParam = jpegParam;
						} else if(outputFileFormat.equalsIgnoreCase("png")) {
							fParam = new com.sun.media.jai.codec.PNGEncodeParam.RGB();
						} else if(outputFileFormat.equalsIgnoreCase("tiff")) {
							com.sun.media.jai.codec.TIFFEncodeParam param = new com.sun.media.jai.codec.TIFFEncodeParam();
							param.setCompression(com.sun.media.jai.codec.TIFFEncodeParam.COMPRESSION_NONE);
							fParam = param;
						} else if(outputFileFormat.equalsIgnoreCase("bmp")) {
							fParam = new com.sun.media.jai.codec.BMPEncodeParam();
						}
						try {
							JAI.create("filestore", bufferedImage, f.getCanonicalPath(), outputFileFormat, fParam);
						} catch(Throwable x) {
							JOptionPane.showMessageDialog(HCLFrame.this, "An error occurred while saving the image.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}.start();
		}
	}



	/**
	 *@author    Joshua Gould
	 */
	class MenuBar extends JMenuBar {
		JMenu displayMenu = new JMenu("Display");
		JMenu fileMenu = new JMenu("File");
		JMenu sizeMenu = new JMenu("Size");
		JMenu colorResponseMenu = new JMenu("Color Response");

		JMenuItem customAbsColorMenuItem;


		public MenuBar() {

			JCheckBoxMenuItem flyOverMenuItem = new JCheckBoxMenuItem("Show Fly Over Text", true);
			flyOverMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						hcl.setShowToolTipText(!hcl.isShowingToolTipText());
					}
				});
			displayMenu.add(flyOverMenuItem);

			JCheckBoxMenuItem showRowLabelsMenuItem = new JCheckBoxMenuItem("Show Row Labels", true);
			showRowLabelsMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						hcl.setShowRowLabels(!hcl.isShowingRowLabels());

					}
				});
			displayMenu.add(showRowLabelsMenuItem);

			JCheckBoxMenuItem showColumnLabelsMenuItem = new JCheckBoxMenuItem("Show Column Labels", true);
			showColumnLabelsMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						hcl.setShowColumnLabels(!hcl.isShowingColumnLabels());
					}
				});
			displayMenu.add(showColumnLabelsMenuItem);

			createColorResponseMenu();
			createColorSchemeMenu();

			createSizeMenu();
			displayMenu.add(sizeMenu);
			createFileMenu();
			add(fileMenu);
			add(displayMenu);
		}


		private void createSizeMenu() {

			JMenuItem zoomInMenuItem = new JMenuItem("Zoom in");
			zoomInMenuItem.addActionListener(hcl.zoomInAction);
			sizeMenu.add(zoomInMenuItem);

			JMenuItem zoomOutMenuItem = new JMenuItem("Zoom out");
			zoomOutMenuItem.addActionListener(hcl.zoomOutAction);
			sizeMenu.add(zoomOutMenuItem);

			JMenuItem inputSizeMenuItem = new JMenuItem("Custom Size...");
			inputSizeMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SetElementSizeDialog dialog = new SetElementSizeDialog(HCLFrame.this, hcl.getXPixPerUnitAsInt(), hcl.getYPixPerUnitAsInt());
						if(dialog.showInputDialog()) {
							hcl.setXPixPerUnit(dialog.getElementWidth());
							hcl.setYPixPerUnit(dialog.getElementHeight());
							hcl.updateSize();
						}
						hcl.repaint();
					}
				});
			sizeMenu.add(inputSizeMenuItem);

		}


		private void createFileMenu() {
			JMenuItem saveImageMenuItem = new JMenuItem("Save Image...");
			//	saveImageMenuItem.setEnabled(false);
			saveImageMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showSaveImageDialog();
					}
				});

			fileMenu.add(saveImageMenuItem);
		}


		private void createColorSchemeMenu() {
			JMenu colorSchemeMenu = new JMenu("Normalization");
			ButtonGroup colorSchemeGroup = new ButtonGroup();

			JRadioButtonMenuItem relativeMenuItem = new JRadioButtonMenuItem("Row", true);
			colorSchemeGroup.add(relativeMenuItem);
			relativeMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						colorResponseMenu.setEnabled(true);
						customAbsColorMenuItem.setEnabled(false);
						hcl.setColorConverter(new RowColorConverter(ColorResponse.LINEAR, hcl.getMatrix()));
						hcl.repaint();
					}
				});

			colorSchemeMenu.add(relativeMenuItem);

			JRadioButtonMenuItem absoluteMenuItem = new JRadioButtonMenuItem("None", false);
			colorSchemeGroup.add(absoluteMenuItem);
			absoluteMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						colorResponseMenu.setEnabled(false);
						customAbsColorMenuItem.setEnabled(true);
						hcl.setColorConverter(new AbsoluteColorConverter(Color.blue, Color.red, Color.black, hcl.getMatrix(), hcl.getMinValue(), hcl.getMaxValue()));
						hcl.repaint();
					}
				});

			colorSchemeMenu.add(absoluteMenuItem);
			displayMenu.add(colorSchemeMenu);

			customAbsColorMenuItem = new JMenuItem("Custom Color Scheme...");

			customAbsColorMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AbsoluteColorConverter conv = (AbsoluteColorConverter) hcl.colorConverter;

						AbsoluteColorSchemeSelectionDialog dialog = new AbsoluteColorSchemeSelectionDialog(HCLFrame.this, conv.getMinColor(), conv.getMaxColor(), conv.getNeutralColor());
						int result = dialog.showModal();
						if(result == JOptionPane.OK_OPTION) {
							hcl.setColorConverter(new AbsoluteColorConverter(dialog.getMinColor(), dialog.getMaxColor(), dialog.getNeutralColor(), hcl.getMatrix(), hcl.getMinValue(), hcl.getMaxValue()));
							hcl.repaint();
						}
					}
				});

			colorSchemeMenu.add(customAbsColorMenuItem);
			displayMenu.add(colorSchemeMenu);

			relativeMenuItem.doClick();
		}


		private void createColorResponseMenu() {

			ButtonGroup colorResponseGroup = new ButtonGroup();

			JRadioButtonMenuItem linearMenuItem = new JRadioButtonMenuItem("Linear", true);
			colorResponseGroup.add(linearMenuItem);
			linearMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						hcl.setColorConverter(new RowColorConverter(ColorResponse.LINEAR, hcl.getMatrix()));
						hcl.repaint();

					}
				});

			colorResponseMenu.add(linearMenuItem);

			JRadioButtonMenuItem logMenuItem = new JRadioButtonMenuItem("Log", false);
			colorResponseGroup.add(logMenuItem);
			logMenuItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						hcl.setColorConverter(new RowColorConverter(ColorResponse.LOG, hcl.getMatrix()));
						hcl.repaint();

					}
				});

			colorResponseMenu.add(logMenuItem);

			displayMenu.add(colorResponseMenu);
		}

	}


	/**
	 *@author    Joshua Gould
	 */
	static class MyFileFilter extends javax.swing.filechooser.FileFilter {
		java.util.List extensions;
		String fileFormat, description;


		public MyFileFilter(String[] extensions, String description, String fileFormat) {
			this.extensions = java.util.Arrays.asList(extensions);
			this.description = description;
			this.fileFormat = fileFormat;

		}


		public boolean accept(File f) {
			if(f.isDirectory()) {
				return true;
			}
			String name = f.getName();
			int dotIndex = name.lastIndexOf(".");
			if(dotIndex > 0) {
				String ext = name.substring(dotIndex + 1, name.length());
				return extensions.contains(ext.toLowerCase());
			}
			return false;
		}


		public String getDescription() {
			return description;
		}


		public String getFileFormat() {
			return fileFormat;
		}
	}
}

