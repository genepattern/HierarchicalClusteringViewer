/*
    WHITEHEAD INSTITUTE
    SOFTWARE COPYRIGHT NOTICE AGREEMENT
    This software and its documentation are copyright 2001 by the
    Whitehead Institute for Biomedical Research.  All rights are reserved.
    This software is supplied without any warranty or guaranteed support
    whatsoever.  The Whitehead Institute can not be responsible for its
    use, misuse, or functionality.
  */
package edu.mit.genome.gp.ui.hclviewer;

import edu.mit.genome.dataobj.*;

import java.io.Serializable;
import java.util.Arrays;
import edu.mit.genome.util.*;


import gnu.trove.TFloatArrayList;

/**
 *  This class represents an immutable matrix. There are no setXXX(...) methods.
 *  See MatrixMutable for a subclass that is mutable. Note that all variables
 *  are private. There are protected getInternalXXX(...) that are for use by
 *  subclasses only. Classes within this package should not be using them. None
 *  of the methods in this class should change the data, either in this Matrix
 *  or in passed in 'other' instances. MatrixMutable objects can have their
 *  state modified by the various methods present. * Lots of code/ideas copied
 *  form Kenji Hiranabe, Eiwa System Management, Inc. GMatrix part of the
 *  javax.vecmath library (unoffical (his) impl)
 *  http://java.sun.com/products/java-media/3D/forDevelopers/j3dapi/index.html
 *  as note: made several of the row related methods unfinal as need to override
 *
 * @author     Aravind Subramanian
 * @created    August 4, 2003
 * @version    %I%, %G%
 */

public final class FloatMatrix implements Matrix, Serializable {

	// fields

	private final static long serialVersionUID = 8728211089911142600L;

	/**
	 *  IMP IMP: if any fields are added to this class, make sure to update the
	 *  constructors and the set(Matrix method
	 */

	/**
	 *  The data of the Matrix.(1-D array. The (i,j) element is stored in
	 *  elementData[i*columnCount + j])
	 */
	private float elementData[];
	private Object[] columnAnnotations;
	private Object[] rowAnnotations;

	/**  The number of rows in this matrix. */
	private int rowCount;

	/**  The number of columns in this matrix. */
	private int columnCount;

	/**  the hash code value or 0 if not calculated */
	private int hashcode = 0;

	/**
	 *  Constructs an rowCount by columnCount all zero matrix. (as change) Note
	 *  that even though row and column numbering begins with zero, rowCount and
	 *  columnCount will be one larger than the maximum possible matrix index
	 *  values.
	 *
	 * @param  nrows  Description of the Parameter
	 * @param  ncols  Description of the Parameter
	 */
	public FloatMatrix(final int nrows, final int ncols) {
		if(nrows < 0) {
			throw new NegativeArraySizeException(nrows + " < 0");
		}
		if(ncols < 0) {
			throw new NegativeArraySizeException(ncols + " < 0");
		}

		this.rowCount = nrows;
		this.columnCount = ncols;
		elementData = new float[rowCount * columnCount];
		columnAnnotations = new Object[columnCount];
		rowAnnotations = new Object[rowCount];
	}

	public FloatMatrix(final int nrows, final int ncols, float[] elements, Object[] rowAnnotations, Object[] columnAnnotations) {
		this.rowCount = nrows;
		this.columnCount = ncols;
		this.elementData = elements; //FIXME distinguish between capacity and current size
		this.rowAnnotations = rowAnnotations;
		this.columnAnnotations = columnAnnotations;
	}
	
	public FloatMatrix(FloatMatrix other) {
		this.rowCount = other.rowCount;
		this.columnCount = other.columnCount;
		this.elementData = other.elementData;
		this.rowAnnotations = other.rowAnnotations;
		this.columnAnnotations = other.columnAnnotations;
	}


	public FloatMatrix swapColumns(int column1, int column2) {
		final int r = rowCount;
		for(int row = 0; row < r; row++) {
			float value1 = getElement(row, column1);
			Object annotation1 = getAnnotationForColumn(column1);

			float value2 = getElement(row, column2);
			Object annotation2 = getAnnotationForColumn(column2);

			setElement(row, column1, value2);
			setAnnotationForColumn(column1, annotation2);

			setElement(row, column2, value1);
			setAnnotationForColumn(column2, annotation1);
		}
		return this;
	}

	public void swapRows(int row1, int row2) {
		final int c = columnCount;
		for(int column = 0; column < c; column++) {
			float value1 = getElement(row1, column);
			Object annotation1 = getAnnotationForRow(row1);

			float value2 = getElement(row2, column);
			Object annotation2 = getAnnotationForRow(row2);

			setElement(row1, column, value2);
			setAnnotationForRow(row1, annotation2);

			setElement(row2, column, value1);
			setAnnotationForRow(row2, annotation1);
		}

	}

