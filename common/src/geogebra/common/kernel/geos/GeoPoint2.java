/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * GeoPoint.java
 *
 * The point (x,y) has homogenous coordinates (x,y,1)
 *
 * Created on 30. August 2001, 17:39
 */

package geogebra.common.kernel.geos;

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.euclidian.EuclidianStyleConstants;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.AbstractKernel;
import geogebra.common.kernel.Locateable;
import geogebra.common.kernel.LocateableList;
import geogebra.common.kernel.MatrixTransformable;
import geogebra.common.kernel.Path;
import geogebra.common.kernel.PathAlgo;
import geogebra.common.kernel.PathMover;
import geogebra.common.kernel.PathNormalizer;
import geogebra.common.kernel.PathOrPoint;
import geogebra.common.kernel.PathParameter;
import geogebra.common.kernel.Region;
import geogebra.common.kernel.RegionParameters;
import geogebra.common.kernel.Matrix.CoordSys;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.algos.AlgoElement;
import geogebra.common.kernel.algos.AlgoElementInterface;
import geogebra.common.kernel.algos.AlgoDependentPoint;
import geogebra.common.kernel.algos.AlgoDynamicCoordinates;
import geogebra.common.kernel.arithmetic.ExpressionNode;
import geogebra.common.kernel.arithmetic.ExpressionValue;
import geogebra.common.kernel.arithmetic.MyVecNode;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.arithmetic.Operation;
import geogebra.common.kernel.arithmetic.VectorValue;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.main.AbstractApplication;
import geogebra.common.util.MyMath;
import geogebra.common.util.StringUtil;
import geogebra.common.util.Unicode;
import geogebra.common.kernel.AbstractAnimationManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 2D Point
 * @author  Markus
 * @version 
 */
