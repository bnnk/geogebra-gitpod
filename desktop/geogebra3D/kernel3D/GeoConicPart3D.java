/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

/* 
 * Created on 03.12.2004
 */

package geogebra3D.kernel3D;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.PathMover;
import geogebra.common.kernel.PathMoverGeneric;
import geogebra.common.kernel.PathParameter;
import geogebra.common.kernel.RegionParameters;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.Matrix.CoordNearest;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.algos.AlgoConicPartCircumcircle;
import geogebra.common.kernel.algos.AlgoConicPartConicPoints;
import geogebra.common.kernel.algos.AlgoElement;
import geogebra.common.kernel.arithmetic.MyDouble;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoNumberValue;
import geogebra.common.kernel.geos.GeoPoint;
import geogebra.common.kernel.kernelND.GeoConicNDConstants;
import geogebra.common.kernel.kernelND.GeoConicPartND;
import geogebra.common.kernel.kernelND.GeoConicPartParameters;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.plugin.GeoClass;

/**
 * GeoCirclePart for
 * 
 * @author Markus Hohenwarter
 * 
 */
public class GeoConicPart3D extends GeoConic3D implements GeoConicPartND, GeoNumberValue {


	// parameters (e.g. angles) for arc
	private GeoConicPartParameters parameters;

	/**
	 * GeoCirclePart is constructed by AlgoCirclePart...
	 * 
	 * @param c construction
	 * @param type
	 *            CONIC_PART_ARC or CONIC_PART_SECTOR
	 */
	public GeoConicPart3D(Construction c, int type) {
		super(c);
		parameters = new GeoConicPartParameters(this, type);
		setConstructionDefaults();
	}

	/**
	 * Copy constructor
	 * 
	 * @param conic conic to copy
	 */
	public GeoConicPart3D(GeoConicPart3D conic) {
		this(conic.cons, conic.getConicPartType());
		set(conic);
	}

	@Override
	public GeoClass getGeoClassType() {
		return GeoClass.CONICPART;
	}

	@Override
	public String getTypeString() {
		switch (parameters.conic_part_type) {
		case CONIC_PART_ARC:
			return "Arc";

		case CONIC_PART_SECTOR:
			return "Sector";

		default:
			return super.getTypeString();
		}
	}

	@Override
	public GeoElement copyInternal(Construction construction) {
		GeoConicPart3D ret = new GeoConicPart3D(construction, parameters.conic_part_type);
		ret.set(this);
		return ret;
	}

	@Override
	public void set(GeoElement geo) {
		super.set(geo);
		if (!geo.isGeoConicPart())
			return;

		GeoConicPart3D cp = (GeoConicPart3D) geo;

		// class specific attributes
		parameters.set(cp.parameters);
	}

	@Override
	public void setVisualStyle(GeoElement geo) {
		super.setVisualStyle(geo);

		if (geo.isGeoConicPart()) {
			GeoConicPartND cp = (GeoConicPartND) geo;
			parameters.allowOutlyingIntersections = cp.getParameters().allowOutlyingIntersections;
		}
	}
	
	public GeoConicPartParameters getParameters(){
		return parameters;
	}

	/**
	 * Sector or arc
	 * 
	 * @return CONIC_PART_ARC or CONIC_PART_SECTOR
	 */
	final public int getConicPartType() {
		if (parameters == null){ //for default settings
			return GeoConicNDConstants.CONIC_PART_ARC;
		}
		return parameters.conic_part_type;
	}

	/**
	 * @return start parameter
	 */
	final public double getParameterStart() {
		return parameters.paramStart;
	}

	/**
	 * @return end parameter
	 */
	final public double getParameterEnd() {
		return parameters.paramEnd;
	}

	/**
	 * @return end parameter - start parameter
	 */
	final public double getParameterExtent() {
		return parameters.paramExtent;
	}

	/**
	 * @return start parameter
	 */
	final public boolean positiveOrientation() {
		return parameters.posOrientation;
	}

	/**
	 * Returns wheter c is equal to this conic part
	 */
	// Michael Borcherds 2008-05-01
	@Override
	final public boolean isEqual(GeoElement geo) {

		if (!geo.isGeoConicPart())
			return false;

		GeoConicPart3D other = (GeoConicPart3D) geo;

		return parameters.isEqual(other.parameters)
				&& super.isEqual(other);
	}

