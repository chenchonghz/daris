package daris.client.gui.control;

import javafx.scene.input.KeyEvent;

public class FloatField extends NumberField<Float> {

    public FloatField(float min, float max, Float value) {
        super(min, max, value);
    }

    @Override
    public void handle(KeyEvent ke) {
        if (ke.getCharacter().equals("-")) {
            if (_min.floatValue() < 0 || getText().contains("-")) {
                ke.consume();
            }
            return;
        }
        if (ke.getCharacter().equals(".")) {
            if (getText().contains(".")) {
                ke.consume();
            }
            return;
        }
        if ("0123456789.".contains(ke.getCharacter())) {
            return;
        }
        ke.consume();
    }

    @Override
    protected void updateValue(String text) {
        float v = Float.parseFloat(text);
        float minV = _min.floatValue();
        if (v < minV) {
            v = minV;
            setText(String.valueOf(minV));
        }
        float maxV = _max.floatValue();
        if (v > maxV) {
            v = maxV;
            setText(String.valueOf(maxV));
        }
        valueProperty().setValue(new Float(v));
    }


}
