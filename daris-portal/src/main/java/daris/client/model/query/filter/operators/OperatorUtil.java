package daris.client.model.query.filter.operators;

import java.util.List;

import arc.mf.expr.Operator;

public class OperatorUtil {

    public static <T extends Operator> T parse(List<T> ops, String s) {
        if (ops != null) {
            for (T op : ops) {
                if (op.matches(s)) {
                    return op;
                }
            }
        }
        return null;
    }

    public static <T extends Operator> T parse(T[] ops, String s) {
        for (T op : ops) {
            if (op.matches(s)) {
                return op;
            }
        }
        return null;
    }



}
