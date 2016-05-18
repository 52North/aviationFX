
package org.n52.aviation.aviationfx.subscribe;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Authentication {

    private final String user;
    private final String password;

    public Authentication(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }


}
