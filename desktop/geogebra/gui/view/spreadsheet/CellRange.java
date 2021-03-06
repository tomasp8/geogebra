package geogebra.gui.view.spreadsheet;


import geogebra.common.awt.Point;
import geogebra.common.kernel.geos.GeoElement;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Utility class for spreadsheet cell ranges.
 *
 *
 * A cell range is any rectangular block of cells defined by two
 * diagonal corner cells. One corner is designated as the anchor 
 * cell. This is the cell first clicked when dragging out a cell range. 
 * Once the corner index values are set the max and min corner
 * index values are calculated as follows:
 *  
 *        minColumn   
 *    minRow *---------*
 *           |         |
 *           *-------- * maxRow
 *                 maxColumn      
 *  
 * Rows and columns have index values of -1.
 * 
 *      row:     minRow >=0, minColumn = -1, maxColumn = -1  
 *   column:  minColumn >=0, minRow = -1, maxRow = -1  
 * 
 * 
 * @author George Sturr, 2010-1-23
 */


public class CellRange implements Cloneable{
	
	private int minColumn = -1;
	private int minRow = -1;
	private int maxColumn = -1;
	private int maxRow = -1;
	private int anchorColumn = -1;
	private int anchorRow = -1;
	
	
	private MyTable table;

	
	
	/** Create new CellRange */
	public CellRange(MyTable table) {
		this.table = table;
	}

	public CellRange(MyTable table, int anchorColumn, int anchorRow, int col2, int row2) {
		this.table = table;
		setCellRange( anchorColumn, anchorRow, col2, row2);

	}

	/** Construct CellRange for single cell */
	public CellRange(MyTable table, int anchorColumn, int anchorRow) {
		this.table = table;
		setCellRange( anchorColumn, anchorRow, anchorColumn, anchorRow);

	}
	// TODO Constructor with string parameter, e.g. CellRange("A1:B10")

	
	
	/** Set cell range for a single cell */
	public void setCellRange(int anchorColumn, int anchorRow) {
		setCellRange(anchorColumn, anchorRow,anchorColumn, anchorRow);	
	}

	/**
	 * Set a cell range using diagonal corner cells. Can be any two diagonal
	 * corners in either order.
	 */
	public void setCellRange(int anchorColumn, int anchorRow, int col2, int row2) {
		
		minColumn = Math.min(anchorColumn, col2);
		maxColumn = Math.max(anchorColumn, col2);
		minRow = Math.min(anchorRow, row2);
		maxRow = Math.max(anchorRow, row2);
		
		this.anchorColumn = anchorColumn;
		this.anchorRow = anchorRow;
		
	}

	public int getMinColumn() {
		return minColumn;
	}

	public int getMinRow() {
		return minRow;
	}

	public int getMaxColumn() {
		return maxColumn;
	}

	public int getMaxRow() {
		return maxRow;
	}
	
	
	public boolean isSingleCell() {
		return ((maxColumn - minColumn == 0) && (maxRow - minRow == 0));
	}
	
	public boolean isColumn() {
		return (anchorRow == -1);
	}

	public boolean isRow() {
		return (anchorColumn == -1);
	}
	
	// TODO -- refactor this name, should mean has either exactly 2 rows or exactly 2 columns
	/**
	 * Returns true if cell range is 2xn or nx2
	 */
	public boolean is2D() {
		return (maxColumn - minColumn == 1) || (maxRow - minRow == 1);
	}

	/**
	 * Returns true if cell range is 1xn, nx1, a row or a column
	 */
	public boolean is1D() {
		return ((maxColumn - minColumn == 0) || (maxRow - minRow == 0));
	}
	
	public boolean isPartialRow() {
		return !isSingleCell() && !isRow() && (maxRow - minRow == 0);
	}
	
	public boolean isPartialColumn() {
		return !isSingleCell() && !isColumn() && (maxColumn - minColumn == 0);
	}
	
	/** isEmpty = cell range contains no geos   */
	public boolean isEmpty() {
		return toGeoList().size() == 0;
	}

	/** isEmptyRange = the range contains no cells   */
	public boolean isEmptyRange(){
		return (minColumn == -1 && maxColumn == -1 && minRow == -1 && maxRow == -1) ;
	}
	
	
	/**
	 * Returns true if all non-empty cells in the given range are GeoPoint 
	 */
	public boolean isPointList() {
		for (int col = minColumn; col <= maxColumn; ++col) {
			for (int row = minRow; row <= maxRow; ++row) {
				GeoElement geo = RelativeCopy.getValue(table, col, row);
				
				if (geo != null && !geo.isGeoPoint())
					return false;
			}
		}
		return true;
	}
	
	
	
	/**
	 * Returns a cell range that holds the actual cell range 
	 * of an input row or column.
	 * e.g. (-1,1,-1,4) ---> (0,1,100,4)
	 */
	public CellRange getActualRange(CellRange cr) {

		CellRange adjustedCellRange = cr.clone();

		if (cr.minRow == -1 && cr.maxRow == -1 && cr.minColumn != -1) {
			adjustedCellRange.minRow = 0;
			adjustedCellRange.maxRow = table.getRowCount() - 1;
		}

		if (cr.minColumn == -1 && cr.maxColumn == -1 && cr.minRow != -1) {
			adjustedCellRange.minColumn = 0;
			adjustedCellRange.maxColumn = table.getColumnCount() - 1;
		}

		return adjustedCellRange;
	}
	
	
	/**
	 * Sets the corners of a row or column to the actual cell range 
	 * e.g. (-1,1,-1,4) ---> (0,1,100,4)
	 */
	public void setActualRange() {
		
		if (minRow == -1 && maxRow == -1 && minColumn == -1 && maxColumn == -1 )
			return;
		
		if (minRow == -1 && maxRow == -1 ) {
			minRow = 0;
			maxRow = table.getRowCount() - 1;
		}

		if (minColumn == -1 && maxColumn == -1 ) {
			minColumn = 0;
			maxColumn = table.getColumnCount() - 1;
		}

	}
	
	
	public int getWidth(){
		return  maxColumn - minColumn + 1;		
	}
	
