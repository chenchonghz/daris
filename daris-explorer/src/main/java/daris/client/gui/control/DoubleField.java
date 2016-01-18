package daris.client.gui.control;

import javafx.scene.input.KeyEvent;

public class DoubleField extends NumberField<Double> {

    public DoubleField(double min, double max, Double value) {
        super(min, max, value);
    }

    @Override
    public void handle(KeyEvent ke) {
        if (ke.getCharacter().equals("-")) {
            if (_min.doubleValue() < 0 || getText().contains("-")) {
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
        double v = Double.parseDouble(text);
        double minV = _min.doubleValue();
        if (v < minV) {
            v = minV;
            setText(String.valueOf(minV));
        }
        double maxV = _max.doubleValue();
        if (v > maxV) {
            v = maxV;
            setText(String.valueOf(maxV));
        }
        valueProperty().setValue(new Double(v));
    }


}
