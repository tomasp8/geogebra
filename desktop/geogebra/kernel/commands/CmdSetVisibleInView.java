package geogebra.kernel.commands;

import geogebra.common.euclidian.EuclidianViewInterfaceSlim;
import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoBoolean;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.main.MyError;
import geogebra.euclidian.EuclidianView;
import geogebra.kernel.Kernel;
import geogebra.main.Application;

/**
 *SetVisibleInView
 */
class CmdSetVisibleInView extends CmdScripting {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdSetVisibleInView(Kernel kernel) {
		super(kernel);
	}

	final public void perform(Command c) throws MyError {
		int n = c.getArgumentNumber();
		GeoElement[] arg;

		switch (n) {
		case 3:
			arg = resArgs(c);
			if (!arg[1].isNumberValue())
				throw argErr(app, c.getName(), arg[1]);


			if (arg[2].isGeoBoolean()) {

				GeoElement geo = (GeoElement) arg[0];
				

				int viewNo = (int)((NumberValue)arg[1]).getDouble();

				EuclidianViewInterfaceSlim ev = null;

				switch (viewNo) {
				case 1:
					ev = app.getEuclidianView();
					break;
				case 2:
					if (!app.hasEuclidianView2()) break;
					ev = ((Application) app).getEuclidianView2();
					break;
				default:
					// do nothing
				}

				if (ev != null) {
					boolean show = ((GeoBoolean)arg[2]).getBoolean();

					if (show) {
						geo.addView(ev.getViewID());
						ev.add(geo);
					} else {
						geo.removeView(ev.getViewID());
						ev.remove(geo);
					}
					
					geo.updateRepaint();
				}

				return;
			} else
				throw argErr(app, c.getName(), arg[2]);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}

