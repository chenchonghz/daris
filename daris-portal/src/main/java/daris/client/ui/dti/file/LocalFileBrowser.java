package daris.client.ui.dti.file;

import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.client.file.LocalFile;

public class LocalFileBrowser {

	public static final double WIDTH = 0.5;
	public static final double HEIGHT = 0.5;

	private Window _win;
	private LocalFileNavigatorPanel _nav;
	private boolean _showing;

	private LocalFileBrowser() {

		_nav = new LocalFileNavigatorPanel(null, LocalFile.Filter.ANY, null, false);

	}

	public void show(Window owner) {

		if (_showing) {
			_win.close();
		}
		WindowProperties wp = new WindowProperties();
		wp.setModal(false);
		wp.setTitle("Local Files");
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(true);
		wp.setOwnerWindow(owner);
		wp.setSize(WIDTH, HEIGHT);
		wp.setCenterInPage(true);
		_win = Window.create(wp);
		_win.addCloseListener(new WindowCloseListener() {

			@Override
			public void closed(Window w) {
				_showing = false;
			}
		});
		_win.setContent(_nav);
		_win.centerInPage();
		_win.show();
		_showing = true;
	}

	private static LocalFileBrowser _instance;

	public static LocalFileBrowser get() {
		if (_instance == null) {
			_instance = new LocalFileBrowser();
		}
		return _instance;
	}
}