	private void ensureElementCapacity(int index) {
		if(index >= elementData.length) {
			float[] temp = elementData;
			int newSize = (temp.length * 3) / 2 + 1;
			elementData = new float[newSize];
			System.arraycopy(temp, 0, elementData, 0, temp.length);
		}
	}

	public void setElement(int row, int column, float value) {
		int index = getIndex(row, column);
		
		ensureElementCapacity(index);
		if(row >= rowCount) {
			rowCount = row+1;
		}
		if(column >= columnCount) {
			columnCount = column + 1;
			//System.out.println("column " + column);
		}
		elementData[index] = value;
	}

	/**
	 *  Sets the matrix cell at coordinate [row,column] to the specified value.
	 *  Provided with invalid parameters this method may access illegal indexes
	 *  without throwing any exception. You should only use this method when you
	 *  are absolutely sure that the coordinate is within bounds. Precondition
	 *  (unchecked): 0 <= column < getColumnCount() && 0 <= row < getRowCount().
	 *
	 * @param  row     - the index of the row-coordinate
	 * @param  column  - the index of the column-coordinate
	 * @param  value   - the value to be filled into the specified cell.
	 */
	public void setElementQuick(int row, int column, float value) {
		int index = getIndex(row, column);
		elementData[index] = value;
	}

	/**
	 *  Sets the matrix cell at coordinate [row,column] to the specified value.
	 *  Provided with invalid parameters this method may access illegal indexes
	 *  without throwing any exception. You should only use this method when you
	 *  are absolutely sure that the coordinate is within bounds. Precondition
	 *  (unchecked): 0 <= column < getColumnCount() && 0 <= row < getRowCount().
	 *
	 * @param  row    - the index of the row-coordinate
	 * @param  value  - the value to be filled into the specified cell.
	 */
	public void setAnnotationForRow(int row, Object value) {
		if(row >= rowAnnotations.length) {
			Object[] temp = rowAnnotations;
			int newSize = (temp.length * 3) / 2 + 1;
			rowAnnotations = new Object[newSize];
			System.arraycopy(temp, 0, rowAnnotations, 0, temp.length);
		}
		rowAnnotations[row] = value;
		//System.out.println("matrix row " + row + " value " + value);
	}

	public Object getAnnotationForRow(int row) {
		return rowAnnotations[row];
	}

	/**
	 *  Sets the matrix cell at coordinate [row,column] to the specified value.
	 *  Provided with invalid parameters this method may access illegal indexes
	 *  without throwing any exception. You should only use this method when you
	 *  are absolutely sure that the coordinate is within bounds. Precondition
	 *  (unchecked): 0 <= column < getColumnCount() && 0 <= row < getRowCount().
	 *
	 * @param  column  - the index of the column-coordinate
	 * @param  value   - the value to be filled into the specified cell.
	 */
	public void setAnnotationForColumn(int column, Object value) {
		if(column >= columnAnnotations.length) {
			Object[] temp = columnAnnotations;
			int newSize = (temp.length * 3) / 2 + 1;
			columnAnnotations = new Object[newSize];
			System.arraycopy(temp, 0, columnAnnotations, 0, temp.length);
		}
	//	System.out.println("column " + column + " " + value);
		columnAnnotations[column] = value;
	}

	public Object getAnnotationForColumn(int column) {
		return columnAnnotations[column];
	}

	public void trimToSize() {
		int size = rowCount*columnCount;
		float[] temp = elementData;
		elementData = new float[size];
		System.arraycopy(temp, 0, elementData, 0, size);

		Object tempRow = rowAnnotations;
		rowAnnotations = new Object[rowCount];
	//	System.out.println("rowCount " + rowCount + " columnCount " + columnCount);
		System.arraycopy(tempRow, 0, rowAnnotations, 0, rowCount);

		Object tempColumn = columnAnnotations;
		columnAnnotations = new Object[columnCount];
		System.arraycopy(tempColumn, 0, columnAnnotations, 0, columnCount);
	}


