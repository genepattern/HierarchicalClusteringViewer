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

package org.genepattern.matrix;

/**
 * Contains constants for datasets
 *
 * @author Joshua Gould
 */
public class DatasetConstants {
    /** Description key */
    public static final String DESCRIPTION = "description";

    /** Affy A/P/M calls key */
    public static final String ABSENT_PRESENT_CALLS = "calls";

    /** Affy Present Call key */
    public static final String PRESENT = "P";

    /** Affy Absent Call key */
    public static final String ABSENT = "A";

    /** Affy Marginal Call key */
    public static final String MARGINAL = "M";

    /** Chromosome key */
    public static final String CHROMOSOME = "Chromosome";

    /** Physical Position key */
    public static final String PHYSICAL_POSITION = "PhysicalPosition";

    /** SNP AA, AB, BB, calls */
    public static final String SNP_CALL = "Snp Call";

    public static final String ALLELE_B = "Allele_B";

    public static final String GENE_ID = "gene id";

    public static final String ARRAY_ID = "array id";

    private DatasetConstants() {
    }
}
