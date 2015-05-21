package daris.client.ui.widget;

import arc.gui.gwt.widget.BaseWidget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

public class IntegerBox extends BaseWidget {

	private com.google.gwt.user.client.ui.TextBox _tb;

	private int _min = 0;
	private int _max = 100;
	private int _step = 1;
	private int _value;

	public IntegerBox(int min, int max, int value, int step) {

		assert min >= 0 && min <= value && value <= max;
		_min = min;
		_max = max;
		_value = value;
		
		setStep(step);

		_tb = new com.google.gwt.user.client.ui.TextBox();
		_tb.setAlignment(TextAlignment.RIGHT);
		_tb.setText(Integer.toString(_value));
		_tb.addBlurHandler(new BlurHandler() {

			@Override
			public void onBlur(BlurEvent event) {
				if (_tb.getText().isEmpty()) {
					setValue(_value, false);
				} else {
					try {
						setValue(Integer.parseInt(_tb.getValue()), true);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});
		_tb.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (_tb.isReadOnly() || !_tb.isEnabled()) {
					return;
				}
				int keyCode = event.getNativeEvent().getKeyCode();
				switch (keyCode) {
				case KeyCodes.KEY_LEFT:
				case KeyCodes.KEY_RIGHT:
				case KeyCodes.KEY_BACKSPACE:
				case KeyCodes.KEY_DELETE:
				case KeyCodes.KEY_TAB:
					return;
				case KeyCodes.KEY_UP:
					if (_step > 0) {
						increase();
						_tb.cancelKey();
					}
					break;
				case KeyCodes.KEY_DOWN:
					if (_step > 0) {
						decrease();
						_tb.cancelKey();
					}
					break;
				}
			}
		});
		_tb.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {

				if (_tb.isReadOnly() || !_tb.isEnabled()) {
					return;
				}
				int keyCode = event.getNativeEvent().getKeyCode();
				switch (keyCode) {
				case KeyCodes.KEY_LEFT:
				case KeyCodes.KEY_RIGHT:
				case KeyCodes.KEY_BACKSPACE:
				case KeyCodes.KEY_DELETE:
				case KeyCodes.KEY_TAB:
				case KeyCodes.KEY_UP:
				case KeyCodes.KEY_DOWN:
					return;
				}
				String oldText = _tb.getText();
				String newText = oldText;
				if (Character.isDigit(event.getCharCode())) {
					int index = _tb.getCursorPos();
					if (_tb.getSelectionLength() > 0) {
						newText = oldText.substring(0, _tb.getCursorPos()) + event.getCharCode()
								+ oldText.substring(_tb.getCursorPos() + _tb.getSelectionLength(), oldText.length());
					} else {
						newText = oldText.substring(0, index) + event.getCharCode()
								+ oldText.substring(index, oldText.length());
					}
				}
				_tb.cancelKey();
				try {
					int newValue = Integer.parseInt(newText);

					if (keyCode == KeyCodes.KEY_ENTER) {
						setValue(newValue, true);
					} else {
						setValue(newValue, false);
					}
				} catch (Throwable e) {
					e.printStackTrace(System.out);
				}
			}
		});
		initWidget(_tb);
	}

	protected void increase() {
		int v = _min;
		try {
			v = Integer.parseInt(_tb.getValue());
		} catch (Throwable e) {
			v = _min;
		}
		int nv = v + _step;
		if (nv > _max) {
			nv = _max;
		}
		setValue(nv, true);
	}

	protected void decrease() {
		int v = _min;
		try {
			v = Integer.parseInt(_tb.getValue());
		} catch (Throwable e) {
			v = _min;
		}
		int nv = v - _step;
		if (nv < _min) {
			nv = _min;
		}
		setValue(nv, true);
	}

	public void setStep(int step) {
		if (step < 0) {
			return;
		}
		_step = step;
	}

	public void setValue(int value) {

		setValue(value, false);
	}

	public void setValue(int value, boolean fireEvents) {

		if (!(_min <= value && value <= _max)) {
			return;
		}
		String text = Integer.toString(value);
		_tb.setText(text);
		if (fireEvents) {
			_value = value;
			ValueChangeEvent.fire(_tb, text);
		}
	}

	public int value() {

		return _value;
	}

	public void addValueChangeHandler(ValueChangeHandler<String> handler) {
		_tb.addValueChangeHandler(handler);
	}

	public void setAlignment(TextAlignment align) {
		_tb.setAlignment(align);
	}

	public void enable() {
		setEnabled(true);
	}

	public void disable() {
		setEnabled(false);
	}

	public void setEnabled(boolean enabled) {
		_tb.setEnabled(enabled);
	}
	
	public void setMin(int min){
		_min = min;
	}
	
	public void setMax(int max){
		_max = max;
	}
}