	/**
	 * Sets parameters and calculates this object's value. For type
	 * CONIC_PART_ARC the value is the length, for CONIC_PART_SECTOR the value
	 * is an area. This method should only be called by the parent algorithm
	 * 
	 * @param start start param
	 * @param end end param
	 * @param positiveOrientation true for positive orientation
	 */
	final public void setParameters(double start, double end,
			boolean positiveOrientation) {
		
		parameters.setParameters(super.isDefined(), start, end, positiveOrientation);
		
	}

	@Override
	final public boolean isDefined() {
		return parameters.value_defined;
	}

	@Override
	public void setUndefined() {
		parameters.value_defined = false;
	}

	/**
	 * Returns arc length / area as appropriate
	 * 
	 * @return arc length / area as appropriate
	 */
	final public double getValue() {
		return parameters.getValue();
	}

	/**
	 * Returns arc length 
	 * 
	 * @return arc length
	 */
	final public double getArcLength() {
		return parameters.getArcLength();
	}

	/**
	 * Returns the area
	 * 
	 * @return area
	 */
	@Override
	final public double getArea() {
		return parameters.getArea();
	}

	@Override
	final public String toString(StringTemplate tpl) {
		sbToString.setLength(0);
		sbToString.append(label);
		sbToString.append(" = ");
		sbToString.append(toValueString(tpl));
		return sbToString.toString();
	}

	private StringBuilder sbToString = new StringBuilder(50);

	@Override
	final public String toValueString(StringTemplate tpl) {
		if (parameters.value_defined)
			return kernel.format(parameters.value, tpl);
		return kernel.format(Double.NaN, tpl);
	}

	public boolean allowOutlyingIntersections() {
		return parameters.allowOutlyingIntersections;
	}

	public void setAllowOutlyingIntersections(boolean flag) {
		parameters.allowOutlyingIntersections = flag;
	}

	public boolean keepsTypeOnGeometricTransform() {
		return parameters.keepTypeOnGeometricTransform;
	}

	public void setKeepTypeOnGeometricTransform(boolean flag) {
		parameters.keepTypeOnGeometricTransform = flag;
	}

	/* TODO
	@Override
	final public boolean isLimitedPath() {
		return true;
	}
	*/

	@Override
	public boolean isIntersectionPointIncident(GeoPoint pt, double eps) {
		if (parameters.allowOutlyingIntersections) {
			return isOnFullConic(pt, eps);
		}
		return isOnPath(pt, eps);
	}

	/**
	 * states wheter P lies on this conic part or not
	 */
	@Override
	public boolean isOnPath(GeoPointND PI, double precision) {
		double eps = precision;
		GeoPoint P = (GeoPoint) PI;

		if (P.getPath() == this)
			return true;

		// check if P lies on conic first
		if (!isOnFullConic(P, eps))
			return false;

		// idea: calculate path parameter and check
		// if it is in [0, 1]

		// remember the old values
		double px = P.x, py = P.y, pz = P.z;
		PathParameter tempParam = getTempPathParameter();
		PathParameter pPP = P.getPathParameter();
		tempParam.set(pPP);

		switch (type) {
		case CONIC_CIRCLE:
		case CONIC_ELLIPSE:
			parameters.setEllipseParameter(P.getCoordsInD2(getCoordSys()),
					P.getPathParameter());
			break;

		// degenerate case: two rays or one segment
		case CONIC_PARALLEL_LINES:
			if (parameters.posOrientation) {
				// segment
				lines[0].pointChanged(P);
			} else {
				// two rays: no point should lie on them
				P.getPathParameter().t = -1;
			}
			break;

		default:
			pPP.t = -1;
			// Application.debug("GeoConicPart.isIncident: unsupported conic part for conic type: "
			// + type);
		}

		// adapt eps for very large circles (almost line)
		if (halfAxes[0] > 100)
			eps = Math.max(Kernel.MAX_PRECISION, eps / halfAxes[0]);

		boolean result = pPP.t >= -eps && pPP.t <= 1 + eps;

		// restore old values
		P.x = px;
		P.y = py;
		P.z = pz;
		pPP.set(tempParam);

		return result;
	}

	private PathParameter tempPP;

