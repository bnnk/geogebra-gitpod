package org.geogebra.common.gui.dialog;

/**
 * interface for 3D export dialogs
 */
public interface Export3dDialogInterface {

	/**
	 * show dialog with initial values and action
	 * 
	 * @param width
	 *            width
	 * @param length
	 *            length
	 * @param height
	 *            height
	 * @param scale
	 *            scale
	 * @param thickness
	 *            thickness
	 * 
	 * @param exportAction
	 *            action for export button pressed
	 */
	void show(double width, double length, double height, double scale,
			double thickness, Runnable exportAction);
}
