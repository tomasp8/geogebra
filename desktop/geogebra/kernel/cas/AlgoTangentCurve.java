package geogebra.kernel.cas;

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.algos.AlgoPointOnPath;
import geogebra.common.kernel.cas.AlgoUsingTempCASalgo;
import geogebra.common.kernel.geos.GeoCurveCartesian;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoLine;
import geogebra.common.kernel.geos.GeoPoint2;

/**
 * @author Victor Franco Espino
 * version 11-02-2007
 * 
 * tangent to Curve f in point P: (b'(t), -a'(t), a'(t)*b(t)-a(t)*b'(t))
 */

public class AlgoTangentCurve extends AlgoUsingTempCASalgo {

	private static final long serialVersionUID = 1L;
	private GeoPoint2 P; // input
    private GeoCurveCartesian f, df; // input f
    private GeoLine tangent; // output  
    private GeoPoint2 T;
    private boolean pointOnCurve;

    public AlgoTangentCurve(Construction cons,String label,GeoPoint2 P,GeoCurveCartesian f) {
        super(cons);
        this.P = P;
        this.f = f;
        tangent = new GeoLine(cons);
        
        // check if P is defined as a point of the curve's graph
        pointOnCurve = false;
        if (P.getParentAlgorithm() instanceof AlgoPointOnPath) {
        	AlgoPointOnPath algo = (AlgoPointOnPath) P.getParentAlgorithm();
        	pointOnCurve = algo.getPath() == f;
        }        
        
        if (pointOnCurve)
        	T = P;
        else
        	T = new GeoPoint2(cons);
        tangent.setStartPoint(T);

        //First derivative of curve f
        algoCAS = new AlgoDerivative(cons, f);
		this.df = (GeoCurveCartesian) ((AlgoDerivative)algoCAS).getResult();
		cons.removeFromConstructionList(algoCAS);		
        
		setInputOutput(); // for AlgoElement                
        compute();
        tangent.setLabel(label);
    }

    public String getClassName() {
        return "AlgoTangentCurve";
    }

    public int getRelatedModeID() {
    	return EuclidianConstants.MODE_TANGENTS;
    }

    
    // for AlgoElement
    protected void setInputOutput() {
        input = new GeoElement[2];
        input[0] = P;
        input[1] = f;

        output = new GeoElement[1];
        output[0] = tangent;
        setDependencies(); // done by AlgoElement
    }

    public GeoLine getTangent() {
        return tangent;
    }
    GeoCurveCartesian getCurve() {
        return f;
    }
    GeoPoint2 getPoint() {
        return P;
    }
    GeoPoint2 getTangentPoint() {
        return T;
    }

    public final void compute() {
        if (!(f.isDefined() && P.isDefined())) {
            tangent.setUndefined();
            return;
        }

        // first derivative
        if (df == null || !df.isDefined()) {
            tangent.setUndefined();
            return;
        }

        // calc the tangent;
        double feval[] = new double[2];
        double dfeval[] = new double[2];        
        
        double tvalue = f.getClosestParameter(P,f.getMinParameter());
        f.evaluateCurve(tvalue, feval);
        df.evaluateCurve(tvalue, dfeval);
        tangent.setCoords(-dfeval[1],dfeval[0],feval[0]*dfeval[1]-dfeval[0]*feval[1]);
        
        if (!pointOnCurve)
        	T.setCoords(feval[0], feval[1], 1.0);
    }
}