final public class GeoPoint2 extends GeoVec3D 
implements VectorValue, PathOrPoint,
Translateable, PointRotateable, Mirrorable, Dilateable, MatrixTransformable, ConicMirrorable, PointProperties,
GeoPointND, Animatable, Transformable {   	

	// don't set point size here as this would overwrite setConstructionDefaults() 
	// in GeoElement constructor
	//public int pointSize = EuclidianStyleConstants.DEFAULT_POINT_SIZE; 
	public int pointSize;
	private int pointStyle;
	
	private double animationValue;
	
	private Path path;
	private PathParameter pathParameter;
	
	private Region region;
	private RegionParameters regionParameters;
	/** equals x/z when updated*/
	private double x2D = 0;
	/** equals y/z when updated*/
	private double y2D = 0;
        
    // temp
    public double inhomX, inhomY;
    private boolean isInfinite, isDefined;
    private boolean showUndefinedInAlgebraView = true;
    
    // list of Locateables (GeoElements) that this point is start point of
    // if this point is removed, the Locateables have to be notified
    private LocateableList locateableList;         
    
    /**
     * create an undefined GeoPoint
     * @param c
     */
    public GeoPoint2(Construction c) {     	 
    	super(c);
    	setAnimationType(ANIMATION_INCREASING);
    	setUndefined();
    	
    }
  
    /**
     * Creates new GeoPoint 
     * @param c 
     * @param label 
     * @param x 
     * @param y 
     * @param z 
     */  
    public GeoPoint2(Construction c, String label, double x, double y, double z) {               
        this(c, x, y, z); 
        setLabel(label);
    }
    
    public GeoPoint2(Construction c, double x, double y, double z) {               
        super(c, x, y, z); // GeoVec3D constructor
    	setAnimationType(ANIMATION_INCREASING);
    }
    
    public GeoPoint2(Construction c, Path path) {
		super(c);
    	setAnimationType(ANIMATION_INCREASING);
		this.path = path;	
	}
    
    public GeoPoint2(Construction c, Region region) {
		super(c);
		this.region = region;	
	}
    
    @Override
	public void setZero() {
    	setCoords(0,0,1);
    }
    
    final public void clearPathParameter() {
    	pathParameter = null;
    }
    
    final public PathParameter getPathParameter() {
    	if (pathParameter == null)
    		pathParameter = new PathParameter(0);
    	return pathParameter;
    }
    
    
    final public RegionParameters getRegionParameters() {
    	if (regionParameters == null)
    		regionParameters = new RegionParameters();
    	return regionParameters;
    }
    
    
	@Override
	public String getClassName() {
		return "GeoPoint";
	}        
	

    @Override
	public int getRelatedModeID() {
    	return toStringMode == AbstractKernel.COORD_COMPLEX ? EuclidianConstants.MODE_COMPLEX_NUMBER
    			: EuclidianConstants.MODE_POINT;
    }
    
    @Override
	protected String getTypeString() {
    	if (toStringMode == AbstractKernel.COORD_COMPLEX)
    		return "ComplexNumber";
    	else
    		return "Point";
	}
    
    @Override
	public GeoClass getGeoClassType() {
    	return GeoClass.POINT;
    }
    
    public GeoPoint2(GeoPoint2 point) {
    	super(point.cons);    	
        set((GeoElement) point);        
    }
    
    
    public void set(GeoPointND p){
    	set((GeoElement) p);
    }
    
    @Override
	public void set(GeoElement geo) { 
    	if (geo.isGeoPoint()) {
	    	GeoPoint2 p = (GeoPoint2) geo;  
	    	if (p.pathParameter != null) {
	    		pathParameter = getPathParameter();
		    	pathParameter.set(p.pathParameter);
	    	}
	    	animationValue = p.animationValue;
	    	setCoords(p.x, p.y, p.z);     
	    	setMode(p.toStringMode); // complex etc
    	}
    	else if (geo.isGeoVector()) {
    		GeoVector v = (GeoVector) geo; 
    		setCoords(v.getX(), v.getY(), 1d);   
	    	setMode(v.getMode()); // complex etc
    	}else throw new IllegalArgumentException();
    } 
    
    @Override
	public GeoPoint2 copy() {
        return new GeoPoint2(this);        
    }                 
       
    /*
	void initSetLabelVisible() {
		setLabelVisible(true);
	}*/
	
	/**
	 * @param i
	 */
	public void setPointSize(int i) {		
		pointSize = i;
	}

	/**
	 * @return
	 */
	public int getPointSize() {
		return pointSize;
	}
	
	/**
	 * @author Florian Sonner
	 * @version 2008-07-17
	 */
	final public int getPointStyle() {
		return pointStyle;
	}
	
	/**
	 * @author Florian Sonner
	 * @version 2008-07-17
	 * @param style the new style to use
	 */
	public void setPointStyle(int style) {
		
		if (style > -1 && style <= EuclidianStyleConstants.MAX_POINT_STYLE)
			pointStyle = style;
		else
			pointStyle = -1;
		
	}
	
	@Override
	public boolean isChangeable() {
		
		// if we drag a AlgoDynamicCoordinates, we want its point to be dragged
		AlgoElement algo = getParentAlgorithm();
		if (algo != null && algo instanceof AlgoDynamicCoordinates) return true;
		
		// make sure Point[circle, param] is not draggable
		if (algo instanceof PathAlgo) {			
			return ((PathAlgo)algo).isChangeable() && !isFixed();
		}
		
		return !isFixed() && (isIndependent() || isPointOnPath() || isPointInRegion()); 
	}	
	
	@Override
	public boolean moveFromChangeableCoordParentNumbers(Coords rwTransVec, Coords endPosition, Coords viewDirection, ArrayList<GeoElement> updateGeos, ArrayList<GeoElement> tempMoveObjectList){
				
		if (!hasChangeableCoordParentNumbers())
			return false;
		
		
		if (endPosition==null){
			endPosition=getInhomCoords().add(rwTransVec);
		}
			

		// translate x and y coordinates by changing the parent coords accordingly
		ArrayList<GeoNumeric> changeableCoordNumbers = getCoordParentNumbers();					
		GeoNumeric xvar = changeableCoordNumbers.get(0);
		GeoNumeric yvar = changeableCoordNumbers.get(1);

		// polar coords (r; phi)
		if (hasPolarParentNumbers()) {
			// radius
			double radius = MyMath.length(endPosition.getX(), endPosition.getY());
			xvar.setValue(radius);

			// angle
			double angle = AbstractKernel.convertToAngleValue(Math.atan2(endPosition.getY(), endPosition.getX()));
			// angle outsid of slider range
			if (yvar.isIntervalMinActive() && yvar.isIntervalMaxActive() &&
					(angle < yvar.getIntervalMin() || angle > yvar.getIntervalMax())) 
			{
				// use angle value closest to closest border
				double minDiff = Math.abs((angle - yvar.getIntervalMin())) ;
				if (minDiff > Math.PI) minDiff = AbstractKernel.PI_2 - minDiff;
				double maxDiff = Math.abs((angle - yvar.getIntervalMax()));
				if (maxDiff > Math.PI) maxDiff = AbstractKernel.PI_2 - maxDiff;

				if (minDiff < maxDiff) 
					angle = angle - AbstractKernel.PI_2;
				else
					angle = angle + AbstractKernel.PI_2;
			}											
			yvar.setValue(angle);
		}

		// cartesian coords (xvar + constant, yvar + constant)
		else {

			xvar.setValue( xvar.getValue() - inhomX + endPosition.getX());
			yvar.setValue( yvar.getValue() - inhomY + endPosition.getY());
		}

		addChangeableCoordParentNumberToUpdateList((GeoElement)xvar, updateGeos, tempMoveObjectList);
		addChangeableCoordParentNumberToUpdateList((GeoElement)yvar, updateGeos, tempMoveObjectList);

		return true;
	}
	
	/**
	 * Returns whether this point has two changeable numbers as coordinates, 
	 * e.g. point A = (a, b) where a and b are free GeoNumeric objects.
	 */
	@Override
	final public boolean hasChangeableCoordParentNumbers() {
		
		if (isFixed())
			return false;
		
		ArrayList<GeoNumeric> coords = getCoordParentNumbers();		
		if (coords.size() == 0) return false;
		
		GeoNumeric num1 = coords.get(0);
		GeoNumeric num2 = coords.get(1);
		
		if (num1 == null || num2 == null) return false;
		
		GeoElement maxObj1 = num1.getIntervalMaxObject();
		GeoElement maxObj2 = num2.getIntervalMaxObject();
		GeoElement minObj1 = num1.getIntervalMinObject();
		GeoElement minObj2 = num2.getIntervalMinObject();
		if (maxObj1 != null && maxObj1.isChildOrEqual((GeoElement)num2))return false;
		if (minObj1 != null && minObj1.isChildOrEqual((GeoElement)num2))return false;
		if (maxObj2 != null && maxObj2.isChildOrEqual((GeoElement)num1))return false;
		if (minObj2 != null && minObj2.isChildOrEqual((GeoElement)num1))return false;
	
		boolean ret = num1.isChangeable() && 
				num2.isChangeable();

		return ret;
	}
	
	
	
	/**
	 * Returns an array of GeoNumeric objects that directly control this point's coordinates. 
	 * For point P = (a, b) the array [a, b] is returned, for P = (x(A) + c, d + y(A)) 
	 * the array [c, d] is returned, for P = (x(A) + c, y(A)) the array [c, null] is returned.	 
	 * 
	 * @return null if this point is not defined using two GeoNumeric objects
	 */
	final public ArrayList<GeoNumeric> getCoordParentNumbers() {				
		// init changeableCoordNumbers
		if (changeableCoordNumbers == null) {
			changeableCoordNumbers = new ArrayList<GeoNumeric>(2);			
			AlgoElement parentAlgo = getParentAlgorithm();
			
			// dependent point of form P = (a, b)
			if (parentAlgo instanceof AlgoDependentPoint) {
				AlgoDependentPoint algo = (AlgoDependentPoint) parentAlgo;
				ExpressionNode en = algo.getExpressionNode();						
				
				// (xExpression, yExpression)
				if (en.isLeaf() && en.getLeft() instanceof MyVecNode) { 			
					// (xExpression, yExpression)
					MyVecNode vn = (MyVecNode) en.getLeft();
					hasPolarParentNumbers = vn.hasPolarCoords();
										
					try {
						// try to get free number variables used in coords for this point
						// don't allow expressions like "a + x(A)" for polar coords (r; phi)
						ExpressionValue xcoord =vn.getX();
						ExpressionValue ycoord =vn.getY();
						GeoNumeric xvar = getCoordNumber(xcoord, !hasPolarParentNumbers);
						GeoNumeric yvar = getCoordNumber(ycoord, !hasPolarParentNumbers);
						if (!xcoord.contains(yvar) && !ycoord.contains(xvar)) { // avoid (a,a) 
							changeableCoordNumbers.add(xvar);
							changeableCoordNumbers.add(yvar);
						}
					} catch (Throwable e) {
						changeableCoordNumbers.clear();
						e.printStackTrace();						
					}								
				}								
			}
		}
		
		return changeableCoordNumbers;
	}
	private ArrayList<GeoNumeric> changeableCoordNumbers = null;
	private boolean hasPolarParentNumbers = false;
	
	/**
	 * Returns whether getCoordParentNumbers() returns polar variables (r; phi).	
	 */
	public boolean hasPolarParentNumbers() {
		return hasPolarParentNumbers;
	}
	
	/**
	 * Returns the single free GeoNumeric expression wrapped in this ExpressionValue. 
	 * For "a + x(A)" this returns a, for "x(A)" this returns null where A is a free point.
	 * If A is a dependent point, "a + x(A)" throws an Exception.
	 */
	private GeoNumeric getCoordNumber(ExpressionValue ev, boolean allowPlusNode) throws Throwable {
		// simple variable "a"
		if (ev.isLeaf()) {
			GeoElementInterface geo = kernel.lookupLabel(ev.isGeoElement() ? ((GeoElement)ev).getLabel() : ev.toString(), false);
			if (geo != null && geo.isGeoNumeric()) return (GeoNumeric) geo;
			else return null;
		}
		
		// are expressions like "a + x(A)" allowed?
		if (!allowPlusNode) return null;
	
		// return value
		GeoNumeric coordNumeric = null;
		
		// expression + expression
		ExpressionNode en = (ExpressionNode) ev;
		if (en.getOperation().equals(Operation.PLUS) && en.getLeft() instanceof GeoNumeric) {		
			
			// left branch needs to be a single number variable: get it
			// e.g. a + x(D)
			coordNumeric = (GeoNumeric) en.getLeft();
			
			// check that variables in right branch are all independent to avoid circular definitions
			HashSet<GeoElement> rightVars = en.getRight().getVariables();			
			if (rightVars != null) {
				Iterator<GeoElement> it = rightVars.iterator();
				while (it.hasNext()) {			
					GeoElement var = (GeoElement) it.next(); 
					if (var.isChildOrEqual((GeoElement)coordNumeric)) 
						throw new Exception("dependent var: " + var);							
				}
			}
		}			
		
		return coordNumeric;
	}
	
	@Override
	final public boolean isPointOnPath() {
		return path != null;
	}
	
	/**
	 * Returns whether this number can be animated. Only free numbers with min and max interval
	 * values can be animated (i.e. shown or hidden sliders). 
	 */
	@Override
	public boolean isAnimatable() {
		return isPointOnPath() && isChangeable();
	}
	
	public boolean hasPath() {
		return path != null;
	}
	
	final public Path getPath() {
		return path;
	}
	
	public void setPath(Path p) {
		path = p;
		
		// tell conic that this point is on it, that's needed to handle reflections
        // of conics correctly for path parameter calculation of point P
        GeoElement geo = (GeoElement)path.toGeoElement();
        if (geo.isGeoConic()) {
        	((GeoConicInterface) geo).addPointOnConic(this);//GeoConicND
        }   
	}
	
	public void addToPathParameter(double a) {
		PathParameter pathParameter = getPathParameter();
		pathParameter.t += a;
		
		// update point relative to path
		path.pathChanged(this);
		updateCoords();
	}
	
	/**
	 * Returns true if this point's path is a circle or ellipse 	 
	 *
	public boolean hasAnglePathParameter() {
		return (path != null) && 
					(path instanceof GeoConic) &&
					(((GeoConic)path).isElliptic());		
	}*/
    
    @Override
	final public boolean isInfinite() {
       return isInfinite;  
    }
    
    final public boolean isFinite() {
       return isDefined && !isInfinite;
    }
    
    @Override
	final public boolean showInEuclidianView() {               
    	return isDefined && !isInfinite;
    }    
    
    @Override
	public final boolean showInAlgebraView() {
        // intersection points
//        return (isDefined || showUndefinedInAlgebraView) && !isI;
    	return (isDefined || showUndefinedInAlgebraView);
    }   
    
	@Override
	final public boolean isDefined() { 
		return isDefined;        
	}     
	
    @Override
	public void setUndefined() { 
    	super.setUndefined();
    	isDefined = false; 
    }       

	@Override
	final public boolean isFixable() {
		return path != null || super.isFixable();
	}		    
	
    public void setCoords2D(double x, double y, double z){
    	this.x = x;
		this.y = y;
		this.z = z;
    }
    
    
	/** 
	 * Sets homogeneous coordinates and updates
	 * inhomogeneous coordinates
	 */
	@Override
	final public void setCoords(double x, double y, double z) {	
		// set coordinates
		this.x = x;
		this.y = y;
		this.z = z;		
				
		// update point on path: this may change coords
		// so updateCoords() is called afterwards
		if (path != null) {
			// remember path parameter for undefined case
			PathParameter tempPathParameter = getTempPathparameter();
			tempPathParameter.set(getPathParameter());
			path.pointChanged(this);			

			
			// make sure animation starts from the correct place
			animationValue = PathNormalizer.toNormalizedPathParameter(getPathParameter().t, path.getMinParameter(), path.getMaxParameter());
		}
		
		// region
		if (hasRegion()){
			region.pointChangedForRegion(this);
		}
			
		// this avoids multiple computations of inhomogeneous coords;
		// see for example distance()
		updateCoords();
				
		// undefined and on path: remember old path parameter
		if (!isDefined && path != null) {
			PathParameter pathParameter = getPathParameter();
			PathParameter tempPathParameter = getTempPathparameter();
			pathParameter.set(tempPathParameter);
		}		
		
	}  
	
	public void setCoords(Coords v, boolean doPathOrRegion){
		
		if (doPathOrRegion)
			setCoords(v.getX(),v.getY(),v.getLast());
		else{
			// set coordinates
			this.x = v.getX();
			this.y = v.getY();
			this.z = v.getLast();	
		}
		updateCoords();
	}
	
	private PathParameter tempPathParameter;
	private PathParameter getTempPathparameter() {
		if (tempPathParameter == null) {
			tempPathParameter = new PathParameter();
		}
		return tempPathParameter;
	}
	
	final public void updateCoords() {
		// infinite point
		if (AbstractKernel.isZero(z)) {
			isInfinite = true;
			isDefined = !(Double.isNaN(x) || Double.isNaN(y));
			inhomX = Double.NaN;
			inhomY = Double.NaN;
		} 
		// finite point
		else {
			isInfinite = false;
			isDefined = !(Double.isNaN(x) || Double.isNaN(y)
                    					  || Double.isNaN(z));
		
			if (isDefined) {
				// make sure the z coordinate is always positive
				// this is important for the orientation of a line or ray
				// computed using two points P, Q with cross(P, Q)
				if (z < 0) {
					x = -x;
					y = -y;
					z = -z;		
				} 
				
				// update inhomogeneous coords
				if (z == 1.0) {
					inhomX = x;
					inhomY = y;
			    } else {        
					inhomX = x / z;
					inhomY = y / z;                              
			    }
			} else {
				inhomX = Double.NaN;
				inhomY = Double.NaN;
			}
		}	
	}
	
	final public void setPolarCoords(double r, double phi) {        
	   setCoords( r * Math.cos( phi ), r * Math.sin( phi ), 1.0d);        
	}   
	
	@Override
	final public void setCoords(GeoVec3D v) {
		 setCoords(v.x, v.y, v.z);
	 } 
	 
	final public void setCoords(GeoVec2D v) {
		setCoords(v.x, v.y, 1.0);
	}  
	
 
    
    /** 
     * Yields true if the inhomogenous coordinates of this point are equal to
     * those of point P. Infinite points are checked for linear dependency.
     */
	// Michael Borcherds 2008-04-30
    @Override
	final public boolean isEqual(GeoElement geo) {
    	
    	if (!geo.isGeoPoint()) return false;
    	
    	GeoPoint2 P = (GeoPoint2)geo;
    	
        if (!(isDefined() && P.isDefined())) return false;                        
        
        // both finite      
        if (isFinite() && P.isFinite())
			return AbstractKernel.isEqual(inhomX, P.inhomX) && 
                    	AbstractKernel.isEqual(inhomY, P.inhomY);
		else if (isInfinite() && P.isInfinite())
			return linDep(P);
		else return false;                        
    }
        
    /** 
     * Writes (x/z, y/z) to res.
     */
    @Override
	final public void getInhomCoords(double [] res) {
       	res[0] = inhomX;
       	res[1] = inhomY;
    }        	
    
    final public void getPolarCoords(double [] res) {
       	res[0] = MyMath.length(inhomX, inhomY);
       	res[1] = Math.atan2(inhomY, inhomX);
    }
        
    final public double getInhomX() {
       	return inhomX;
    }        	
        
    final public double getInhomY() {
       	return inhomY;
    }     
    
    final public double[] vectorTo(GeoPointND QI){
    	GeoPoint2 Q = (GeoPoint2) QI;
    	return new double[]{
    			Q.getInhomX()-getInhomX(),
    			Q.getInhomY()-getInhomY(),
    			0
    	                  };
    }
    
    @Override
	public double distance(GeoPointND P){
    	//TODO dimension ?
    	return getInhomCoordsInD(3).distance(P.getInhomCoordsInD(3));
    }
    
    // euclidian distance between this GeoPoint and P
    @Override
	final public double distance(GeoPoint2 P) {       
        return MyMath.length(	((GeoPoint2)P).inhomX - inhomX, 
        						((GeoPoint2)P).inhomY - inhomY);
    }            
    
    /** returns the square distance of this point and P (may return
     * infinty or NaN).            
     */
    final public double distanceSqr(GeoPoint2 P) {          
        double vx = P.inhomX - inhomX;
        double vy = P.inhomY - inhomY;        
        return vx*vx + vy*vy;
    }
    
    /** 
     * Returns whether the three points A, B and C are collinear. 
     */
	public static boolean collinear(GeoPoint2 A, GeoPoint2 B, GeoPoint2 C) {
		// A, B, C are collinear iff det(ABC) == 0
		
		// calculate the determinante of ABC
		// det(ABC) = sum1 - sum2		
		
		double sum1 = A.x * B.y * C.z + 
		B.x * C.y * A.z +
		C.x * A.y * B.z;
		double sum2 = A.z * B.y * C.x +
		B.z * C.y * A.x +
		C.z * A.y * B.x;
				
		// det(ABC) == 0  <=>  sum1 == sum2	
		
		// A.z, B.z, C.z could be zero
		double eps = Math.max(AbstractKernel.MIN_PRECISION, AbstractKernel.MIN_PRECISION * A.z
				* B.z * C.z);
		
		return AbstractKernel.isEqual(sum1, sum2, eps );
	}
    
    /**
     * Calcs determinant of P and Q. Note: no test for defined or infinite is done here.
     */
	public static final double det(GeoPoint2 P, GeoPoint2 Q) {		 
		return (P.x * Q.y - Q.x * P.y) / (P.z * Q.z); 
	}	
	
	/**
	 * Returns the affine ratio for three collinear points A, B and C. 
	 * The ratio is lambda with C = A + lambda * AB, i.e. lambda = AC/AB.
	 * Note: the collinearity is not checked in this method.
	 */
	public static final double affineRatio(GeoPoint2 A, GeoPoint2 B, GeoPoint2 C) {		
		double ABx = B.inhomX - A.inhomX;
		double ABy = B.inhomY - A.inhomY;
		
		// avoid division by a number close to zero
		if (Math.abs(ABx) > Math.abs(ABy)) {
			return (C.inhomX - A.inhomX) / ABx;
		} else {
			return (C.inhomY - A.inhomY) / ABy;
		}		
	}
    
/***********************************************************
 * MOVEMENTS
 ***********************************************************/
    
    /**
     * translate by vector v
     */
    final public void translate(Coords v) { 
    		setCoords(x + v.getX() * z, y + v.getY() * z, z); 
    }        
    
	@Override
	final public boolean isTranslateable() {
		return true;
	}
    
    /**
     * dilate from S by r
     */
    final public void dilate(NumberValue rval, GeoPoint2 S) {  
       double r = rval.getDouble();	
       double temp = (1 - r);
       setCoords(r * x + temp * S.getInhomX() * z,
       			 r * y + temp * S.getInhomY() * z,
				 z);    
    } 
    
    /**
     * rotate this point by angle phi around (0,0)
     */
    final public void rotate(NumberValue phiValue) {
    	double phi = phiValue.getDouble();
        double cos = Math.cos(phi);
        double sin = Math.sin(phi);
        
        setCoords( x * cos - y * sin,
      					 x * sin + y * cos,
      					 z );
    }
        
    /**
     * rotate this point by angle phi around Q
     */    
    final public void rotate(NumberValue phiValue, GeoPoint2 Q) {
    	double phi = phiValue.getDouble();
		double cos = Math.cos(phi);
		double sin = Math.sin(phi);   
        double qx = z * Q.getInhomX();
        double qy = z * Q.getInhomY();
        
		setCoords( (x - qx) * cos + (qy - y) * sin + qx,
        				 (x - qx) * sin + (y - qy) * cos + qy,
      					 z );         
    }
    
    /**
     * mirror this point at point Q
     */
    final public void mirror(GeoPoint2 Q) {
		double qx = z * Q.getInhomX();
		double qy = z * Q.getInhomY();
        
        setCoords( 2.0 * qx - x,
        				 2.0 * qy - y,
        				 z );
    }
    
/*
 * Michael Borcherds 2008-02-10
 * Invert point in circle
 */
    final public void mirror(GeoConicInterface c) {
    	if (c.getType()==4/*GeoConic.CONIC_CIRCLE*/)
    	{ // Mirror point in circle
    		double r =  c.getHalfAxes()[0];
    		GeoVec2D midpoint=(GeoVec2D)(c.getTranslationVector());
    		double a=midpoint.x;
    		double b=midpoint.y;
    		if(Double.isInfinite(x)||Double.isInfinite(y2D))
    			setCoords(a,b,1.0);
    		else {
    			double sf=r*r/((inhomX-a)*(inhomX-a)+(inhomY-b)*(inhomY-b));
    			setCoords( a+sf*(inhomX-a), b+sf*(inhomY-b) ,1.0);
    		}
    	}
    	else
    	{
    		setUndefined();
    	}
    }
    
    /**
     * mirror this point at line g
     */
    final public void mirror(GeoLine g) {
        // Y = S(phi).(X - Q) + Q
        // where Q is a point on g, S(phi) is the mirrorTransform(phi)
        // and phi/2 is the line's slope angle
        
        // get arbitrary point of line
        double qx, qy; 
        if (Math.abs(g.getX()) > Math.abs(g.getY())) {
            qx = -z * g.getZ() / g.getX();
            qy = 0.0d;
        } else {
            qx = 0.0d;
            qy = -z * g.getZ() / g.getY();
        }
        
        // translate -Q
        x -= qx;
        y -= qy;        
        
        // S(phi)        
        mirrorXY(2.0 * Math.atan2(-g.getX(), g.getY()));
        
        // translate back +Q
        x += qx;
        y += qy;
        
         // update inhom coords
       updateCoords();
    }
    
  
 
/***********************************************************/
    
    @Override
	final public String toString() {     
    	sbToString.setLength(0);                               
		sbToString.append(label);	
		
		if (toStringMode==AbstractKernel.COORD_COMPLEX) {
			sbToString.append(" = ");
		} else {
			switch (kernel.getCoordStyle()) {
			case AbstractKernel.COORD_STYLE_FRENCH:
				// no equal sign
				sbToString.append(": ");

			case AbstractKernel.COORD_STYLE_AUSTRIAN:
				// no equal sign
				break;

			default: 
				sbToString.append(" = ");
			}
		}
		
		sbToString.append(buildValueString());       
        return sbToString.toString();
    }
    
    @Override
	final public String toStringMinimal() {
    	sbToString.setLength(0);
    	sbToString.append(toValueStringMinimal());
    	return sbToString.toString();
    }
    
    private StringBuilder sbToString = new StringBuilder(50);        
    
    @Override
	final public String toValueString() {
    	return buildValueString().toString();	
    }       
    
	@Override
	final public String toValueStringMinimal() {
		sbBuildValueString.setLength(0);
		if (isInfinite()) {
			sbBuildValueString.append(app.getPlain("undefined"));
			return sbBuildValueString.toString();
		}
		sbBuildValueString
				.append(regrFormat(inhomX) + " " + regrFormat(inhomY));
		return sbBuildValueString.toString();
	} 
    
	private StringBuilder buildValueString() { 
		sbBuildValueString.setLength(0);
		
		switch (kernel.getCASPrintForm()) {
			case MATH_PIPER:
				sbBuildValueString.append("{");
				sbBuildValueString.append(getInhomX());
				sbBuildValueString.append(", ");
				sbBuildValueString.append(getInhomY());
				sbBuildValueString.append("}");
				return sbBuildValueString;
		
			case MAXIMA:
				sbBuildValueString.append("[");
				sbBuildValueString.append(getInhomX());
				sbBuildValueString.append(", ");
				sbBuildValueString.append(getInhomY());
				sbBuildValueString.append("]");
				return sbBuildValueString;
				
			case MPREDUCE:
				if (toStringMode==AbstractKernel.COORD_COMPLEX){
					sbBuildValueString.append("(");
					sbBuildValueString.append(getInhomX());
					sbBuildValueString.append("+i*");
					sbBuildValueString.append(getInhomY());
					sbBuildValueString.append(")");
				} else {
					sbBuildValueString.append("list(");
					sbBuildValueString.append(getInhomX());
					sbBuildValueString.append(",");
					sbBuildValueString.append(getInhomY());
					sbBuildValueString.append(")");
				}
				return sbBuildValueString;
				
			default: // continue below
		}
		
    	if (isInfinite()) {
			sbBuildValueString.append(app.getPlain("undefined"));
			return sbBuildValueString;
    	}
			
        switch (toStringMode) {
        case AbstractKernel.COORD_POLAR:                                            
    		sbBuildValueString.append('(');    
			sbBuildValueString.append(kernel.format(MyMath.length(getInhomX(), getInhomY())));
			sbBuildValueString.append("; ");
			sbBuildValueString.append(kernel.formatAngle(Math.atan2(getInhomY(), getInhomX())));
			sbBuildValueString.append(')');
            break;                                
                        
        case AbstractKernel.COORD_COMPLEX:                    
        	//if (!isI) { // return just "i" for special i
				sbBuildValueString.append(kernel.format(getInhomX()));
				sbBuildValueString.append(" ");
				sbBuildValueString.append(kernel.formatSignedCoefficient(getInhomY()));
        	//}
			sbBuildValueString.append(Unicode.IMAGINARY);
            break;                                
                        
           default: // CARTESIAN                
       			sbBuildValueString.append('(');    
				sbBuildValueString.append(kernel.format(getInhomX()));
				switch (kernel.getCoordStyle()) {
					case AbstractKernel.COORD_STYLE_AUSTRIAN:
						sbBuildValueString.append(" | ");
						break;
					
					default:
						sbBuildValueString.append(AbstractApplication.unicodeComma);												
						sbBuildValueString.append(" ");												
				}
				sbBuildValueString.append(kernel.format(getInhomY()));                                
				sbBuildValueString.append(')');
        }        
		return sbBuildValueString;
    }
	private StringBuilder sbBuildValueString = new StringBuilder(50);   
    
    /**
     * interface VectorValue implementation
     */    
    public GeoVec2D getVector() {
        GeoVec2D ret = new GeoVec2D(kernel, inhomX, inhomY);
        ret.setMode(toStringMode);
        return ret;
    }        
        
    @Override
	public boolean isConstant() {
        return false;
    }
    
    @Override
	public boolean isLeaf() {
        return true;
    }
    
    @Override
	public HashSet<GeoElement> getVariables() {
    	HashSet<GeoElement> varset = new HashSet<GeoElement>();        
        varset.add(this);        
        return varset;          
    }
        
    
    /** POLAR or CARTESIAN */
  
    @Override
	public ExpressionValue evaluate() { return this; }
      
    
    /**
     * returns all class-specific xml tags for saveXML
     * GeoGebra File Format
     */
	@Override
	protected void getXMLtags(StringBuilder sb) {
        super.getXMLtags(sb); 
        
        /* should not be needed
        if (path != null) {        	
        	pathParameter.appendXML(sb);
        }*/
        	       
        // polar or cartesian coords
        switch(toStringMode) {
        case AbstractKernel.COORD_POLAR:
            sb.append("\t<coordStyle style=\"polar\"/>\n");
            break;

        case AbstractKernel.COORD_COMPLEX:
            sb.append("\t<coordStyle style=\"complex\"/>\n");
            break;

            default:
            	// don't save default
               // sb.append("\t<coordStyle style=\"cartesian\"/>\n");
        }
        
		// point size
		sb.append("\t<pointSize val=\"");
			sb.append(pointSize);
		sb.append("\"/>\n");
		
    
		// point style, Florian Sonner 2008-07-17
		if (pointStyle >= 0) {
			sb.append("\t<pointStyle val=\"");
				sb.append(pointStyle);
			sb.append("\"/>\n");
		}
 
    }
	
    public String getStartPointXML() {
    	StringBuilder sb = new StringBuilder();    	
		sb.append("\t<startPoint ");
		
    	if (isAbsoluteStartPoint()) {		
			sb.append(" x=\"" + x + "\"");
			sb.append(" y=\"" + y + "\"");
			sb.append(" z=\"" + z + "\"");			
    	} else {
			sb.append("exp=\"");
			boolean oldValue = kernel.isPrintLocalizedCommandNames();
			kernel.setPrintLocalizedCommandNames(false);
			sb.append(StringUtil.encodeXML(getLabel()));
			kernel.setPrintLocalizedCommandNames(oldValue);
			sb.append("\"");			    	
    	}
		sb.append("/>\n");
		return sb.toString();
    }
    
	final public boolean isAbsoluteStartPoint() {
		return isIndependent() && !isLabelSet();
	}
    
	@Override
	public boolean isNumberValue() {
		return false;
	}

	@Override
	public boolean isVectorValue() {
		return true;
	}

	@Override
	public boolean isPolynomialInstance() {
		return false;
	}   
	
	@Override
	public boolean isTextValue() {
		return false;
	}   
	
	 
	/**
	 * Calls super.update() and updateCascade() for all registered locateables.	 
	 */
	@Override
	public void update() {  	
		super.update();
						
		// update all registered locatables (they have this point as start point)
		if (locateableList != null) {	
			GeoElement.updateCascade(locateableList, getTempSet(), false);
		}			
	}
	
	private static volatile TreeSet<AlgoElementInterface> tempSet;	
	protected static TreeSet<AlgoElementInterface> getTempSet() {
		if (tempSet == null) {
			tempSet = new TreeSet<AlgoElementInterface>();
		}
		return tempSet;
	}
	
	
	public LocateableList getLocateableList(){
		if (locateableList == null)
			locateableList = new LocateableList(this);
		return locateableList;
	}
	
	/*
	/**
	 * Tells this point that the given Locateable has this point
	 * as start point.
	 *
	public void registerLocateable(Locateable l) {	
		if (locateableList == null) locateableList = new ArrayList();
		if (locateableList.contains(l)) return;
		
		// add only locateables that are not already
		// part of the updateSet of this point
		AlgoElement parentAlgo = l.toGeoElement().getParentAlgorithm();
		if (parentAlgo == null ||
			!(getAlgoUpdateSet().contains(parentAlgo))) {
			// add the locatable
			locateableList.add(l);			
		}
	}
	
	/**
	 * Tells this point that the given Locatable no longer has this point
	 * as start point.
	 *
	public void unregisterLocateable(Locateable l) {
		if (locateableList != null) {
			locateableList.remove(l);			
		}
	}
	
	*/
	
	/**
	 * Tells Locateables that their start point is removed
	 * and calls super.remove()
	 */
	@Override
	public void doRemove() {
		if (locateableList != null) {
			
			locateableList.doRemove();
			
			/*
			// copy locateableList into array
			Object [] locs = locateableList.toArray();	
			locateableList.clear();
			
			// tell all locateables 
			for (int i=0; i < locs.length; i++) {		
				Locateable loc = (Locateable) locs[i];
				loc.removeStartPoint(this);				
				loc.toGeoElement().updateCascade();			
			}	
			*/		
		}
		
		//TODO: remove this part because the path should be in incidenceList already.
		if (path != null) {
			GeoElement geo = (GeoElement)path.toGeoElement();
			if (geo.isGeoConic()) {
				((GeoConicInterface) geo).removePointOnConic(this);//GeoConicND
			}
		}
		
		//TODO: modify this using removeIncidence
		if (incidenceList!=null) {
			for (int i=0; i<incidenceList.size(); ++i) {
				GeoElement geo = incidenceList.get(i);
				if (geo.isGeoConic()) {
					((GeoConicInterface) geo).removePointOnConic(this);//GeoConicND
				} else if (geo.isGeoLine()) {
					((GeoLine) geo).removePointOnLine(this);
				}
			}
		}
		
		super.doRemove();
	}
	
	@Override
	public void setVisualStyle(GeoElement geo) {
		super.setVisualStyle(geo);
		if (geo.isGeoPoint()) {
			pointSize = ((GeoPointND) geo).getPointSize();
			pointStyle = ((GeoPointND) geo).getPointStyle();
		}		
		else if (geo instanceof PointProperties) {
			setPointSize(((PointProperties)geo).getPointSize());
			setPointStyle(((PointProperties)geo).getPointStyle());
		}
	}
    
	@Override
	final public boolean isGeoPoint() {
		return true;
	}
	
	public void showUndefinedInAlgebraView(boolean flag) {
		showUndefinedInAlgebraView = flag;
	}
	
	/**
	 * Returns a comparator for GeoPoint objects.
	 * (sorts on X coordinate)
	 * If equal, doesn't return zero (otherwise TreeSet deletes duplicates)
	 * @return comparator for GeoPoint objects.
	 */
	public static Comparator<GeoPoint2> getComparatorX() {
		if (comparatorX == null) {
			comparatorX = new Comparator<GeoPoint2>() {
				public int compare(GeoPoint2 itemA, GeoPoint2 itemB) {
		        
						double compX = itemA.inhomX - itemB.inhomX;
	
						if (AbstractKernel.isZero(compX)) {
							double compY = itemA.inhomY - itemB.inhomY;
							
							// if x-coords equal, sort on y-coords
							if (!AbstractKernel.isZero(compY))
								return compY < 0 ? -1 : +1;
							
							// don't return 0 for equal objects, otherwise the TreeSet deletes duplicates
							return itemA.getConstructionIndex() > itemB.getConstructionIndex() ? -1 : 1;
						}
						else
						{
							return compX < 0 ? -1 : +1;
						}
					}
				};
			
			}
		
			return comparatorX;
		}
	  private static volatile Comparator<GeoPoint2> comparatorX;
    
	    
	    /////////////////////////////////////////////
	    // REGION

		
		@Override
		final public boolean isPointInRegion() {
			return region != null;
		}
		
	    public boolean hasRegion() {
	    	return region != null;
	    }

	    public Region getRegion() {
	    	return region;
	    }
	    
	    public void setRegion(Region a_region){
	    	region=a_region;
	    }

		@Override
		public boolean isVector3DValue() {
			return false;
		}
	    
		
		public void updateCoords2D(){
			x2D = x/z;
			y2D = y/z;
		}
		
		public double getX2D(){
			return x2D;
		}
		
		public double getY2D(){
			return y2D;
		}
		
		//only used for 3D stuff
		public void updateCoordsFrom2D(boolean doPathOrRegion, CoordSys coordsys){
		}
		
		public Coords getInhomCoords(){
			return new Coords(new double[] {inhomX, inhomY});
		}

		public Coords getInhomCoordsInD(int dimension){
			switch(dimension){
			case 2:
				return getInhomCoords();
			case 3:
			/*
			  	if (label!=null && label.equals("S3'"))
					Application.debug(label+": "+inhomX+","+inhomY);
					*/
				return new Coords(inhomX,inhomY,0,1);
			default:
				return null;
			}
		}
		
		public Coords getCoordsInD2(CoordSys coordSys){ //TODO use coord sys ?
			return new Coords(new double[] {x,y,z});
		}
		
		public Coords getCoordsInD(int dimension){
			switch(dimension){
			case 2:
				return new Coords(new double[] {x,y,z});
			case 3:
				//Application.debug(getLabel()+": "+x+","+y+","+z);
				return new Coords(x,y,0,z);
			default:
				return null;
			}
		}		
		
		@Override
		public boolean isMatrixTransformable() {
			return true;
		}

		public void matrixTransform(double a,double b,double c,double d) {
	 
			Double x1 = a*x + b*y;
			Double y1 = c*x + d*y;

			setCoords(x1, y1, z);
		}
		
		
		//////////////////////////////////////
		// 3D stuff
		//////////////////////////////////////
		
	  	@Override
		public boolean hasDrawable3D() {
			return true;
		}
	    
	  	@Override
		public Coords getLabelPosition(){
	  		/*
	  		Coords v = new Coords(4);
	  		v.set(getInhomCoordsInD(3));
	  		v.setW(1);
	  		*/
	  		return getInhomCoordsInD(3);
		}

		public void pointChanged(GeoPointND p) {
			p.setCoords2D(x, y, z);
			
			p.getPathParameter().setT(0);
			
		}

		public void pathChanged(GeoPointND PI) {
			PI.setCoords(x, y, z);
			PI.getPathParameter().setT(0);
			
		}

		public boolean isOnPath(GeoPointND PI, double eps) {
			return isEqual((GeoElement) PI);
		}

		public double getMinParameter() {
			return 0;
		}

		public double getMaxParameter() {
			return 0;
		}

		public boolean isClosedPath() {
			return false;
		}

		public PathMover createPathMover() {
			return null;
		}

		/**
		 * Performs the next automatic animation step for this numbers. This changes
		 * the value but will NOT call update() or updateCascade().
		 * 
		 * @return whether the value of this number was changed
		 */
		final public synchronized boolean doAnimationStep(double frameRate) {
			
			
			PathParameter pp = getPathParameter();
			
			// remember old value of parameter to decide whether update is necessary
			double oldValue = pp.t;
			
			// compute animation step based on speed and frame rates
			double intervalWidth = 1;
			double step = intervalWidth * getAnimationSpeed() * getAnimationDirection() /
					      (AbstractAnimationManager.STANDARD_ANIMATION_TIME * frameRate);
			
			// update animation value
			if (Double.isNaN(animationValue))
				animationValue = oldValue;
			animationValue = animationValue + step;
			
			// make sure we don't get outside our interval
			switch (getAnimationType()) {		
				case GeoElement.ANIMATION_DECREASING:
				case GeoElement.ANIMATION_INCREASING:
					// jump to other end of slider
					if (animationValue > 1) 
						animationValue = animationValue - intervalWidth;
					else if (animationValue < 0) 
						animationValue = animationValue + intervalWidth;		
					break;
				
				case GeoElement.ANIMATION_INCREASING_ONCE:
					// stop if outside range
					if (animationValue > 1) {
						animationValue = 1;
						setAnimating(false);
					} else if (animationValue < 0) {
						animationValue = 0;
						setAnimating(false);
					}
					break;
				
			case GeoElement.ANIMATION_OSCILLATING:
				default: 		
					if (animationValue >= 1) {
						animationValue = 1;
						changeAnimationDirection();
					} 
					else if (animationValue <= 0) {
						animationValue = 0;
						changeAnimationDirection();			
					}		
					break;
			}
						
			// change slider's value without changing animationValue
			pp.t = PathNormalizer.toParentPathParameter(animationValue, path.getMinParameter(), path.getMaxParameter());
			
			// return whether value of slider has changed
			if (pp.t != oldValue) {
		        path.pathChanged(this);
		        updateCoords();
		        return true;
			} else {
				return false;
			}
		}	
		
		
		/////////////////////////////////////////
		// MOVING THE POINT (3D)
		/////////////////////////////////////////
		
		

		public void switchMoveMode(){
			
		}
		

		public int getMoveMode(){
			if (!isIndependent() || isFixed())
				return MOVE_MODE_NONE;
			else if (hasPath())
				return MOVE_MODE_Z;
			else
				return MOVE_MODE_XY;
		}
		

		@Override
		final public boolean isCasEvaluableObject() {
			return true;
		}
		
//		// reserved for the constant sqrt(-1)
//		boolean isI = false;
//
//		public boolean isI() {
//			return isI;
//		}
//		
//		public void setIsI() {
//			isI = true;
//		}
		
		@Override
		public boolean isFixed() {
//			return fixed && !isI;
			return fixed;
		}

		public void matrixTransform(double a00, double a01, double a02,
				double a10, double a11, double a12, double a20, double a21,
				double a22) {

	 
			double x1 = a00 * x + a01 * y + a02 * z;
			double y1 = a10 * x + a11 * y + a12 * z;
			double z1 = a20 * x + a21 * y + a22 * z;			
			setCoords(x1,y1,z1);
			
			
		}

		public void removePath() {
			path = null;
			pathParameter = null;			
		}

		// needed for GeoPointND interface for 3D, do nothing
		public void setCoords(double x, double y, double z, double w) {
		}
		
		@Override
		public void moveDependencies(GeoElement oldGeo) {
			if (oldGeo.isGeoPoint()
					&& ((GeoPoint2) oldGeo).locateableList != null) {

				locateableList = ((GeoPoint2) oldGeo).locateableList;
				for (Locateable loc : locateableList){ 				
					GeoPointND[]pts = loc.getStartPoints();
					for(int i=0;i<pts.length;i++)
						if(pts[i]== (GeoPoint2)oldGeo)
							pts[i] = this;
					loc.toGeoElement().updateRepaint();
				}				
				((GeoPoint2) oldGeo).locateableList = null;
			}
		}

		
		//for identifying incidence by construction
		//case by case.
		//currently implemented for
		// lines: line by two point, intersect lines, line/conic, point on line
		//TODO: parallel line, perpenticular line
		private ArrayList<GeoElement> incidenceList;
		public ArrayList<GeoElement> nonIncidenceList;
		
		public ArrayList<GeoElement> getIncidenceList(){
			return incidenceList;
		}
		public void setIncidenceList(ArrayList<GeoElement> list){
			incidenceList = new ArrayList<GeoElement>(list);
		}
		
		/**
		 * initialize incidenceList, and add the point itself
		 * to the list as the first element.
		 */
		public void createIncidenceList(){
			incidenceList = new ArrayList<GeoElement>();
			incidenceList.add(this);
		}
		public void createNonIncidenceList(){
			nonIncidenceList = new ArrayList<GeoElement>();
		}	
		/**
		 * add geo to incidenceList of this, and also
		 * add this to pointsOnConic (when geo is a conic) or
		 * to pointsOnLine (when geo is a line)
		 * @param geo
		 */
		public void addIncidence(GeoElement geo) {
			if (incidenceList==null)
				createIncidenceList();
			if (!incidenceList.contains(geo))
				incidenceList.add(geo);
			
			//GeoConicND, GeoLine, GeoPoint are the three types who have an incidence list 
			if (geo.isGeoConic())
				((GeoConicInterface)geo).addPointOnConic(this);//GeoConicND
			else if (geo.isGeoLine())
				((GeoLine)geo).addPointOnLine(this);
			//TODO: if geo instanceof GeoPoint...
		}
		
		public void addNonIncidence(GeoElement geo) {
			if (nonIncidenceList==null)
				createNonIncidenceList();
			if (!nonIncidenceList.contains(geo))
				nonIncidenceList.add(geo);
		}
		
		public final void removeIncidence(GeoElement geo) {
			if (incidenceList!=null)
				incidenceList.remove(geo);
			
			if (geo.isGeoConic())
				((GeoConicInterface)geo).removePointOnConic(this);//GeoConicND
			else if (geo.isGeoLine())
				((GeoLine)geo).removePointOnLine(this);
			//TODO: if geo instanceof GeoPoint...
		}
		public boolean addIncidenceWithProbabilisticChecking(GeoElement geo) {
			boolean incident = false;
			
			// check if this is currently on geo
			if (geo.isGeoPoint() && this.isEqual(geo) ||
					geo.isPath() && ((Path) geo).isOnPath(this, AbstractKernel.EPSILON)) {
			
				incident = true;
				
				//get all "randomizable" predecessors of this and geo
				TreeSet<GeoElement> pred = this.getAllRandomizablePredecessors();
				ArrayList<GeoElement> predList = new ArrayList<GeoElement>();
				TreeSet<AlgoElementInterface> tempSet = new TreeSet<AlgoElementInterface>();
				
				predList.addAll(pred);
				pred.addAll(geo.getAllRandomizablePredecessors());
				
				// store parameters of current construction
				Iterator<GeoElement> it = pred.iterator();
				while (it.hasNext()) {
					GeoElement predGeo = it.next();
					predGeo.storeClone();
				}
				
				// alter parameters of construction and test if this is still on geo. Do it N times
				for (int i = 0; i<5; ++i) {
					it = pred.iterator();
					while (it.hasNext()) {
						GeoElement predGeo = it.next();
						predGeo.randomizeForProbabilisticChecking();
					}
					
					GeoElement.updateCascadeUntil(predList, new TreeSet<AlgoElementInterface>(), this.algoParent);
					GeoElement.updateCascadeUntil(predList, new TreeSet<AlgoElementInterface>(), geo.algoParent);
					/*
					if (!this.isFixed())
						this.updateCascade();
					if (!geo.isFixed())
						geo.updateCascade();
						*/
					
					if (geo.isGeoPoint()) {
						if (!this.isEqual(geo)) incident = false;
					} else if (geo.isPath()) {
						if (!((Path) geo).isOnPath(this, AbstractKernel.EPSILON))
							incident = false;
					} else {
						incident = false;
					}
					if (!incident) break;
				}

				// recover parameters of current construction
				it = pred.iterator();
				while (it.hasNext()) {
					GeoElement predGeo = it.next();
					if ( !predGeo.isIndependent()) {
						GeoElement.updateCascadeUntil(predList, tempSet, predGeo.algoParent);
					}
					predGeo.recoverFromClone();
				}
				
				//this does not work!
				//if (!this.isFixed())
				//this.updateCascade();
				//if (!geo.isFixed())
				//geo.updateCascade();
				
				GeoElement.updateCascade(predList, tempSet, false);
				
				// if all of the cases are good, add incidence
				if (incident)
					addIncidence(geo);
				else
					addNonIncidence(geo);
			}
			
			return incident;
		}	
		
		@Override
		public boolean isRandomizable() {
			return isChangeable();
		}
		
		@Override
		public void randomizeForProbabilisticChecking(){
			setCoords(x + (Math.random() *2 -1) *z,
					y + (Math.random() *2 -1) *z,
					z);
		}		
		
		public void randomizeForErrorEstimation(){
			setCoords(x + (Math.random() *2 -1) *AbstractKernel.EPSILON_SQRT *z,//TODO: record the error of the point
					y + (Math.random() *2 -1) *AbstractKernel.EPSILON_SQRT *z,
					z);
		}	
		
		
		public void setParentAlgorithm(AlgoElement algorithm) {
			super.setParentAlgorithm(algorithm);			
			if (algorithm != null)
				setConstructionDefaults(); // set colors to dependent colors
		}
		
		public boolean movePoint(Coords a,Coords b){
			return super.movePoint(a, b);
		}


		public void setX(double x) {
			this.x = x;
			
		}

		public void setY(double y) {
			this.y = y;
			
		}

		public void setZ(double z) {
			this.z = z;
		}
}
