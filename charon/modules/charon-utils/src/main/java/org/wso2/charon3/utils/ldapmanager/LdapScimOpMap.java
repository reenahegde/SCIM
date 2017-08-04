/**
 * 
 */
package org.wso2.charon3.utils.ldapmanager;

/**
 * @author AkshathaKadri
 *
 */
public enum LdapScimOpMap {
	eq("="), 
	ne("&"), 
	ge(">="), 
	gt(">"), 
	le("<="), 
	lt("<"),
	
	and("&"), 
	or("|"), 
	not("!");
	
	private final String value;
	LdapScimOpMap(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
