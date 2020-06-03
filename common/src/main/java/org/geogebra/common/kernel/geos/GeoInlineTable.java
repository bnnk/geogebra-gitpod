package org.geogebra.common.kernel.geos;

import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.euclidian.draw.HasFormat;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.ValueType;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.GeoClass;

public class GeoInlineTable extends GeoInline implements TextStyle {

	private boolean defined = true;
	private String content;

	public static final int DEFAULT_WIDTH = 200;
	public static final int DEFAULT_HEIGHT = 72;

	private double minHeight;

	/**
	 * Creates new GeoElement for given construction
	 *
	 * @param c Construction
	 *
	 * @param location on-screen location
	 */
	public GeoInlineTable(Construction c, GPoint2D location) {
		super(c);
		setLocation(location);
		setWidth(DEFAULT_WIDTH);
		setHeight(DEFAULT_HEIGHT);
	}

	@Override
	public GeoClass getGeoClassType() {
		return GeoClass.TABLE;
	}

	@Override
	public GeoElement copy() {
		GeoInlineTable copy = new GeoInlineTable(cons,
				new GPoint2D(getLocation().getX(), getLocation().getY()));
		copy.set(this);
		return copy;
	}

	@Override
	public void set(GeoElementND geo) {
		if (geo instanceof GeoInlineTable) {
			this.content = ((GeoInlineTable) geo).content;
		} else {
			setUndefined();
		}
	}

	@Override
	public boolean isDefined() {
		return defined;
	}

	@Override
	public void setUndefined() {
		defined = false;
	}

	@Override
	public String toValueString(StringTemplate tpl) {
		return null;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.TEXT;
	}

	@Override
	public boolean showInAlgebraView() {
		return true;
	}

	@Override
	protected boolean showInEuclidianView() {
		return true;
	}

	@Override
	public boolean isEqual(GeoElementND geo) {
		return geo == this;
	}

	@Override
	public HitType getLastHitType() {
		return HitType.ON_FILLING;
	}

	@Override
	public int getFontStyle() {
		return GeoInlineText.getFontStyle(getDrawable());
	}

	protected HasFormat getDrawable() {
		return (HasFormat) kernel.getApplication()
				.getActiveEuclidianView().getDrawableFor(this);
	}

	@Override
	public double getFontSizeMultiplier() {
		return GeoText.getRelativeFontSize(GeoText.FONTSIZE_SMALL);
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public double getMinWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	public double getMinHeight() {
		return Math.max(minHeight, DEFAULT_HEIGHT);
	}
}
