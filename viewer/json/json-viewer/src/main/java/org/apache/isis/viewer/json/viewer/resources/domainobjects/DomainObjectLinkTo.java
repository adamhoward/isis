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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.links.Rel;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;

public class DomainObjectLinkTo implements ObjectAdapterLinkTo {

    protected ResourceContext resourceContext;
    protected ObjectAdapter objectAdapter;

    @Override
    public final DomainObjectLinkTo usingResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
        return this;
    }

    @Override
    public ObjectAdapterLinkTo with(ObjectAdapter objectAdapter) {
        this.objectAdapter = objectAdapter;
        return this;
    }

    
    @Override
    public LinkBuilder builder() {
        return builder(null);
    }

    @Override
    public LinkBuilder builder(Rel rel) {
        return LinkBuilder.newBuilder(resourceContext, relElseDefault(rel), RepresentationType.DOMAIN_OBJECT, linkRef());
    }

    /**
     * hook method
     */
    protected String linkRef() {
        if(resourceContext == null) {
            throw new IllegalStateException("resourceContext not provided");
        }
        if(objectAdapter == null) {
            throw new IllegalStateException("objectAdapter not provided");
        }
        StringBuilder buf = new StringBuilder("objects/");
        buf.append(resourceContext.getOidStringifier().enString(objectAdapter.getOid()));
        return buf.toString();
    }

    private Rel relElseDefault(Rel rel) {
        return rel != null ? rel : defaultRel();
    }

    /**
     * hook method; used by {@link #builder(Rel)}.
     */
    protected Rel defaultRel() {
        return Rel.OBJECT;
    }

    @Override
    public final LinkBuilder memberBuilder(Rel rel, MemberType memberType, ObjectMember objectMember, String... parts) {
        return memberBuilder(rel, memberType, objectMember, memberType.getRepresentationType(), parts);
    }

    @Override
    public final LinkBuilder memberBuilder(Rel rel, MemberType memberType, ObjectMember objectMember, RepresentationType representationType, String... parts) {
        StringBuilder buf = new StringBuilder(linkRef());
        buf.append("/").append(memberType.getUrlPart()).append(objectMember.getId());
        for(String part: parts) {
            if(part == null) {
                continue;
            }
            buf.append("/").append(part);
        }
        String url = buf.toString();
        return LinkBuilder.newBuilder(resourceContext, rel, representationType, url);
    }
    

}