	public int getHeight(){
		return maxRow - minRow + 1;		
	}
	
	public Rectangle getRect(){
		return new Rectangle(minRow,minColumn,getHeight(), getWidth());
	}
	

	
	
	
	/**
	 * ArrayList of all geos found in the cell range 
	 */
	public ArrayList<GeoElement> toGeoList() {

		ArrayList<GeoElement> list = new ArrayList();
		
		for (int col = minColumn; col <= maxColumn; ++col) {
			for (int row = minRow; row <= maxRow; ++row) {
				GeoElement geo = RelativeCopy.getValue(table, col, row);
				if (geo != null){
					list.add(geo);
				}
			}
		}
		return list;
	}
		
	/**
	 * ArrayList of labels for each geo found in the cell range
	 */
	public ArrayList toGeoLabelList(boolean scanByColumn, boolean copyByValue) {

		ArrayList list = new ArrayList();

		if (scanByColumn) { 
			for (int col = minColumn; col <= maxColumn; ++col) {
				for (int row = minRow; row <= maxRow; ++row) {
					GeoElement geo = RelativeCopy.getValue(table, col, row);
					if (geo != null){
						if (copyByValue)
							list.add(geo.getValueForInputBar());
						else
							list.add(geo.getLabel());
					}
				}
			}
		} else {
			for (int row = minRow; row <= maxRow; ++row) {
				for (int col = minColumn; col <= maxColumn; ++col) {
					GeoElement geo = RelativeCopy.getValue(table, col, row);
					if (geo != null)
						if (copyByValue)
							list.add(geo.getValueForInputBar());
						else
							list.add(geo.getLabel());
				}
			}
		}

		return list;
	}
	
	/**
	 * ArrayList of all cells found in the cell range 
	 */
	public ArrayList<Point> toCellList(boolean scanByColumn) {

		ArrayList<Point> list = new ArrayList<Point>();
		if (scanByColumn) {
			for (int col = minColumn; col <= maxColumn; ++col) {
				for (int row = minRow; row <= maxRow; ++row) {
					list.add(new Point(col, row));
				}
			}
		} else {
			for (int row = minRow; row <= maxRow; ++row) {
				for (int col = minColumn; col <= maxColumn; ++col) {
					list.add(new Point(col, row));
				}
			}
		}
		
		return list;
	}
	
	
	
	
	
	public boolean hasSameAnchor(CellRange cr){
		return (cr.anchorRow == anchorRow) && (cr.anchorColumn == anchorColumn);
	}
	
	/** Returns true if at least one cell is empty (has no geo)  */
	public boolean hasEmptyCells(){
		boolean hasEmptyCells = false;
		for (int col = minColumn; col <= maxColumn; ++col) {
			for (int row = minRow; row <= maxRow; ++row) {
				GeoElement geo = RelativeCopy.getValue(table, col, row);
				if (geo == null){
					return true;
				}
			}
		}
		
		return hasEmptyCells;
	}
	
	/** Returns true if the cell range has valid coordinates for this table
	 */
	public boolean isValid(){
		return (minRow >= -1 && minRow < table.getView().MAX_ROWS)
		&& (maxRow >= -1 && maxRow < table.getView().MAX_ROWS)
		&& (minColumn >= -1 && minColumn < table.getView().MAX_COLUMNS)
		&& (maxColumn >= -1 && maxColumn < table.getView().MAX_COLUMNS);
	}
	
	
	
	
	
	public CellRange clone(){
		return new CellRange(table, minColumn,minRow,maxColumn,maxRow);
	}
	
	public boolean equals(Object obj) {
		CellRange cr;
		if (obj instanceof CellRange) {
			cr = (CellRange) obj;
			return (cr.minColumn == minColumn && cr.minRow == minRow
					&& cr.maxColumn == maxColumn && cr.maxRow == maxRow);
		} else
			return false;
	}
	
	
	public boolean contains(Object obj) {
		
		CellRange cr;
		if (obj instanceof CellRange) {
			cr = (CellRange) obj;
			return (this.toCellList(true).containsAll(cr.toCellList(true)));

		} else if (obj instanceof GeoElement){	
			Point location = ((GeoElement) obj).getSpreadsheetCoords();
			// if the geo is a cell then test if inside the cell range
			if(location != null && location.x < SpreadsheetView.MAX_COLUMNS && location.y < SpreadsheetView.MAX_ROWS){
				setActualRange();
				return (location.y >= minRow && location.y <= maxRow 
						&& location.x >= minColumn && location.x <= maxColumn);
			}
		}
		
		return false;
	}

	/**
	 * Prints debugging information about the cell range
	 */
	public void debug(){
		System.out.println("anchor cell:  (" + anchorColumn + "," + anchorRow + ")" );
		System.out.println("corner cells: (" + minColumn + "," + minRow + ")  (" + maxColumn + "," + maxRow + ")"  );
		System.out.println("isRow: " + isRow()  );
		System.out.println("isColumn: " + isColumn()  );
	}

	
	
	
	
}