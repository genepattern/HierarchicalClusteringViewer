import com.sun.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import mapletree.*;

import mapletree.data.hierarchy.*;

import mapletree.view.*;

import mapletree.view.hierarchy.*;


public class HierarchicalClusterViewer {
   HierarchyFileParser parser;
   DesktopFrame desktop=new DesktopFrame();
   String cdtString;
   String gtrString;
   String atrString;

   public static void main(String[] args) {
      new HierarchicalClusterViewer(args);
   }

   public HierarchicalClusterViewer(String[] args) {
      desktop.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      desktop.show();
      cdtString=args[0];
      if(args.length==2) {
         String s=args[1];
         if(s.toLowerCase().endsWith(".atr")) {
            atrString=s;
         } else {
            gtrString=s;
         }
      } else if(args.length==3) {
         String s=args[1];
         if(s.toLowerCase().endsWith(".atr")) {
            atrString=s;
            gtrString=args[2];
         } else {
            gtrString=s;
            atrString=args[2];
         }
      }
      if(atrString==null&&gtrString==null) {
         System.err.println("Must specify a gtr or atr file");
         System.exit(1);
      }
      try {
         desktop.getGlassPane().setCursor(Cursor.getPredefinedCursor(
                                                 Cursor.WAIT_CURSOR));
         desktop.getGlassPane().setVisible(true);
         final ProgressMonitor pm=new ProgressMonitor(desktop, "Parsing Files", 
                                                      "Please Wait...", 0, 100);
         pm.setProgress(0);
         final SwingWorker worker=new SwingWorker() {
            public Object construct() {
               if(gtrString==null) {
                  parser=new HierarchyFileParser(cdtString, atrString, 
                                                 HierarchyFileParser.ARRAYS_ONLY, 
                                                 pm);
               } else if(atrString==null) {
                  parser=new HierarchyFileParser(cdtString, gtrString, 
                                                 HierarchyFileParser.GENES_ONLY, 
                                                 pm);
               } else {
                  parser=new HierarchyFileParser(cdtString, gtrString, 
                                                 atrString, pm);
               }
               return parser;
            }

            public void finished() {
               //System.out.println("finished with parsing");
               File f=new File(cdtString);
               String name=f.getName();
               String t="Hierachical Thumbnail: " + name;
               final InternalNodeFrame iframe=new InternalNodeFrame(t, true, //resizable
                                                                    true, //cloasable
                                                                    true, //maximizable
                                                                    true, // iconifiable
                                                                    parser, 
                                                                    desktop);
               desktop.getContentPane().add(iframe);
               iframe.getHierarchyPane().adjust();
               // remove wait cursor
               desktop.getGlassPane().setVisible(false);
            }
         };
         worker.start();
      } catch(Exception e) {
         // remove wait cursor
         desktop.getGlassPane().setVisible(false);
         //show error dialog
         JOptionPane option=new JOptionPane();
         option.showInternalMessageDialog(desktop, 
                                          "There was a problem loading these files.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
     
         return;
      }
       catch(OutOfMemoryError error) {
         //print stacktrace for debugging
       
         desktop.getGlassPane().setVisible(false);
         //show error dialog
         JOptionPane option=new JOptionPane();
         option.showInternalMessageDialog(desktop, 
                                          "MapleTree ran out of memory loading these files.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
         return;
      }
   }
}