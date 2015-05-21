package daris.client.ui.object;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.object.Thumbnail;
import daris.client.model.object.messages.ObjectThumbnailImageGet;
import daris.client.ui.widget.ImagePanel;
import daris.client.ui.widget.IntegerBox;

public class ThumbnailPanel extends ContainerWidget implements InterfaceComponent {

	public static final String IMG_FIRST = Resource.INSTANCE.first24().getSafeUri().asString();

	public static final String IMG_LAST = Resource.INSTANCE.last24().getSafeUri().asString();

	public static final String IMG_NEXT = Resource.INSTANCE.forward24().getSafeUri().asString();

	public static final String IMG_FAST_FORWARD = Resource.INSTANCE.fastForward24().getSafeUri().asString();

	public static final String IMG_PREV = Resource.INSTANCE.rewind24().getSafeUri().asString();

	public static final String IMG_FAST_REWIND = Resource.INSTANCE.fastRewind24().getSafeUri().asString();

	private Thumbnail _t;

	private VerticalPanel _vp;

	private ImagePanel _ip;

	private Label _label;

	private Button _firstButton;
	private Button _fastRewindButton;
	private Button _prevButton;
	private IntegerBox _indexField;
	private Button _nextButton;
	private Button _fastForwardButton;
	private Button _lastButton;
	private int _idx;

	public ThumbnailPanel(Thumbnail t) {
		_t = t;
		_vp = new VerticalPanel();

		_ip = new ImagePanel();
		_ip.fitToParent();
		_vp.add(_ip);

		CenteringPanel cp = new CenteringPanel(Axis.HORIZONTAL);
		cp.setHeight(20);
		cp.setWidth100();
		_label = new Label();
		cp.setContent(_label);
		_vp.add(cp);

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER);
		bb.setHeight(28);
		bb.setWidth100();
		bb.setColourEnabled(false);
		bb.setBackgroundColour(new RGB(0xdd,0xdd,0xdd));

		_firstButton = createButton(IMG_FIRST, 16, 16);
		_firstButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!_t.hasImages()) {
					return;
				}
				setImage(0);
			}
		});
		bb.add(_firstButton);

		_fastRewindButton = createButton(IMG_FAST_REWIND, 16, 16);
		_fastRewindButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!_t.hasImages()) {
					return;
				}
				int size = _t.images().size();
				int step = size / 10;
				step = step > 0 ? step : 1;
				if (_idx + step > size - 1) {
					setImage(size - 1);
				} else {
					setImage(_idx + step);
				}
			}
		});
		bb.add(_fastRewindButton);

		_prevButton = createButton(IMG_PREV, 16, 16);
		_prevButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!_t.hasImages()) {
					return;
				}
				if (_idx - 1 >= 0) {
					setImage(_idx - 1);
				}
			}
		});
		bb.add(_prevButton);

		Label indexLabel = new Label("Index:");
		indexLabel.setFontSize(12);
		indexLabel.setFontWeight(FontWeight.BOLD);
		bb.add(indexLabel);

		_indexField = new IntegerBox(1, t.images() == null ? 1 : _t.images().size(), 1, 1);
		_indexField.setFontSize(12);
		_indexField.setWidth(30);
		_indexField.setAlignment(TextAlignment.RIGHT);
		_indexField.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!_t.hasImages()) {
					return;
				}
				setImage(_indexField.value() - 1);
			}
		});
		bb.add(_indexField);

		Label sizeLabel = new Label("/" + (_t.hasImages() ? _t.images().size() : 0));
		sizeLabel.setFontSize(12);
		bb.add(sizeLabel);

		_nextButton = createButton(IMG_NEXT, 16, 16);
		_nextButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!_t.hasImages()) {
					return;
				}
				int size = _t.images().size();
				if (_idx + 1 < size) {
					setImage(_idx + 1);
				}
			}
		});
		bb.add(_nextButton);

		_fastForwardButton = createButton(IMG_FAST_FORWARD, 16, 16);
		_fastForwardButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!_t.hasImages()) {
					return;
				}
				int size = _t.images().size();
				int step = size / 10;
				step = step > 0 ? step : 1;
				if (_idx - step >= 0) {
					setImage(_idx - step);
				} else {
					setImage(size - 1);
				}
			}
		});
		bb.add(_fastForwardButton);

		_lastButton = createButton(IMG_LAST, 16, 16);
		_lastButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!_t.hasImages()) {
					return;
				}
				setImage(_t.images().size() - 1);
			}
		});
		bb.add(_lastButton);
		_vp.add(bb);

		initWidget(_vp);

		if (_t.hasImages()) {
			setImage(0);
		}
	}

	private Button createButton(String icon, int width, int height) {
		Button button = new Button("<img width=\"" + width + "px\" height=\"" + height + "px\" src=\"" + icon
				+ "\" style=\"vertical-align: middle; padding-top: 0px;\"></img>", false);
		button.setWidth(30);
		return button;
	}

	private void setImage(int idx) {
		_idx = idx;
		_firstButton.disable();
		_fastRewindButton.disable();
		_prevButton.disable();
		_indexField.disable();
		_nextButton.disable();
		_fastForwardButton.disable();
		_lastButton.disable();
		final Thumbnail.Image image = _t.image(_idx);
		_ip.showLoadingMessage("Loading image: " + image.name() + "...");
		new ObjectThumbnailImageGet(_t, _idx).send(new ObjectMessageResponse<Null>() {
			@Override
			public void responded(Null r) {
				_ip.setImage(image.url());
				_label.setText(image.name());
				_indexField.setValue(_idx + 1, false);
				if (_idx > 0) {
					_firstButton.enable();
					_fastRewindButton.enable();
					_prevButton.enable();
				}
				_indexField.enable();
				if (_idx < _t.images().size() - 1) {
					_nextButton.enable();
					_fastForwardButton.enable();
					_lastButton.enable();
				}
			}
		});

	}

	@Override
	public Widget gui() {
		return _vp;
	}

}
