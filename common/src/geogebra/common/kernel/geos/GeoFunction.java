/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.common.kernel.geos;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.AbstractConstructionDefaults;
import geogebra.common.kernel.AbstractKernel;
import geogebra.common.kernel.Path;
import geogebra.common.kernel.PathMover;
import geogebra.common.kernel.PathMoverGeneric;
import geogebra.common.kernel.PathParameter;
import geogebra.common.kernel.Region;
import geogebra.common.kernel.RegionParameters;
import geogebra.common.kernel.VarString;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.algos.AlgoMacroInterface;
import geogebra.common.kernel.arithmetic.ExpressionNode;
import geogebra.common.kernel.arithmetic.ExpressionValue;
import geogebra.common.kernel.arithmetic.Function;
import geogebra.common.kernel.arithmetic.FunctionInterface;
import geogebra.common.kernel.arithmetic.FunctionNVar;
import geogebra.common.kernel.arithmetic.FunctionVariable;
import geogebra.common.kernel.arithmetic.Functional;
import geogebra.common.kernel.arithmetic.FunctionalNVar;
import geogebra.common.kernel.arithmetic.IneqTree;
import geogebra.common.kernel.arithmetic.MyDouble;
import geogebra.common.kernel.arithmetic.MyList;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.arithmetic.ExpressionNodeConstants.StringType;
import geogebra.common.kernel.arithmetic.Operation;
import geogebra.common.kernel.implicit.GeoImplicitPolyInterface;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.kernel.roots.RealRootFunction;
import geogebra.common.main.AbstractApplication;
import geogebra.common.main.MyError;
import geogebra.common.util.StringUtil;
import geogebra.common.util.Unicode;

import java.util.HashMap;
import java.util.Iterator;
//import java.util.Locale;
import java.util.TreeSet;

/**
 * Explicit function in one variable ("x"). This is actually a wrapper class for Function
 * in geogebra.kernel.arithmetic. In arithmetic trees (ExpressionNode) it evaluates
 * to a Function.
 * 
 * @author Markus Hohenwarter
 */
