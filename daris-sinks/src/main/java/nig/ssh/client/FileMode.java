package nig.ssh.client;

import java.util.regex.Pattern;

public class FileMode {
    private FileMode() {
    }

    public static boolean isValid(String mode) {
        if (mode == null) {
            return false;
        }
        if (!Pattern.matches("\\d{4}", mode)) {
            return false;
        } else {
            return true;
        }
    }
}
