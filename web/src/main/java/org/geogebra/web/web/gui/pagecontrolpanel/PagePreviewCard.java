package org.geogebra.web.web.gui.pagecontrolpanel;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.KeyEvent;
import org.geogebra.common.euclidian.event.KeyHandler;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.gui.SetLabels;
import org.geogebra.common.main.Localization;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.euclidian.EuclidianViewWInterface;
import org.geogebra.web.html5.event.FocusListenerW;
import org.geogebra.web.html5.gui.inputfield.AutoCompleteTextFieldW;
import org.geogebra.web.html5.gui.util.ClickStartHandler;
import org.geogebra.web.html5.gui.util.MyToggleButton;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.css.MaterialDesignResources;
import org.geogebra.web.web.gui.view.algebra.InputPanelW;

import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Page Preview Card showing preview of EuclidianView
 * 
 * @author Alicia Hofstaetter
 *
 */
public class PagePreviewCard extends FlowPanel implements SetLabels {

	private AppW app;
	private Localization loc;
	private EuclidianView view;
	private int pageIndex;
	private FlowPanel imagePanel;
	private String image;
	private FlowPanel titlePanel;
	private AutoCompleteTextFieldW textField;
	private boolean isTitleSet = false;
	private MyToggleButton moreBtn;

	private ContextMenuPagePreview contextMenu = null;

	/**
	 * @param view
	 *            associated view
	 * @param pageIndex
	 *            current page index
	 */
	public PagePreviewCard(EuclidianView view, int pageIndex) {
		this.view = view;
		this.pageIndex = pageIndex;
		this.app = (AppW) view.getApplication();
		this.loc = app.getLocalization();
		initGUI();
	}

	private void initGUI() {
		addStyleName("mowPagePreviewCard");

		imagePanel = new FlowPanel();
		imagePanel.addStyleName("mowImagePanel");

		titlePanel = new FlowPanel();
		titlePanel.addStyleName("mowTitlePanel");

		add(imagePanel);
		add(titlePanel);

		setPreviewImage();
		addTextField();
		setDefaultLabel();
		addMoreButton();
	}

	private void addTextField() {
		textField = InputPanelW.newTextComponent(app);
		textField.setAutoComplete(false);
		textField.addFocusListener(new FocusListenerW(this) {
			@Override
			protected void wrapFocusLost() {
				onEnter();
			}
		});
		textField.addKeyHandler(new KeyHandler() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isEnterKey()) {
					onEnter();
				}
			}
		});
		titlePanel.add(textField);
	}

	/**
	 * execute textfield input
	 */
	protected void onEnter() {
		rename(textField.getText());
	}

	private void setPreviewImage() {
		image = ((EuclidianViewWInterface) view).getExportImageDataUrl(0.2,
				false);

		if (image != null && image.length() > 0) {
			imagePanel.getElement().getStyle().setBackgroundImage(
					"url(" + Browser.normalizeURL(image) + ")");
		}
	}

	/**
	 * Updates the preview image
	 */
	public void updatePreviewImage() {
		imagePanel.clear();
		setPreviewImage();
	}

	/**
	 * get the index of the page
	 * 
	 * @return page index
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * set index of page
	 * 
	 * @param index
	 *            new index
	 */
	public void setPageIndex(int index) {
		pageIndex = index;
		setDefaultLabel();
	}

	private void setDefaultLabel() {
		if (!isTitleSet) {
			textField.setText(loc.getMenu("page") + " " + (pageIndex + 1));
		}
	}

	private void addMoreButton(){
		if (moreBtn == null) {
			moreBtn = new MyToggleButton(
					new Image(
					new ImageResourcePrototype(null,
							MaterialDesignResources.INSTANCE.more_vert_black()
									.getSafeUri(),
									0, 0, 24, 24, false, false)),
					app);
		}
		moreBtn.getUpHoveringFace()
				.setImage(
						new Image(new ImageResourcePrototype(null,
								MaterialDesignResources.INSTANCE
										.more_vert_mebis().getSafeUri(),
								0, 0, 24, 24, false, false)));
		moreBtn.addStyleName("mowMoreButton");
		ClickStartHandler.init(moreBtn, new ClickStartHandler(true, true) {
			@Override
			public void onClickStart(int x, int y, PointerEventType type) {
				showContexMenu();
			}
		});
		titlePanel.add(moreBtn);
	}
	
	/**
	 * rename title of page
	 * 
	 * @param text
	 *            title of page to set
	 */
	public void rename(String text) {
		textField.setText(text);
		isTitleSet = true;
	}

	/**
	 * show context menu of preview card
	 */
	protected void showContexMenu() {
		if (contextMenu == null) {
			contextMenu = new ContextMenuPagePreview(app, this);
		}
		contextMenu.show(moreBtn.getAbsoluteLeft(),
				moreBtn.getAbsoluteTop() - 3);
	}
	
	/**
	 * @return the page that is associated with this preview card
	 */
	public EuclidianView getAssociatedView() {
		return view;
	}

	@Override
	public void setLabels() {
		if (moreBtn != null) {
			moreBtn.setAltText(loc.getMenu("Options"));
		}
		if (contextMenu != null) {
			contextMenu.setLabels();
		}
		setDefaultLabel();
	}
}