public class GeoFunction extends GeoElement
implements VarString, Path, Translateable, Traceable, Functional, FunctionalNVar, GeoFunctionable,Region,
CasEvaluableFunction, ParametricCurve, LineProperties, RealRootFunction, Dilateable, Transformable {

	/** inner function representation */
	protected Function fun;		
	/** true if this function should be considered defined */
	protected boolean isDefined = true;
	private boolean trace;	
	
	// if the function includes a division by var, e.g. 1/x, 1/(2+x)
    private boolean includesDivisionByVar = false;
    
    /**  function may be limited to interval [a, b] */ 
    protected boolean interval = false;
    /** lower interval bound */
    protected double intervalMin;
    /** upper interval bound */
    protected double intervalMax; 
    private boolean evalSwapped;
    // parent conditional function
   // private GeoFunctionConditional parentCondFun = null;

	private boolean isInequality;    
	
	/**
	 * Creates new function
	 * @param c construction
	 */
	public GeoFunction(Construction c) {
		super(c);
		
		// moved from GeoElement's constructor
		// must be called from the subclass, see
		//http://benpryor.com/blog/2008/01/02/dont-call-subclass-methods-from-a-superclass-constructor/
		setConstructionDefaults(); // init visual settings

	}

	/**
	 * Creates new function
	 * @param c construction
	 * @param label label for function
	 * @param f function
	 */
	public GeoFunction(Construction c, String label, Function f) {
		this(c, f);
		setLabel(label);
		//TODO: Remove following code for 5.0 -- it's there to make sure no functions of y are created	
		if(isLabelSet() && !isBooleanFunction() && "y".equals(fun.getVarString(0))){
			this.remove();
			throw new MyError(app,"InvalidFunction");
		}
	}
	
	public GeoFunction(Construction c, Function f) {
		this(c);
		fun = f;				
		fun.initFunction();
		if(fun.isBooleanFunction()){
			GeoElement ge = (GeoElement) cons.getConstructionDefaults().getDefaultGeo(AbstractConstructionDefaults.DEFAULT_INEQUALITY_1VAR);
			setVisualStyle(ge);
			setAlphaValue(ge.getAlphaValue());
			//initialize inequlaities to make sure that drawable is made when necessary
			getIneqs();
		}
	}

	GeoImplicitPolyInterface iPoly;
	GeoFunction[] substituteFunctions;
	static int FUNCTION_DIRECT = 1;
	static int FUNCTION_COMPOSITE_IPOLY_FUNCS = 3;
	int geoFunctionType = FUNCTION_DIRECT;
	
	//Currently, the composite function is only for internal use
	//The expression is not correct but it is not to be shown anyway.
	public GeoFunction(Construction c, GeoImplicitPolyInterface iPoly, GeoFunction f, GeoFunction g) { // composite iPoly(f(x), g(x))
		this(c);
		this.iPoly = iPoly;
		geoFunctionType = FUNCTION_COMPOSITE_IPOLY_FUNCS;

		substituteFunctions = new GeoFunction[2];
		substituteFunctions[0] = f;
		substituteFunctions[1] = g;
		
		if (f==null && g!=null) {
			setInterval(g.intervalMin, g.intervalMax);

			fun = new Function(c.getKernel()) {
				@Override
				public double evaluate(double x) {

					return GeoFunction.this.iPoly.evalPolyAt(x,
							substituteFunctions[1].getFunction().evaluate(x));
				}
			};
				
			/*	Iterator it = iPoly.poly.getVariables().iterator();
				ExpressionNode iPolyEN = (ExpressionNode)iPoly.poly.deepCopy(kernel);
				ExpressionNode gEN = (ExpressionNode)g.getFunctionExpression().deepCopy(kernel);
				ExpressionValue varX = null;
				ExpressionValue varY = null;
				ExpressionValue vargX = null;
				vargX = g.getFunction().getFunctionVariable();
				if (it.hasNext())	varX = (ExpressionValue)it.next();
				if (it.hasNext())	varY = (ExpressionValue)it.next();
				
				if (vargX!= null && varX !=null && varY !=null) {
					
					ExpressionNode dummyX = new ExpressionNode();
					gEN = gEN.replaceAndWrap(g.getFunction().getFunctionVariable(), dummyX);
					iPolyEN = iPolyEN.replaceAndWrap(varY, gEN);
					gEN = gEN.replaceAndWrap(dummyX, vargX);
					
				}*/
				
			//}
				
			// g.getFunction().getFunctionVariable();
			// TODO: set the correct expression
			fun.setExpression(new ExpressionNode(kernel, new GeoNumeric(c, 0)));

		} else if (f != null && g == null) {
			setInterval(f.intervalMin, f.intervalMax);

			fun = new Function(c.getKernel()) {
				@Override
				public double evaluate(double x) {

					return GeoFunction.this.iPoly
							.evalPolyAt(substituteFunctions[0].getFunction()
									.evaluate(x), x);
				}
			};
			// TODO: set the correct expression
			fun.setExpression(new ExpressionNode(kernel, new GeoNumeric(c, 0)));

		} else if (f != null && g != null) {

			setInterval(Math.max(f.intervalMin, g.intervalMin),
					Math.min(f.intervalMax, g.intervalMax));

			fun = new Function(c.getKernel()) {
				@Override
				public double evaluate(double x) {

					return GeoFunction.this.iPoly.evalPolyAt(
							substituteFunctions[0].getFunction().evaluate(x),
							substituteFunctions[1].getFunction().evaluate(x));
				}
			};

			// TODO: set the correct expression
			fun.setExpression(new ExpressionNode(kernel, new GeoNumeric(c, 0)));

		} // else: error
	}
	
	@Override
	public void setVisualStyle(GeoElement g){
		super.setVisualStyle(g);
		if(g instanceof GeoFunction)
			setShowOnAxis(((GeoFunction)g).showOnAxis); 
	}
	
	@Override
	public String getClassName() {
		return "GeoFunction";
	}
	
	@Override
	protected String getTypeString() {
		return isInequality?"Inequality":"Function";
	}
	
    @Override
	public GeoClass getGeoClassType() {
    	return GeoClass.FUNCTION;
    }

	/** copy constructor 
	 * @param f Function to be copied */
	public GeoFunction(GeoFunction f) {
		this(f.cons);
		set(f);
	}

	@Override
	public GeoElement copy() {
		return new GeoFunction(this);
	}
	
	@Override
	public void set(GeoElement geo) {
		Function geoFun = ((GeoFunction) geo).getFunction();
			
		if (geo == null || geoFun == null) {
			fun = null;
			isDefined = false;
			return;
		} else {
			isDefined = geo.isDefined();
			fun = new Function(geoFun, kernel);		
		}			
	
		// macro OUTPUT
		if (geo.cons != cons && isAlgoMacroOutput()) {								
			// this object is an output object of AlgoMacro
			// we need to check the references to all geos in its function's expression
			if (!geo.isIndependent()) {
				AlgoMacroInterface algoMacro = (AlgoMacroInterface) getParentAlgorithm();
				algoMacro.initFunction(this.fun);	
			}			
		}
		isInequality = fun.initIneqs(this.getFunctionExpression(),this);
	}
	

	/**
	 * Sets the inner function
	 * @param f function
	 */
	public void setFunction(FunctionInterface f) {
		fun = (Function)f;
	}
			
	public Function getFunction() {
		return fun;
	}	
	
	/**
	 * Sets interval for the function
	 * @param a lower bound
	 * @param b upper bound
	 * @return true if the resulting interval is non-empty
	 */
	final public boolean setInterval(double a, double b) {
    	if (a <= b) {         
            interval = true;
            this.intervalMin = a; 
            this.intervalMax = b;              
        } else {
        	interval = false;            
        }   
    	
    	return interval;  
    }		
	
   /**
    * Returns function expression
    * @return function expression
    */
	final public ExpressionNode getFunctionExpression() {
		if (getFunction() == null)
			return null;
		else 
			return getFunction().getExpression();
	}	
	
	 /**
     * Replaces geo and all its dependent geos in this function's
     * expression by copies of their values.
     * @param geo geo to be replaced 
     */
    public void replaceChildrenByValues(GeoElement geo) {     	
    	if (fun != null) {
    		fun.replaceChildrenByValues(geo);
    	}
    }
	
	/**
	 * Returns the corresponding Function for the given x-value.
	 * This is important for conditional functions where we have
	 * two differen Function objects.
	 * @param x x-value
	 * @return coresponding function
	 */
	public Function getFunction(double x) {
		return fun;
	}
	
	/**
	 * Set this function to the n-th derivative of f
	 * @param fd function to be differenced
	 * @param n order of derivative
	 */
	public void setDerivative(CasEvaluableFunction fd, int n) {
		GeoFunction f = (GeoFunction) fd;
		
		if (f.isDefined()) {
			fun = f.fun.getDerivative(n);
			isDefined = fun != null;
		} else {
			isDefined = false;
		}		
	}
	
	/**
	 * Sets this function by applying a GeoGebraCAS command to a function.
	 * 
	 * @param ggbCasCmd the GeoGebraCAS command needs to include % in all places
	 * where the function f should be substituted, e.g. "Derivative(%,x)"
	 * @param f the function that the CAS command is applied to
	 */
	public void setUsingCasCommand(String ggbCasCmd, CasEvaluableFunction f, boolean symbolic){
		GeoFunction ff = (GeoFunction) f;
		
		if (ff.isDefined()) {
			fun = (Function) ff.fun.evalCasCommand(ggbCasCmd, symbolic);
			isDefined = fun != null;
		} else {
			isDefined = false;
		}		
	}	
	
	/**
	 * Returns this function's value at position x.
	 * @param x
	 * @return f(x)
	 */
	public double evaluate(double x) {
		if (fun == null)
			return Double.NaN;
		else {
			/*if (geoFunctionType == FUNCTION_COMPOSITE_IPOLY_FUNCS) {
				double evalX = substituteFunctions[0].evaluate(x);
				double evalY = substituteFunctions[1].evaluate(x);
				return iPoly.evalPolyAt(evalX, evalY);
			}
			else*/
				return fun.evaluate(x);
		}
	}
	
	/**
	 * Returns this function's value at position x.
	 * @param vals array of length 1 containing x
	 * @return f(val[0]) or f(val[1])
	 */
	public double evaluate(double[] vals) {
		
		return evaluate(vals[0]);
	}
	
	/**
	 * If restricted to interval, returns its minimum
	 * @return interval minimum
	 */
	public final double getIntervalMin() {
		return intervalMin;
	}
	/**
	 * If restricted to interval, returns its maximum
	 * @return interval maximum
	 */
	public final double getIntervalMax() {
		return intervalMax;
	}
	/**
	 * Iff restricted to interval, returns true
	 * @return true iff restricted to interval
	 */
	public final boolean hasInterval() {
		return interval;
	}
	
	/**
	 * Returns this boolean function's value at position x.
	 * @param x
	 * @return f(x)
	 */
	final public boolean evaluateBoolean(double x) {		
		return fun.evaluateBoolean(x);
	}
	
	public GeoFunction getGeoDerivative(int order){	
		if (derivGeoFun == null) {
			derivGeoFun = new GeoFunction(cons);
		}
		
		//if (getParentAlgorithm() instanceof AlgoFunctionFreehand)
		//	derivGeoFun.setUndefined();
		//else		
			derivGeoFun.setDerivative(this, order);
		
		return derivGeoFun;					
	}
	private GeoFunction derivGeoFun;
	
	@Override
	public ExpressionValue evaluate() {
		return this;
	}
	
	/**
	 * translate function by vector v
	 */
	final public void translate(Coords v) {
		translate(v.getX(), v.getY());
	}
	
	@Override
	final public boolean isTranslateable() {
		return fun != null && !isBooleanFunction();
	}
	
	/**
	 * Shifts the function by vx to right and by vy up
	 * @param vx horizontal shift
	 * @param vy vertical shift
	 */
	public void translate(double vx, double vy) {
		fun.translate(vx, vy);
	}

	
	/**
	 * Returns true if this function is a polynomial.
	 * @return true if this function is a polynomial.
	 * @param forRootFinding set to true if you want to allow
	 * functions that can be factored into polynomial factors
	 * for root finding (e.g. sqrt(x) could be replaced by x)
	 * @param symbolic function's symbolic expression must be a polynomial,
	 * e.g. x^2 is ok but not x^a
	 */
	public boolean isPolynomialFunction(boolean forRootFinding, boolean symbolic) {		
		// don't do root finding simplification here
		// i.e. don't replace a factor "sqrt(x)" by "x"
		if (!isDefined()) 
			return false;
		else
			return fun.isConstantFunction() || 
				(symbolic ? 
						fun.getSymbolicPolynomialFactors(forRootFinding) :
						fun.getPolynomialFactors(forRootFinding))
					!= null;
	}

	/**
	 * Returns true if this function is a polynomial.
	 * @return true if this function is a polynomial.
	 * @param forRootFinding set to true if you want to allow
	 * functions that can be factored into polynomial factors
	 * for root finding (e.g. sqrt(x) could be replaced by x)
	 */
	
	public boolean isPolynomialFunction(boolean forRootFinding) {
		return isPolynomialFunction(forRootFinding, false);
	}
	
	/**
     * Returns whether this function includes a division by variable,
     * e.g. f(x) = 1/x, 1/(2+x), sin(3/x), ...
     * @return true iff this function includes a division by variable
     */
    final public boolean includesDivisionByVar() {
    	if (includesDivisionByVarFun != fun) {
    		includesDivisionByVarFun = fun;    		
    		includesDivisionByVar = fun != null && fun.includesDivisionByVariable();    		
    	}
    	return includesDivisionByVar;
    }
    private Function includesDivisionByVarFun = null;
    

	@Override
	public boolean isDefined() {
		return isDefined && fun != null;
	}
	
	@Override
	public boolean isFillable(){
		return isInequality;
	}

	/**
	 * Changes the defined state
	 * @param defined true iff the function should be considered defined
	 */
	public void setDefined(boolean defined) {
		isDefined = defined;
	}

	@Override
	public void setUndefined() {
		isDefined = false;
	}

	@Override
	public boolean showInAlgebraView() {
		return true;
	}

	@Override
	protected boolean showInEuclidianView() {		
		return isDefined() && (!isBooleanFunction() || isInequality);
	}
		
	/**
	 * @return function description as f(x)=... for real and e.g. f:x>4 for bool
	 */
	@Override
	public String toString() {		
		sbToString.setLength(0);
		if(isLabelSet()) {
			sbToString.append(label);
			if(isBooleanFunction())
				sbToString.append(": ");
			else{
				sbToString.append("(");
				sbToString.append(getVarString());
				sbToString.append(") = ");
			}
		}
		sbToString.append(toValueString());
		return sbToString.toString();
	}
	
	/** StringBuilder for temporary string manipulation */
	protected StringBuilder sbToString = new StringBuilder(80);

	private boolean showOnAxis;
	
	@Override
	public String toValueString() {	

		if (fun != null && isDefined())
			return fun.toValueString();
		else
			return app.getPlain("undefined");
	}	
	
	/*
	 * (non-Javadoc)
	 * @see geogebra.kernel.GeoElement#toOutputValueString()
	 * needed for eg KeepIf[x!="h",{"h","k","o"}]
	 */
	@Override
	public String toOutputValueString() {	
		
		if (isLocalVariable())
			return label;

		if (fun != null && isDefined())
			return fun.toOutputValueString();
		else
			return app.getPlain("undefined");
	}	
	
	public String toSymbolicString() {	
		if (fun != null && isDefined())
			return fun.toString();
		else
			return app.getPlain("undefined");
	}
	
	@Override
	public String toLaTeXString(boolean symbolic) {
		if (fun != null && isDefined())
			return fun.toLaTeXString(symbolic);
		else
			return app.getPlain("undefined");
	}
		
	/**
	 * save object in xml format
	 */
	@Override
	public final void getXML(StringBuilder sb) {

		// an indpendent function needs to add
		// its expression itself
		// e.g. f(x) = x^2 - 3x
		if (isIndependent() && getDefaultGeoType() < 0) {
			sb.append("<expression");
			sb.append(" label =\"");
			sb.append(label);
			sb.append("\" exp=\"");
			sb.append(StringUtil.encodeXML(toString()));
			// expression
			sb.append("\"/>\n");
		}

		sb.append("<element");
		sb.append(" type=\"function\"");
		sb.append(" label=\"");
		sb.append(label);
		if (getDefaultGeoType() >= 0) {
			sb.append("\" default=\"");
			sb.append(getDefaultGeoType());
		}
		sb.append("\">\n");
		getXMLtags(sb);
		sb.append(getCaptionXML());
		sb.append("</element>\n");
	}
	
	/**
	 * returns all class-specific xml tags for getXML
	 */
	@Override
	protected void getXMLtags(StringBuilder sb) {
		super.getXMLtags(sb);

		// line thickness and type
		getLineStyleXML(sb);
		if (showOnAxis()) {
			sb.append("<showOnAxis val=\"true\" />");
		}
	}

	/*
	 * Path interface
	 */	 
	public void pointChanged(GeoPointND PI) {			
		
		GeoPoint2 P = (GeoPoint2) PI;
		
		if (P.getZ() == 1.0) {
			//P.x = P.x;			
		} else {
			P.setX( P.getX() / P.getZ());			
		}
				
		
		if(!isBooleanFunction()){
			if (interval) {
				//	don't let P move out of interval			
				if (P.getX() < intervalMin) 
					P.setX(intervalMin);
				else if (P.getX() > intervalMax) 
					P.setX(intervalMax);
			}
			P.setY(evaluate(P.getX()));// changed from fun.evaluate so that it works with eg Point[If[x < -1, x + 1, x^2]] 
		}
		else {
			pointChangedBoolean(true,P);
		}
		P.setZ(1.0);
		
		// set path parameter for compatibility with
		// PathMoverGeneric
		PathParameter pp = P.getPathParameter();
		pp.t = P.getX();
	}
	
	private void pointChangedBoolean(boolean b, GeoPoint2 P) {
		double px;
		boolean yfun = getVarString().equals("y");
		if (yfun) {
			if (b)
				P.setX(0.0);
			px = P.getY();
		} else {
			if (b)
				P.setY(0.0);
			px = P.getX();
		}
		double bestDist = Double.MAX_VALUE;
		getIneqs();
		if (!this.evaluateBoolean(px)) {
			IneqTree ineqs = fun.getIneqs();
			int ineqCount = ineqs.getSize();
			for (int i = 0; i < ineqCount; i++) {
				for (GeoPoint2 point : ineqs.get(i).getZeros()) {
					if (Math.abs(point.getX() - px) < bestDist) {
						bestDist = Math.abs(point.getX() - px);
						if (yfun) {
							P.setY(point.getX());
						} else {
							P.setX(point.getX());
						}
					}
				}
			}
		}
	}

	public boolean isOnPath(GeoPointND PI, double eps) {
		
		GeoPoint2 P = (GeoPoint2) PI;
		
		if (P.getPath() == this)
			return true;
		
		if(!isBooleanFunction()){
			return isDefined &&	Math.abs(fun.evaluate(P.getInhomX()) - P.getInhomY()) <= eps;
		}
		else{
			double px = getVarString().equals("y") ? P.getY() :P.getX();
			if (P.getZ() != 1.0) {
					px = px / P.getZ();		
			}
			return evaluateBoolean(px);
		}
	}

	public void pathChanged(GeoPointND PI) {
		
		GeoPoint2 P = (GeoPoint2) PI;
		
		PathParameter pp = P.getPathParameter();
		P.setX(pp.t);
		pointChanged(P);
	}
	
	@Override
	public boolean isPath() {
		return true;
	}

	/**
	 * Returns the smallest possible parameter value for this
	 * path (may be Double.NEGATIVE_INFINITY)
	 * Last change by Zbynek Konecny, see #5
	 * @version 2010-05-14
	 * @return smallest possible parameter value (may be Double.NEGATIVE_INFINITY)
	 */
	public double getMinParameter() {
		if (interval)
			return Math.max(kernel.getViewsXMin(this), intervalMin);
		else
			return kernel.getViewsXMin(this);
	}
	
	/**
	 * Returns the largest possible parameter value for this
	 * path (may be Double.POSITIVE_INFINITY)
	 * Last change by Zbynek Konecny, see #5
	 * @version 2010-05-14
	 * @return largest possible parameter value (may be Double.POSITIVE_INFINITY)
	 */
	public double getMaxParameter() {
		if (interval)
			return Math.min(kernel.getViewsXMax(this), intervalMax);
		else
			return kernel.getViewsXMax(this);
	}
	
	public PathMover createPathMover() {
		return new PathMoverGeneric(this);
	}
	
	public boolean isClosedPath() {
		return false;
	}

	@Override
	final public boolean isCasEvaluableObject() {
		return true;
	}
	
	@Override
	public boolean isNumberValue() {
		return false;		
	}

	@Override
	public boolean isVectorValue() {
		return false;
	}

	@Override
	public boolean isPolynomialInstance() {
		return false;
	}   

	@Override
	public boolean isTextValue() {
		return false;
	}
	
	@Override
	public boolean isTraceable() {
		return true;
	}

	public boolean getTrace() {		
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;	
	}   

	//G.Sturr 2010-5-18  get/set spreadsheet trace not needed here
	/*
	public void setSpreadsheetTrace(boolean spreadsheetTrace) {
		this.spreadsheetTrace = spreadsheetTrace;
	}

	public boolean getSpreadsheetTrace() {
		return spreadsheetTrace;
	}
	*/
	
	
	public GeoFunction getGeoFunction() {
		return this;
	}
	
	@Override
	public boolean isGeoFunction() {
		if (fun != null)
			return !fun.isBooleanFunction();
		else
			return true;
	}
	
	public String getAssignmentLHS() {
		sbToString.setLength(0);
		sbToString.append(label);
		sbToString.append("(");
		sbToString.append(getVarString());
		sbToString.append(")");
		return sbToString.toString();
	}
	
	@Override
	public boolean isGeoFunctionable() {
		return isGeoFunction();
	}
		
	public boolean isBooleanFunction() {
		if (fun != null)
			return fun.isBooleanFunction();
		else
			return false;
	}

	/** changes variable interpretation:
	 * if swapped, the function is considered to be 
	 * x=f(y).
	 */
	public void swapEval(){
		evalSwapped = !evalSwapped;
	}
	
	public void evaluateCurve(double t, double[] out) {
		if(evalSwapped){
			out[1] = t;
			out[0] = evaluate(t);
		}
		else{
			out[0] = t;
			out[1] = evaluate(t);
		}
	}		
	
	/**
	 * Evaluates curvature for function:  k(x) = f''/T^3, T = sqrt(1+(f')^2)
	 * @author Victor Franco Espino, Markus Hohenwarter
	 */
	public double evaluateCurvature(double x) {			
		Function f1 = fun.getDerivative(1);
		Function f2 = fun.getDerivative(2);				
		if (f1 == null || f2 == null)
			return Double.NaN;
		
		double f1eval = f1.evaluate(x);
		double t = Math.sqrt(1 + f1eval * f1eval);
		double t3 = t * t * t;
		return f2.evaluate(x) / t3;    	
	}
    
	final public RealRootFunction getRealRootFunctionX() {
		return new RealRootFunction() {
			public double evaluate(double t) {
				return t;
			}
		};
	}
	
	final public RealRootFunction getRealRootFunctionY() {
		return new RealRootFunction() {
			public double evaluate(double t) {
				return GeoFunction.this.evaluate(t);
			}
		};
	}

	public GeoVec2D evaluateCurve(double t) {
		return new GeoVec2D(kernel, t, evaluate(t));
	}
	
	public String getVarString() {	
		if (fun == null) {
			return kernel.printVariableName("x");
		} else {
			return fun.getVarString();
		}
	}

	final public boolean isFunctionInX() {		
		return true;
	}
	
	/*
	public final GeoFunctionConditional getParentCondFun() {
		return parentCondFun;
	}

	public final void setParentCondFun(GeoFunctionConditional parentCondFun) {
		this.parentCondFun = parentCondFun;
	}*/
	
    // Michael Borcherds 2009-02-15
	@Override
	public boolean isEqual(GeoElement geo) {		
		if (!geo.isGeoFunction() || geo.getGeoClassType() .equals(GeoClass.INTERVAL))
			return false;
				
		// return return geo.isEqual(this); rather than false
		// in case we improve checking in GeoFunctionConditional in future
		if (geo.getGeoClassType() .equals(GeoClass.FUNCTIONCONDITIONAL))
			return geo.isEqual(this);
		else
			return isDifferenceZeroInCAS(geo);				
	}
	
	/**
	 * Sums two functions and stores the result to another 
	 * @param resultFun resulting function
	 * @param fun1 first addend
	 * @param fun2 second addend
	 * @return resultFun
	 */
	public static GeoFunction add(GeoFunction resultFun, GeoFunction fun1, GeoFunction fun2) {
		
		AbstractKernel kernel = fun1.getKernel();
		
    	FunctionVariable x1 = fun1.getFunction().getFunctionVariable();
    	FunctionVariable x2 = fun2.getFunction().getFunctionVariable();
    	FunctionVariable x =  new FunctionVariable(kernel);
    	
    	ExpressionNode left = fun1.getFunctionExpression().getCopy(kernel);
       	ExpressionNode right = fun2.getFunctionExpression().getCopy(kernel);    
       	
    	ExpressionNode sum = new ExpressionNode(fun1.getKernel(), left.replaceAndWrap(x1,x), Operation.PLUS, right.replaceAndWrap(x2,x));
    	
    	Function f = new Function(sum,x);
    	
       	resultFun.setFunction(f);
       	resultFun.setDefined(true);
       	
       	return resultFun;
	}
	/**
	 * Applies an operation on first and second function and returns the result
	 * @param op
	 * @param lt 
	 * @param rt
	 * @return resulting GeoFunction or GeFunctionNvar
	 */
	public static FunctionNVar operationSymb(Operation op, FunctionalNVar lt , FunctionalNVar rt) {
		AbstractKernel kernel = lt.getFunction().getKernel();
		TreeSet<String> varNames = new TreeSet<String>();
		for(int i=0;i<lt.getFunction().getVarNumber();i++)
			varNames.add(lt.getFunction().getVarString(i));
		for(int i=0;i<rt.getFunction().getVarNumber();i++)
			varNames.add(rt.getFunction().getVarString(i));
		HashMap<String,FunctionVariable> varmap = new HashMap<String,FunctionVariable>();
		for(String name:varNames){
			varmap.put(name, new FunctionVariable(kernel,name));
		}
		ExpressionNode ltExpr=toExpr(lt,varmap,kernel),
			rtExpr=toExpr(rt,varmap,kernel),
			sum = new ExpressionNode(kernel,ltExpr,op,rtExpr);
		FunctionNVar f = fromExpr(sum,varmap,varNames);
		
    	f.initFunction();
    	
    	return f;
       	//AlgoDependentFunction adf = new AlgoDependentFunction(fun1.getConstruction(),f);
       	//return adf.getFunction();
       	
	}
	
	private static FunctionNVar fromExpr(ExpressionNode sum,
			HashMap<String, FunctionVariable> varmap,TreeSet<String>varNames) {
		int size = varmap.size();		
		if(size>1){
			FunctionVariable[]varArray =new FunctionVariable[size];
			int i= 0;
			for(String name:varNames){
				varArray[i] = varmap.get(name);
				i++;
			}
			FunctionNVar f = new FunctionNVar(sum,varArray);			
			return f;
	    	
	       	//AlgoDependentFunctionNVar adf = new AlgoDependentFunctionNVar(fun1.getConstruction(),null,f);
	       	//return adf.getFunction();
		}
		else{
			Iterator<FunctionVariable> var = varmap.values().iterator();
			return new Function(sum,var.next());
		}
	}

	private static ExpressionNode toExpr(FunctionalNVar lt,
			HashMap<String, FunctionVariable> varMap,AbstractKernel kernel) {
		if(lt instanceof GeoFunction)
			return new ExpressionNode(kernel,(GeoFunction)lt,
					Operation.FUNCTION,varMap.get(lt.getVarString()));
		if(lt instanceof GeoFunctionNVar){
			MyList varList = new MyList(kernel);
			for(int i=0;i<lt.getFunction().getVarNumber();i++){
				varList.addListElement(varMap.get(lt.getFunction().getVarString(i)));
			}
			return new ExpressionNode(kernel,(GeoFunctionNVar)lt,
					Operation.FUNCTION_NVAR,varList);
		}
		if(lt instanceof FunctionNVar){
			ExpressionNode ret =((FunctionNVar)lt).getExpression();
			for(int i=0; i<((FunctionNVar)lt).getVarNumber();i++)
			ret = ret.replaceAndWrap(((FunctionNVar)lt).getFunctionVariables()[i],
					varMap.get(((FunctionNVar)lt).getVarString(i)));
			return ret;
		}
		return null;
	}

	/**
	 * Applies an operation on this function and number value
	 * @param op
	 * @param fun1
	 * @param nv
	 * @param right f op nv for true, nv op f for false
	 * @return resulting function
	 */
	public static FunctionNVar applyNumberSymb(Operation op, FunctionalNVar fun1, ExpressionValue nv,boolean right) {
		AbstractKernel kernel = fun1.getFunction().getKernel();
		TreeSet<String> varNames = new TreeSet<String>();
		for(int i=0;i<fun1.getFunction().getVarNumber();i++)
			varNames.add(fun1.getFunction().getVarString(i));		
		HashMap<String,FunctionVariable> varmap = new HashMap<String,FunctionVariable>();
		for(String name:varNames){
			varmap.put(name, new FunctionVariable(kernel,name));
		}
		ExpressionNode sum, myExpr;
		myExpr = toExpr(fun1, varmap, kernel);
		
		if(nv instanceof ExpressionNode)
			for(String name:varNames){
				((ExpressionNode)nv).replaceVariables(name, varmap.get(name));
			}
		else if(nv instanceof FunctionVariable)
			nv = varmap.get( ((FunctionVariable)nv).toString());
		
		
		if(right){sum = new ExpressionNode(kernel,myExpr,				
				op, nv);			
		}
		else{ sum = new ExpressionNode(kernel,nv,op,
				myExpr);
		}
		FunctionNVar f = fromExpr(sum,varmap,varNames);
    	f.initFunction();       	
       	return f;
	}
	
	/**
	 * Subtracts two functions and stores the result to another 
	 * @param resultFun resulting function
	 * @param fun1 minuend
	 * @param fun2 subtrahend
	 * @return resultFun
	 */
	public static GeoFunction subtract(GeoFunction resultFun, GeoFunction fun1, GeoFunction fun2) {
		
		AbstractKernel kernel = fun1.getKernel();
		
    	FunctionVariable x1 = fun1.getFunction().getFunctionVariable();
    	FunctionVariable x2 = fun2.getFunction().getFunctionVariable();
    	FunctionVariable x =  new FunctionVariable(kernel);
    	

    	ExpressionNode left = fun1.getFunctionExpression().getCopy(kernel);
       	ExpressionNode right = fun2.getFunctionExpression().getCopy(kernel);    
       	
    	ExpressionNode sum = new ExpressionNode(fun1.getKernel(), left.replaceAndWrap(x1,x), Operation.MINUS, right.replaceAndWrap(x2,x));
    	
    	Function f = new Function(sum,x);
    	
       	resultFun.setFunction(f);
       	resultFun.setDefined(true);
       	
       	return resultFun;
	}	

	/** Multiplication of number and function.
	 * Needed in Fit[<List of Points>,<List of Functions>]
	 * to make the result a linear combination of existing functions; fit(x)=a*f(x)+b*g(x)+c*h(x)+..
	 * @author Hans-Petter Ulven
	 * @version 2010-02-22
	 * @param resultFun Resulting function
	 * @param number number
	 * @param fun function
	 * @return resultFun
	 * 
	 */
	public static GeoFunction mult(GeoFunction resultFun, double number, GeoFunction fun) {
		
		AbstractKernel kernel = fun.getKernel();
		geogebra.common.kernel.arithmetic.MyDouble num = new geogebra.common.kernel.arithmetic.MyDouble(kernel,number);
		
    	FunctionVariable xold = fun.getFunction().getFunctionVariable();
    	FunctionVariable x =  new FunctionVariable(kernel);
    	

    	ExpressionNode left = new ExpressionNode(kernel,num);
       	ExpressionNode right = fun.getFunctionExpression().getCopy(kernel);    
       	
    	ExpressionNode product = new ExpressionNode(kernel,left, Operation.MULTIPLY, right.replaceAndWrap(xold,x));
    	
    	Function f = new Function(product,x);
    	
       	resultFun.setFunction(f);
       	resultFun.setDefined(true);
       	
       	return resultFun;
	}//mult()
	
	@Override
	public boolean isVector3DValue() {
		return false;
	}
	
	/**
	 * Returns true iff x is in the interval
	 * over-ridden in GeoFunctionConditional
	 * @param x
	 * @return true iff x is in the interval
	 */
	public boolean evaluateCondition(double x) {
		if (!interval) return true;
		return x > intervalMin && x < intervalMax;
	}
	
	/**
	 * Returns the limit
	 * 
	 * @param x
	 *            point to evaluate the limit
	 * @param direction
	 *            1 for limit above, -1 for limit below, standard limit
	 *            otherwise
	 * @return the limit
	 */
	public double getLimit(double x, int direction) {
		// get function and function variable string using temp variable
		// prefixes,
		// e.g. f(x) = a x^2 returns {"ggbtmpvara ggbtmpvarx^2", "ggbtmpvarx"}
		String[] funVarStr = getTempVarCASString(false);

		if (sb == null) {
			sb = new StringBuilder();
		} else {
			sb.setLength(0);
		}
		sb.setLength(0);
		sb.append("Numeric(Limit");
		if (direction == -1) {
			sb.append("Above");
		} else if (direction == 1) {
			sb.append("Below");
		}
		sb.append('(');
		sb.append(funVarStr[0]); // function expression
		sb.append(',');
		sb.append(funVarStr[1]); // function variable
		sb.append(',');
		sb.append(Double.toString(x));
		sb.append("),");
		// increase precision to improve problems like
		// http://www.geogebra.org/trac/ticket/1106
		sb.append("50)");

		try {
			String functionOut = kernel
					.evaluateCachedGeoGebraCAS(sb.toString());
			NumberValue nv = kernel.getAlgebraProcessor().evaluateToNumeric(
					functionOut, false);
			return nv.getDouble();
		} catch (Throwable e) {
			e.printStackTrace();
			return Double.NaN;
		}
	}

	/**
	 * Adds vertical asymptotes to the StringBuilder
	 *  over-ridden in GeoFunctionConditional
	 * @param f function whose asymptotes we are looking for
	 * @param verticalSB StringBuilder for the result
	 * @param reverse if true, we reverse the parent conditional function condition
	 */
	public void getVerticalAsymptotes(GeoFunction f, StringBuilder verticalSB, boolean reverse) {
		getVerticalAsymptotesStatic(this, f, verticalSB, reverse);
	}
	
	/**
	 * Adds horizontal positive asymptotes to the StringBuilder
	 *  over-ridden in GeoFunctionConditional
	 * @param f function whose asymptotes we are looking for
	 * @param SB StringBuilder for the result
	 */
	public void getHorizontalPositiveAsymptote(GeoFunction f, StringBuilder SB) {
		getHorizontalAsymptoteStatic(this, f, SB, true);		
	}
	
	/**
	 * Adds horizontal negative asymptotes to the StringBuilder
	 *  over-ridden in GeoFunctionConditional
	 * @param f function whose asymptotes we are looking for
	 * @param SB StringBuilder for the result
	 */
	public void getHorizontalNegativeAsymptote(GeoFunction f, StringBuilder SB) {
		getHorizontalAsymptoteStatic(this, f, SB, false);		
	}
	
	/**
	 * Adds diagonal positive asymptotes to the StringBuilder
	 *  over-ridden in GeoFunctionConditional
	 * @param f function whose asymptotes we are looking for
	 * @param SB StringBuilder for the result
	 */
	
	public void getDiagonalPositiveAsymptote(GeoFunction f, StringBuilder SB) {
		getDiagonalAsymptoteStatic(this, f, SB, true);		
	}
	
	/**
	 * Adds diagonal negative asymptotes to the StringBuilder
	 *  over-ridden in GeoFunctionConditional
	 * @param f function whose asymptotes we are looking for
	 * @param SB StringBuilder for the result
	 */
	public void getDiagonalNegativeAsymptote(GeoFunction f, StringBuilder SB) {
		getDiagonalAsymptoteStatic(this, f, SB, false);		
	}
	
	private static StringBuilder sb;

	/**
     * Adds diagonal asymptotes to the string builder SB
     * @param f function whose asymptotes we are looking for
     * @param parentFunction parent function (in case of conditional functions)
     * @param SB StringBuilder for the result
     * @param positiveInfinity if true, we look for limit at positive infinity, for false, we use negative infinity
     */
    protected void getDiagonalAsymptoteStatic(GeoFunction f, GeoFunction parentFunction, StringBuilder SB, boolean positiveInfinity) {

    	try {
    		// get first derivative
        	GeoFunction deriv = f.getGeoDerivative(1);
        	
        	// get function and function variable string using temp variable prefixes,
    		// e.g. f(x) = a x^2 returns {"ggbtmpvara ggbtmpvarx^2", "ggbtmpvarx"}
    		String [] derivVarStr = deriv.getTempVarCASString(false);
    		String [] funVarStr = f.getTempVarCASString(false);
    		
        	if (sb == null) sb = new StringBuilder();
        	else sb.setLength(0);    	
	
			String gradientStrMinus="";
			String interceptStrMinus="";
			
			sb.setLength(0);
	        sb.append("Limit(");
	        sb.append(derivVarStr[0]); // derivative expression		
	        sb.append(',');
	        sb.append(derivVarStr[1]); // derivative function variable
	        sb.append(',');
	        if (!positiveInfinity) sb.append('-'); // -Infinity
	        sb.append(Unicode.Infinity);
	        sb.append(')');

			gradientStrMinus = kernel.evaluateCachedGeoGebraCAS(sb.toString());
			//Application.debug(sb.toString()+" = "+gradientStrMinus,1);
			
			double grad;
			try {
				grad = kernel.getAlgebraProcessor().evaluateToDouble(gradientStrMinus, true);
			} catch (Exception e) {
				grad = 0;
			}
			
			if (!GeoFunction.CASError(gradientStrMinus, false,app) && !AbstractKernel.isZero(grad)) {
				sb.setLength(0);
		        sb.append("Limit(");
		        sb.append(funVarStr[0]); // function expression
		        sb.append(" - ");
		        sb.append(gradientStrMinus);
		        sb.append(" * ");
		        sb.append(derivVarStr[1]); // derivative function variable
		        sb.append(',');
		        sb.append(derivVarStr[1]); // derivative function variable
		        sb.append(',');
		        if (!positiveInfinity) sb.append('-'); // -Infinity
		        sb.append(Unicode.Infinity);
		        sb.append(')');

		        interceptStrMinus = kernel.evaluateCachedGeoGebraCAS(sb.toString());
				//Application.debug(sb.toString()+" = "+interceptStrMinus,1);
				
				if (!GeoFunction.CASError(interceptStrMinus, false,app)) {
					sb.setLength(0);
					sb.append("y = ");
					sb.append(gradientStrMinus);
					sb.append(" * x +");
					sb.append(interceptStrMinus);
					
					if (!SB.toString().endsWith(sb.toString())) { // not duplicated
						if (SB.length() > 1) SB.append(',');
						SB.append(sb);
						//Application.debug("diagonal asymptote minus: y = "+gradientStrMinus+"x + "+interceptStrMinus,1);			
					}
					
				}
			}		
    	}  catch (Throwable e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Adds horizontal asymptotes to the string builder SB
     * @param f function whose asymptotes we are looking for
     * @param parentFunction parent function (in case of conditional functions)
     * @param SB StringBuilder for the result
     * @param positiveInfinity if true, we look for limit at positive infinity, for false, we use negative infinity
     */
    protected void getHorizontalAsymptoteStatic(GeoFunction f, GeoFunction parentFunction, StringBuilder SB, boolean positiveInfinity) {    	
    	// get function and function variable string using temp variable prefixes,
		// e.g. f(x) = a x^2 returns {"ggbtmpvara ggbtmpvarx^2", "ggbtmpvarx"}
		String [] funVarStr = f.getTempVarCASString(false);
    	
    	if (sb == null) sb = new StringBuilder();
    	else sb.setLength(0);
        sb.append("Limit(");
        sb.append(funVarStr[0]); // function expression
        sb.append(',');
        sb.append(funVarStr[1]); // function variable
        sb.append(',');
        if (!positiveInfinity) sb.append('-'); // -Infinity
        sb.append(Unicode.Infinity);
        sb.append(")");

        try {
			String limit = kernel.evaluateCachedGeoGebraCAS(sb.toString()).trim();
			
			//Application.debug(sb.toString()+" = "+limit,1);
			
		    if (!GeoFunction.CASError(limit, false,app)) {
		    	   	
		    	// check not duplicated
		    	sb.setLength(0);
		    	sb.append("y=");
		    	sb.append(limit);
		    	if (!SB.toString().endsWith(sb.toString())) { // not duplicated
			    	if (SB.length() > 1) SB.append(',');
			    	SB.append(sb);
		    	}
		    }
        } catch (Throwable t) {
        	// nothing to do
        }
    }
    
    @Override
	protected char getLabelDelimiter(){
		return isBooleanFunction()?':':'=';
	}
    /**
     * Adds vertical asymptotes to the string builder VerticalSB
     * @param f function whose asymptotes we are looking for
     * @param parentFunction parent function (in case of conditional functions)
     * @param verticalSB StringBuilder for the result
     * @param reverseCondition if true, we reverse the parent conditional function condition
     */
    protected void getVerticalAsymptotesStatic(GeoFunction f, GeoFunction parentFunction, StringBuilder verticalSB, boolean reverseCondition) {
    	// get function and function variable string using temp variable prefixes,
		// e.g. f(x) = a x^2 returns {"ggbtmpvara ggbtmpvarx^2", "ggbtmpvarx"}
		String [] funVarStr = f.getTempVarCASString(false);
		
 	    // solve 1/f(x) == 0 to find vertical asymptotes
    	if (sb == null) sb = new StringBuilder();
    	else sb.setLength(0);
	    
        sb.append("Solve(1/(");        
        sb.append(funVarStr[0]); // function expression with "ggbtmpvarx" as function variable
        sb.append(")=0");
        sb.append(",");
        sb.append(funVarStr[1]); // function variable "ggbtmpvarx"
        sb.append(")");
        
        try {
			String verticalAsymptotes = kernel.evaluateCachedGeoGebraCAS(sb.toString());
			//Application.debug(sb.toString()+" = "+verticalAsymptotes,1);
	    	
	    	if (!GeoFunction.CASError(verticalAsymptotes, false,app) && verticalAsymptotes.length() > 2) {
		    	verticalAsymptotes = verticalAsymptotes.replace('{',' ');
		    	verticalAsymptotes = verticalAsymptotes.replace('}',' ');
		    	//verticalAsymptotes = verticalAsymptotes.replace('(',' '); // eg (-1)
		    	//verticalAsymptotes = verticalAsymptotes.replace(')',' ');
		    	verticalAsymptotes = verticalAsymptotes.replaceAll("x==", "");
		    	verticalAsymptotes = verticalAsymptotes.replaceAll("x =", "");
		    	//verticalAsymptotes = verticalAsymptotes.replaceAll("Complex(.*)", ""); // remove complex roots (MathPiper)
		    	
		    	//verticalAsymptotes = verticalAsymptotes.replaceAll("%i", ""); // remove complex roots (Maxima)

		    	String[] verticalAsymptotesArray = verticalAsymptotes.split(",");
		    	
		    	// check they are really asymptotes
		    	for (int i = 0 ; i < verticalAsymptotesArray.length ; i++) {
		    		//Application.debug(verticalAsymptotesArray[i]);
		    		boolean repeat = false;
		    		if (i > 0 && verticalAsymptotesArray.length > 1) { // check for repeats
		    			for (int j = 0  ; j < i ; j++) {
		    				if (verticalAsymptotesArray[i].equals(verticalAsymptotesArray[j])) {
		    					repeat = true;
		    					break;
		    				}
		    			}
		    		}
		    		
		    		boolean isInRange = false;
		    		try {
		    			//Application.debug(verticalAsymptotesArray[i]+"");
		    			if (verticalAsymptotesArray[i].trim().equals("")) isInRange = false; // was complex root
		    			//isInRange = parentFunction.evaluateCondition(Double.parseDouble(verticalAsymptotesArray[i]));
		    			else isInRange = parentFunction.evaluateCondition(kernel.getAlgebraProcessor().evaluateToNumeric(verticalAsymptotesArray[i], true).getDouble());
		    		} catch (Exception e) {AbstractApplication.debug("Error parsing: "+verticalAsymptotesArray[i]);}
		    		if (reverseCondition) isInRange = !isInRange;
		    		
		    		if (!repeat && isInRange) {
		    		
			    		sb.setLength(0);
			            sb.append("Numeric(Limit(");
			            sb.append(funVarStr[0]); // function expression with "ggbtmpvarx" as function variable
			            sb.append(',');
			            sb.append(funVarStr[1]); // function variable "ggbtmpvarx"
			            sb.append(",");
			            sb.append(verticalAsymptotesArray[i]);
			            sb.append("))");
	
			            try {
			     		String limit = kernel.evaluateCachedGeoGebraCAS(sb.toString());
			            //Application.debug("checking for vertical asymptote: "+sb.toString()+" = "+limit,1);
			            if (limit.equals("?") || !GeoFunction.CASError(limit, true,app)) {
			            	if (verticalSB.length() > 1) verticalSB.append(',');
			            	verticalSB.append("x=");
			            	verticalSB.append(verticalAsymptotesArray[i]);
			            }
			            } catch (Throwable e) {
			            	e.printStackTrace();
			            }
		    		}
		   		
		    	}
	    	}
	    		
	    }catch (Throwable t) { t.printStackTrace(); }
	}

	final private static boolean CASError(String str, boolean allowInfinity,AbstractApplication app) {
		if (str == null || str.length()==0) return true;
		if (str.equals("?")) return true; // undefined/NaN
//		if (str.indexOf("%i") > -1 ) return true; // complex answer
		str = app.toLowerCase(str);
		if (str.startsWith("'")) return true; // maxima error eg 'diff(
		if (!allowInfinity && str.indexOf(Unicode.Infinity) > -1) return true;
		if (str.length() > 6) {
			if (str.startsWith("limit")) return true;
			if (str.startsWith("solve")) return true;
			if (str.startsWith("undefined")) return true;
			//if (!allowInfinity && str.indexOf("Infinity") > -1) return true;
		}
		return false;    	
    }
	
    /**
	 * Returns this function in currently set CAS print form.
	 * For example, "a*x^2"
	 */
	@Override
	public String getCASString(boolean symbolic) {
		return fun.getExpression().getCASstring(symbolic);
	}
	
    /**
	 * Returns geo and its function variable in currently set CAS print form
	 * unsing temp variable prefixes.
	 * For example, f(x) = a x^2 returns {"ggbtmpvara ggbtmpvarx^2", "ggbtmpvarx"}
	 */
	final public String [] getTempVarCASString(boolean symbolic) {
		 boolean oldUseTempVariablePrefix = kernel.isUseTempVariablePrefix();
		 kernel.setUseTempVariablePrefix(true);
		 String [] ret = 
		 {
			   getCASString(symbolic),
			   getVarString()
		 };
		 kernel.setUseTempVariablePrefix(oldUseTempVariablePrefix);
		 return ret;
	}
    
	 
	 /**
	  * Converts this function to cartesian curve and stores result to given curve
	  * @param curve Curve to be stored to
	  */
	public void toGeoCurveCartesian(GeoCurveCartesian curve) {   
		FunctionVariable t = new FunctionVariable(kernel,"t");
		FunctionVariable x = fun.getFunctionVariable();
		ExpressionNode yExp = (ExpressionNode)((ExpressionNode)getFunction().getExpression().deepCopy(kernel)).replace(x,t);
		curve.setFunctionY(new Function(yExp,t));
		Function varFun = new Function(new ExpressionNode(kernel,t),t);
		curve.setFunctionX(varFun);
		if(this.hasInterval()){
			curve.setInterval(intervalMin, intervalMax);
		}else{
			double min = app.getXminForFunctions();
			double max = app.getXmaxForFunctions();   
			curve.setInterval(min, max);
			curve.setHideRangeInFormula(true);
		}
	}

	public void dilate(NumberValue r, GeoPoint2 S) {
		double rd=r.getNumber().getDouble(),
		a=S.getX(),
		b=S.getY();
		if(AbstractKernel.isZero(rd)){
			setUndefined();
			return;
		}
		FunctionVariable oldX = fun.getFunctionVariable();
		ExpressionNode newX= new ExpressionNode(kernel,
				new MyDouble(kernel,1/rd),
				Operation.MULTIPLY,
				new ExpressionNode(kernel,oldX,Operation.PLUS,new MyDouble(kernel,a*rd-a)));
		ExpressionNode oldY = fun.getExpression().replaceAndWrap(oldX, newX);
		if(!isBooleanFunction()){
			
		
		fun.setExpression(new ExpressionNode(kernel,
				new ExpressionNode(kernel,oldY,Operation.MULTIPLY,r),
				Operation.PLUS,
				new MyDouble(kernel,-b*rd+b)));
		}else
			fun.setExpression(oldY);
	}
	
	/*
	 * gets shortest distance to point p
	 * for compound paths
	 * (returns *vertical* distance for functions)
	 */
	@Override
	public double distance(GeoPoint2 p) {
		return Math.abs(evaluate(((GeoPoint2)p).getInhomX()) -
				((GeoPoint2)p).getInhomY());
	}

	public boolean isInRegion(GeoPointND P) {
		return isInRegion(P.getX2D(),P.getY2D());
	}

	public boolean isInRegion(double x0, double y0) {
		if(getVarString().equals("y"))
			return evaluateBoolean(y0);	
		return evaluateBoolean(x0);
	}

	public void pointChangedForRegion(GeoPointND PI) {
		GeoPoint2 P = (GeoPoint2) PI;
		
		if (P.getZ() == 1.0) {
			//P.x = P.x;			
		} else {
			P.setX(P.getX() / P.getZ());			
		}
					
		pointChangedBoolean(false,P);
		
		P.setZ(1.0);
		
		// set path parameter for compatibility with
		// PathMoverGeneric
		RegionParameters pp = P.getRegionParameters();
		pp.setT1(P.getX());
		pp.setT2(P.getY());		
	}

	@Override
	public boolean isRegion(){
		return isBooleanFunction();
	}
	
	public void regionChanged(GeoPointND P) {
		pointChangedForRegion(P);		
	}
	
	/**
	 * Reset all inequalities (slow, involves parser)
	 */
	public void resetIneqs(){
		isInequality = fun.initIneqs(getFunctionExpression(),this);
	}
	
	public IneqTree getIneqs() {
		if(fun.getIneqs() == null){
			isInequality = fun.initIneqs(fun.getExpression(),this);			
		}
		return fun.getIneqs();
	}

	/**
	 * For inequalities.
	 * @return true iff should be drawn on x-Axis only
	 */
	public boolean showOnAxis() {		
		return showOnAxis;
	}
	
	/**
	 * For inequalities.
	 * @param showOnAxis true iff should be drawn on x-Axis only
	 */
	public void setShowOnAxis(boolean showOnAxis){
		this.showOnAxis=showOnAxis;
	}
	
	@Override
	public void update(){				
		super.update();
	}
	
	@Override
	public boolean isGeoFunctionBoolean(){
		return isBooleanFunction();
	}
	
	@Override
	public  boolean isLaTeXDrawableGeo(String latexStr) {
		return isLaTeXneeded(latexStr);
	}
	
	@Override
	public String getFormulaString(StringType ExpressionNodeType, boolean substituteNumbers)
	{

		StringType tempCASPrintForm = kernel.getCASPrintForm();
		

		String ret="";
		if (this.isGeoFunctionConditional()) {
			kernel.setCASPrintForm(ExpressionNodeType);
			GeoFunctionConditional geoFun = (GeoFunctionConditional)this;
			if (ExpressionNodeType .equals(StringType.MATH_PIPER)) {

			// get in form If(x<3, etc
			ret = geoFun.toSymbolicString();
			//Application.debug(ret);
			} else if (ExpressionNodeType .equals(StringType.LATEX)) {
				ret = geoFun.conditionalLaTeX(substituteNumbers);								
			}

		} else if (this.isGeoFunction()) {
			kernel.setCASPrintForm(ExpressionNodeType);
			

	 		if (isIndependent()) {
	 			ret = toValueString();
	 		} else {
	 			
	 			if (getFunction() == null) {
	 				ret = app.getPlain("undefined");
	 			} else	 			
		 			ret = substituteNumbers ?
		 					getFunction().toValueString():
		 					getFunction().toString();
	 		}
		}
		else return super.getFormulaString(ExpressionNodeType, substituteNumbers);
		
		// GeoNumeric eg a=1
		if ("".equals(ret) && this.isGeoNumeric() && !substituteNumbers && isLabelSet()) {
			ret = kernel.printVariableName(label);
		}
		if ("".equals(ret) && !this.isGeoText()) {
			// eg Text[ (1,2), false]
			ret = toOutputValueString();
		}

		/* we don't want to deal with list bracess in here since  
		 * GeoList.toOutputValueString() takes care of it */
		
		kernel.setCASPrintForm(tempCASPrintForm);

		if (ExpressionNodeType .equals(StringType.LATEX)) {
			if ("?".equals(ret)) ret = app.getPlain("undefined");
			else if ((Unicode.Infinity+"").equals(ret)) ret = "\\infty";
			else if ((Unicode.MinusInfinity+"").equals(ret)) ret = "-\\infty";
		}

		return ret;

	}
	
	@Override
	public String getRealFormulaString(StringType ExpressionNodeType, boolean substituteNumbers)
	{



		String ret="";
		if (this.isGeoFunctionConditional()) {
			kernel.setCASPrintForm(ExpressionNodeType);
			GeoFunctionConditional geoFun = (GeoFunctionConditional)this;
			if (ExpressionNodeType .equals(StringType.MATH_PIPER)) {

			// get in form If(x<3, etc
			ret = geoFun.toSymbolicString();
			//Application.debug(ret);
			} else if (ExpressionNodeType .equals(StringType.LATEX)) {
				ret = geoFun.conditionalLaTeX(substituteNumbers);								
			}

		}
		if (this.isGeoFunction()) {
			

	 		if (isIndependent()) {
	 			ret = toValueString();
	 		} else {
	 			ret = substituteNumbers ?
	 					getFunction().toValueString():
	 					getFunction().toRealString();
	 		}
		}
		// matrices

		else return super.getRealFormulaString(ExpressionNodeType, substituteNumbers);
		// GeoNumeric eg a=1
		if ("".equals(ret) && this.isGeoNumeric() && !substituteNumbers && isLabelSet()) {
			ret = kernel.printVariableName(label);
		}
		if ("".equals(ret) && !this.isGeoText()) {
			// eg Text[ (1,2), false]
			ret = toOutputValueString();
		}





		return ret;

	}



}
