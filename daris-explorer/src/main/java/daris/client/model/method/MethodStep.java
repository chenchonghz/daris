package daris.client.model.method;

public class MethodStep {

    private MethodRef _method;
    private String _stepPath;

    public MethodStep(MethodRef method, String stepPath) {
        _method = method;
        _stepPath = stepPath;
    }

    public MethodStep(String methodId, String stepPath) {
        _method = new MethodRef(methodId);
    }

    public String path() {
        return _stepPath;
    }

    public MethodRef method() {
        return _method;
    }

    @Override
    public String toString() {
        return _stepPath;
    }

}
