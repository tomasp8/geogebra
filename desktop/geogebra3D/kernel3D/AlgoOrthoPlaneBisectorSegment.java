/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/


package geogebra3D.kernel3D;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.Matrix.CoordMatrix4x4;
import geogebra.common.kernel.Matrix.CoordSys;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoLineND;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.kernel.kernelND.GeoSegmentND;
import geogebra.kernel.Kernel;
import geogebra.main.Application;


/**
 * Compute a plane through a point and orthogonal to a line (or segment, ...)
 *
 * @author  matthieu
 * @version 
 */
public class AlgoOrthoPlaneBisectorSegment extends AlgoOrthoPlane {

 
	private GeoSegmentND segment; // input


    public AlgoOrthoPlaneBisectorSegment(Construction cons, String label, GeoSegmentND segment) {
        super(cons);
        this.segment = segment;
        
        setInputOutput(new GeoElement[] {(GeoElement) segment}, new GeoElement[] {getPlane()});

        // compute plane 
        compute();
        getPlane().setLabel(label);
    }

    public String getClassName() {
        return "AlgoPlaneBisector";
    }




    protected Coords getNormal(){
    	return ((GeoElement) segment).getMainDirection();
    }

    protected Coords getPoint(){
    	return segment.getPointInD(3, 0.5);
    }

}
