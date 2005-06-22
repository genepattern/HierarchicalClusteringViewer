/*
    ColorRelationship.java
    Created on April 23, 2002, 8:53 AM
  */
package edu.mit.genome.gp.ui.hclviewer.colorconverter;

/**
 * @author     KOhm
 * @created    August 14, 2003
 * @version
 */
public class ColorResponse {
	// fields
	/**  when displaying the pink o' gram use the linear color response (classic way) */
	public final static ColorResponse LINEAR =
			new ColorResponse("Linear color respose", 0);
	/**  when displaying the pink o' gram use the log color response */
	public final static ColorResponse LOG =
			new ColorResponse("Logarithmic color response", 1);

   String name;
   int value;
	/**
	 *  Creates new ColorRelationship
	 *
	 * @param  name   Description of the Parameter
	 * @param  value  Description of the Parameter
	 */
	private ColorResponse(String name, int value) {
		this.name = name;
      this.value = value;
	}
}

