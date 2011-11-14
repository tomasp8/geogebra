/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.kernel;


public class AlgoLetterToUnicode extends AlgoElement {

	protected GeoText text;  // input
    protected GeoNumeric num;     // output           
        
    protected AlgoLetterToUnicode(Construction cons, String label, GeoText text) {       
	  super(cons); 
      this.text = text;

      num = new GeoNumeric(cons); 
      setInputOutput(); // for AlgoElement
      
      compute();     
          
      num.setLabel(label);
    }   
  
    @Override
	public String getClassName() {
        return "AlgoLetterToUnicode";
    }
    
    // for AlgoElement
    @Override
	protected void setInputOutput() {
        input =  new GeoElement[1];
        input[0] = text;
     
        super.setOutputLength(1);
        super.setOutput(0, num);
        setDependencies(); // done by AlgoElement
    }    
    
    protected GeoNumeric getResult() { return num; }        

    @Override
	protected void compute()
    {
      String t = text.getTextString();
      if (t == null || t.length() != 1)
      {
    	  num.setUndefined();
      }
      else
      {
    	  num.setValue(t.charAt(0));
      }

    }
}