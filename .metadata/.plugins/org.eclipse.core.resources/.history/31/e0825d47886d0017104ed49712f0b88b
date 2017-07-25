package org.wso2.charon3.utils.ldapmanager;

import java.io.UnsupportedEncodingException;
import java.security.Security;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSocketFactory;

/**
 * 
 * @author AkshathaKadri
 *
 */
public class LdapConnectUtil {

	static final String KEYSTORE = "C:\\Program Files\\Java\\jdk1.8.0_131\\jre\\lib\\security\\cacerts1";

	public static LDAPConnection getConnection() {
		return getConnection(false);
	}
	@SuppressWarnings("restriction")
	public static LDAPConnection getConnection(boolean isTest) {
		int ldapPort 		  = LDAPConnection.DEFAULT_SSL_PORT;

		int ldapVersion		  = LDAPConnection.LDAP_V3;

		String ldapHost, loginDN, password;
		if (!isTest) {
			ldapHost       = "192.168.1.11";
			loginDN        = "cn=admin,ou=services,o=system";
			password       = "admin123";
		} else {
			ldapHost       = "10.0.8.54";
			loginDN        = "cn=admin,ou=services,o=system";
			password       = "abcd1234";
		}

		try {
			// Dynamically set JSSE as a security provider
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

			// Dynamically set the property that JSSE uses to identify
			// the keystore that holds trusted root certificates
			System.setProperty("javax.net.ssl.trustStore", KEYSTORE);

			LDAPSocketFactory ssf = new LDAPJSSESecureSocketFactory();

			// Set the socket factory as the default for all future connections
			LDAPConnection.setSocketFactory(ssf);

			LDAPConnection lc = new LDAPConnection();
			// Note: the socket factory can also be passed in as a parameter
			// connect to the server
			lc.connect (ldapHost, ldapPort);

			// authenticate to the server
			lc.bind(ldapVersion, loginDN, password.getBytes("UTF8"));

			// at this point you are connected with a secure connection
			System.out.println("Successful SSL bind with server.");

			return lc;
		} catch( LDAPException e ) {
			System.out.println( "Error: " + e.toString() );
		} catch( UnsupportedEncodingException e ) {
			System.out.println( "Error: " + e.toString() );
		}
		return null;
	}
}
