package nig.ssh.client.jsch;

import nig.ssh.client.UserDetails;

import com.jcraft.jsch.UserInfo;

public class JSchUserInfo {

    static UserInfo makeUserInfo(final UserDetails userDetails) {
        return new UserInfo() {

            @Override
            public String getPassphrase() {
                return userDetails.passphrase();
            }

            @Override
            public String getPassword() {
                return userDetails.password();
            }

            @Override
            public boolean promptPassword(String message) {
                return getPassword() != null;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return getPassphrase() != null;
            }

            @Override
            public boolean promptYesNo(String message) {
                return false;
            }

            @Override
            public void showMessage(String message) {

            }
        };
    }

}
