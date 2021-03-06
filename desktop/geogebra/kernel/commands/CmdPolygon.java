package geogebra.kernel.commands;


import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.kernel.geos.GeoPoint2;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;


/*
 * Polygon[ <GeoPoint>, ..., <GeoPoint> ]
 * Polygon[ <GeoPoint>, <GeoPoint>, <Number>] for regular polygon
 */
public class CmdPolygon extends CommandProcessorDesktop {
	
	public CmdPolygon(Kernel kernel) {
		super(kernel);
	}
	
public GeoElement[] process(Command c) throws MyError {
    int n = c.getArgumentNumber();
    GeoElement[] arg;

    arg = resArgs(c);
    switch (n) {
	    case 0 :
	    	throw argNumErr(app, c.getName(), n);
    	//G.Sturr 2010-3-14
		case 1:
		if (arg[0].isGeoList())
			return kernel.Polygon(c.getLabels(), (GeoList) arg[0]);
		//END G.Sturr
		
    	case 3:        
        // regular polygon
        if (arg[0].isGeoPoint() && 
	        arg[1].isGeoPoint() &&
	        arg[2].isNumberValue())
				return kernel.RegularPolygon(c.getLabels(), (GeoPoint2) arg[0], (GeoPoint2) arg[1], (NumberValue) arg[2]);		
        
        default:
			// polygon for given points
	        GeoPoint2[] points = new GeoPoint2[n];
	        // check arguments
	        for (int i = 0; i < n; i++) {
	            if (!(arg[i].isGeoPoint()))
					throw argErr(app, c.getName(), arg[i]);
				else {
	                points[i] = (GeoPoint2) arg[i];
	            }
	        }
	        // everything ok
	        return kernel.Polygon(c.getLabels(), points);
		}	
}
}