	private PathParameter getTempPathParameter() {
		if (tempPP == null)
			tempPP = new PathParameter();
		return tempPP;
	}

	/*
	 * Path Interface implementation
	 */

	@Override
	public boolean isClosedPath() {
		return false;
	}

	@Override
	public void pointChanged(Coords P, PathParameter pp) {

		pp.setPathType(type);

		switch (type) {
		case CONIC_CIRCLE:
		case CONIC_ELLIPSE:
			parameters.setEllipseParameter(P, pp);
			parameters.clipEllipseParameter(P, pp);
			break;

		// degenerate case: two rays or one segment
		case CONIC_PARALLEL_LINES:
			if (parameters.posOrientation) {
				// segment
				lines[0].doPointChanged(P, pp);

				// make sure we don't get outside [0,1]
				if (pp.t < 0) {
					pp.t = 0;
					pathChanged(P, pp);
				} else if (pp.t > 1) {
					pp.t = 1;
					pathChanged(P, pp);
				}
			} else {
				// two rays
				// we take point at infinty
				/*
				 * P.x = -lines[0].y; P.y = lines[0].x; P.z = 0.0;
				 */
				P.setX(-lines[0].y);
				P.setY(lines[0].x);
				P.setZ(0);
			}
			break;

		default:
			pp.t = Double.NaN;
			// Application.debug("GeoConicPart.pointChanged(): unsupported conic part for conic type: "
			// + type);
		}
	}



	@Override
	protected void pathChanged(Coords P, PathParameter pp) {
		if (!parameters.value_defined) {
			P.setX(Double.NaN);
			return;
		}
		if (pp.getPathType() != type || Double.isNaN(pp.t)) {
			pointChanged(P, pp);
			return;
		}

		if (pp.t < 0.0) {
			pp.t = 0;
		} else if (pp.t > 1.0) {
			pp.t = 1;
		}

		// handle conic types
		switch (type) {
		case CONIC_CIRCLE:
		case CONIC_ELLIPSE:
			// if type of path changed (other conic) then we
			// have to recalc the parameter with pointChanged()
			if (pp.getPathType() != type) {
				pointChanged(P, pp);
				return;
			}

			// calc Point on conic using this parameter (in eigenvector space)
			double t = parameters.posOrientation ? pp.t : 1.0 - pp.t;
			double angle = parameters.paramStart + t * parameters.paramExtent;

			P.setX(halfAxes[0] * Math.cos(angle));
			P.setY(halfAxes[1] * Math.sin(angle));
			P.setZ(1);
			coordsEVtoRW(P);
			break;

		case CONIC_PARALLEL_LINES:
			if (parameters.posOrientation) { // segment
				// if type of path changed (other conic) then we
				// have to recalc the parameter with pointChanged()
				if (pp.getPathType() != type) {
					pointChanged(P, pp);
				} else {
					lines[0].pathChanged(P, pp);
				}
			} else {
				// two rays
				// we take point at infinty
				P.setX(-lines[0].y);
				P.setY(lines[0].x);
				P.setZ(0);

			}
			break;

		default:
			// Application.debug("GeoConicPart.pathChanged(): unsupported conic part for conic type: "
			// + type);
		}
	}

	/**
	 * Returns the smallest possible parameter value for this path (may be
	 * Double.NEGATIVE_INFINITY)
	 * 
	 */
	@Override
	public double getMinParameter() {
		switch (type) {
		case CONIC_CIRCLE:
		case CONIC_ELLIPSE:
			return 0;

			// degenerate case: two rays or one segment
		case CONIC_PARALLEL_LINES:
			if (parameters.posOrientation) {
				// segment
				return 0;
			}
			// two rays
			return Double.NEGATIVE_INFINITY;

		default:
			return Double.NaN;
		}
	}

	/**
	 * Returns the largest possible parameter value for this path (may be
	 * Double.POSITIVE_INFINITY)
	 * 
	 */
	@Override
	public double getMaxParameter() {
		switch (type) {
		case CONIC_CIRCLE:
		case CONIC_ELLIPSE:
			return 1;

			// degenerate case: two rays or one segment
		case CONIC_PARALLEL_LINES:
			if (parameters.posOrientation) {
				// segment
				return 1;
			}
			// two rays
			return Double.POSITIVE_INFINITY;

		default:
			return Double.NaN;
		}
	}

