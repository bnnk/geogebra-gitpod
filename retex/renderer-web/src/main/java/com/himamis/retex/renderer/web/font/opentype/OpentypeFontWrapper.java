/**
 * This file is part of the ReTeX library - https://github.com/himamis/ReTeX
 *
 * Copyright (C) 2015 Balazs Bencze
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * Linking this library statically or dynamically with other modules 
 * is making a combined work based on this library. Thus, the terms 
 * and conditions of the GNU General Public License cover the whole 
 * combination.
 * 
 * As a special exception, the copyright holders of this library give you 
 * permission to link this library with independent modules to produce 
 * an executable, regardless of the license terms of these independent 
 * modules, and to copy and distribute the resulting executable under terms 
 * of your choice, provided that you also meet, for each linked independent 
 * module, the terms and conditions of the license of that module. 
 * An independent module is a module which is not derived from or based 
 * on this library. If you modify this library, you may extend this exception 
 * to your version of the library, but you are not obliged to do so. 
 * If you do not wish to do so, delete this exception statement from your 
 * version.
 * 
 */
package com.himamis.retex.renderer.web.font.opentype;


import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;
import com.himamis.retex.renderer.share.platform.FactoryProvider;
import com.himamis.retex.renderer.share.platform.geom.Rectangle2D;
import com.himamis.retex.renderer.web.font.FontWrapper;

public class OpentypeFontWrapper implements FontWrapper {

	private JavaScriptObject impl;

	public OpentypeFontWrapper(JavaScriptObject impl) {
		this.impl = impl;
	}
	
	@Override
	public void drawGlyph(String c, int x, int y, int size, Context2d ctx) {
		drawGlyphNative(c, x, y, size, ctx);
	}

	@Override
	public Rectangle2D measureGlyph(String c) {
		JsArrayNumber obj = JavaScriptObject.createArray(4).cast();
		measureNative(c, obj);
		Rectangle2D ret = FactoryProvider.INSTANCE.getGeomFactory()
				.createRectangle2D(obj.get(0), obj.get(1),
						obj.get(2) - obj.get(0), obj.get(3) - obj.get(1));
		return ret;
	}

	private native void measureNative(String text, JsArrayNumber arr) /*-{
		var that = this;
		var font = that.@com.himamis.retex.renderer.web.font.opentype.OpentypeFontWrapper::impl;
		var glyph = font.charToGlyph(text);
		arr[0] = glyph.xMin;
		arr[1] = glyph.yMin;
		arr[2] = glyph.xMax;

		arr[3] = glyph.yMax;
		$wnd.console.log(arr);
	}-*/;

	private native void drawGlyphNative(String c, int x, int y, int size, Context2d ctx) /*-{
		var that = this;
		var font = that.@com.himamis.retex.renderer.web.font.opentype.OpentypeFontWrapper::impl;
		var path = font.getPath(c, x, y, size);

		path.fill = ctx.fillStyle;
		path.stroke = null;
		path.strokeWidth = 1;

		// reset the path before to ensure that the previous drawing doesn't interfere with this one
		ctx.beginPath();

		path.draw(ctx);

		// reset the path, so that the following calls to stroke() and fill() will not draw again the character
		ctx.beginPath();
	}-*/;
}
