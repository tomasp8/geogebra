package geogebra.kernel.commands;

import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoPoint2;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;

/**
 * CrossRtio[<Point>,<Point>,<Point>,<Point>]
 * 
 * @author Victor Franco Espino
 */
class CmdCrossRatio extends CommandProcessorDesktop {
	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdCrossRatio(Kernel kernel) {
		super(kernel);
	}

	public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		switch (n) {
		case 4:
			arg = resArgs(c);
			if ((ok[0] = (arg[0].isGeoPoint()))
					&& (ok[1] = (arg[1].isGeoPoint()))
					&& (ok[2] = (arg[2].isGeoPoint()))
					&& (ok[3] = (arg[3].isGeoPoint()))) {
				GeoElement[] ret = { kernel.CrossRatio(c.getLabel(),
						(GeoPoint2) arg[0], (GeoPoint2) arg[1],
						(GeoPoint2) arg[2], (GeoPoint2) arg[3]) };
				return ret;
			} else {
				if (!ok[0])
					throw argErr(app, c.getName(), arg[0]);
				if (!ok[1])
					throw argErr(app, c.getName(), arg[1]);
				if (!ok[2])
					throw argErr(app, c.getName(), arg[2]);
				else
					throw argErr(app, c.getName(), arg[3]);
			}

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}