	@Override
	public PathMover createPathMover() {
		return new PathMoverGeneric(this);
	}

	/**
	 * returns all class-specific xml tags for saveXML
	 */
	@Override
	protected void getXMLtags(StringBuilder sb) {
		super.getXMLtags(sb);
		
		parameters.getXMLtags(sb);

	}

	/**
	 * interface NumberValue
	 */
	public MyDouble getNumber() {
		return new MyDouble(kernel, getValue());
	}

	final public double getDouble() {
		return getValue();
	}

	@Override
	public boolean isNumberValue() {
		return true;
	}

	@Override
	public boolean isGeoConicPart() {
		return true;
	}

	/* TODO
	public GeoElement[] createTransformedObject(Transform t, String transformedLabel) {
		if (parameters.keepTypeOnGeometricTransform) {
			algoParent = getParentAlgorithm();
		}
		
		int conic_part_type = parameters.conic_part_type;

		// CREATE CONIC PART
		if (algoParent instanceof AlgoConicPartCircle) {
			// transform points
			AlgoConicPartCircle algo = (AlgoConicPartCircle) algoParent;
			GeoPointND[] points = { algo.getCenter(), algo.getStartPoint(),
					algo.getEndPoint() };

			// create circle with center through startPoint
			AlgoCircleTwoPoints algoCircle = new AlgoCircleTwoPoints(cons,
					(GeoPoint) points[0], (GeoPoint) points[1]);
			cons.removeFromConstructionList(algoCircle);
			GeoConic circle = algoCircle.getCircle();

			// transform points and circle
			points = t.transformPoints(points);
			GeoConicND transformedCircle = t
					.getTransformedConic(circle);
			cons.removeFromConstructionList(transformedCircle
					.getParentAlgorithm());

			// create a new arc from the transformed circle using startPoint and
			// endPoint
			AlgoConicPartConicPoints algoResult = new AlgoConicPartConicPoints(
					cons, transformedLabel, transformedCircle, (GeoPoint) points[1],
					(GeoPoint) points[2], conic_part_type);
			GeoConicPart3D conicPart = algoResult.getConicPart();
			conicPart.setVisualStyleForTransformations(this);
			GeoElement[] geos = { conicPart, (GeoElement) points[0],
					(GeoElement) points[2], (GeoElement) points[1] };

			return geos;
		} else if (algoParent instanceof AlgoConicPartCircumcircle) {
			GeoPointND[] points = { (GeoPoint) algoParent.input[0],
					(GeoPoint) algoParent.input[1],
					(GeoPoint) algoParent.input[2] };
			points = t.transformPoints(points);

			AlgoConicPartCircumcircle algo = new AlgoConicPartCircumcircle(
					cons, transformedLabel, (GeoPoint) points[0], (GeoPoint) points[1],
					(GeoPoint) points[2], conic_part_type);
			GeoConicPart3D res = algo.getConicPart();
			res.setLabel(transformedLabel);
			res.setVisualStyleForTransformations(this);
			GeoElement[] geos = { res, (GeoElement) points[1],
					(GeoElement) points[2], (GeoElement) points[0] };
			return geos;
		} else if (algoParent instanceof AlgoConicPartConicParameters) {
			AlgoConicPartConicParameters algo = (AlgoConicPartConicParameters) algoParent;

			GeoConicND transformedConic = t
					.getTransformedConic(algo.conic);
			cons.removeFromConstructionList(transformedConic
					.getParentAlgorithm());

			algo = new AlgoConicPartConicParameters(cons, transformedLabel,
					transformedConic, algo.startParam, algo.endParam,
					conic_part_type);
			GeoElement ret = algo.getConicPart();
			ret.setVisualStyleForTransformations(this);
			GeoElement[] geos = { ret };
			return geos;
		} else if (algoParent instanceof AlgoConicPartConicPoints) {
			AlgoConicPartConicPoints algo = (AlgoConicPartConicPoints) algoParent;
			GeoPointND[] points = { algo.getStartPoint(), algo.getEndPoint() };
			points = t.transformPoints(points);
			GeoConicND orgConic = algo.getConic();

			GeoConicND transformedConic = t
					.getTransformedConic(orgConic);
			cons.removeFromConstructionList(transformedConic
					.getParentAlgorithm());

			algo = new AlgoConicPartConicPoints(cons, transformedLabel, transformedConic,
					(GeoPoint) points[0], (GeoPoint) points[1],
					conic_part_type);
			GeoConicPart3D conicPart = algo.getConicPart();
			conicPart.setVisualStyleForTransformations(this);
			GeoElement[] geos = { conicPart, (GeoPoint) points[0],
					(GeoPoint) points[1] };
			return geos;
		} else if (algoParent instanceof AlgoSemicircle) {
			AlgoElement algo = algoParent;
			GeoPointND[] points = { ((AlgoSemicircle) algo).getA(),
					((AlgoSemicircle) algo).getB() };
			points = t.transformPoints(points);

			GeoConic semCirc;
			if (t instanceof TransformMirror && t.changesOrientation()) {
				semCirc = kernel.getAlgoDispatcher().Semicircle(transformedLabel,
						(GeoPoint) points[1], (GeoPoint) points[0]);
			} else if (t.isSimilar()) {
				semCirc = kernel.getAlgoDispatcher().Semicircle(transformedLabel,
						(GeoPoint) points[0], (GeoPoint) points[1]);
			} else {

				GeoConic orgConic = ((AlgoSemicircle) algo).getConic();
				GeoConicND transformedConic = t
						.getTransformedConic(orgConic);
				(cons).removeFromConstructionList(transformedConic
						.getParentAlgorithm());
				if (t.changesOrientation()) {
					algo = new AlgoConicPartConicPoints(cons, transformedLabel,
							transformedConic, (GeoPoint) points[0],
							(GeoPoint) points[1], conic_part_type);
				} else
					algo = new AlgoConicPartConicPoints(cons, transformedLabel,
							transformedConic, (GeoPoint) points[1],
							(GeoPoint) points[0], conic_part_type);
				GeoConicPart3D conicPart = ((AlgoConicPartConicPoints) algo)
						.getConicPart();
				conicPart.setVisualStyleForTransformations(this);
				GeoElement[] geos = { conicPart, (GeoElement) points[0],
						(GeoElement) points[1] };
				return geos;
			}
			semCirc.setVisualStyleForTransformations(this);
			GeoElement[] geos = { semCirc, (GeoElement) points[0],
					(GeoElement) points[1] };
			return geos;
		} else {
			// create CONIC
			GeoConicND transformedConic = t.getTransformedConic(this);
			transformedConic.setLabel(transformedLabel);
			GeoElement[] ret = { transformedConic };
			return ret;
		}
	}
	*/

