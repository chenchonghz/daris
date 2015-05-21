package nig.mf.plugin.pssd.sc;

public enum Status {
    editable("editable"), await_processing("await processing"), assigned("assigned"), processing("processing"), data_ready(
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
    public String toString() {
        return _status;
    }

    public static Status fromString(String status) {
        if (status != null) {
            Status[] vs = values();
            for (Status v : vs) {
                if (v.status().equalsIgnoreCase(status)) {
                    return v;
                }
            }
        }
        return null;
    }

}
