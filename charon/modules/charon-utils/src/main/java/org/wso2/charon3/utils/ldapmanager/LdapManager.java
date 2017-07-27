/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.charon3.utils.ldapmanager;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.ConflictException;
import org.wso2.charon3.core.exceptions.NotFoundException;
import org.wso2.charon3.core.exceptions.NotImplementedException;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.utils.CopyUtil;
import org.wso2.charon3.core.utils.codeutils.Node;
import org.wso2.charon3.core.utils.codeutils.SearchRequest;


import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchResults;

/**
 * This is a ldap user store.
 */
public class LdapManager implements UserManager {
	private static final Logger logger = LoggerFactory.getLogger(LdapManager.class);
	//in memory user manager stores users
	//ConcurrentHashMap<String, User> inMemoryUserList = new ConcurrentHashMap<String, User>();
	ConcurrentHashMap<String, Group> inMemoryGroupList = new ConcurrentHashMap<String, Group>();

	@Override
	public User createUser(User user, Map<String, Boolean> map)
			throws CharonException, ConflictException, BadRequestException {
		User user1 = null;
		LDAPConnection lc = LdapConnectUtil.getConnection(false);
		LDAPAttributeSet attributeSet = LdapUtil.copyUserToLdap(user);

		String id = attributeSet.getAttribute(LdapScimAttrMap.id.getValue()).getStringValue();
		String dn = "uid="+id+",ou=users,o=people";
		LDAPEntry entry = new LDAPEntry(dn, attributeSet);

		try {
			user1 = (User) CopyUtil.deepCopy(user); 
			lc.add(entry);
			lc.disconnect();
		} catch (LDAPException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ConflictException("User with the id : " + user.getId() + "already exists");
		}
		return user1;
	}

	@Override
	public User getUser(String id, Map<String, Boolean> map)
			throws CharonException, BadRequestException, NotFoundException {

		//Added by Reena Hegde

		LDAPConnection lc = LdapConnectUtil.getConnection(false);
		List<LDAPEntry> userList = new ArrayList<>();
		User user = new User();

		try {
			LDAPEntry searchResult = lc.read("uid="+id+",ou=users,o=people");
			user = LdapUtil.convertLdapToUser(searchResult);
			user.setSchemas();
			lc.disconnect();
		} catch (LDAPException e) {
			throw new NotFoundException("No user with the id : " + id);
		}

		return user;
	}
	/*LDAPConnection lc = LdapConnectUtil.getConnection(false);
		User user = null;
		try {
			LDAPSearchResults searchResults = lc.search("ou=users,o=people", 
					LDAPConnection.SCOPE_SUB, "(uid="+id+")", null, false);

			if (searchResults.hasMore()) {
				LDAPEntry nextEntry = searchResults.next();
				user = (User) CopyUtil.deepCopy(nextEntry);   
			}
			// disconnect with the server
			lc.disconnect();
		} catch (LDAPException e) {
			throw new NotFoundException("No user with the id : " + id);
		}
		if(user == null){
			throw new NotFoundException("No user with the id : " + id);
		}else {
			return user;
		}*/

	@Override
	public void deleteUser(String id)
			throws NotFoundException, CharonException, NotImplementedException, BadRequestException {
		LDAPConnection lc = LdapConnectUtil.getConnection(false);
		try {
			System.out.println("Ldap connection successful, ID: "+id);
			String dn = "uid="+id+",ou=users,o=people";
			lc.delete(dn);
			System.out.print(dn+" Deleted");
			// disconnect with the server
			lc.disconnect();
			return;
		}
		catch (LDAPException e) {
			System.out.println("Error:  " + e.toString());
			throw new NotFoundException("No user with the id : " + id);
		} 
	}

	@Override
	public List<Object> listUsersWithGET(Node rootNode, int startIndex, int count, String sortBy,
			String sortOrder, Map<String, Boolean> requiredAttributes)
					throws CharonException, NotImplementedException, BadRequestException {
		if (sortBy != null || sortOrder != null) {
			throw new NotImplementedException("Sorting is not supported");
		}  else if (startIndex != 1) {
			throw new NotImplementedException("Pagination is not supported");
		} else if (rootNode != null) {
			throw new NotImplementedException("Filtering is not supported");
		} else {
			return listUsers(requiredAttributes);
		}
	}

