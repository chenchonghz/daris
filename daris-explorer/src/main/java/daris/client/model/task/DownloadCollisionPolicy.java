package daris.client.model.task;

public enum DownloadCollisionPolicy {
    SKIP, OVERWRITE, RENAME;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static DownloadCollisionPolicy fromString(String s,
            DownloadCollisionPolicy def) {
        if (s != null) {
            DownloadCollisionPolicy[] vs = values();
            for (DownloadCollisionPolicy v : vs) {
                if (v.name().equalsIgnoreCase(s)) {
                    return v;
                }
            }
        }
        return def;
    }

}
