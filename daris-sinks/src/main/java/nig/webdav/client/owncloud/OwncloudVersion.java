package nig.webdav.client.owncloud;


public class OwncloudVersion implements Comparable<OwncloudVersion> {

    public static final OwncloudVersion OC1_00 = new OwncloudVersion(0x010000);
    public static final OwncloudVersion OC2_00 = new OwncloudVersion(0x020000);
    public static final OwncloudVersion OC3_00 = new OwncloudVersion(0x030000);
    public static final OwncloudVersion OC4_00 = new OwncloudVersion(0x040000);
    public static final OwncloudVersion OC4_05 = new OwncloudVersion(0x040500);
    public static final OwncloudVersion MIN_VERISION_SUPPORT_SHARE = new OwncloudVersion(0x05000D);

    private int _version;

    /**
     * Constractor.
     * 
     * @param version
     *            the version string. it is in the format of AA.BB.CC. and it will be stored as 0xAABBCC. For example,
     *            version 2.0.3 will be stored as 0x020003.
     */
    public OwncloudVersion(String version) throws Throwable {

        String[] nums = version.replaceAll("[^\\d.]", "").split("\\.");
        if (nums.length > 3 || nums.length <= 0) {
            throw new IllegalArgumentException("Invalid version string: " + version);
        }
        int v = 0;
        if (nums.length > 0) {
            v += Integer.parseInt(nums[0]);
        }
        v = v << 8;
        if (nums.length > 1) {
            v += Integer.parseInt(nums[1]);
        }
        v = v << 8;
        if (nums.length > 2) {
            v += Integer.parseInt(nums[2]);
        }
        _version = v;
    }

    public OwncloudVersion(int version) {
        _version = version;
    }

    public int version() {
        return _version;
    }

    public int major() {
        return (_version >> 16) & 0xff;
    }

    public int minor() {
        return (_version >> 8) & 0xff;
    }

    public int build() {
        return _version & 0xff;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(major()).append(".").append(minor()).append(".").append(build()).toString();
    }

    @Override
    public int compareTo(OwncloudVersion o) {
        if (o == null) {
            return 1;
        }
        if (_version > o.version()) {
            return 1;
        } else if (_version == o.version()) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && (o instanceof OwncloudVersion)) {
            return _version == ((OwncloudVersion) o).version();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return _version;
    }

}
