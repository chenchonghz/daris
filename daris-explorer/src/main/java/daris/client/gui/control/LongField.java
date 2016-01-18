package daris.client.gui.control;

import javafx.scene.input.KeyEvent;

public class LongField extends NumberField<Long> {

    public LongField(long min, long max, Long value) {
        super(min, max, value);
    }

    @Override
    public void handle(KeyEvent ke) {
        if (ke.getCharacter().equals("-")) {
            if (_min.longValue() < 0 || getText().contains("-")) {
                ke.consume();
            }
            return;
        }
        if ("0123456789".contains(ke.getCharacter())) {
            return;
        }
        ke.consume();
    }

    @Override
    protected void updateValue(String text) {
        long v = Long.parseLong(text);
        long minV = _min.longValue();
        if (v < minV) {
            v = minV;
            setText(String.valueOf(minV));
        }
        long maxV = _max.longValue();
        if (v > maxV) {
            v = maxV;
            setText(String.valueOf(maxV));
        }
        valueProperty().setValue(new Long(v));
    }


}
