package geogebra.kernel.commands;

import geogebra.common.kernel.CircularDefinitionException;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.kernel.geos.GeoNumeric;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;

/*
 * Product[ list ]
 * adapted from CmdSum by Michael Borcherds 2008-02-16
 */
class CmdProduct extends CommandProcessorDesktop{

	public CmdProduct(Kernel kernel) {
		super(kernel);
	}

	

	@Override
	public GeoElement[] process(geogebra.common.kernel.arithmetic.Command c)
			throws MyError, CircularDefinitionException {
		int n = c.getArgumentNumber();
		GeoElement[] arg;
		arg = resArgs(c);
		
		// needed for Sum[]
		if (arg.length == 0) {
			throw argNumErr(app, c.getName(), n);
		}
		if(!arg[0].isGeoList())
			throw argErr(app, c.getName(), arg[0]);
		GeoList list = (GeoList)arg[0];
		switch (n) {
		case 1:
				if (((GeoList)arg[0]).getGeoElementForPropertiesDialog().isNumberValue()) 
				{
					GeoElement[] ret = { 
							kernel.Product(c.getLabel(),list) };
					return ret;
				}
				else 
					throw argErr(app, c.getName(), arg[0]);
		case 2:
			if (arg[1].isGeoNumeric()) {

				if (((GeoList)arg[0]).getGeoElementForPropertiesDialog().isNumberValue()) 
				{
					GeoElement[] ret = { 
							kernel.Product(c.getLabel(),list,(GeoNumeric)arg[1]) };
					return ret;
				}
				else 
					throw argErr(app, c.getName(), arg[0]);
			} else
				throw argErr(app, c.getName(), arg[1]);
		
		default:
			throw argNumErr(app, c.getName(), n);
		}
	}

	}


