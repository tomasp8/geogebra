package geogebra.main.settings;

import geogebra.common.main.settings.AbstractSettings;
import geogebra.common.main.settings.SettingListener;

import java.util.LinkedList;

/**
 * Settings for the algebra view.
 */
public class AlgebraSettings extends AbstractSettings {

	public AlgebraSettings(LinkedList<SettingListener> listeners) {
		super(listeners);
	}

	public AlgebraSettings() {
		super();
	}
}
