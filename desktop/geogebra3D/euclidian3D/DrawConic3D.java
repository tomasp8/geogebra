package geogebra3D.euclidian3D;

import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.arithmetic.Functional2Var;
import geogebra.common.kernel.geos.GeoConic;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoConicND;
import geogebra.euclidian.Previewable;
import geogebra.main.Application;
import geogebra3D.euclidian3D.opengl.PlotterBrush;
import geogebra3D.euclidian3D.opengl.PlotterSurface;
import geogebra3D.euclidian3D.opengl.Renderer;
import geogebra3D.kernel3D.GeoConic3D;

/**
 * @author ggb3D
 * 
 * Drawable for GeoConic3D
 *
 */
public class DrawConic3D extends Drawable3DCurves implements Functional2Var, Previewable {
	
	
	
	
	
	
	
	
	/**
	 * @param view3d the 3D view where the conic is drawn
	 * @param conic the conic to draw
	 */
	public DrawConic3D(EuclidianView3D view3d, GeoConicND conic) {
		super(view3d,conic);
	}

	

	protected void updateColors(){
		updateAlpha();
		setColorsOutlined();
	}
	
	
	
	

	public void drawGeometry(Renderer renderer) {
		
		GeoConicND conic = (GeoConicND) getGeoElement();
		
		switch(conic.getType()){
		case GeoConic.CONIC_CIRCLE:
		case GeoConic.CONIC_ELLIPSE:
		case GeoConic.CONIC_HYPERBOLA:
		case GeoConic.CONIC_PARABOLA:
		case GeoConic.CONIC_DOUBLE_LINE:
		case GeoConic.CONIC_INTERSECTING_LINES:
		case GeoConic.CONIC_PARALLEL_LINES:
		case GeoConic.CONIC_SINGLE_POINT:
			renderer.getGeometryManager().draw(getGeometryIndex());
			break;
		default:
			break;
		
		}

	}

	


	
	

	// method used only if surface is not transparent
	public void drawNotTransparentSurface(Renderer renderer){
		
		if(!isVisible()){
			return;
		}
		

		if (getAlpha()<1)
			return;
		

		setLight(renderer);
		setSurfaceHighlightingColor();

		drawSurfaceGeometry(renderer);
		

		
	}


	
	

	private double acosh(double x){
		if (x<=1)
			return 0;
		else
			return Math.log(x+Math.sqrt(x*x-1));
	}
	

