package edu.mit.genome.gp.ui.hclviewer;

import edu.mit.genome.dataobj.jg.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.swing.*;

public class HeatMap {
	public static void main(String[] args) throws Exception {

		//	DatasetReader reader = DatasetIO.getReaderByFileName(args[0]);
		//Dataset d = reader.read(args[0]);
		Dataset d = Decoder.decodeDataset(args[0]);
		
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for(int i = 0, rows = d.getRowDimension(); i < rows; i++) {
			for(int j = 0, columns = d.getColumnDimension(); j < columns; j++) {
				double value = d.get(i, j);
				min = value < min ? value : min;
				max = value > max ? value : max;
			}
		}
		final HCL hcl = new HCL(d, min, max, null, null);
		
		hcl.zoomIn();
		
		// saving image requires usually requires at least 512MB of memory
		BufferedImage snaphotImage = hcl.heatMapSnapshot();
		
		ij.ImagePlus ip = new ij.ImagePlus();
		ip.setImage(snaphotImage);
		ij.io.FileSaver fs = new ij.io.FileSaver(ip);
		
		String outputFormat = args[2];
		String outputFileName = args[1];
		if(outputFormat.equals("tiff")) {
			if(!outputFileName.toLowerCase().endsWith(".tiff")) {
				outputFileName += ".tiff";
			}
			fs.saveAsTiff(outputFileName);
		} else if(outputFormat.equals("bmp")) {
			if(!outputFileName.toLowerCase().endsWith(".bmp")) {
				outputFileName += ".bmp";
			}
			fs.saveAsBmp(outputFileName);
		
		} else {
			if(!outputFileName.toLowerCase().endsWith(".jpg") && !outputFileName.toLowerCase().endsWith(".jpeg")) { // fails on windows
				outputFileName += ".jpg";
			}
			fs.saveAsJpeg(outputFileName);
		}
	
		System.exit(1);
	}
	
}

/*
java -Xmx512m -cp <libdir>colt.jar<path.separator><libdir>file_support.jar<path.separator><libdir>gp-common.jar<path.separator><libdir>hclviewer.jar<path.separator><libdir>ij.jar<path.separator><libdir>trove.jar edu.mit.genome.gp.ui.hclviewer.HeatMap ~/res_files/ALL.res out jpeg
*/
