/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.kernel.algos;

import geogebra.kernel.Kernel;
import geogebra.kernel.geos.GeoElementSpreadsheet;

import geogebra.common.awt.Point;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.algos.AlgoDependentList;
import geogebra.common.kernel.algos.AlgoElement;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;

import java.util.ArrayList;

/**
 * Algorithm to create a GeoList with GeoElement objects of a given range 
 * in GeoGebra's spreadsheet. For example, CellRange[A1, B2] (or A1:B2)
 * returns the list {A1, B1, A2, B2}.
 *
 * @author  Markus Hohenwarter
 * @date 29.06.2008
 */
public class AlgoCellRange extends AlgoElement {

	private GeoList geoList;     // output list of range    
    private GeoElement startCell, endCell; // input cells
    private String toStringOutput;
           
    /**
     * Creates an algorithm that produces a list of GeoElements for a range of cells in the spreadsheet.
     * @param startCell e.g. A1
     * @param endCell e.g. B2
     */
    public AlgoCellRange(Construction cons, String label, GeoElement startCell, GeoElement endCell) {    	    
    	super(cons);    
    	this.startCell = startCell;
    	this.endCell = endCell;    	            	    	    
    	
        setInputOutput(); 
              
        geoList.setLabel(label);    	
    }   
    
	@Override
	public String getClassName() {
		return "Expression";
	}
	
	@Override
	public void remove() {      
       super.remove();                                     
       clearGeoList();    
    }   
	
	private void clearGeoList() {
		 // remove this algorithm as cell range user to allow renaming again        
        for (int i = 0; i < geoList.size(); i++) {
            geoList.get(i).removeCellRangeUser();
        }   
        
        geoList.clear();
	}
        
	// for AlgoElement
	@Override
	protected void setInputOutput() {
		// TODO: change to support $A1, just get the spreadsheet coords based on label
		
		// get range: cell coordinates of range in spreadsheet
		String startLabel = startCell.getLabel();		
		String endLabel = endCell.getLabel();
		GeoElementSpreadsheet geoElementSpreadsheet = (GeoElementSpreadsheet)kernel.getGeoElementSpreadsheet();
    	Point startCoords = geoElementSpreadsheet.dogetSpreadsheetCoordsForLabel(startLabel);
    	Point endCoords = geoElementSpreadsheet.dogetSpreadsheetCoordsForLabel(endLabel);
    	toStringOutput = startLabel + ":" + endLabel;
    	
    	// build list with cells in range
    	ArrayList<GeoElement> listItems = initCellRangeList(startCoords, endCoords);   
    	
    	// create dependent geoList for cells in range
    	AlgoDependentList algo = new AlgoDependentList((Construction)cons, listItems, true);
    	((Construction) cons).removeFromConstructionList(algo);
        geoList = algo.getGeoList();
		
		
		// input: start and end cell
		// needed for XML saving only
        input = algo.input;
		
        super.setOutputLength(1);
        super.setOutput(0, geoList);
        
        setDependencies();    
        
//        // change input now for XML saving    
//        input = new GeoElement[2]; 
//        input[0] = startCell;
//      	input[1] = endCell; 	        
    }    
    
    /**
     * Builds geoList with current objects in range of spreadsheet. Renaming 
     * of all cells added to the geoList is turned off, otherwise the user could
     * move an object out of the range by renaming it.
     * 
     * @param startCoords
     * @param endCoords
     */
    private ArrayList<GeoElement> initCellRangeList(Point startCoords, Point endCoords) { 
    	ArrayList<GeoElement> listItems = new ArrayList<GeoElement>();   	    					
    	
    	// check if we have valid spreadsheet coordinates
    	boolean validRange = startCoords != null && endCoords != null;
    	if (!validRange) {    		
    		return listItems;
    	}    		
    	
    	// min and max column and row of range
    	int minCol = Math.min(startCoords.x, endCoords.x);    	
    	int maxCol = Math.max(startCoords.x, endCoords.x);
    	int minRow = Math.min(startCoords.y, endCoords.y);
    	int maxRow = Math.max(startCoords.y, endCoords.y);
        	
    	// build the list
    	for (int colIndex = minCol; colIndex <= maxCol; colIndex++) {
    		for (int rowIndex = minRow; rowIndex <= maxRow; rowIndex++) {
    			// get cell object for col, row
    			String cellLabel = GeoElementSpreadsheet.getSpreadsheetCellName(colIndex, rowIndex);    			
    			GeoElement geo = ((Kernel)kernel).lookupLabel(cellLabel);
    			
    			// create missing object in cell range
    			if (geo == null) {
    				geo = ((Construction)cons).createSpreadsheetGeoElement(startCell, cellLabel);    				
    			}
    			
    			// we got the cell object, add it to the list
    			listItems.add(geo);
    			
    			// make sure that this cell object cannot be renamed by the user
    			// renaming would move the object outside of our range
    			geo.addCellRangeUser();
       		}    		
    	}    
    	
    	return listItems; 
    }        
    
    public GeoList getList() { 
    	return geoList; 
    }       
    
        
    @Override
	public final void compute() {    	    	
    	// nothing to do in compute, update is simply passed on to dependent algos
    }   
    
    @Override
	final public String getCommandDescription() {
    	return toStringOutput;
    }
    
    @Override
	final public String toString() {
    	return toStringOutput;
    }        
    
    public Point[] getRectangle() {
		String startLabel = startCell.getLabel();		
		String endLabel = endCell.getLabel();
    	Point startCoords = GeoElementSpreadsheet.getSpreadsheetCoordsForLabel(startLabel);
    	Point endCoords = GeoElementSpreadsheet.getSpreadsheetCoordsForLabel(endLabel);

    	Point[] ret = {startCoords, endCoords};
    	return ret;  	
    }
    
}