	protected boolean updateForItSelf(){

		//update alpha value
		updateColors();
    	
		Renderer renderer = getView3D().getRenderer();
		
		GeoConicND conic = (GeoConicND) getGeoElement();
		
		
		// outline
		
		if (conic.getType()==GeoConic.CONIC_SINGLE_POINT){
			
			PlotterSurface surface;

			surface = renderer.getGeometryManager().getSurface();
			surface.start(this);
			//number of vertices depends on point size
			int nb = 2+conic.getLineThickness();
			surface.setU((float) getMinParameter(0), (float) getMaxParameter(0));surface.setNbU(2*nb); 
			surface.setV((float) getMinParameter(1), (float) getMaxParameter(1));surface.setNbV(nb);
			surface.draw();
			setGeometryIndex(surface.end());
			
		}else{

			PlotterBrush brush = renderer.getGeometryManager().getBrush();	
			brush.start(8);

			brush.setThickness(getGeoElement().getLineThickness(),(float) getView3D().getScale());		


			brush.setAffineTexture(0f,0f);
			double tMax;
			

			Coords m;
			Coords d;
			Coords ev1, ev2;
			double e1, e2;
			double[] minmax;
			
			switch(conic.getType()){
			case GeoConic.CONIC_CIRCLE:
				updateCircle(brush);
				//Application.debug(m.toString()+"\n2D:\n"+conic.getMidpoint2D().toString());
				break;
			case GeoConic.CONIC_ELLIPSE:
				updateEllipse(brush);
				break;
			case GeoConic.CONIC_HYPERBOLA:
				m = conic.getMidpoint3D();
				ev1 = conic.getEigenvec3D(0);
				ev2 = conic.getEigenvec3D(1);
				e1 = conic.getHalfAxis(0);
				e2 = conic.getHalfAxis(1);
				minmax = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						getView3D().getToScreenMatrix().mul(m), getView3D().getToScreenMatrix().mul(ev1.mul(e1).add(ev2.mul(e2))), true);				
				double[] minmax2 = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						getView3D().getToScreenMatrix().mul(m), getView3D().getToScreenMatrix().mul(ev1.mul(e1).add(ev2.mul(-e2))), true);

				tMax=acosh(minmax2[1])*1.1; //extends a little
				brush.quarterHyperbola(m, ev1, ev2.mul(-1), e1, e2,tMax);
				tMax=acosh(minmax[1])*1.1;
				brush.quarterHyperbola(m, ev1, ev2, e1, e2,tMax);
				tMax=acosh(-minmax[0])*1.1;
				brush.quarterHyperbola(m, ev1.mul(-1), ev2.mul(-1), e1, e2,tMax);
				tMax=acosh(-minmax2[0])*1.1;
				brush.quarterHyperbola(m, ev1.mul(-1), ev2, e1, e2,tMax);
				break;
			case GeoConic.CONIC_PARABOLA:
				m = conic.getMidpoint3D();
				ev1 = conic.getEigenvec3D(0);
				ev2 = conic.getEigenvec3D(1);
				double p = conic.p;
				minmax = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						getView3D().getToScreenMatrix().mul(m), getView3D().getToScreenMatrix().mul(ev1), true);	
				tMax=Math.sqrt(2*minmax[1]/p);
				//Application.debug("max="+minmax[1]+", tMax="+tMax);
				//tMax=4;
				brush.parabola(m, ev1, ev2, p,-2*tMax,2*tMax);
				break;
			case GeoConic.CONIC_DOUBLE_LINE:
				d = conic.getDirection3D(0);
				m = conic.getMidpoint3D();
				minmax = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						m, d, true);
				brush.segment(m.add(d.mul(minmax[0])), m.add(d.mul(minmax[1])));
				break;
			case GeoConic.CONIC_INTERSECTING_LINES:
				m = conic.getMidpoint3D();
				
				d = conic.getDirection3D(0);
				minmax = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						getView3D().getToScreenMatrix().mul(m), getView3D().getToScreenMatrix().mul(d), true);
				brush.segment(m.add(d.mul(minmax[0])), m.add(d.mul(minmax[1])));

				d = conic.getDirection3D(1);
				minmax = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						getView3D().getToScreenMatrix().mul(m), getView3D().getToScreenMatrix().mul(d), true);
				brush.segment(m.add(d.mul(minmax[0])), m.add(d.mul(minmax[1])));
				break;
			case GeoConic.CONIC_PARALLEL_LINES:
				m = conic.getOrigin3D(0);
				d = conic.getDirection3D(0);
				minmax = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						getView3D().getToScreenMatrix().mul(m), getView3D().getToScreenMatrix().mul(d), true);
				brush.segment(m.add(d.mul(minmax[0])), m.add(d.mul(minmax[1])));

				m = conic.getOrigin3D(1);
				d = conic.getDirection3D(1);
				minmax = getView3D().getRenderer().getIntervalInFrustum(
						new double[] {Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},
						getView3D().getToScreenMatrix().mul(m), getView3D().getToScreenMatrix().mul(d), true);
				brush.segment(m.add(d.mul(minmax[0])), m.add(d.mul(minmax[1])));
				break;
			default:
				break;

			}

			setGeometryIndex(brush.end());
			
			// surface
			PlotterSurface surface = renderer.getGeometryManager().getSurface();
			surface.start();
			
			switch(conic.getType()){
			case GeoConic.CONIC_CIRCLE:
				//surface.disc(conic.getMidpoint3D(), conic.getEigenvec3D(0), conic.getEigenvec3D(1), conic.getHalfAxis(0));
				//break;
			case GeoConic.CONIC_ELLIPSE:
				surface.ellipsePart(conic.getMidpoint3D(), conic.getEigenvec3D(0), conic.getEigenvec3D(1), conic.getHalfAxis(0), conic.getHalfAxis(1),getStart(),getExtent());
				break;
			default:
				break;
			
			}
			
			setSurfaceIndex(surface.end());
		}
		
		
		
		return true;
	}
	
	
	protected double getStart(){
		return 0;
	}
	
	protected double getExtent(){
		return 2*Math.PI;
	}
	
	
	protected void updateCircle(PlotterBrush brush){
		
		GeoConicND conic = (GeoConicND) getGeoElement();
		Coords m = conic.getMidpoint3D();
		brush.circle(m, conic.getEigenvec3D(0), conic.getEigenvec3D(1), conic.getHalfAxis(0));

	}
	
	protected void updateEllipse(PlotterBrush brush){
		GeoConicND conic = (GeoConicND) getGeoElement();
		Coords m = conic.getMidpoint3D();
		brush.arcEllipse(m, conic.getEigenvec3D(0), conic.getEigenvec3D(1), conic.getHalfAxis(0), conic.getHalfAxis(1),0,2*Math.PI);

	}

	protected void updateForView(){
		if (getView3D().viewChanged())
			switch(((GeoConicND) getGeoElement()).getType()){
			case GeoConic.CONIC_DOUBLE_LINE:
			case GeoConic.CONIC_HYPERBOLA:
			case GeoConic.CONIC_INTERSECTING_LINES:
			case GeoConic.CONIC_LINE:
			case GeoConic.CONIC_PARABOLA:
			case GeoConic.CONIC_PARALLEL_LINES:
				updateForItSelf();
				break;
			case GeoConic.CONIC_CIRCLE:
			case GeoConic.CONIC_ELLIPSE:
			case GeoConic.CONIC_SINGLE_POINT:
				if (getView3D().viewChangedByZoom()) //update only if zoom occurred
					updateForItSelf();
				break;
			}
		
	}
	
	
	
	public int getPickOrder() {
		return DRAW_PICK_ORDER_1D;
	}
	
	
	
	public void addToDrawable3DLists(Drawable3DLists lists){
		super.addToDrawable3DLists(lists);
		if (((GeoConicND) getGeoElement()).isEndOfQuadric())
			addToDrawable3DLists(lists,DRAW_TYPE_CLOSED_SURFACES);
		else
			addToDrawable3DLists(lists,DRAW_TYPE_SURFACES);
	}
    
    public void removeFromDrawable3DLists(Drawable3DLists lists){
    	super.removeFromDrawable3DLists(lists);
		if (((GeoConicND) getGeoElement()).isEndOfQuadric())
	    	removeFromDrawable3DLists(lists,DRAW_TYPE_CLOSED_SURFACES);
		else
	    	removeFromDrawable3DLists(lists,DRAW_TYPE_SURFACES);
   	
    }
    
    
    private void drawSurfaceGeometry(Renderer renderer){

    	switch(((GeoConicND) getGeoElement()).getType()){
    	case GeoConic.CONIC_CIRCLE:
		case GeoConic.CONIC_ELLIPSE:
			renderer.setLayer(getGeoElement().getLayer()); //+0f to avoid z-fighting with planes
    		renderer.getGeometryManager().draw(getSurfaceIndex());
    		renderer.setLayer(0);
    		break;
    	}

    }
    

    public void drawTransp(Renderer renderer){
    	if(!isVisible()){
    		return;
    	}


    	if (!hasTransparentAlpha())
    		return;

    	setLight(renderer);

    	setSurfaceHighlightingColor();

    	drawSurfaceGeometry(renderer);

    }
    
    


	public void drawHiding(Renderer renderer){
		if(!isVisible())
			return;

		if (!hasTransparentAlpha())
			return;
		
		
		drawSurfaceGeometry(renderer);
		
	}
	
	
	
	
	
	
	///////////////////////////////////
	// FUNCTION2VAR INTERFACE
	///////////////////////////////////
	
	








	public Coords evaluatePoint(double u, double v) {
		
		GeoConicND conic = (GeoConicND) getGeoElement();
		
		double r = conic.getLineThickness()/getView3D().getScale()*1.5;
		Coords n = new Coords(new double[] {
				Math.cos(u)*Math.cos(v)*r,
				Math.sin(u)*Math.cos(v)*r,
				Math.sin(v)*r});
		
		return (Coords) n.add(conic.getMidpoint3D());
	}


	

	public Coords evaluateNormal(double u, double v) {
		return new Coords(new double[] {
				Math.cos(u)*Math.cos(v),
				Math.sin(u)*Math.cos(v),
				Math.sin(v)});
	}




	public double getMinParameter(int index) {
		switch(index){
		case 0: //u
		default:
			return 0;
		case 1: //v
			return -Math.PI/2;
		}
	}


	public double getMaxParameter(int index) {
		switch(index){
		case 0: //u
		default:
			return 2*Math.PI; 
		case 1: //v
			return Math.PI/2;
		}
		
	}






	public void updatePreview() {
		if (getView3D().getEuclidianController().previewFromResultedGeo) { 
			getView3D().previewLine.setEuclidianVisible(false);
	
			GeoElement geo = getView3D().getEuclidianController().resultedGeo;
			//Application.debug(geo);
			if (geo!=null && geo.isGeoConic() && !getView3D().getEuclidianController().hideIntersection) {
		
				((GeoConic3D) getGeoElement()).set(geo);
				getGeoElement().setEuclidianVisible(true);
				setWaitForUpdate();
			} else {
				getGeoElement().setEuclidianVisible(false);
				setWaitForUpdate();
			}
			/*getGeoElement().setEuclidianVisible(true);
			if(geo!=null && geo.isGeoConic() &&
					!getView3D().getEuclidianController().hideIntersection) {
				getGeoElement().setLineThickness(2);
				setWaitForUpdate();
			} else {
				getGeoElement().setLineThickness(0);
				setWaitForUpdate();
			}*/
		} else {
			getGeoElement().setEuclidianVisible(false);
		}
	}






	public void updateMousePos(double x, double y) {
		// TODO Auto-generated method stub
		
	}
	
	


}
