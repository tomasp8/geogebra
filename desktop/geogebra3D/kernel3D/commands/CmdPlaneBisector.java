package geogebra3D.kernel3D.commands;

import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.commands.CommandProcessor;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoCoordSys;
import geogebra.common.kernel.kernelND.GeoLineND;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.kernel.kernelND.GeoSegmentND;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;
import geogebra.kernel.commands.CommandProcessorDesktop;
import geogebra3D.kernel3D.GeoCoordSys1D;
import geogebra3D.kernel3D.GeoPlane3D;
import geogebra3D.kernel3D.GeoPoint3D;
import geogebra3D.kernel3D.Kernel3D;

/*
 * Orthogonal[ <GeoPoint3D>, <GeoCoordSys> ]
 */
public class CmdPlaneBisector extends CommandProcessorDesktop {
	
	
	
	public CmdPlaneBisector(Kernel kernel) {
		super(kernel);
	}
	
	

	public GeoElement[] process(Command c) throws MyError {
	    int n = c.getArgumentNumber();
	    boolean[] ok = new boolean[n];
	    GeoElement[] arg;

	    switch (n) {
	    case 1 :
	    	arg = resArgs(c);
	    	if (
	    			(ok[0] = (arg[0] instanceof GeoSegmentND ) )
	    			
	    	) {
	    		GeoElement[] ret =
	    		{
	    				(GeoElement) kernel.getManager3D().PlaneBisector(
	    						c.getLabel(),
	    						(GeoSegmentND) arg[0])};
	    		return ret;
	    	}else{
	    		throw argErr(app, "PlaneBisector", arg[0]);
	    	}

	    case 2 :
	    	arg = resArgs(c);
	    	if (
	    			(ok[0] = (arg[0] .isGeoPoint() ) )
	    			&& (ok[1] = (arg[1] .isGeoPoint() ))
	    	) {
	    		GeoElement[] ret =
	    		{
	    				(GeoElement) kernel.getManager3D().PlaneBisector(
	    						c.getLabel(),
	    						(GeoPointND) arg[0],
	    						(GeoPointND) arg[1])};
	    		return ret;
	    	}else{
	    		if (!ok[0])
	    			throw argErr(app, "PlaneBisector", arg[0]);
	    		else 
	    			throw argErr(app, "PlaneBisector", arg[1]);
	    	}
	    	
	    default :
	    	throw argNumErr(app, "PlaneBisector", n);
	    }
	    

	}
	
}