	@Override
	final public GeoElement copy() {
		return new GeoConicPart3D(this);
	}

	@Override
	public boolean isInRegion(double x0, double y0) {

		if (!super.isInRegion(x0, y0))
			return false;

		return parameters.isInRegion(x0, y0);
	}



	@Override
	protected void moveBackToRegion(GeoPointND pi, RegionParameters rp) {
		Coords coords = pi.getCoordsInD2(getCoordSys());
		PathParameter pp = pi.getPathParameter();

		// try to find the nearest point in the conic part
		CoordNearest nearestPoint = new CoordNearest(coords);

		// check points of the conic part
		Coords midPoint = getMidpoint2D();
		if (getConicPartType() == CONIC_PART_SECTOR)
			nearestPoint.check(midPoint);

		Coords ev0 = new Coords(3);
		ev0.set(getEigenvec(0));
		Coords ev1 = new Coords(3);
		ev1.set(getEigenvec(1));

		Coords firstPoint = midPoint.add(
				ev0.mul(getHalfAxis(0) * Math.cos(parameters.paramStart))).add(
				ev1.mul(getHalfAxis(1) * Math.sin(parameters.paramStart)));
		nearestPoint.check(firstPoint);
		Coords secondPoint = midPoint.add(
				ev0.mul(getHalfAxis(0) * Math.cos(parameters.paramEnd))).add(
				ev1.mul(getHalfAxis(1) * Math.sin(parameters.paramEnd)));
		nearestPoint.check(secondPoint);

		// check project points on segments edges
		Coords[] segPoint;
		if (getConicPartType() == CONIC_PART_SECTOR) {
			segPoint = coords.projectLine(midPoint, firstPoint.sub(midPoint));
			if (segPoint[1].getX() > 0 && segPoint[1].getX() < 1) // check if
																	// the
																	// projected
																	// point is
																	// on the
																	// segment
				nearestPoint.check(segPoint[0]);
			segPoint = coords.projectLine(midPoint, secondPoint.sub(midPoint));
			if (segPoint[1].getX() > 0 && segPoint[1].getX() < 1) // check if
																	// the
																	// projected
																	// point is
																	// on the
																	// segment
				nearestPoint.check(segPoint[0]);
		} else {
			segPoint = coords.projectLine(firstPoint,
					secondPoint.sub(firstPoint));
			if (segPoint[1].getX() > 0 && segPoint[1].getX() < 1) // check if
																	// the
																	// projected
																	// point is
																	// on the
																	// segment
				nearestPoint.check(segPoint[0]);
		}

		// may calc the nearest point of the global conic
		if (!super.isInRegion(coords.getX(), coords.getY())) {
			Coords pointConic = coords.copyVector();
			pointChanged(pointConic, pp);
			nearestPoint.check(pointConic);
			rp.setIsOnPath(true);
		}

		// take nearest point above all
		coords = nearestPoint.get();

		pi.setCoords2D(coords.getX(), coords.getY(), coords.getZ());
		pi.updateCoordsFrom2D(false, getCoordSys());
		pi.updateCoords();
	}

