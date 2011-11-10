/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.Rel;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.codehaus.jackson.node.NullNode;

public class TypeActionIsSubtypeOfReprRenderer extends ReprRendererAbstract<TypeActionIsSubtypeOfReprRenderer, ObjectSpecAndSuperSpec> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.TYPE_ACTION_RESULT);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new TypeActionIsSubtypeOfReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, Rel rel, ObjectSpecAndSuperSpec objectSpecAndSuperSpec) {
        String typeFullName = objectSpecAndSuperSpec.getObjectSpecification().getFullIdentifier();
        String url = "domainTypes/" + typeFullName + "/typeactions/isSubtypeOf/invoke";
        
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.TYPE_ACTION_RESULT, url);
        
        final JsonRepresentation arguments = argumentsTo(resourceContext, objectSpecAndSuperSpec.getSuperSpecification());
        
        return linkBuilder.withArguments(arguments);
    }

    private static JsonRepresentation argumentsTo(ResourceContext resourceContext, final ObjectSpecification superSpec) {
        final JsonRepresentation arguments = JsonRepresentation.newMap();
        final JsonRepresentation supertypeLink = JsonRepresentation.newMap();
        arguments.mapPut("supertype", supertypeLink);
        if(superSpec != null) {
            supertypeLink.mapPut("href", resourceContext.urlFor("domainTypes/" + superSpec.getFullIdentifier()));
        } else {
            supertypeLink.mapPut("href", NullNode.instance);
        }
        return arguments;
    }

    private ObjectSpecification objectSpecification;
    private ObjectSpecification superSpecification;

    public TypeActionIsSubtypeOfReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public TypeActionIsSubtypeOfReprRenderer with(ObjectSpecAndSuperSpec objectSpecAndSuperSpec) {
        this.objectSpecification = objectSpecAndSuperSpec.getObjectSpecification();
        this.superSpecification = objectSpecAndSuperSpec.getSuperSpecification();
        return cast(this);
    }

    public JsonRepresentation render() {

        // self
        if(includesSelf) {
            final JsonRepresentation selfLink = newLinkToBuilder(getResourceContext(), Rel.SELF, new ObjectSpecAndSuperSpec(objectSpecification, superSpecification)).build();
            getLinks().arrayAdd(selfLink);
        }

        representation.mapPut("value", isSubtypeOf());
        getExtensions();
        
        return representation;
    }
    
    private boolean isSubtypeOf() {
        return objectSpecification.isOfType(superSpecification);
    }

    protected void putExtensionsIfService() {
        getExtensions().mapPut("isService", objectSpecification.isService());
    }

}