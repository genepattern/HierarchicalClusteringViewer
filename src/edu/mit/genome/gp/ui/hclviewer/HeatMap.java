package edu.mit.genome.gp.ui.hclviewer;

import edu.mit.genome.dataobj.jg.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.swing.*;
import java.io.*;

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
		org.apache.batik.transcoder.image.ImageTranscoder transcoder = null; 
        // createImage();
        String outputFileName = args[1];
        String outputFormat = args[2];
        if(outputFormat.equals("png")) {
        	transcoder = new org.apache.batik.transcoder.image.PNGTranscoder();
			if(!outputFileName.toLowerCase().endsWith(".png")) {
				outputFileName += ".png";
			}
		} else {
			transcoder = new org.apache.batik.transcoder.image.JPEGTranscoder();
			transcoder.addTranscodingHint(org.apache.batik.transcoder.image.JPEGTranscoder.KEY_QUALITY, new Float(.8));
        	if(!outputFileName.toLowerCase().endsWith(".jpg") && !outputFileName.toLowerCase().endsWith(".jpeg")) {
				outputFileName += ".jpg";
			}
		}
		int columnWidth = 8;
		int rowWidth = 8;
		
		for(int i = 3; i < args.length; ) { // 1st arg is input file name, 2nd arg is output file name, 3rd arg is format
			if(args[i].equals("-cw")) {
				columnWidth = Integer.parseInt(args[++i]);
				break;
			}else if(args[i].equals("-rw")) {
				rowWidth = Integer.parseInt(args[++i]);
				break;
			} else {
				System.err.println("unknown option " + args[i]);
				System.exit(1);
			}
		}
		BufferedImage snapshotImage = hcl.heatMapSnapshot(transcoder, columnWidth, rowWidth);
        FileOutputStream fos = new FileOutputStream(outputFileName);
        org.apache.batik.transcoder.TranscoderOutput out = new org.apache.batik.transcoder.TranscoderOutput(fos);
        transcoder.writeImage(snapshotImage, out);
        fos.close();
		// saving image requires usually requires at least 512MB of memory
	/*	BufferedImage snaphotImage = hcl.heatMapSnapshot();
		
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
		*/
		System.exit(1);
	}
	
}

/*
java -Xmx512m -cp <libdir>colt.jar<path.separator><libdir>file_support.jar<path.separator><libdir>gp-common.jar<path.separator><libdir>hclviewer.jar<path.separator><libdir>ij.jar<path.separator><libdir>trove.jar edu.mit.genome.gp.ui.hclviewer.HeatMap ~/res_files/ALL.res out jpeg
*/