	public final static float[] parseString(final String data, final int[] row_col_cnt) throws NumberFormatException {
		final ReusableStringTokenizer r_tok = new ReusableStringTokenizer();
		final ReusableStringTokenizer val_tok = new ReusableStringTokenizer();
		final String ws = ReusableStringTokenizer.WHITESPACE;

		r_tok.resetTo(data, "[", false);
		final int num_rows = r_tok.countTokens() - 1;

		final TFloatArrayList vector = new TFloatArrayList(num_rows * 100);// est. size
		int r = 0;// est. size
		int c = 0;// est. size
		int row_len = 0;
		try {
			while(r_tok.hasMoreTokens()) {
				final String row = r_tok.nextToken();
				val_tok.resetTo(row, ws + "[]", false);//val_tok.resetTo(row, ws, false);
				c = 0;
				while(val_tok.hasMoreTokens()) {
					final String token = val_tok.nextToken();
					final float value = Float.parseFloat(token);
					vector.add(value);
					c++;
				}
				// sanity check
				if(row_len != 0) {
					if(c != row_len) {
						throw new ArrayIndexOutOfBoundsException("Length of row "
								 + r + " is " + c + " but should be " + row_len + "!");
					}
				} else {// first time so set row_len
					row_len = c;
				}

				r++;
			}
		} catch(NumberFormatException ex) {
			throw new NumberFormatException(ex + ":\nat row " + r + ", column " + c);
		}

		row_col_cnt[0] = num_rows;
		row_col_cnt[1] = row_len;// which is the numbner of columns

		return vector.toNativeArray();
	}

	/**
	 *  get a new Matrix that is the transpose of this matrix.
	 *
	 * @return    The transposedMatrix value
	 */
	public final Matrix getTransposedMatrix() {
		final float[] elements = new float[elementData.length];
		int num_rows = rowCount;
		int num_cols = columnCount;
		for(int r = 0; r < num_rows; r++) {
			for(int c = 0; c < num_cols; c++) {//rc -> cr
				elements[c * num_rows + r] = elementData[r * num_cols + c];
			}
		}
		return new FloatMatrix(num_cols, num_rows, elements, (Object[])columnAnnotations.clone(),(Object[]) rowAnnotations.clone());
	}

	/**
	 *  returns true if this Matrix is unsafe and its values can be changed
	 *
	 * @return    The mutable value
	 */
	public final boolean isMutable() {
		return true;
	}

	// internal or subclass-only methods
	// These methods throw an Exception if this is not mutable!

	/**
	 *  gets the elementData
	 *
	 * @return    The internalElementData value
	 */
	protected final float[] getInternalElementData() {
		return elementData;
	}

	/**
	 *  sets the elementData
	 *
	 * @param  elementData  The new internalElementData value
	 */
	protected final void setInternalElementData(final float[] elementData) {
		this.elementData = elementData;
	}

	/**
	 *  sets the number of rows
	 *
	 * @param  rowcnt  The new internalRowCount value
	 */
	protected final void setInternalRowCount(final int rowcnt) {
		this.rowCount = rowcnt;
	}

	/**
	 *  sets the number of columns
	 *
	 * @param  colcnt  The new internalColCount value
	 */
	protected final void setInternalColCount(final int colcnt) {
		this.columnCount = colcnt;
	}

	/**
	 *  returns the index into the data array given the row and column index (zero
	 *  based)
	 *
	 * @param  row     Description of the Parameter
	 * @param  column  Description of the Parameter
	 * @return         The index value
	 */
	protected final int getIndex(final int row, final int column) {
		return getIndex(row, column, rowCount, columnCount);
	}

	/**
	 *  returns the index into the data array given the row and column index (zero
	 *  based) and row and column counts
	 *
	 * @param  row      Description of the Parameter
	 * @param  column   Description of the Parameter
	 * @param  row_cnt  Description of the Parameter
	 * @param  col_cnt  Description of the Parameter
	 * @return          The index value
	 */
	public final static int getIndex(final int row, final int column, final int row_cnt, final int col_cnt) {
		return row * col_cnt + column;
	}
	// end special subclass-only methods

	/**
	 *  Copies a sub-matrix derived from this matrix into the target matrix. The
	 *  upper left of the sub-matrix is located at (rowSource, colSource); the
	 *  lower right of the sub-matrix is located at (lastRowSource,lastColSource).
	 *  The sub-matrix is copied into the the target matrix starting at (rowDest,
	 *  colDest).
	 *
	 * @param  rowSource  the top-most row of the sub-matrix
	 * @param  colSource  the left-most column of the sub-matrix
	 * @param  numRow     the number of rows in the sub-matrix
	 * @param  numCol     the number of columns in the sub-matrix
	 * @param  rowDest    the top-most row of the position of the copied sub-matrix
	 *      within the target matrix
	 * @param  colDest    the left-most column of the position of the copied
	 *      sub-matrix within the target matrix
	 * @param  target     the matrix into which the sub-matrix will be copied
	 */


public final void copySubMatrix(final int rowSource, final int colSource, final int numRow, final int numCol,
			final int rowDest, final int colDest, final MatrixMutable target) {
		if(rowSource < 0 || colSource < 0 || rowDest < 0 || colDest < 0) {
			throw new ArrayIndexOutOfBoundsException(
					"rowSource,colSource,rowDest,colDest < 0.");
		}
		if(rowCount < numRow + rowSource || columnCount < numCol + colSource) {
			throw new ArrayIndexOutOfBoundsException("Source Matrix too small.");
		}
		if(target.getRowCount() < numRow + rowDest || target.getColumnCount() < numCol + colDest) {
			throw new ArrayIndexOutOfBoundsException("Target Matrix too small.");
		}

		final float[] target_data = null; // FIXME target.getElementData();
		for(int i = 0; i < numRow; i++) {
			for(int j = 0; j < numCol; j++) {
				target_data[(i + rowDest) * columnCount + (j + colDest)] = elementData[(i + rowSource) * columnCount + (j + colSource)];
			}
		}
	}