	@Override
	public void regionChanged(GeoPointND PI) {

		// if kernel doesn't use path/region parameters, do as if point changed
		// its coords
		if (!getKernel().usePathAndRegionParameters(PI)) {
			pointChangedForRegion(PI);
			return;
		}

		super.regionChanged(PI);
		PI.updateCoords2D();
		if (!isInRegion(PI))
			pointChanged(PI);
	}

	@Override
	public boolean hasDrawable3D() {
		return true;
	}

	public boolean isAllEndpointsLabelsSet() {
		AlgoElement algo = this.getParentAlgorithm();
		if (algo == null)// should never happen, just to be sure
			return false;
		if (algo instanceof AlgoConicPartConicPoints)
			return ((AlgoConicPartConicPoints) algo).getStartPoint()
					.isLabelSet()
					&& ((AlgoConicPartConicPoints) algo).getEndPoint()
							.isLabelSet();
		if (algo instanceof AlgoConicPartCircumcircle)
			return algo.getInput()[0].isLabelSet()
					&& algo.getInput()[1].isLabelSet()
					&& algo.getInput()[2].isLabelSet();
		return false;
	}

	/**
	 * @param param path parameter from 0 to 1
	 * @return point with this parameter
	 */
	public GeoPoint getPointParam(double param) {
		GeoPoint ret = new GeoPoint(cons);
		this.pathChanged(ret);
		ret.getPathParameter().setT(param);
		this.pathChanged(ret);
		ret.updateCoords();
		return ret;
	}
	

	/**
	 * super method
	 * @param P
	 * @param pp
	 */
	public void superPointChanged(Coords P, PathParameter pp) {
		super.pointChanged(P, pp);
	}
	
	
	private Coords lineOrigin1, lineOrigin2, lineEnd;
	

	/**
	 * set origin and end of first segment/ray (for degenerate cases)
	 * @param o origin
	 * @param end end
	 */
	public void setFirstLine(Coords o, Coords end){
		lineOrigin1 = o;
		lineEnd = end;
	}
	
	/**
	 * set origin of first segment/ray (for degenerate cases)
	 * @param o origin
	 */
	public void setSecondLineOrigin(Coords o){
		lineOrigin2 = o;
	}
	
	
	/*
	@Override
	public Coords getOrigin3D(int i) {
		if (i==0){
			return lineOrigin1;
		}
		
		return lineOrigin2;
	}


	public Coords getSegmentEnd3D() {
		return lineEnd;
	}
	*/
	
	@Override
	public Coords getOrigin3D(int i) {
		return getCoordSys().getPoint(lines[i].startPoint.inhomX, lines[i].startPoint.inhomY);
	}


	public Coords getSegmentEnd3D() {
		return getCoordSys().getPoint(lines[0].endPoint.inhomX, lines[0].endPoint.inhomY);
	}

}
