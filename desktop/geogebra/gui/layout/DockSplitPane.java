package geogebra.gui.layout;

import geogebra.io.layout.DockSplitPaneData;
import geogebra.main.Application;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * Split pane which is used to separate two DockPanels. 
 * 
 * @author Florian Sonner
 */
public class DockSplitPane extends JSplitPane {	
	private static final long serialVersionUID = 1L;
	private boolean dividerVisible;
	
	public DockSplitPane() {
		this(JSplitPane.HORIZONTAL_SPLIT);
		
		this.addPropertyChangeListener(paneResizeListener);
	}
	
	public DockSplitPane(int newOrientation) {
		super(newOrientation);
		
		setResizeWeight(0.5);
		setBorder(BorderFactory.createEmptyBorder());
		
		dividerVisible = false;
		this.addPropertyChangeListener(paneResizeListener);
	}
	
	
	/**
	 * Listener for split pane resizing. Transfers focus to the split pane after
	 * a resize event, thus removing focus and sending a focus lost event to the
	 * DockSplitPane components.
	 */
	PropertyChangeListener paneResizeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent changeEvent) {
			JSplitPane splitPane = (JSplitPane) changeEvent.getSource();
			String propertyName = changeEvent.getPropertyName();
			if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
				splitPane.requestFocus();		
			}
		}
	};

	
	
	/**
	 * Return the component which is opposite to the parameter.
	 * 
	 * @param component
	 * @return
	 */
	public Component getOpposite(Component component) {
		if(component == leftComponent)
			return rightComponent;
		else if(component == rightComponent)
			return leftComponent;
		else 
			throw new IllegalArgumentException();
	}
	
	/**
	 * Set the left component of this DockSplitPane and remove the divider
	 * if the left component is null.
	 */
	@Override
	public void setLeftComponent(Component component) {
		super.setLeftComponent(component);
		updateDivider();
	}
	
	/**
	 * Set the right component of this DockSplitPane and remove the divider
	 * if the right component is null.
	 */
	@Override
	public void setRightComponent(Component component) {
		super.setRightComponent(component);
		updateDivider();
	}
	
	/**
	 * Replace a component from the split pane with another.
	 * 
	 * @param component
	 * @param replacement
	 */
	public void replaceComponent(Component component, Component replacement) {
		if(component == leftComponent)
			setLeftComponent(replacement);
		else if(component == rightComponent)
			setRightComponent(replacement);
		else 
			throw new IllegalArgumentException();
	}
	
	/**
	 * Update the visibility of the divider.
	 */
	private void updateDivider() {
		if(leftComponent == null || rightComponent == null)
			dividerVisible = false;
		else
			dividerVisible = true;
	}
	
	/**
	 * Update the UI by drawing the divider just if the dividerVisible attribute is 
	 * set to true.
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		SplitPaneUI splitPaneUI = getUI();
		if (splitPaneUI instanceof BasicSplitPaneUI) {
			BasicSplitPaneUI basicUI = (BasicSplitPaneUI) splitPaneUI;
			basicUI.getDivider().setVisible(dividerVisible);
		}
	}
	
	/**
	 * A helper class used to get the split pane information array
	 * of the current layout. Use {@link #getInfo(DockSplitPane)} with the root pane
	 * as parameter to get the array.
	 * 
	 * @author Florian Sonner
	 * @version 2008-10-26
	 */
	public static class TreeReader
	{
		private Application app;
		private ArrayList<DockSplitPaneData> splitPaneInfo;
		private int windowWidth;
		private int windowHeight;
		
		public TreeReader(Application app) {
			this.app = app;
			
			splitPaneInfo = new ArrayList<DockSplitPaneData>();
		}
		
		public DockSplitPaneData[] getInfo(DockSplitPane rootPane) {
			splitPaneInfo.clear();
			
			if(app.isApplet()) {
				windowWidth = app.getApplet().width;
			} else {
				windowWidth = app.getFrame().getWidth();
			}
			
			if(app.isApplet()) {
				windowHeight = app.getApplet().height;
			} else {
				windowHeight = app.getFrame().getHeight();
			}

			saveSplitPane("", rootPane);
			
			DockSplitPaneData[] info = new DockSplitPaneData[splitPaneInfo.size()];
			return (DockSplitPaneData[])splitPaneInfo.toArray(info);
		}
		
		/**
		 * Save a split pane into the splitPaneInfo array list
		 *  
		 * @param parentLocation
		 * @param parent
		 */
		private void saveSplitPane(String parentLocation, DockSplitPane parent) {
			double dividerLocation = 0.2;
			
			// get relative divider location depending on the current orientation
			if(parent.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
				dividerLocation = (double)parent.getDividerLocation() / windowWidth;
			} else {
				dividerLocation = (double)parent.getDividerLocation() / windowHeight;
			}
			
			splitPaneInfo.add(new DockSplitPaneData(parentLocation, dividerLocation, parent.getOrientation()));
			
			if(parentLocation.length() > 0)
				parentLocation += ",";
			
			if(parent.getLeftComponent() instanceof DockSplitPane) {
				saveSplitPane(parentLocation + "0", (DockSplitPane)parent.getLeftComponent());
			}
			
			if(parent.getRightComponent() instanceof DockSplitPane) {
				saveSplitPane(parentLocation + "1", (DockSplitPane)parent.getRightComponent());
			}
		}
	}
}