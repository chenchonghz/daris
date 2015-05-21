package nig.mf.plugin.pssd.secure.wallet;

import arc.mf.plugin.ServiceExecutor;

public class SecureWallet {

    public static final String ENTRY_URL_PREFIX = "swkey:";

    public static String keyFromUrl(String url) {
        return url.substring(ENTRY_URL_PREFIX.length());
    }

    public static boolean isEntryUrl(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith(ENTRY_URL_PREFIX);
    }

    public static String getEntryValueByKey(ServiceExecutor executor, String key) throws Throwable {
        return executor.execute("secure.wallet.get", "<args><key>" + key + "</key></args>", null, null).value("value");
    }

    public static String getEntryValueByUrl(ServiceExecutor executor, String url) throws Throwable {
        return getEntryValueByKey(executor, keyFromUrl(url));
    }
}
