package daris.client.ui.object.action;

import daris.client.ui.util.WindowUtil;
import arc.gui.window.Window;

public class ActionIntefaceUtil {


	public static int windowWidth(Window owner, double ratio) {

		return WindowUtil.windowWidth(owner, ratio);
	}

	public static int windowHeight(Window owner, double ratio) {

		return WindowUtil.windowHeight(owner, ratio);
	}
}
