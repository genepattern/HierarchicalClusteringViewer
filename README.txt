HierarchicalClusteringViewer.

This README was added for v10, aiming to fix issues with running with Java 7 on a Mac.

Note that we have incomplete source in SVN HEAD, both here and in the common directory.  
The code base used to reside under common only, but unfortunately that has changed over
time and the necessary code is not there.  The most recent version of the code can be 
found by going through the SVN Tags, combined with what is found here in the src dir.

Treat the code in 'src' as the most recent versions of those particular classes.  For
code that is not there, start by looking in the SVN Tags for previous prereleases,
starting here:
   https://vc.broadinstitute.org/gp2/tags/urn=lsid=broad_mit_edu=cancer_software_genepattern_module_visualizer=00031=9/
and working backward through 8_4, 8_3, etc. back to 8_0.  Not all of the code is 
present in all of those Tags but it is present over the span of all of them.  A nearly
complete set of files is present in 8_0 (missing only org.genepattern.stats.Util) but
other Tags contain newer files.

It would probably be possible to bring together a full set of required files but it would 
require an effort of combing through SVN that may not be justified.  Instead, we are taking
the approach of just recovering individual files as necessary.  Doing otherwise brings the
risk of reintroducing old bugs back into the module.

Look to the hcl-o.jar for guidance about what files are required.

Notes for v10:
- Recovered only the org.genepattern.clustering.hierarchical.HCLApp class (from 8_0 Tag). 
  One line change to fix launching on a Mac with Java 7.
