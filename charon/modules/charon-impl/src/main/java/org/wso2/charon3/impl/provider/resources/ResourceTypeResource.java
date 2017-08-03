/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.charon3.impl.provider.resources;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.protocol.SCIMResponse;
import org.wso2.charon3.core.protocol.endpoints.ResourceTypeResourceManager;
import org.wso2.charon3.utils.DefaultCharonManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * Endpoints of the ResourceType in micro service. This will basically list 
 * all resourceTypes supported
 */

@Api(value = "/scim/v2/ResourceType")
@SwaggerDefinition(
        info = @Info(
                title = "/ResourceType Endpoint Swagger Definition", version = "1.0",
                description = "SCIM 2.0 /ResourceType endpoint")
)
@Path("/scim/v2/ResourceType")
public class ResourceTypeResource extends AbstractResource {

    @GET
    @Produces({"application/json", "application/scim+json"})
    @ApiOperation(value = "Return ResourceType ")

    @ApiResponses(value = {@ApiResponse(code = 200, message = "ResourceTypes")})

    public Response getResourceType() throws CharonException {
    	UserManager userManager = DefaultCharonManager.getInstance().getUserManager();
    	
        // create charon-SCIM user resource manager and hand-over the request.
    	ResourceTypeResourceManager resourceManager = new ResourceTypeResourceManager();

		SCIMResponse scimResponse = resourceManager.get(null,userManager,null,null);

		return buildResponse(scimResponse);
    }

}
