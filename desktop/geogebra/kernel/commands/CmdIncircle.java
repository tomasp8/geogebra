package geogebra.kernel.commands;

import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoPoint2;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;

/*
 * Incircle[ <GeoPoint>, <GeoPoint>, <GeoPoint> ]
 * dsun [6/26/2011]
 */
class CmdIncircle extends CommandProcessorDesktop {
    public CmdIncircle(Kernel kernel) {
	super(kernel);
    }
    public GeoElement[] process(Command c) throws MyError {
	int n = c.getArgumentNumber();
	boolean[] ok = new boolean[n];
	GeoElement[] arg;
	switch (n) {
	case 3 :
	    arg = resArgs(c);
	    if ((ok[0] = (arg[0] .isGeoPoint()))
		&& (ok[1] = (arg[1] .isGeoPoint()))
		&& (ok[2] = (arg[2] .isGeoPoint()))) {
		GeoElement[] ret =
		{
		    kernel.Incircle(
			c.getLabel(),
			(GeoPoint2) arg[0],
			(GeoPoint2) arg[1],
			(GeoPoint2) arg[2])};
		return ret;
	    } else {
		if (!ok[0])
		    throw argErr(app, "Incircle", arg[0]);
		else if (!ok[1])
		    throw argErr(app, "Incircle", arg[1]);
		else
		    throw argErr(app, "Incircle", arg[2]);
	    }
	default :
	    throw argNumErr(app, "Incircle", n);
	}
    }
} // CmdIncircle
