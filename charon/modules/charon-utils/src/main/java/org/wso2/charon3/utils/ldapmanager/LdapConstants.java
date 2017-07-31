package org.wso2.charon3.utils.ldapmanager;

public class LdapConstants {
	public static final String objectclass = "objectclass";
	
	public static final String userContainer = "ou=users,o=people";
	public static final String userClass = "inetOrgPerson";
	
	public static final String groupContainer = "ou=groups,o=people";
	//public static final String groupClass = "organizationalUnit";
	public static final String groupClass = "groupOfNames";
	
	
	public class GroupConstants {
		
		public static final String cn = "cn";
		public static final String groupID = "l"; //'groupId' is Integer in Ldap
		public static final String localityname = "fullName";
		public static final String revision = "businessCategory";
		public static final String description = "description";
		public static final String member = "member";
		public static final String equivalent = "equivalentToMe";
	}
	
	public class UserConstants {
		public static final String uid = "uid";
	}
}
