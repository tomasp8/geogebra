/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * AlgoEccentricity.java
 *
 */

package geogebra.kernel.algos;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.algos.AlgoElement;
import geogebra.common.kernel.geos.GeoConic;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoNumeric;

/**
 *
 * @author  Michael
 * @version 
 */
public class AlgoEccentricity extends AlgoElement {

    private GeoConic c; // input
    private GeoNumeric num; // output                  

    public AlgoEccentricity(Construction cons, String label, GeoConic c) {
        super(cons);
        this.c = c;
        num = new GeoNumeric(cons);
        setInputOutput(); // for AlgoElement                
        compute();
        num.setLabel(label);
    }

    @Override
	public String getClassName() {
        return "AlgoEccentricity";
    }

    // for AlgoElement
    @Override
	protected void setInputOutput() {
        input = new GeoElement[1];
        input[0] = c;

        super.setOutputLength(1);
        super.setOutput(0, num);
        setDependencies(); // done by AlgoElement
    }

    public GeoNumeric getEccentricity() {
        return num;
    }
    GeoConic getConic() {
        return c;
    }

    // set excentricity
    @Override
	public final void compute() {
        switch (c.type) {
        case GeoConic.CONIC_CIRCLE :
            num.setValue(0.0);
            break;

        case GeoConic.CONIC_PARABOLA :
            num.setValue(1.0);
            break;

            case GeoConic.CONIC_HYPERBOLA :
            case GeoConic.CONIC_ELLIPSE :
                num.setValue(c.eccentricity);
                break;

            default :
                num.setUndefined();
        }
    }

    @Override
	final public String toString() {
        // Michael Borcherds 2008-03-30
        // simplified to allow better Chinese translation
    	return app.getPlain("EccentricityOfA",c.getLabel());
    }
}
