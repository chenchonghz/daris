package daris.util;

import java.util.ArrayList;
import java.util.List;

public class PathBuilder {

    private List<String> _paths;

    public PathBuilder() {
        _paths = new ArrayList<String>();
    }

    public PathBuilder append(String path) {
        if (path != null) {
            _paths.add(path);
        }
        return this;
    }

    public String build() {
        return PathUtils.join(_paths);
    }

    @Override
    public String toString() {
        return build();
    }

}
