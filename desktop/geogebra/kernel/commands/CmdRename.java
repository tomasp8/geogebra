package geogebra.kernel.commands;

import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoText;
import geogebra.common.main.MyError;
import geogebra.gui.dialog.handler.RenameInputHandler;
import geogebra.kernel.Kernel;

/**
 *Rename
 */
class CmdRename extends CmdScripting {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdRename(Kernel kernel) {
		super(kernel);
	}

	final public void perform(Command c) throws MyError {
		int n = c.getArgumentNumber();
		GeoElement[] arg;

		switch (n) {
		case 2:
			arg = resArgs(c);
			if (arg[1].isGeoText()) {

				GeoElement geo = (GeoElement) arg[0];

				if (RenameInputHandler.checkName(geo, ((GeoText) arg[1]).getTextString())) {
					geo.rename(((GeoText) arg[1]).getTextString());
					geo.updateRepaint();

					
					return;
				} else {
					throw argErr(app, c.getName(), arg[1]);
				}
			} else
				throw argErr(app, c.getName(), arg[1]);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}
