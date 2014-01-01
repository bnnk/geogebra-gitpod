/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.common.kernel.algos;

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.commands.Commands;
import geogebra.common.kernel.geos.GeoConicPart;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoLine;
import geogebra.common.kernel.geos.GeoPoint;
import geogebra.common.kernel.geos.GeoVec3D;
import geogebra.common.kernel.kernelND.GeoConicND;
import geogebra.common.kernel.kernelND.GeoConicNDConstants;
import geogebra.common.kernel.kernelND.GeoConicPartND;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.main.App;

/**
 * Circle arc or sector defined by three points.
 */
public abstract class AlgoConicPartCircumcircleND extends AlgoConicPart {

	protected GeoPointND A, B, C;	
	
	private GeoLine line; // for degenerate case

	

    public AlgoConicPartCircumcircleND(Construction cons, String label,
    		GeoPointND A, GeoPointND B, GeoPointND C,
    		int type) 
    {
    	this(cons, A, B, C, type);
    	 conicPart.setLabel(label);
    }
    
    public AlgoConicPartCircumcircleND(Construction cons,
    		GeoPointND A, GeoPointND B, GeoPointND C,
    		int type) {
        super(cons, type);        
        this.A = A;
        this.B = B; 
        this.C = C;
        
        // helper algo to get circle
        AlgoCircleThreePoints algo = getAlgo();
        cons.removeFromConstructionList(algo);		
        conic = algo.getCircle(); 
        
        conicPart = createConicPart(cons, type);
        conicPart.addPointOnConic(A);
        conicPart.addPointOnConic(B);
        conicPart.addPointOnConic(C);
        
        setInputOutput(); // for AlgoElement      
        compute();               
        setIncidence();
    } 
    
    /**
     * 
     * @param cons
     * @param type
     * @return output conic part
     */
    abstract protected GeoConicND createConicPart(Construction cons, int type);
    
    /**
     * 
     * @return circle algo
     */
    abstract protected AlgoCircleThreePoints getAlgo();
    
	private void setIncidence() {
		A.addIncidence(conicPart);
		B.addIncidence(conicPart);
		C.addIncidence(conicPart);
	}

	@Override
	public Commands getClassName() {
		switch (type) {
			case GeoConicPart.CONIC_PART_ARC:
				return Commands.CircumcircleArc;
			default:
				return Commands.CircumcircleSector;
		}		
	}
	
	@Override
	public int getRelatedModeID() {
		switch (type) {
			case GeoConicPart.CONIC_PART_ARC:
				return EuclidianConstants.MODE_CIRCUMCIRCLE_ARC_THREE_POINTS;
			default:
				return EuclidianConstants.MODE_CIRCUMCIRCLE_SECTOR_THREE_POINTS;
		}
	}

    // for AlgoElement
	@Override
	protected void setInputOutput() {
        input = new GeoElement[3];
        input[0] = (GeoElement) A;      
        input[1] = (GeoElement) B;
        input[2] = (GeoElement) C;        

        super.setOutputLength(1);
        super.setOutput(0, conicPart);

        setDependencies();
    }
	
    
    @Override
	public void compute() {
    	if (!conic.isDefined()) {
    		conicPart.setUndefined();
    		return;
    	}
    	
    	conicPart.set(conic); 
    	switch (conicPart.getType()) {
    		case GeoConicNDConstants.CONIC_PARALLEL_LINES: 	
    			computeDegenerate();
    			break;
    		
			case GeoConicNDConstants.CONIC_CIRCLE: 
				computeCircle();
		    	break;
		    
		    default:
		    	// this should not happen
		    	App.debug("AlgoCirclePartPoints: unexpected conic type: " + conicPart.getType());
		    	conicPart.setUndefined();
    	}	
    }
    
//  arc degenerated to segment or two rays
	private void computeDegenerate() {
		if (line == null) { // init lines 
			line = conicPart.getLines()[0];
			line.setStartPoint(getA());
			line.setEndPoint(getC());
			conicPart.getLines()[1].setStartPoint(getC());
		}
		
		// make sure the line goes through A and C
		GeoVec3D.lineThroughPoints(getA(), getC(), line);
		
		// check if B is between A and C => (1) segment AC
		// otherwise we got (2) two rays starting at A and C in oposite directions
		// case (1): use parameters 0, 1 and positive orientation to tell conicPart how to behave
		// case (2): use parameters 0, 1 and negative orientation
		double lambda = GeoPoint.affineRatio(getA(), getC(), getB());
		if (lambda < 0 || lambda > 1) {
			// two rays
			// second ray with start point C and direction of AC 				
			conicPart.getLines()[1].setCoords(line);
			// first ray with start point A and oposite direction
			line.changeSign();
			
			// tell conicPart about this case: two rays
			((GeoConicPartND) conicPart).setParameters(0, 1, false);
		} else {
			// segment
			// tell conicPart about this case: one segment
			((GeoConicPartND) conicPart).setParameters(0, 1, true);
		}
    }
        
//  circle through A, B, C 
    private void computeCircle() {
    	// start angle from vector MA
    	double alpha = Math.atan2(getAy() - conicPart.getTranslationVector().getY(), getAx() - conicPart.getTranslationVector().getX()); 
		// end angle from vector MC
    	double beta = Math.atan2(getCy() - conicPart.getTranslationVector().getY(), getCx() - conicPart.getTranslationVector().getX());
    	
    	// check orientation of triangle A, B, C to see
		// whether we have to swap start and end angle
		double det =  (getBx() - getAx()) * (getCy() - getAy())
					- (getBy() - getAy()) * (getCx() - getAx());
		
		((GeoConicPartND) conicPart).setParameters(alpha, beta, det > 0);
    }
    
    

	
	/**
	 * Method for LocusEqu.
	 * @return first point.
	 */
	abstract public GeoPoint getA();

	/**
	 * Method for LocusEqu.
	 * @return second point.
	 */
	abstract public GeoPoint getB();

	/**
	 * Method for LocusEqu.
	 * @return third point.
	 */
	abstract public GeoPoint getC();
	


	final protected double getAx(){
    	return getA().inhomX;
    }
    

	final protected double getAy(){
    	return getA().inhomY;
    }
    

	final protected double getBx(){
    	return getB().inhomX;
    }
    

	final protected double getBy(){
    	return getB().inhomY;
    }
    

	final protected double getCx(){
    	return getC().inhomX;
    }
    

	final protected double getCy(){
    	return getC().inhomY;
    }


}
