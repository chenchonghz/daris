package daris.client.gui.control;

import javafx.scene.input.KeyEvent;

public class IntegerField extends NumberField<Integer> {

    public IntegerField(int min, int max, Integer value) {
        super(min, max, value);
    }

    @Override
    public void handle(KeyEvent ke) {
        if (ke.getCharacter().equals("-")) {
            if (_min.intValue() < 0 || getText().contains("-")) {
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
        int v = Integer.parseInt(text);
        int minV = _min.intValue();
        if (v < minV) {
            v = minV;
            setText(String.valueOf(minV));
        }
        int maxV = _max.intValue();
        if (v > maxV) {
            v = maxV;
            setText(String.valueOf(maxV));
        }
        valueProperty().setValue(new Integer(v));
    }

}
