package daris.client.ui.widget;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.SimplePanel;

import com.google.gwt.dom.client.Style.Position;

public class ProgressBar extends AbsolutePanel {

	private SimplePanel _pb;
	private SimplePanel _bar;
	private double _p;
	private HTML _msg;

	public ProgressBar() {
		_bar = new SimplePanel();
		_bar.setHeight100();
		_bar.setWidth(0);
		// _bar.setBackgroundColour("#ef6023");
		_bar.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM, new RGB(0xef,0x60,0x23), new RGB(0xff,0x70,0x33)));

		_pb = new SimplePanel();
		_pb.setHeight100();
		_pb.setPosition(Position.ABSOLUTE);

		_pb.setBorder(1, new RGB(0xaa,0xaa,0xaa));
		_pb.setBackgroundColour(new RGB(0xee,0xee,0xee));
		_pb.setContent(_bar);

		_p = 0;

		add(_pb);

		_msg = new HTML("");
		_msg.setPosition(Position.ABSOLUTE);
		_msg.setColour(new RGB(0x13,0x41,0x8b));

		add(_msg);

		setHeight(16);
	}

	public void reset() {
		_p = 0;
		_msg.setHTML("");
		render();
	}

	/**
	 * Sets progress.
	 * 
	 * @param p
	 *            Should be in the range [0,1]
	 */
	public void setProgress(double p, String msg) {
		if (p < 0 || p > 1) {
			throw new AssertionError("Progress value out of range: " + p);
		}

		_p = p;
		_msg.setHTML(msg);
		render();
	}

	/**
	 * Sets progress.
	 * 
	 * @param p
	 *            Should be in the range [0,1]
	 */
	public void setProgress(double p) {
		setProgress(p, "");
	}

	protected void doLayoutChildren() {
		super.doLayoutChildren();

		render();
	}

	private void render() {
		_pb.setWidth(width() - 2);
		_msg.setLeft(_pb.width() / 2 - _msg.width() / 2);
		_msg.setTop(height() / 2 - _msg.height() / 2);
		_bar.setWidth((int) ((double) _pb.width() * _p));
	}
}