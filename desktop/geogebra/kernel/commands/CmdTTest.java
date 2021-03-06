package geogebra.kernel.commands;

import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.kernel.geos.GeoNumeric;
import geogebra.common.kernel.geos.GeoText;
import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;

/**
 * TTest (t test of a sample mean)
 */
class CmdTTest extends CommandProcessorDesktop {
	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdTTest(Kernel kernel) {
		super(kernel);
	}

	public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;
		arg = resArgs(c);

		switch (n) {

		case 3:
			if ((ok[0] = arg[0].isGeoList()) 
					&& (ok[1] = arg[1].isGeoNumeric())
					&& (ok[2] = arg[2].isGeoText())) {
				GeoElement[] ret = { kernel.TTest(c.getLabel(),
						(GeoList) arg[0], (GeoNumeric) arg[1], (GeoText) arg[2]) };
				return ret;

			} else if (!ok[0])
				throw argErr(app, c.getName(), arg[0]);
			else if (!ok[1])
				throw argErr(app, c.getName(), arg[1]);
			else 
				throw argErr(app, c.getName(), arg[2]);

		case 5:
			if ((ok[0] = arg[0].isGeoNumeric()) 
					&& (ok[1] = arg[1].isGeoNumeric())
					&& (ok[2] = arg[2].isGeoNumeric())
					&& (ok[3] = arg[3].isGeoNumeric())
					&& (ok[4] = arg[4].isGeoText())
			) {
				GeoElement[] ret = { kernel.TTest(c.getLabel(),
						(GeoNumeric) arg[0], 
						(GeoNumeric) arg[1],
						(GeoNumeric) arg[2],
						(GeoNumeric) arg[3],
						(GeoText) arg[4]) };
				return ret;

			} else if (!ok[0])
				throw argErr(app, c.getName(), arg[0]);
			else if (!ok[1])
				throw argErr(app, c.getName(), arg[1]);
			else if (!ok[2])
				throw argErr(app, c.getName(), arg[2]);
			else if (!ok[3])
				throw argErr(app, c.getName(), arg[3]);
			else
				throw argErr(app, c.getName(), arg[4]);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}