	/**
	 *  creates a new FloatMatrix out of the row and column indices Uses all of the
	 *  columns or rows if the cols or rows array, respectively, is null or size 0.
	 *
	 * @param  rows  Description of the Parameter
	 * @param  cols  Description of the Parameter
	 * @return       Description of the Return Value
	 */

	public final Matrix createSubMatrix(int[] rows, int[] cols) {
		if(rows != null && rows.length > 0) {
			if(!ArrayUtils.arrayValuesLessThan(rows, rowCount)) {
				throw new IllegalArgumentException("Some values in the row index "
						 + "array are greater than the number of rows (" + rowCount + "):\n"
						 + ArrayUtils.toString(rows));
			}
		} else {
			rows = ArrayUtils.rangeAsElements(0, rowCount);
		}

		if(cols != null && cols.length > 0) {
			if(!ArrayUtils.arrayValuesLessThan(cols, columnCount)) {
				throw new IllegalArgumentException("Some values in the column index "
						 + "array are greater than the number of columns (" + columnCount + "):\n"
						 + ArrayUtils.toString(cols));
			}
		} else {
			cols = ArrayUtils.rangeAsElements(0, columnCount);
		}

		final int num_rows = rows.length;

		final int num_cols = cols.length;
		final FloatMatrix matrix = new FloatMatrix(num_rows, num_cols);
		final float[] data = matrix.elementData;

		for(int r = 0; r < num_rows; r++) {
			for(int c = 0; c < num_cols; c++) {
				data[matrix.getIndex(r, c)] =
						this.elementData[this.getIndex(rows[r], cols[c])];
			}
		}

		return matrix;
	}

	/**
	 *  Returns the number of rows in this matrix.
	 *
	 * @return    number of rows in this matrix
	 */
	public final int getRowCount() {
		return rowCount;
	}

	/**
	 *  Returns the number of colmuns in this matrix.
	 *
	 * @return    number of columns in this matrix
	 */
	public final int getColumnCount() {
		return columnCount;
	}

