package daris.client.model.sc;


public enum Status {
    editable("editable"), await_processing("await  processing"), assigned("assigned"), processing("processing"), data_ready(
            "data ready"), fulfilled("fulfilled"), rejected("rejected"), error("error"), withdrawn("withdrawn"), aborted(
            "aborted");

    private String _status;

    Status(String status) {
        _status = status;
    }

    public final String status() {
        return _status;
    }

    @Override
    public final String toString() {
        return _status;
    }

    public static Status fromString(String status) {
        Status[] vs = values();
        for (Status v : vs) {
            if (v.status().equals(status)) {
                return v;
            }
        }
        return null;
    }

}
