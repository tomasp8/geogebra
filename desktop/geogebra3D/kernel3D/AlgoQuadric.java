package geogebra3D.kernel3D;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoPointND;

/**
 * @author ggb3D
 * @param <Computer> 
 *
 */
public abstract class AlgoQuadric extends AlgoElement3D {
	
	
	private GeoQuadric3D quadric;
	private GeoElement secondInput;
	private NumberValue number;
	
	private AlgoQuadricComputer computer;
	

	/**
	 * @param c construction
	 */
	public AlgoQuadric(Construction c, GeoElement secondInput, NumberValue number,
			AlgoQuadricComputer computer){		
		super(c);
		quadric = computer.newQuadric(c);
		this.number = number;
		
		this.secondInput = secondInput;
		
		this.computer=computer;

	}
	
	protected AlgoQuadricComputer getComputer(){
		return computer;
	}
	
	
	public String getClassName(){
		return computer.getClassName();
	}
	


	
	
	/**
	 * 
	 * @return second input
	 */
	protected GeoElement getSecondInput(){
		return secondInput;
	}
	
	/**
	 * 
	 * @return radius or angle
	 */
	protected GeoElement getNumber(){
		return (GeoElement) number;
	}	
	/**
	 * 
	 * @return direction of the axis
	 */
	protected abstract Coords getDirection();
	
	
	/**
	 * @return the cone
	 */
	public GeoQuadric3D getQuadric(){
		return quadric;
	}

}