	private List<Object> listUsers(Map<String, Boolean> requiredAttributes) {
		/*List<Object> userList = new ArrayList<>();
		userList.add(0);
		//first item should contain the number of total results
		for (Map.Entry<String, User> entry : inMemoryUserList.entrySet()) {
			userList.add(entry.getValue());
		}
		userList.set(0, userList.size() - 1);
		try {
			return (List<Object>) CopyUtil.deepCopy(userList);
		} catch (CharonException e) {
			logger.error("Error in listing users");
			return  null;
		}*/

		//Added by Reena Hegde

		List<Object> userList = new ArrayList<>();
		User u = new User();
		try {

			LDAPConnection lc = LdapConnectUtil.getConnection(false);

			LDAPSearchResults searchResults =

					lc.search("ou=users,o=people", // container to search

							LDAPConnection.SCOPE_ONE, // search scope

							"cn=*", // search filter

							null, // "1.1" returns entry name only

							false); // no attributes are returned
			userList.add(0);
			while (searchResults.hasMore()) {

				LDAPEntry nextEntry = null;

				try {

					nextEntry = searchResults.next();
					u = LdapUtil.convertLdapToUser(nextEntry);
					userList.add(u);

				}

				catch (LDAPException e) {

					System.out.println("Error: " + e.toString());

					// Exception is thrown, go for next entry

					continue;

				}
				userList.set(0, userList.size() - 1);
				System.out.println("\n" + nextEntry.getDN());

			}

			lc.disconnect();

			return (List<Object>) CopyUtil.deepCopy(userList);
		} catch (CharonException e) {

			return null;
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public List<Object> listUsersWithPost(SearchRequest searchRequest, Map<String, Boolean> requiredAttributes)
			throws CharonException, NotImplementedException, BadRequestException {
		return listUsersWithGET(searchRequest.getFilter(), searchRequest.getStartIndex(), searchRequest.getCount(),
				searchRequest.getSortBy(), searchRequest.getSortOder(), requiredAttributes);
	}

	@Override
	public User updateUser(User user, Map<String, Boolean> map)
			throws NotImplementedException, CharonException, BadRequestException, NotFoundException {
		String id = user.getId();
		if (id == null) {
			id = "16600144-8cd6-49fb-9879-bafb78b5d3c9";
			//TODO: Enable exception
			//throw new NotFoundException("No user with the id : " + user.getId());
		}
		LDAPConnection lc = LdapConnectUtil.getConnection(false);
		String dn = "uid="+id+",ou=users,o=people";
		try {
			/*
				LDAPEntry searchResult = lc.read(dn);		
				User dbUser = new User(); //(User)searchResult;

				List<LDAPModification> modList = LdapUpdateHelper.getModifications(dbUser, LDAPModification.DELETE);
				modList.addAll(LdapUpdateHelper.getModifications(user, LDAPModification.ADD));
				modList.add(new LDAPModification(0, new LDAPAttribute(LdapIPersonConstants.uid,user.getId())));
			 */
			List<LDAPModification> modList = LdapUpdateHelper.getModifications(user, LDAPModification.REPLACE);
			LDAPModification[] mods = new LDAPModification[modList.size()];
			mods = (LDAPModification[]) modList.toArray(mods);

			lc.modify(dn, mods);
			lc.disconnect();
		} catch (LDAPException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return (User) CopyUtil.deepCopy(user);
	}

	@Override
	public User getMe(String s, Map<String, Boolean> map)
			throws CharonException, BadRequestException, NotFoundException {
		return null;
	}

	@Override
	public User createMe(User user, Map<String, Boolean> map)
			throws CharonException, ConflictException, BadRequestException {
		return null;
	}

	@Override
	public void deleteMe(String s)
			throws NotFoundException, CharonException, NotImplementedException, BadRequestException {

	}

	@Override
	public User updateMe(User user, Map<String, Boolean> map)
			throws NotImplementedException, CharonException, BadRequestException, NotFoundException {
		return null;
	}

	@Override
	public Group createGroup(Group group, Map<String, Boolean> map)
			throws CharonException, ConflictException, NotImplementedException, BadRequestException {
		inMemoryGroupList.put(group.getId(), group);
		return (Group) CopyUtil.deepCopy(group);
	}

	@Override
	public Group getGroup(String id, Map<String, Boolean> map)
			throws NotImplementedException, BadRequestException, CharonException, NotFoundException {
		if (inMemoryGroupList.get(id) != null) {
			return (Group) CopyUtil.deepCopy(inMemoryGroupList.get(id));
		} else {
			throw new NotFoundException("No user with the id : " + id);
		}
	}

	@Override
	public void deleteGroup(String id)
			throws NotFoundException, CharonException, NotImplementedException, BadRequestException {
		if (inMemoryGroupList.get(id) == null) {
			throw new NotFoundException("No user with the id : " + id);
		} else {
			inMemoryGroupList.remove(id);
		}
	}

	@Override
	public List<Object> listGroupsWithGET(Node rootNode, int startIndex, int count, String sortBy,
			String sortOrder, Map<String, Boolean> requiredAttributes)
					throws CharonException, NotImplementedException, BadRequestException {
		if (sortBy != null || sortOrder != null) {
			throw new NotImplementedException("Sorting is not supported");
		}  else if (startIndex != 1) {
			throw new NotImplementedException("Pagination is not supported");
		} else if (rootNode != null) {
			throw new NotImplementedException("Filtering is not supported");
		} else {
			return listGroups(requiredAttributes);
		}
	}

	private List<Object> listGroups(Map<String, Boolean> requiredAttributes) {
		List<Object> groupList = new ArrayList<>();
		groupList.add(0, 0);
		for (Group group : inMemoryGroupList.values()) {
			groupList.add(group);
		}
		groupList.set(0, groupList.size() - 1);
		try {
			return (List<Object>) CopyUtil.deepCopy(groupList);
		} catch (CharonException e) {
			logger.error("Error in listing groups");
			return  null;
		}

	}

	@Override
	public Group updateGroup(Group group, Group group1, Map<String, Boolean> map)
			throws NotImplementedException, BadRequestException, CharonException, NotFoundException {
		if (group.getId() != null) {
			inMemoryGroupList.replace(group.getId(), group);
			return (Group) CopyUtil.deepCopy(group);
		} else {
			throw new NotFoundException("No user with the id : " + group.getId());
		}
	}

	@Override
	public List<Object> listGroupsWithPost(SearchRequest searchRequest, Map<String, Boolean> requiredAttributes)
			throws NotImplementedException, BadRequestException, CharonException {
		return listGroupsWithGET(searchRequest.getFilter(), searchRequest.getStartIndex(), searchRequest.getCount(),
				searchRequest.getSortBy(), searchRequest.getSortOder(), requiredAttributes);
	}
}