	/**
	 *  Retrieves the value at the specified row and column of this matrix.
	 *
	 * @param  row     the row number to be retrieved (zero indexed)
	 * @param  column  the column number to be retrieved (zero indexed)
	 * @return         the value at the indexed element
	 */
	public final float getElement(final int row, final int column) {
		if(rowCount <= row) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " >= matrix's row count:" + rowCount);
		}
		if(row < 0) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " < 0");
		}
		if(columnCount <= column) {
			throw new ArrayIndexOutOfBoundsException("column:" + column + " >= matrix's column count:" + columnCount);
		}
		if(column < 0) {
			throw new ArrayIndexOutOfBoundsException("column:" + column + " < 0");
		}

		return elementData[getIndex(row, column)];
	}

	/**
	 *  gets the value at the specified index into the elementData array This
	 *  should only be used by subclasses not intended for general use.
	 *
	 * @param  i  Description of the Parameter
	 * @return    The elementAtIndex value
	 */
	public final float getElementAtIndex(final int i) {
		return elementData[i];
	}

	/**
	 *  Places the values of the specified row into the array parameter.
	 *
	 * @param  row    the target row number
	 * @param  array  the array into which the row values will be placed
	 * @return        The row value
	 */
	public final float[] getRow(final int row, final float[] array) {
		if(rowCount <= row) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " > matrix's rowCount:" + rowCount);
		}
		if(row < 0) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " < 0");
		}
		if(array.length < columnCount) {
			throw new ArrayIndexOutOfBoundsException("array length:" + array.length + " smaller than matrix's columnCount:" + columnCount);
		}

		System.arraycopy(elementData, row * columnCount, array, 0, columnCount);
		return array;
	}

	/**
	 *  Places the values of the specified row into the vector parameter.
	 *
	 * @param  row     the target row number
	 * @param  vector  the vector into which the row values will be placed
	 * @return         The row value
	 */
	public final FloatVector getRow(final int row, final FloatVector vector) {
		if(rowCount <= row) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " > matrix's rowCount:" + rowCount);
		}
		if(row < 0) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " < 0");
		}
		if(vector.getSize() < columnCount) {
			vector.setSize(columnCount);
		}

		// if may use package friendly accessibility, would do;
		//FIXME System.arraycopy(elementData, row * columnCount, vector.elementData, 0, columnCount);
		return vector;
	}

	/**
	 *  a safe copy is returned.
	 *
	 * @param  row  Description of the Parameter
	 * @return      The rowv value
	 */
	public final FloatVector getRowv(final int row) {
		if(rowCount <= row) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " > matrix's rowCount:" + rowCount);
		}
		if(row < 0) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " < 0");
		}

		FloatVector vector = new FloatVector(columnCount);
		//FIXME System.arraycopy(elementData, row * columnCount, vector.elementData, 0, columnCount);

		return vector;
	}

	/**
	 *  a safe copy is returned.
	 *
	 * @param  row  Description of the Parameter
	 * @return      The row value
	 */
	public final float[] getRow(final int row) {
		if(rowCount <= row) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " > matrix's rowCount:" + rowCount);
		}
		if(row < 0) {
			throw new ArrayIndexOutOfBoundsException("row:" + row + " < 0");
		}

		float[] ret = new float[columnCount];
		System.arraycopy(elementData, row * columnCount, ret, 0, columnCount);
		return ret;
	}

	/**
	 *  Places the values of the specified column into the array parameter.
	 *
	 * @param  col    the target column number
	 * @param  array  the array into which the column values will be placed
	 * @return        The column value
	 */
	public final float[] getColumn(final int col, final float[] array) {
		if(columnCount <= col) {
			throw new ArrayIndexOutOfBoundsException("col:" + col + " > matrix's columnCount:" + columnCount);
		}
		if(col < 0) {
			throw new ArrayIndexOutOfBoundsException("col:" + col + " < 0");
		}
		if(array.length < rowCount) {
			throw new ArrayIndexOutOfBoundsException("array.length:" + array.length + " < matrix's rowCount=" + rowCount);
		}

		for(int i = 0; i < rowCount; i++) {
			array[i] = elementData[i * columnCount + col];
		}
		return array;
	}

	/**
	 *  Places the values of the specified column into the vector parameter.
	 *
	 * @param  col     the target column number
	 * @param  vector  the vector into which the column values will be placed
	 * @return         The column value
	 */
	public final FloatVector getColumn(final int col, final FloatVector vector) {
		if(columnCount <= col) {
			throw new ArrayIndexOutOfBoundsException("col:" + col + " > matrix's columnCount:" + columnCount);
		}
		if(col < 0) {
			throw new ArrayIndexOutOfBoundsException("col:" + col + " < 0");
		}
		if(vector.getSize() < rowCount) {
			throw new ArrayIndexOutOfBoundsException("vector size:" + vector.getSize() + " < matrix's rowCount:" + rowCount);
		}
		for(int i = 0; i < rowCount; i++) {
			vector.setElement(i, elementData[i * columnCount + col]);
		}
		return vector;
	}

	public final FloatVector getColumnv(final int col) {
		if(col < 0) {
			throw new ArrayIndexOutOfBoundsException("col:" + col + " < 0");
		}

		FloatVector vector = new FloatVector(rowCount);
		//FIXME System.arraycopy(elementData, col * rowCount, vector.elementData, 0, rowCount);

		return vector;
	}

	/**
	 *  Places the values from this matrix into the matrix m1; m1 should be at
	 *  least as large as this Matrix.
	 *
	 * @param  m1  The matrix that will hold the new values
	 * @return     Description of the Return Value
	 */
	public final Matrix get(final MatrixMutable m1) {
		// need error check.
		final int m1_row_cnt = m1.getRowCount();
		// need error check.
		final int m1_col_cnt = m1.getColumnCount();
		if(m1_row_cnt < rowCount || m1_col_cnt < columnCount) {
			throw new IllegalArgumentException(
					"m1 matrix is smaller than this matrix.");
		}

		if(m1_col_cnt == columnCount) {
			; //FIXME System.arraycopy(elementData, 0, m1.getElementData(), 0, rowCount * columnCount);
		} else {
			for(int i = 0; i < rowCount; i++) {
				; //FIXME System.arraycopy(elementData, i * columnCount, m1.getElementData(), i * m1_col_cnt, columnCount);
			}
		}
		return m1;
	}

	/**
	 *  dumps the contents of the values to a Sting on a single line (no line
	 *  seperators)
	 *
	 * @return    Description of the Return Value
	 */
	public String dump() {
		return dump("");
	}

	/**
	 *  helper method that puts the contents of all values into a String
	 *
	 * @param  nl  the string that separates one row from the next
	 * @return     Description of the Return Value
	 */
	public String dump(final String nl) {
		StringBuffer out = new StringBuffer("[");
		out.append(nl);

		for(int i = 0; i < rowCount; i++) {
			out.append("  [");
			for(int j = 0; j < columnCount; j++) {
				if(0 < j) {
					out.append("\t");
				}
				out.append(elementData[i * columnCount + j]);
			}
			if(i + 1 < rowCount) {
				out.append("]");
				out.append(nl);
			} else {
				out.append("] ]");
			}
		}
		return out.toString();
	}

	/**
	 *  Returns a string that contains the values of this Matrix in a human
	 *  readable form.
	 *
	 * @return    the String representation
	 */
	public final String toString() {
		String nl = System.getProperty("line.separator");
		return dump(nl);
	}

	/**
	 *  returns a DataModel that defines the type of model this implementation
	 *  represents
	 *
	 * @return    The dataModel value
	 */
	public DataModel getDataModel() {
		return DATA_MODEL;
	}

	/**
	 *  returns the name of this matrix
	 *
	 * @return    The name value
	 */
	public String getName() {
		return toString();
	}

	/**
	 *  Returns a hash number based on the data values in this object. Two
	 *  different Matrix objects with identical data values (ie, returns true for
	 *  equals(Matrix) ) will return the same hash number. Two objects with
	 *  different data members may return the same hash value, although this is not
	 *  likely.
	 *
	 * @return    the integer hash value
	 */
	public final int hashCode() {
		if(hashcode == 0 || isMutable()) {
			int result = 17;// some prime #
			final int cnt = getRowCount() * getColumnCount();
			for(int i = 0; i < cnt; i++) {
				result = 37 * result + Float.floatToIntBits(elementData[i]);
			}
			if(isMutable()) {// for mutable classes recalculate every time
				return result;
			} else {
				hashcode = result;
			}
		}
		return hashcode;
	}
	//    public final  int hashCode() {
	//        int hash = 0;
	//        for (int i = 0; i < rowCount*columnCount; i++) {
	//
	//            long bits = Double.doubleToLongBits(elementData[i]);
	//            hash ^= (int)(bits ^ (bits >> 32));
	//        }
	//        return hash;
	//    }

	/**
	 *  Returns true if all of the data members of Matrix4d m1 are equal to the
	 *  corresponding data members in this Matrix4d.
	 *
	 * @param  m1  The matrix with which the comparison is made.
	 * @return     true or false
	 */
	public final boolean equals(final Matrix m1) {
		if(m1 == this) {
			return true;
		}
		if(m1 == null || rowCount != m1.getRowCount() || columnCount != m1.getColumnCount()) {
			return false;
		}
		if(m1 instanceof FloatMatrix) {
			return java.util.Arrays.equals(elementData, ((FloatMatrix) m1).elementData);
		} else {// this case could violate the transitive property of equality
			// is equavalent to being equals(Matrix) and the "if" part would be equals(FloatMatrix)
			final int row_cnt = rowCount;// this case could violate the transitive property of equality
			// is equavalent to being equals(Matrix) and the "if" part would be equals(FloatMatrix)
			final int col_cnt = columnCount;
			for(int i = 0; i < row_cnt; i++) {
				for(int j = 0; j < col_cnt; j++) {
					if(elementData[i * col_cnt + j] != m1.getElement(i, j)) {
						return false;
					}
				}
			}
		}
		return true;
		//        if (m1 == null || rowCount != m1.getRowCount() || columnCount != m1.getColumnCount())
		//            return false;
		//        if(m1 instanceof FloatMatrix)
		//            return java.util.Arrays.equals(elementData, ((FloatMatrix)m1).elementData);
		//        else {
		//            final int row_cnt = rowCount, col_cnt = columnCount;
		//            for (int i = 0; i < row_cnt; i++)
		//                for (int j = 0; j < col_cnt; j++)
		//                    if (elementData[i*col_cnt + j] != m1.getElement(i, j))
		//                        return false;
		//        }
		//        return true;
	}

	/**
	 *  Returns true if the Object o1 is of type Matrix and all of the data members
	 *  of t1 are equal to the corresponding data members in this Matrix.
	 *
	 * @param  o1  the object with which the comparison is made.
	 * @return     Description of the Return Value
	 */
	public final boolean equals(Object o1) {
		return (o1 instanceof Matrix) && equals((Matrix) o1);
	}

	//    /**
	//     * Returns true if the L-infinite distance between this matrix and
	//     * matrix m1 is less than or equal to the epsilon parameter,
	//     * otherwise returns false. The L-infinite distance is equal to
	//     * MAX[i=0,1,2, . . .n ; j=0,1,2, . . .n ; abs(this.m(i,j) - m1.m(i,j)] .
	//     * @deprecated The double version of this method should be used.
	//     * @param m1 The matrix to be compared to this matrix
	//     * @param epsilon the threshold value
	//     */
	//    public final  boolean epsilonEquals(final Matrix m1, final float epsilon) {
	//        if(m1.getRowCount() != rowCount)
	//            return false;
	//        if(m1.getColumnCount() != columnCount)
	//            return false;
	//        final int row_cnt = rowCount, col_cnt = columnCount;
	//        for (int r = 0; r < row_cnt; r++)
	//            for (int c = 0; c < col_cnt; c++)
	//                if (epsilon <
	//                 Math.abs(elementData[r*columnCount + c] - m1.getElement(r, c)))
	//                    return false;
	//
	//        return true;
	//    }
	/**
	 *  Returns true if the L-infinite distance between this matrix and matrix m1
	 *  is less than or equal to the epsilon parameter, otherwise returns false.
	 *  The L-infinite distance is equal to MAX[i=0,1,2, . . .n ; j=0,1,2, . . .n ;
	 *  abs(this.m(i,j) - m1.m(i,j)] .
	 *
	 * @param  m1       The matrix to be compared to this matrix
	 * @param  epsilon  the threshold value
	 * @return          Description of the Return Value
	 */
	public final boolean epsilonEquals(final Matrix m1, final double epsilon) {
		if(m1 == this) {
			return true;
		}
		if(m1.getRowCount() != this.getRowCount() || m1.getColumnCount() != this.getColumnCount()) {
			return false;
		}
		final int row_cnt = getRowCount();
		final int col_cnt = getColumnCount();
		for(int r = 0; r < row_cnt; r++) {
			for(int c = 0; c < col_cnt; c++) {
				if(epsilon <
						Math.abs(elementData[r * col_cnt + c] - m1.getElement(r, c))) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 *  Returns the trace of this matrix.
	 *
	 * @return    the trace of this matrix.
	 */
	public final double trace() {
		int min = rowCount < columnCount ? rowCount : columnCount;
		double trace = 0.0;
		for(int i = 0; i < min; i++) {
			trace += elementData[i * columnCount + i];
		}
		return trace;
	}

	protected final double getDiag(final int i) {
		return elementData[i * columnCount + i];
	}

	protected final double dpythag(final float a, final float b) {
		double absa = Math.abs(a);
		double absb = Math.abs(b);
		if(absa > absb) {
			if(absa == 0.0) {
				return 0.0;
			}
			double term = absb / absa;
			if(Math.abs(term) <= Double.MIN_VALUE) {
				return absa;
			}
			return (absa * Math.sqrt(1.0 + term * term));
		} else {
			if(absb == 0.0) {
				return 0.0;
			}
			double term = absa / absb;
			if(Math.abs(term) <= Double.MIN_VALUE) {
				return absb;
			}
			return (absb * Math.sqrt(1.0 + term * term));
		}
	}

	/**
	 *  Siginifcance by row
	 *
	 * @param  level  Description of the Parameter
	 * @return        Description of the Return Value
	 * @todo          signfic needs review by pt
	 */
	public final FloatVector sigByRow(final double level) {
		FloatVector v = new FloatVector(rowCount);
		for(int r = 0; r < rowCount; r++) {
			v.setElement(r, this.getRowv(r).sig(level));
		}
		return v;
	}

	public final FloatVector sigByCol(final double level) {
		FloatVector v = new FloatVector(columnCount);
		for(int c = 0; c < columnCount; c++) {
			v.setElement(c, this.getColumnv(c).sig(level));
		}
		return v;
	}

	/**
	 *  (the first max value) max element in the entire matrix absolute -> entri
	 *  matrix else by row.
	 *
	 * @return    Description of the Return Value
	 */
	public final Matrix.Cell max() {
		Matrix.Cell max = new Matrix.Cell();
		max.value = Float.MIN_VALUE;
		for(int r = 0; r < rowCount; r++) {
			for(int c = 0; c < columnCount; c++) {
				if(elementData[r * columnCount + c] > max.value) {
					max.row = r;
					max.col = c;
					max.value = elementData[r * columnCount + c];
				}
			}
		}
		return max;
	}

	/**
	 *  the global first min value
	 *
	 * @return    Description of the Return Value
	 */
	public final Matrix.Cell min() {
		final Matrix.Cell min = new Matrix.Cell();
		min.value = Float.MAX_VALUE;
		for(int i = 0; i < rowCount; i++) {
			for(int j = 0; j < columnCount; j++) {
				if(elementData[i * columnCount + j] < min.value) {
					min.row = i;
					min.col = j;
					min.value = elementData[i * columnCount + j];
				}
			}
		}
		return min;
	}

	/**
	 *  the min value by row for each row
	 *
	 * @return    Description of the Return Value
	 */
	public Matrix.Cell[] minByRow() {
		final int r_cnt = rowCount;
		final int c_cnt = columnCount;
		final Matrix.Cell[] mins = new Matrix.Cell[r_cnt];
		for(int r = 0; r < r_cnt; r++) {
			mins[r] = new Matrix.Cell();
			mins[r].value = Float.MAX_VALUE;
			for(int c = 0; c < c_cnt; c++) {
				final int index = getIndex(r, c);
				if(elementData[index] < mins[r].value) {
					mins[r].row = r;
					mins[r].col = c;
					mins[r].value = elementData[index];
				}
			}
		}
		return mins;
	}

	/**
	 *  much more efficent than getting each row and calc'ing max
	 *
	 * @return    Description of the Return Value
	 */
	public final Matrix.Cell[] maxByRow() {
		final Matrix.Cell[] maxes = new Matrix.Cell[rowCount];
		for(int r = 0; r < rowCount; r++) {
			maxes[r] = new Matrix.Cell();
			maxes[r].value = Float.MIN_VALUE;
			for(int c = 0; c < columnCount; c++) {
				if(elementData[r * columnCount + c] > maxes[r].value) {
					maxes[r].row = r;
					maxes[r].col = c;
					maxes[r].value = elementData[r * columnCount + c];
				}
			}
		}
		return maxes;
	}

	/**
	 *  absolute binning --- entrie matrix adds to the specified ranges.
	 *
	 * @param  ranges  Description of the Parameter
	 */
	public final void bin(final Range[] ranges) {
		for(int i = 0; i < rowCount * columnCount; i++) {
			for(int j = 0; j < ranges.length; j++) {
				if(ranges[j].isMember(elementData[i])) {
					ranges[j].increment();
				}
			}
		}
	}

	public final void bin(final java.util.List ranges) {
		for(int i = 0; i < rowCount * columnCount; i++) {
			for(int j = 0; j < ranges.size(); j++) {
				Range range = (Range) ranges.get(j);
				if(range.isMember(elementData[i])) {
					range.increment();
				}
			}
		}
	}

	/**
	 *  Discretize the matrix into the specified ranges.
	 *
	 * @param  ranges  Description of the Parameter
	 * @return         A whole new discretized matrix. The floats are not shared.
	 *      Matrix will contain value from 0 to ranges-1
	 */
	// More efficient to create a new matrix here (note diff with the other discz api)
	// Iterate through matrix assigning each element to one and only one range.
	// Unlike binning, the number of discretized(binned) elements must exactly
	// equal the numb of elements in the matrix.
	public final Matrix discretize(final Range[] ranges) {

		FloatMatrix newmatrix = new FloatMatrix(getRowCount(), getColumnCount());
		for(int i = 0; i < rowCount * columnCount; i++) {
			for(int j = 0; j < ranges.length; j++) {
				if(ranges[j].isMember(elementData[i])) {// checking this matrix
					newmatrix.elementData[i] = j;// but assigning to new matrix
					break;
				}
			}
		}

		return newmatrix;
	}

	/**
	 *  equalk numbf of lements actually equal numb of DIFF(float) elements.
	 *  changes the matrix "in place" matrix must be not be immutable for this to
	 *  work each disc has classdensity elements Sort the array Make it unique Now,
	 *  the number of ranges = uniq.length / classdensity Create the ranges Iterate
	 *  through untouched (unsorted, ununiqueized) array and file ranges.
	 *
	 * @param  classdensity  Description of the Parameter
	 * @return               Description of the Return Value
	 */
	public final Matrix discretize(final int classdensity) {

		FloatMatrix newmatrix = new FloatMatrix(this);

//		log.debug("Starting uniq for discretize");
		float[] uniq = ArrayUtils.unique(newmatrix.elementData);
		//	log.debug("Done uniq for discretize");
		Arrays.sort(uniq);
		//	log.debug("Done sort for discretize");

		Range[] ranges = Range.createRanges(classdensity, uniq);

//		log.debug("Number of ranges = " + ranges.length);

		for(int i = 0; i < rowCount * columnCount; i++) {
			for(int j = 0; j < ranges.length; j++) {
				if(ranges[j].isMember(this.elementData[i])) {
					newmatrix.elementData[i] = j;
					break;
				}
			}
		}

		return newmatrix;
	}

	// I N N E R    C L A S S E S

	/**
	 *  Represents a single matrix cell.
	 *
	 * @author     jgould
	 * @created    August 4, 2003
	 */
	public static class Cell implements Serializable {
		private final static long serialVersionUID = 991018889913842280L;
		public int row;
		public int col;
		public float value;
	}// End Cell

}

