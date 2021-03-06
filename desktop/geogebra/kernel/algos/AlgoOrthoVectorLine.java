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

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.CircularDefinitionException;
import geogebra.common.kernel.algos.AlgoElement;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoLine;
import geogebra.common.kernel.geos.GeoPoint2;
import geogebra.common.kernel.geos.GeoVector;


/**
 *
 * @author  Markus
 * @version 
 */
public class AlgoOrthoVectorLine extends AlgoElement {
    
    private GeoLine g; // input
    private GeoVector  n;     // output       
    
    //private GeoPoint startPoint;
        
    /** Creates new AlgoOrthoVectorLine */
    public AlgoOrthoVectorLine(Construction cons, String label, GeoLine g) {        
        super(cons);
        this.g = g;                
        n = new GeoVector(cons); 

        GeoPoint2 possStartPoint = g.getStartPoint();
        if (possStartPoint != null && possStartPoint.isLabelSet()) {
	        try{
	            n.setStartPoint(possStartPoint);
	        } catch (CircularDefinitionException e) {}
        }
        
        setInputOutput(); // for AlgoElement
        
        // compute line through P, Q
        n.z = 0.0d;
        compute();      
        n.setLabel(label);
    }   
    
    @Override
	public String getClassName() {
        return "AlgoOrthoVectorLine";
    }
    
    @Override
	public int getRelatedModeID() {
    	return EuclidianConstants.MODE_ORTHOGONAL;
    }
      
    // for AlgoElement
    @Override
	protected void setInputOutput() {
        input = new GeoElement[1];        
        input[0] = g;

        super.setOutputLength(1);
        super.setOutput(0, n);
        setDependencies(); // done by AlgoElement
    }    
    
    public GeoVector getVector() { return n; }    
    GeoLine getg() { return g; }
    
    // line through P normal to v
    @Override
	public final void compute() {        
        n.x = g.x;
        n.y = g.y;        
    }       

    @Override
	final public String toString() {
        // Michael Borcherds 2008-03-30
        // simplified to allow better Chinese translation
        return app.getPlain("VectorPerpendicularToA",g.getLabel());

    }
}
