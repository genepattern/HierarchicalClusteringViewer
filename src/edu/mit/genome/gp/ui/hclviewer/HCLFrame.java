package edu.mit.genome.gp.ui.hclviewer;

import edu.mit.genome.gp.ui.hclviewer.colorconverter.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;


public class HCLFrame extends JFrame {
	HCL hcl;

	public HCLFrame(HCL hcl) {
		this.hcl = hcl;
		setJMenuBar(new MenuBar());
		Container c = getContentPane();
		c.setBackground(Color.white);
		c.add(hcl.getComponent(), BorderLayout.CENTER);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (screen.width * .60), (int) (screen.height * .80));
		hcl.updateSize();
		show();
	}


//
	private void showSaveImageDialog() {
		JOptionPane.showMessageDialog(this, "Please be patient. Creating the image can take several minutes.");
		try {
			// saving image requires usually requires 512MB of memory
			final BufferedImage snaphotImage = hcl.snapshot();
		/*	JFrame f= new JFrame();
			JPanel p = new JPanel() {
				public void paintComponent(Graphics g) {
					g.drawImage(snaphotImage, 0, 0, null);
				}
			};
			f.getContentPane().add(p);
			f.setSize(400,400);
			f.show();
			*/
			
			ij.ImagePlus ip = new ij.ImagePlus();
			ip.setImage(snaphotImage);
			ij.io.FileSaver fs = new ij.io.FileSaver(ip);
			fs.saveAsTiff();
		} catch(java.lang.OutOfMemoryError e) {
			JOptionPane.showMessageDialog(this, "Out of memory. Try allocating more memory or try using a smaller pixel size.");
		}
		// fs.saveAsBmp(); also works, gif and jpeg don't
		/*
		    final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		    chooser.setAcceptAllFileFilterUsed(false);
		    /chooser.setCurrentDirectory(new File("Data"));
		    /		chooser.addChoosableFileFilter(new BMPFileFilter());
		    chooser.addChoosableFileFilter(new JPGFileFilter());
		    chooser.addChoosableFileFilter(new PNGFileFilter());
		    chooser.addChoosableFileFilter(new TIFFFileFilter());
		    int chooserState = chooser.showSaveDialog(null);
		    if(chooserState == JFileChooser.APPROVE_OPTION) {
		    final File fFile = chooser.getSelectedFile();
		    if(fFile.exists()) {
		    int selected = JOptionPane.showConfirmDialog(this,
		    "A file named " + fFile.getName() + " already exists. Are you sure you want to replace it?",
		    "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
		    if(selected != JOptionPane.YES_OPTION) {
		    return;
		    }
		    }
		    *	final String fFormat = ((ImageFileFilter) chooser.getFileFilter()).getFileFormat();
		    final ImageEncodeParam fParam = ((ImageFileFilter) chooser.getFileFilter()).getImageEncodeParam();
		    try {
		    Thread thread =
		    new Thread() {
		    public void run() {
		    JAI.create("filestore", fImage, fFile.getPath(), fFormat, fParam);
		    JOptionPane.showMessageDialog(null, "Your image was saved successfully.");
		    }
		    };
		    /  thread.setPriority(Thread.MIN_PRIORITY);
		    thread.start();
		    } catch(Exception e) {
		    /Manager.message(getFrame(), e);
		    }
		    }
		 */
	}

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
						hcl.setShowFlyOverText(!hcl.isShowingFlyOverText());
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
						SetElementSizeDialog dialog = new SetElementSizeDialog(HCLFrame.this, hcl.getIntXPixPerUnit(), hcl.getIntYPixPerUnit());
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
			JMenuItem saveImageMenuItem = new JMenuItem("Save Image");
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
			JMenu colorSchemeMenu = new JMenu("Color Scheme");
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

			JRadioButtonMenuItem absoluteMenuItem = new JRadioButtonMenuItem("Global", false);
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
						AbsoluteColorConverter conv = (AbsoluteColorConverter) hcl.getColorConverter();
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
}

