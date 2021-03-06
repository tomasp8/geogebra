package geogebra.kernel.commands; 
/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/
import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoFunction;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;

/** 
 * Fit[<List Points>,<List of Functions>]  (linear combination)
 * Fit[<List Points>, <Function>] (nonlinear with gliders as startvalues)
 * @author Hans-Petter Ulven
 * @version 2011-03-15
 */
public class CmdFit extends CommandProcessorDesktop{

    public CmdFit(Kernel kernel) {super(kernel);}
    
    public GeoElement[] process(Command c) throws MyError {
        int n=c.getArgumentNumber();
        GeoElement[] arg=resArgs(c);;
        switch(n) {
            case 2:
                    if(  (arg[0].isGeoList() )&& (arg[1].isGeoList())  ){ 
                        GeoElement[] ret={kernel.Fit(c.getLabel(),(GeoList)arg[0],(GeoList) arg[1]) };
                        return ret;
                    }else if(  (arg[0].isGeoList() )&& (arg[1].isGeoFunction())  ){
                    	GeoElement[] ret={kernel.Fit(c.getLabel(),(GeoList)arg[0],(GeoFunction) arg[1])  };
                    	return ret;
                    }else{
                        throw argErr(app,c.getName(),arg[0]);
                    }//if arg[0] is GeoList 

            default :

    			throw argNumErr(app, c.getName(), n);
        }//switch(number of arguments)
    }//process(Command) 
}// class CmdFit
