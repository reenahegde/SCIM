package org.wso2.charon3.utils.ldapmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.charon3.core.attributes.Attribute;
import org.wso2.charon3.core.attributes.ComplexAttribute;
import org.wso2.charon3.core.attributes.MultiValuedAttribute;
import org.wso2.charon3.core.attributes.SimpleAttribute;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.schema.SCIMConstants.UserSchemaConstants;
import org.wso2.charon3.core.schema.SCIMDefinitions;

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

	public static LDAPEntry copyUserToLdap0(User user) throws CharonException{
		System.out.println(user);
		LDAPAttributeSet attributeSet = new LDAPAttributeSet();
		attributeSet.add(new LDAPAttribute("uid",user.getId()));

		Map<String, Attribute> attributes = user.getAttributeList();
		Set<String> keys = attributes.keySet();
		for(String key:keys){
			attributeSet.add(new LDAPAttribute(key,attributes.get(key).toString()));
		}
		String dn = "cn="+user.getId()+",ou=users,o=people";
		LDAPEntry entry = new LDAPEntry(dn,attributeSet);
		//attributeSet.addAll((Collection) user.getAttributeList());
		//(LDAPAttributeSet) CopyUtil.deepCopy(map);   
		return entry;
	}
	
	public static User convertLdapToUser(LDAPEntry entry) {

  		User user = new User();
  		try{
  			 LDAPAttributeSet attributeSet = entry.getAttributeSet();
  			 
             user.setId(attributeSet.getAttribute(LdapScimAttrMap.id.getValue()).getStringValue());
             user.setUserName(attributeSet.getAttribute(LdapScimAttrMap.userName.getValue()).getStringValue());
                  
             SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");

				if (attributeSet.getAttribute(LdapScimAttrMap.created.getValue()) != null) {
					Date createdDate = formatter.parse(attributeSet.getAttribute(LdapScimAttrMap.created.getValue()).getStringValue());
					user.setCreatedDate(createdDate);
				} 
				else {
					user.setCreatedDate(new Date());
				}
				if (attributeSet.getAttribute(LdapScimAttrMap.lastModified.getValue()) != null) {
					Date lastModified = formatter.parse(attributeSet.getAttribute(LdapScimAttrMap.lastModified.getValue()).getStringValue());
					user.setLastModified(lastModified);
				} else {
					user.setLastModified(new Date());
				}

				if (attributeSet.getAttribute(LdapScimAttrMap.location.getValue()) != null) {
					user.setLocation(attributeSet.getAttribute(LdapScimAttrMap.location.getValue()).getStringValue());
				} else {
					user.setLocation("http://localhost:8080/scim/v2/Users/" + user.getId());
				}
             
  		}
  		catch(Exception e){
  			System.out.println(e);
  		}
  		return user;
	}

	public static LDAPAttributeSet copyUserToLdap(User user) {
		LDAPAttributeSet attributeSet = new LDAPAttributeSet();
		attributeSet.add( new LDAPAttribute("objectclass", "inetOrgPerson")); 
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
							List<Attribute> subAttributeList  =
									((MultiValuedAttribute) (subAttribute)).getAttributeValues();

							for (Attribute subValue : subAttributeList) {

								ComplexAttribute complexSubAttribute = (ComplexAttribute) subValue;
								Map<String, Attribute> subSubAttributes = complexSubAttribute.getSubAttributesList();

								for (Attribute subSubAttribute : subSubAttributes.values()) {
									if (subSubAttribute instanceof SimpleAttribute) {

										attributeSet =  addSimpleAttribute(null, attributeSet,
												(Attribute) ((SimpleAttribute) subSubAttribute));

									} else if (subSubAttribute instanceof MultiValuedAttribute) {
										attributeSet = addMultiValuedPrimitiveAttribute(
												((MultiValuedAttribute) subSubAttribute).getAttributePrimitiveValues(),
												subSubAttribute.getName(),attributeSet);
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
										subSubAttribute.getName(),attributeSet);
							}
						}
					}
				}
			} else if (attribute instanceof MultiValuedAttribute) {
				MultiValuedAttribute multiValuedAttribute = (MultiValuedAttribute) attribute;
				if (multiValuedAttribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
					List<Attribute> subAttributeList  = multiValuedAttribute.getAttributeValues();
					for (Attribute subAttribute : subAttributeList) {
						ComplexAttribute complexSubAttribute = (ComplexAttribute) subAttribute;
						Map<String, Attribute> subSubAttributes = complexSubAttribute.getSubAttributesList();
						//If address, check for home address START-------
						if(subAttribute.getURI().equals(UserSchemaConstants.ADDRESSES_URI)) {
							String value = null;
							boolean isHome = false;
							for (Attribute subSubAttribute : subSubAttributes.values()) {
								SimpleAttribute simpleAttribute = (SimpleAttribute) subSubAttribute;
								if(subSubAttribute.getName().equals("type")){
									//Check if type is "home"
									if(simpleAttribute.getValue().equals(UserSchemaConstants.HOME)) { 
										isHome = true;
									} else {
										if(LdapScimAttrMap.addresses.isSet()) {
											continue;
										}
										break;
									}
								} else if (subSubAttribute.getName().equals("formatted")) {
									value = (String) simpleAttribute.getValue();
								}
							}
							if(isHome) {
								attributeSet.add(new LDAPAttribute(LdapIPersonConstants.homePostalAddress,value));	
							}
							continue;
						}
						//If address END-------
						String parent = getAttributeName(subAttribute);
						for (Attribute subSubAttribute : subSubAttributes.values()) {
							if (subSubAttribute instanceof SimpleAttribute) {
								if(subSubAttribute.getName().equals("value")) {
									attributeSet = addSimpleAttribute(parent, attributeSet,
											(Attribute) ((SimpleAttribute) subSubAttribute));
								} /*if (UserSchemaConstants.ADDRESSES.equals(parent)) {
									parent=parent+"_"+
								}*/

							} else if (subSubAttribute instanceof MultiValuedAttribute) {
								attributeSet = addMultiValuedPrimitiveAttribute(
										((MultiValuedAttribute) subSubAttribute).getAttributePrimitiveValues(),
										subSubAttribute.getName(),attributeSet);
							}
						}
					}
				} else {
					List<Object> primitiveValueList = multiValuedAttribute.getAttributePrimitiveValues();
					attributeSet = addMultiValuedPrimitiveAttribute(primitiveValueList,
							multiValuedAttribute.getName(),attributeSet);
				}

			}
		}
		return attributeSet;
	}

	private static LDAPAttributeSet addSimpleAttribute (String parentName, LDAPAttributeSet attributeSet, Attribute attribute) {
		SimpleAttribute simpleAttribute = (SimpleAttribute) attribute;
		try{
			LdapScimAttrMap name;
			if(parentName!=null) {
				name = LdapScimAttrMap.valueOf(parentName);
			} else {
				name = LdapScimAttrMap.valueOf(simpleAttribute.getName());
			}

			if(name != null && attributeSet != null) {
				attributeSet.add(new LDAPAttribute(name.getValue(),simpleAttribute.getValue().toString()));
			}
		} catch (Exception e){
			System.out.println("Mapping for '"+simpleAttribute.getName()+"' missing!");
		}
		return attributeSet;
	}

	private static LDAPAttributeSet addMultiValuedPrimitiveAttribute(List<Object> attributePrimitiveValues, String attributeName,LDAPAttributeSet attributeSet) {
		try{
			LdapScimAttrMap name = LdapScimAttrMap.valueOf(attributeName);
			if(name != null && attributeSet != null) {
				for (Object item  : attributePrimitiveValues) {
					attributeSet.add(new LDAPAttribute(attributeName,(String) item));
				}
			}
		} catch (Exception e){
			System.out.println("Mapping for '"+attributeName+"' missing!");
		}
		return attributeSet;
	}

	private static String getAttributeName (Attribute subAttribute) {
		String parent = null;
		ComplexAttribute complexSubAttribute = (ComplexAttribute) subAttribute;
		Map<String, Attribute> subSubAttributes = complexSubAttribute.getSubAttributesList();

		switch (subAttribute.getURI()) {
		case UserSchemaConstants.EMAILS_URI:
			parent = UserSchemaConstants.EMAILS;
		case UserSchemaConstants.PHONE_NUMBERS_URI:
			parent = (parent== null)?UserSchemaConstants.PHONE_NUMBERS:parent;
		case UserSchemaConstants.PHOTOS_URI:
			parent = parent== null?UserSchemaConstants.PHOTOS:parent;
			for (Attribute subSubAttribute : subSubAttributes.values()) {
				if(subSubAttribute.getName().equals("type")){
					SimpleAttribute simpleAttribute = (SimpleAttribute) subSubAttribute;
					parent = parent+"_"+simpleAttribute.getValue();
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
			//parent = subAttribute.getName();
			break;
		}
		return parent;
	}
	

	
}