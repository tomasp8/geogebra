package geogebra.euclidian;

import geogebra.common.kernel.commands.AlgebraProcessor;
import geogebra.kernel.Kernel;

import org.scilab.forge.jlatexmath.dynamic.ExternalConverter;
import org.scilab.forge.jlatexmath.dynamic.ExternalConverterFactory;

public class LatexConvertorFactory implements ExternalConverterFactory {
	
	private Kernel kernel;
	
	   public LatexConvertorFactory(Kernel kernel) {
		this.kernel = kernel;
	}

	public ExternalConverter getExternalConverter() {
	       // you can associated an Geogebra env. with a DynamicAtom
	       return new LatexConvertor(getCurrentGeoGebraEnv(), kernel.getConstruction());
	   }

	private AlgebraProcessor getCurrentGeoGebraEnv() {
		return kernel.getAlgebraProcessor();
	}
	}


