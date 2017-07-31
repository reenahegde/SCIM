package org.wso2.charon3.utils.ldapmanager;

public class LdapConstants {
	public static final String objectclass = "objectclass";
	
	public static final String userContainer = "ou=users,o=people";
	public static final String userClass = "inetOrgPerson";
	
	public static final String groupContainer = "ou=groups,o=people";
	//public static final String groupClass = "organizationalUnit";
	public static final String groupClass = "groupOfNames";
	
	public static final String LDAP_DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";
    public static final String SCIM_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public enum LDAPGroupMap{
		name, groupID, createdDate, modifiedDate, location, member;
	}
	public class GroupConstants {
		
		public static final String name = "cn";
		public static final String groupID = "l"; //'groupId' is Integer in Ldap
		public static final String createdDate = "fullName";
		public static final String modifiedDate = "businessCategory";
		public static final String location = "description";
		public static final String member = "member";
		public static final String equivalent = "equivalentToMe";
	}
	
	public class UserConstants {
		public static final String uid = "uid";
	}
	
}
