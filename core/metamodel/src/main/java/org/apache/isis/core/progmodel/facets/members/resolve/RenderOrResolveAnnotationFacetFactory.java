/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.progmodel.facets.members.resolve;

import org.apache.isis.applib.annotation.Render;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.resolve.RenderFacet;

public class RenderOrResolveAnnotationFacetFactory extends FacetFactoryAbstract {

    public RenderOrResolveAnnotationFacetFactory() {
        super(FeatureType.MEMBERS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Render renderAnnotation = Annotations.getAnnotation(processMethodContext.getMethod(), Render.class);
        FacetUtil.addFacet(create(renderAnnotation, processMethodContext.getFacetHolder()));

        @SuppressWarnings("deprecation")
        final org.apache.isis.applib.annotation.Resolve resolveAnnotation = Annotations.getAnnotation(processMethodContext.getMethod(), org.apache.isis.applib.annotation.Resolve.class);
        FacetUtil.addFacet(create(resolveAnnotation, processMethodContext.getFacetHolder()));
    }

    private RenderFacet create(final Render annotation, final FacetHolder holder) {
        return annotation == null ? null : new RenderFacetAnnotation(holder, annotation.value());
    }

    @SuppressWarnings("deprecation")
    private RenderFacet create(final org.apache.isis.applib.annotation.Resolve annotation, final FacetHolder holder) {
        return annotation == null ? null : new RenderFacetViaResolveAnnotation(holder, annotation.value());
    }

}
