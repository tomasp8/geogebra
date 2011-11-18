/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * AlgoOrthoVectorLine.java
 *
 * Created on 30. August 2001, 21:37
 */

package geogebra.kernel.algos;

import geogebra.kernel.CircularDefinitionException;
import geogebra.kernel.Construction;
import geogebra.kernel.geos.GeoElement;
import geogebra.kernel.geos.GeoLine;
import geogebra.kernel.geos.GeoPoint;
import geogebra.kernel.geos.GeoVec2D;
import geogebra.kernel.geos.GeoVector;


/**
 *
 * @author  Markus
 * @version 
 */
public class AlgoUnitVectorLine extends AlgoElement {
    
    private GeoLine g; // input
    private GeoVector  u;     // output       
    
    private double length; 
        
    /** Creates new AlgoOrthoVectorLine */
    public AlgoUnitVectorLine(Construction cons, String label,GeoLine g) {        
        super(cons);
        this.g = g;                
        u = new GeoVector(cons); 
       
        GeoPoint possStartPoint = g.getStartPoint();
        if (possStartPoint != null && possStartPoint.isLabelSet()) {
	        try{
	            u.setStartPoint(possStartPoint);
	        } catch (CircularDefinitionException e) {}
        }
        
        setInputOutput(); // for AlgoElement
        
        // compute line through P, Q
        u.z = 0.0d;
        compute();      
        u.setLabel(label);
    }   
    
    @Override
	public String getClassName() {
        return "AlgoUnitVectorLine";
    }
    
    // for AlgoElement
    @Override
	protected void setInputOutput() {
        input = new GeoElement[1];        
        input[0] = g;
             
        super.setOutputLength(1);
        super.setOutput(0, u);
        setDependencies(); // done by AlgoElement
    }    
    
    public GeoVector getVector() { return u; }    
    GeoLine getg() { return g; }
    
    // line through P normal to v
    @Override
	public final void compute() {        
        length = GeoVec2D.length(g.x, g.y);
        u.x = g.y / length;
        u.y = -g.x / length;        
    }   
    
    @Override
	final public String toString() {
        // Michael Borcherds 2008-03-31
        // simplified to allow better translation
        return app.getPlain("UnitVectorOfA",g.getLabel());
    }
}