
package org.n52.aviation.aviationfx.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Credentials {

    private String user;
    private String password;

    public Credentials() {
    }

    public Credentials(String user, String password) {
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
