package org.wso2.charon3.utils.ldapmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.charon3.core.attributes.Attribute;
import org.wso2.charon3.core.attributes.ComplexAttribute;
import org.wso2.charon3.core.attributes.DefaultAttributeFactory;
import org.wso2.charon3.core.attributes.MultiValuedAttribute;
import org.wso2.charon3.core.attributes.SimpleAttribute;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.schema.AttributeSchema;
import org.wso2.charon3.core.schema.SCIMConstants;
import org.wso2.charon3.core.schema.SCIMConstants.UserSchemaConstants;
import org.wso2.charon3.core.schema.SCIMDefinitions;
import org.wso2.charon3.core.schema.SCIMSchemaDefinitions;
import org.wso2.charon3.core.utils.AttributeUtil;
import org.wso2.charon3.utils.ldapmanager.LdapConstants.GroupConstants;
import org.wso2.charon3.utils.ldapmanager.LdapConstants.UserConstants;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;

/**
 * 
 * @author AkshathaKadri
 * 
 * @author ReenaHegde
 *
 */

public class LdapUtil {

	public static LDAPEntry copyUserToLdap0(User user) throws CharonException {
		System.out.println(user);
		LDAPAttributeSet attributeSet = new LDAPAttributeSet();
		attributeSet.add(new LDAPAttribute("uid", user.getId()));

		Map<String, Attribute> attributes = user.getAttributeList();
		Set<String> keys = attributes.keySet();
		for (String key : keys) {
			attributeSet.add(new LDAPAttribute(key, attributes.get(key).toString()));
		}
		String dn = "cn=" + user.getId() + ",ou=users,o=people";
		LDAPEntry entry = new LDAPEntry(dn, attributeSet);
		// attributeSet.addAll((Collection) user.getAttributeList());
		// (LDAPAttributeSet) CopyUtil.deepCopy(map);
		return entry;
	}

