package geogebra.kernel.commands;

import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoFunction;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;

/*
 * IterationList[ <function>, <start>, <n> ]
 */
public class CmdIterationList extends CommandProcessorDesktop {
	
	public CmdIterationList(Kernel kernel) {
		super(kernel);
	}

	
final public  GeoElement[] process(Command c) throws MyError {
    int n = c.getArgumentNumber();
    boolean[] ok = new boolean[n]; 
    GeoElement[] arg;
    
    switch (n) {    	
    	case 3 :
    		arg = resArgs(c);
            if ((ok[0] = arg[0].isGeoFunction())
               	 && (ok[1] = arg[1].isNumberValue())
               	 && (ok[2] = arg[2].isNumberValue()))
               {
            	GeoElement[] ret = {  kernel.IterationList(
                                c.getLabel(),
                                (GeoFunction) arg[0],
                                (NumberValue) arg[1],
                                (NumberValue) arg[2]) };
                   return ret; 
               } else {          
               	for (int i=0; i < n; i++) {
               		if (!ok[i]) throw argErr(app, c.getName(), arg[i]);	
               	}            	
               }                   		    		     

        default :
            throw argNumErr(app, c.getName(), n);
    }
}
}