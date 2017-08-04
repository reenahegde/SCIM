/**
 * 
 */
package org.wso2.charon3.utils.ldapmanager;

import org.wso2.charon3.utils.ldapmanager.LdapConstants.GroupConstants;

/**
 * @author AkshathaKadri
 *
 */
public enum LdapScimGroupMap {
	id(GroupConstants.cn), 
	displayName(GroupConstants.name), 
	created(GroupConstants.createdDate), 
	lastModified(GroupConstants.modifiedDate), 
	location(GroupConstants.location), 
	members(GroupConstants.member);
	
	private final String value;
	LdapScimGroupMap(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
