package geogebra.kernel.commands;

import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;
import geogebra.kernel.Kernel;

/*
 * Determinant[ <List> ]
 * Michael Borcherds 
 */
class CmdDeterminant extends CmdOneListFunction {

	public CmdDeterminant(Kernel kernel) {
		super(kernel);
	}

	final protected GeoElement doCommand(String a, GeoList b)
	{
		return kernel.Determinant(a, b);
	}

}
