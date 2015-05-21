package daris.client.ui.image;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.label.Label;
import arc.mf.client.util.ActionListener;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

import daris.client.Resource;
import daris.client.ui.widget.IntegerBox;

public class ImageSwitchControl extends ButtonBar {

    public static interface ImageSwitchListener {
        void switchTo(int index, ActionListener al);
    }

    public static final arc.gui.image.Image IMG_FIRST = new arc.gui.image.Image(Resource.INSTANCE.first24()
            .getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image IMG_LAST = new arc.gui.image.Image(Resource.INSTANCE.last24().getSafeUri()
            .asString(), 16, 16);

    public static final arc.gui.image.Image IMG_NEXT = new arc.gui.image.Image(Resource.INSTANCE.forward24()
            .getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image IMG_FAST_NEXT = new arc.gui.image.Image(Resource.INSTANCE.fastForward24()
            .getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image IMG_PREV = new arc.gui.image.Image(Resource.INSTANCE.rewind24()
            .getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image IMG_FAST_PREV = new arc.gui.image.Image(Resource.INSTANCE.fastRewind24()
            .getSafeUri().asString(), 16, 16);

    private int _index;
    private int _total;

    private Button _firstButton;
    private Button _fastPrevButton;
    private Button _prevButton;
    private IntegerBox _idxField;
    private Button _nextButton;
    private Button _fastNextButton;
    private Button _lastButton;

    private List<ImageSwitchListener> _ls;

    public ImageSwitchControl(int total) {
        this("index", total);
    }

    public ImageSwitchControl(String label, int total) {
        super(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER);
        _index = 0;
        _total = total;

        setHeight(28);
        setWidth100();
        setColourEnabled(false);
        setBackgroundColour(new RGB(0xdd, 0xdd, 0xdd));

        _firstButton = createImageButton(IMG_FIRST);
        _firstButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                first();
            }
        });
        add(_firstButton);

        _fastPrevButton = createImageButton(IMG_FAST_PREV);
        _fastPrevButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fastPrev();
            }
        });
        add(_fastPrevButton);

        _prevButton = createImageButton(IMG_PREV);
        _prevButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                prev();
            }
        });
        add(_prevButton);

        Label lbl = new Label(label);
        lbl.setFontSize(12);
        lbl.setFontWeight(FontWeight.BOLD);
        add(lbl);

        _idxField = new IntegerBox(1, _total, _index + 1, 1);
        _idxField.setFontSize(12);
        _idxField.setWidth(30);
        _idxField.setAlignment(TextAlignment.RIGHT);
        _idxField.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                goTo(_idxField.value() - 1, null);
            }
        });
        add(_idxField);

        Label totalLabel = new Label("/" + _total);
        totalLabel.setFontSize(12);
        add(totalLabel);

        _nextButton = createImageButton(IMG_NEXT);
        _nextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                next();
            }
        });
        add(_nextButton);

        _fastNextButton = createImageButton(IMG_FAST_NEXT);
        _fastNextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                fastNext();
            }
        });
        add(_fastNextButton);

        _lastButton = createImageButton(IMG_LAST);
        _lastButton.setWidth(30);
        _lastButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                last();
            }
        });
        add(_lastButton);

    }

    public void addListener(ImageSwitchListener l) {
        if (_ls == null) {
            _ls = new ArrayList<ImageSwitchListener>();
        }
        if (l != null) {
            _ls.add(l);
        }
    }

    public void removeListener(ImageSwitchListener l) {
        if (_ls != null) {
            _ls.remove(l);
        }
    }

    public void goTo(int index, final ActionListener al) {
        lockComponents();
        if (_ls != null) {
            _index = index;
            _idxField.setValue(_index+1, false);
            for (ImageSwitchListener l : _ls) {
                l.switchTo(index, new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        unlockComponents();
                        if (al != null) {
                            al.executed(succeeded);
                        }
                    }
                });
            }
        }
    }

    private void lockComponents() {
        _firstButton.disable();
        _fastPrevButton.disable();
        _prevButton.disable();
        _idxField.disable();
        _nextButton.disable();
        _fastNextButton.disable();
        _lastButton.disable();
    }

    private void unlockComponents() {
        _firstButton.enable();
        _fastPrevButton.enable();
        _prevButton.enable();
        _idxField.enable();
        _nextButton.enable();
        _fastNextButton.enable();
        _lastButton.enable();
    }

    public int index() {
        return _index;
    }

    public int total() {
        return _total;
    }

    public void first(ActionListener al) {
        goTo(0, al);
    }

    public void first() {
        goTo(0, null);
    }

    public void last(ActionListener al) {
        goTo(total() - 1, al);
    }

    public void last() {
        goTo(total() - 1, null);
    }

    public void next(ActionListener al) {
        if (_index >= _total - 1) {
            return;
        }
        goTo(_index + 1, al);
    }

    public void next() {
        next(null);
    }

    public void fastNext(ActionListener al) {
        if (_index >= _total - 1) {
            return;
        }
        int step = _total / 10;
        if (step < 1) {
            step = 1;
        }
        if (_index + step < _total - 1) {
            goTo(_index + step, al);
        } else {
            goTo(_total - 1, al);
        }
    }

    public void fastNext() {
        fastNext(null);
    }

    public void prev(ActionListener al) {
        if (_index > 0) {
            goTo(_index - 1, al);
        }
    }

    public void prev() {
        prev(null);
    }

    public void fastPrev(ActionListener al) {
        int step = _total / 10;
        if (step < 1) {
            step = 1;
        }
        if (_index - step > 0) {
            goTo(_index - step, al);
        } else {
            goTo(0, al);
        }
    }

    public void fastPrev() {
        fastPrev(null);
    }

    private static Button createImageButton(arc.gui.image.Image image) {
        Button button = new Button("<img width=\"" + image.width() + "px\" height=\"" + image.height() + "px\" src=\""
                + image.path() + "\" style=\"vertical-align: middle; padding-top: 0px;\"></img>", false);
        button.setWidth(30);
        return button;
    }

    public void middle() {
        goTo(_total / 2, null);
    }

}