	/**
	 * Converts LDAP Entry object in to SCIM object
	 * @param entry
	 * @return
	 */
	public static User convertLdapToUser(LDAPEntry entry) {

		User user = new User();
		try {
			LDAPAttributeSet attributeSet = entry.getAttributeSet();
			Date createdDate;
			Date lastModified;
			String createdDateStr;
			String lastModifiedStr;
			SimpleDateFormat ldapFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
			SimpleDateFormat scimFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			user.setId(attributeSet.getAttribute(LdapScimAttrMap.id.getValue()).getStringValue());
			user.setUserName(attributeSet.getAttribute(LdapScimAttrMap.userName.getValue()).getStringValue());

			if (attributeSet.getAttribute(LdapScimAttrMap.created.getValue()) != null) {
				createdDate = ldapFormat
						.parse(attributeSet.getAttribute(LdapScimAttrMap.created.getValue()).getStringValue());
				createdDateStr = scimFormat.format(createdDate);
				createdDate = AttributeUtil.parseDateTime(createdDateStr);
			} else {
				createdDate = new Date();
			}

			user.setCreatedDate(createdDate);

			if (attributeSet.getAttribute(LdapScimAttrMap.lastModified.getValue()) != null) {
				lastModified = ldapFormat
						.parse(attributeSet.getAttribute(LdapScimAttrMap.lastModified.getValue()).getStringValue());
				lastModifiedStr = scimFormat.format(lastModified);
				lastModified = AttributeUtil.parseDateTime(lastModifiedStr);
			} else {
				lastModified = new Date();
			}

			user.setLastModified(lastModified);

			if (attributeSet.getAttribute(LdapScimAttrMap.location.getValue()) != null) {
				user.setLocation(attributeSet.getAttribute(LdapScimAttrMap.location.getValue()).getStringValue());
			} else {
				user.setLocation("http://localhost:8080/scim/v2/Users/" + user.getId());
			}

			if(isNameSetInLdap(attributeSet)){
				ComplexAttribute nameAttribute = createComplexAttribute(SCIMSchemaDefinitions.SCIMUserSchemaDefinition.NAME, 
						SCIMConstants.UserSchemaConstants.NAME, user);
				if(attributeSet.getAttribute(LdapScimAttrMap.formatted.getValue()) != null ){
					SimpleAttribute formattedNameAttribute = createSimpleSubAttribute(SCIMConstants.UserSchemaConstants.FORMATTED_NAME, 
							attributeSet.getAttribute(LdapScimAttrMap.formatted.getValue()).getStringValue(),
							SCIMSchemaDefinitions.SCIMUserSchemaDefinition.FORMATTED);
					nameAttribute.setSubAttribute(formattedNameAttribute);
				}

				if(attributeSet.getAttribute(LdapScimAttrMap.familyName.getValue()) != null){
					SimpleAttribute familyNameAttribute = createSimpleSubAttribute(SCIMConstants.UserSchemaConstants.FAMILY_NAME, 
							attributeSet.getAttribute(LdapScimAttrMap.familyName.getValue()).getStringValue(),
							SCIMSchemaDefinitions.SCIMUserSchemaDefinition.FAMILY_NAME);
					nameAttribute.setSubAttribute(familyNameAttribute);
				}

				if(attributeSet.getAttribute(LdapScimAttrMap.givenName.getValue()) !=null){			
					SimpleAttribute givenNameAttribute = createSimpleSubAttribute(SCIMConstants.UserSchemaConstants.GIVEN_NAME, 
							attributeSet.getAttribute(LdapScimAttrMap.givenName.getValue()).getStringValue(),
							SCIMSchemaDefinitions.SCIMUserSchemaDefinition.GIVEN_NAME);
					nameAttribute.setSubAttribute(givenNameAttribute);
				}

				if(attributeSet.getAttribute(LdapScimAttrMap.middleName.getValue()) !=null){		
					SimpleAttribute middleNameAttribute = createSimpleSubAttribute(SCIMConstants.UserSchemaConstants.MIDDLE_NAME, 
							attributeSet.getAttribute(LdapScimAttrMap.middleName.getValue()).getStringValue(),
							SCIMSchemaDefinitions.SCIMUserSchemaDefinition.MIDDLE_NAME);
					nameAttribute.setSubAttribute(middleNameAttribute);
				}

				if(attributeSet.getAttribute(LdapScimAttrMap.honorificPrefix.getValue()) !=null){	
					SimpleAttribute honoroficPrefixAttribute = createSimpleSubAttribute(SCIMConstants.UserSchemaConstants.HONORIFIC_PREFIX, 
							attributeSet.getAttribute(LdapScimAttrMap.honorificPrefix.getValue()).getStringValue(),
							SCIMSchemaDefinitions.SCIMUserSchemaDefinition.HONORIFIC_PREFIX);
					nameAttribute.setSubAttribute(honoroficPrefixAttribute);
				}

				if(attributeSet.getAttribute(LdapScimAttrMap.honorificSuffix.getValue()) !=null){	
					SimpleAttribute honoroficSuffixAttribute = createSimpleSubAttribute(SCIMConstants.UserSchemaConstants.HONORIFIC_SUFFIX, 
							attributeSet.getAttribute(LdapScimAttrMap.honorificSuffix.getValue()).getStringValue(),
							SCIMSchemaDefinitions.SCIMUserSchemaDefinition.HONORIFIC_SUFFIX);
					nameAttribute.setSubAttribute(honoroficSuffixAttribute);
				}

				user.setAttribute(nameAttribute);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
		return user;
	}

	public static LDAPAttributeSet copyUserToLdap(User user) {
		LDAPAttributeSet attributeSet = new LDAPAttributeSet();
		attributeSet.add(new LDAPAttribute(LdapConstants.objectclass, LdapConstants.userClass));
		Map<String, Attribute> attributeList = user.getAttributeList();
		for (Attribute attribute : attributeList.values()) {
			if (attribute instanceof SimpleAttribute) {
				attributeSet = addSimpleAttribute(null, attributeSet, attribute);

			} else if (attribute instanceof ComplexAttribute) {
				ComplexAttribute complexAttribute = (ComplexAttribute) attribute;
				Map<String, Attribute> subAttributes = complexAttribute.getSubAttributesList();
				for (Attribute subAttribute : subAttributes.values()) {
					if (subAttribute instanceof SimpleAttribute) {
						attributeSet = addSimpleAttribute(null, attributeSet,
								(Attribute) ((SimpleAttribute) subAttribute));

					} else if (subAttribute instanceof MultiValuedAttribute) {
						if (!subAttribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
							attributeSet = addMultiValuedPrimitiveAttribute(
									((MultiValuedAttribute) subAttribute).getAttributePrimitiveValues(),
									subAttribute.getName(), attributeSet);
						} else {
							List<Attribute> subAttributeList = ((MultiValuedAttribute) (subAttribute))
									.getAttributeValues();

							for (Attribute subValue : subAttributeList) {

								ComplexAttribute complexSubAttribute = (ComplexAttribute) subValue;
								Map<String, Attribute> subSubAttributes = complexSubAttribute.getSubAttributesList();

								for (Attribute subSubAttribute : subSubAttributes.values()) {
									if (subSubAttribute instanceof SimpleAttribute) {

										attributeSet = addSimpleAttribute(null, attributeSet,
												(Attribute) ((SimpleAttribute) subSubAttribute));

									} else if (subSubAttribute instanceof MultiValuedAttribute) {
										attributeSet = addMultiValuedPrimitiveAttribute(
												((MultiValuedAttribute) subSubAttribute).getAttributePrimitiveValues(),
												subSubAttribute.getName(), attributeSet);
									}
								}
							}
						}
					} else if (subAttribute instanceof ComplexAttribute) {
						ComplexAttribute complexSubAttribute = (ComplexAttribute) subAttribute;
						Map<String, Attribute> subSubAttributes = complexSubAttribute.getSubAttributesList();

						for (Attribute subSubAttribute : subSubAttributes.values()) {
							if (subSubAttribute instanceof SimpleAttribute) {
								attributeSet = addSimpleAttribute(null, attributeSet,
										(Attribute) ((SimpleAttribute) subSubAttribute));

							} else if (subSubAttribute instanceof MultiValuedAttribute) {
								attributeSet = addMultiValuedPrimitiveAttribute(
										((MultiValuedAttribute) subSubAttribute).getAttributePrimitiveValues(),
										subSubAttribute.getName(), attributeSet);
							}
						}
					}
				}
			} else if (attribute instanceof MultiValuedAttribute) {
				MultiValuedAttribute multiValuedAttribute = (MultiValuedAttribute) attribute;
				if (multiValuedAttribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
					List<Attribute> subAttributeList = multiValuedAttribute.getAttributeValues();
					for (Attribute subAttribute : subAttributeList) {
						ComplexAttribute complexSubAttribute = (ComplexAttribute) subAttribute;
						Map<String, Attribute> subSubAttributes = complexSubAttribute.getSubAttributesList();
						// If address, check for home address START-------
						if (subAttribute.getURI().equals(UserSchemaConstants.ADDRESSES_URI)) {
							String value = null;
							boolean isHome = false;
							for (Attribute subSubAttribute : subSubAttributes.values()) {
								SimpleAttribute simpleAttribute = (SimpleAttribute) subSubAttribute;
								if (subSubAttribute.getName().equals("type")) {
									// Check if type is "home"
									if (simpleAttribute.getValue().equals(UserSchemaConstants.HOME)) {
										isHome = true;
									} else {
										if (LdapScimAttrMap.addresses.isSet()) {
											continue;
										}
										break;
									}
								} else if (subSubAttribute.getName().equals("formatted")) {
									value = (String) simpleAttribute.getValue();
								}
							}
							if (isHome) {
								attributeSet.add(new LDAPAttribute(LdapIPersonConstants.homePostalAddress, value));
							}
							continue;
						}
						// If address END-------
						String parent = getAttributeName(subAttribute);
						for (Attribute subSubAttribute : subSubAttributes.values()) {
							if (subSubAttribute instanceof SimpleAttribute) {
								if (subSubAttribute.getName().equals("value")) {
									attributeSet = addSimpleAttribute(parent, attributeSet,
											(Attribute) ((SimpleAttribute) subSubAttribute));
								} /*
								 * if (UserSchemaConstants.ADDRESSES.equals(
								 * parent)) { parent=parent+"_"+ }
								 */

							} else if (subSubAttribute instanceof MultiValuedAttribute) {
								attributeSet = addMultiValuedPrimitiveAttribute(
										((MultiValuedAttribute) subSubAttribute).getAttributePrimitiveValues(),
										subSubAttribute.getName(), attributeSet);
							}
						}
					}
				} else {
					List<Object> primitiveValueList = multiValuedAttribute.getAttributePrimitiveValues();
					attributeSet = addMultiValuedPrimitiveAttribute(primitiveValueList, multiValuedAttribute.getName(),
							attributeSet);
				}

			}
		}
		return attributeSet;
	}

	private static LDAPAttributeSet addSimpleAttribute(String parentName, LDAPAttributeSet attributeSet,
			Attribute attribute) {
		SimpleAttribute simpleAttribute = (SimpleAttribute) attribute;
		try {
			LdapScimAttrMap name;
			if (parentName != null) {
				name = LdapScimAttrMap.valueOf(parentName);
			} else {
				name = LdapScimAttrMap.valueOf(simpleAttribute.getName());
			}

			if (name != null && attributeSet != null) {
				attributeSet.add(new LDAPAttribute(name.getValue(), simpleAttribute.getValue().toString()));
			}
		} catch (Exception e) {
			System.out.println("Mapping for '" + simpleAttribute.getName() + "' missing!");
		}
		return attributeSet;
	}

	private static LDAPAttributeSet addMultiValuedPrimitiveAttribute(List<Object> attributePrimitiveValues,
			String attributeName, LDAPAttributeSet attributeSet) {
		try {
			LdapScimAttrMap name = LdapScimAttrMap.valueOf(attributeName);
			if (name != null && attributeSet != null) {
				for (Object item : attributePrimitiveValues) {
					attributeSet.add(new LDAPAttribute(attributeName, (String) item));
				}
			}
		} catch (Exception e) {
			System.out.println("Mapping for '" + attributeName + "' missing!");
		}
		return attributeSet;
	}

	private static String getAttributeName(Attribute subAttribute) {
		String parent = null;
		ComplexAttribute complexSubAttribute = (ComplexAttribute) subAttribute;
		Map<String, Attribute> subSubAttributes = complexSubAttribute.getSubAttributesList();

		switch (subAttribute.getURI()) {
		case UserSchemaConstants.EMAILS_URI:
			parent = UserSchemaConstants.EMAILS;
		case UserSchemaConstants.PHONE_NUMBERS_URI:
			parent = (parent == null) ? UserSchemaConstants.PHONE_NUMBERS : parent;
		case UserSchemaConstants.PHOTOS_URI:
			parent = parent == null ? UserSchemaConstants.PHOTOS : parent;
			for (Attribute subSubAttribute : subSubAttributes.values()) {
				if (subSubAttribute.getName().equals("type")) {
					SimpleAttribute simpleAttribute = (SimpleAttribute) subSubAttribute;
					parent = parent + "_" + simpleAttribute.getValue();
					break;
				}
			}
			break;
		case UserSchemaConstants.GROUP_URI:
			parent = UserSchemaConstants.GROUPS;
			break;
		case UserSchemaConstants.IMS_URI:
			parent = UserSchemaConstants.IMS;
			break;
		case UserSchemaConstants.X509CERTIFICATES_URI:
			parent = UserSchemaConstants.X509CERTIFICATES;
			break;
		case UserSchemaConstants.ADDRESSES_URI:
			LdapScimAttrMap.valueOf(subAttribute.getName()).setSet(true);
			// parent = subAttribute.getName();
			break;
		}
		return parent;
	}

	/**
	 * Creates complex SCIM attribute
	 * 
	 * @param schema
	 * @param compAttr
	 * @throws CharonException
	 * @throws BadRequestException
	 */
	private static ComplexAttribute createComplexAttribute(AttributeSchema schema, String compAttrName, User u)
			throws CharonException, BadRequestException {
		ComplexAttribute complexAttribute;
		if (!u.getAttributeList().containsKey(compAttrName)) {
			complexAttribute = (ComplexAttribute) DefaultAttributeFactory.createAttribute(schema,
					new ComplexAttribute(compAttrName));
			u.getAttributeList().put(compAttrName, complexAttribute);
		} else {
			complexAttribute = (ComplexAttribute) u.getAttribute(compAttrName);
		}
		return complexAttribute;

	}

	/**
	 * Creates simple subAttribute for SCIM
	 * 
	 * @param createdDate
	 * @throws CharonException
	 * @throws BadRequestException
	 */
	private static SimpleAttribute createSimpleSubAttribute(String subAttributeName, Object value,
			AttributeSchema schema) throws CharonException, BadRequestException {
		// create the created date attribute as defined in schema.
		SimpleAttribute simpleAttribute = new SimpleAttribute(subAttributeName, value);
		simpleAttribute = (SimpleAttribute) DefaultAttributeFactory.createAttribute(schema, simpleAttribute);

		return simpleAttribute;

	}

	private static boolean isNameSetInLdap(LDAPAttributeSet attributeSet){
		if(attributeSet.getAttribute(LdapScimAttrMap.formatted.getValue()) != null )
			return true;
		else if(attributeSet.getAttribute(LdapScimAttrMap.familyName.getValue()) != null)
			return true;
		else if(attributeSet.getAttribute(LdapScimAttrMap.givenName.getValue()) != null)
			return true;
		else if(attributeSet.getAttribute(LdapScimAttrMap.middleName.getValue()) !=null)
			return true;
		else if(attributeSet.getAttribute(LdapScimAttrMap.honorificPrefix.getValue()) !=null)
			return true;
		else if(attributeSet.getAttribute(LdapScimAttrMap.honorificSuffix.getValue()) !=null)
			return true;
		else 
			return false;
	}

	public static LDAPAttributeSet copyGroupToLdap(Group group) {
		LDAPAttributeSet attributeSet = new LDAPAttributeSet();
		attributeSet.add(new LDAPAttribute(LdapConstants.objectclass, LdapConstants.groupClass));
		try {
			attributeSet.add(new LDAPAttribute(GroupConstants.cn, group.getDisplayName()));
			attributeSet.add(new LDAPAttribute(GroupConstants.groupID, group.getId()));
			attributeSet.add(new LDAPAttribute(GroupConstants.localityname, group.getLocation()));
			attributeSet.add(new LDAPAttribute(GroupConstants.revision, group.getCreatedDate().toString()));
			attributeSet.add(new LDAPAttribute(GroupConstants.description, group.getLastModified().toString()));
			//List<String> members = group.getMembersWithDisplayName();
			List<Object> members = group.getMembers();
			String[] groupMembers = new String[members.size()];
			int i=0;
			for(Object id: members){
				String uid = (String) id;
				String dn = UserConstants.uid+ "="+ uid+ "," +LdapConstants.userContainer;
				groupMembers[i++] = dn;
			}
			attributeSet.add(new LDAPAttribute(GroupConstants.member, groupMembers));
		} catch (CharonException e) {
			e.printStackTrace();
		}
		return attributeSet;
	}
}
