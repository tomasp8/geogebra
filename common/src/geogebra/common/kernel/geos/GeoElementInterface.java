package geogebra.common.kernel.geos;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.algos.AlgoElementInterface;

import java.util.List;

public interface GeoElementInterface {
	public boolean needsReplacingInExpressionNode();
	public String getLabel();
	public String getRealLabel();	
	public boolean isLabelSet();
	public boolean isIndependent();
	public boolean isLocalVariable();
	public boolean isGeoVector();
	public boolean isGeoLine();
	public boolean isNumberValue();
	public String toValueString();
	public String getAlgebraDescriptionTextOrHTML();
	public String getNameDescriptionTextOrHTML();
	public String getCaptionDescriptionHTML(boolean b);
	public int getConstructionIndex();
	public String getDefinitionDescription();
	public String getCommandDescription();
	public String getAlgebraDescription();
	public String getNameDescription();
	public String getAlgebraDescriptionHTML(boolean b);
	public String getCommandDescriptionHTML(boolean b);
	public String getDefinitionDescriptionHTML(boolean b);
	public boolean isConsProtocolBreakpoint();
	public void setConsProtocolBreakpoint(boolean newVal);
	public int getRelatedModeID();
	public void update();
	public boolean setCaption(String string);
	public String getNameDescriptionHTML(boolean b, boolean c);
	public boolean isChildOf(GeoElementInterface parent);
	public int getMinConstructionIndex();
	public boolean isGeoNumeric();
	public List<Integer> getViewSet();
	public void setRandomGeo(boolean b);
	public boolean isAnimating();
	public boolean isGeoList();
	public boolean isDefined();
	public void setLabel(String label);
	public GeoElementInterface copyInternal(Construction cons);
	public AlgoElementInterface getParentAlgorithm();
	public boolean algoUpdateSetContains(AlgoElementInterface parentAlgo);
	public String getClassName();
	public void updateCascade();
	public void updateRepaint();
	public void removeAlgorithm(AlgoElementInterface parent);
	public Construction getConstruction();
	public boolean isMoveable();
	public GeoClass getGeoClassType();
	public int getLayer();
	public boolean isDrawable();
	public boolean isColorSet();
	public boolean isSelectionAllowed();
	public boolean isGeoImage();
	public boolean isGeoPolygon();
	public boolean isGeoConic();
	public String getCASString(boolean b);
	public boolean isGeoFunction();	
	public void setLocalVariableLabel(String string);
	public void setAlphaValue(float defaultConicAlpha);
	public void setDefaultGeoType(int defaultConic);
	public void setObjColor(geogebra.common.awt.Color color);
	
}
