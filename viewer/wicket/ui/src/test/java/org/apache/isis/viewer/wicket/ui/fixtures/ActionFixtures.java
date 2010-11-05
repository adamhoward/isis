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


package org.apache.isis.viewer.wicket.ui.fixtures;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.ConsentAbstract;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.jmock.Expectations;
import org.jmock.Mockery;

public final class ActionFixtures {

	private Mockery context;

	public ActionFixtures(Mockery context) {
		this.context = context;
	}

	public <T extends Facet> void getFacet(final ObjectAction mockAction, final Class<T> facetClass, final T returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).getFacet(with(facetClass));
				will(returnValue(returns));
			}
		});
	}

	public void getName(final ObjectAction mockAction, final String returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).getName();
				will(returnValue(returns));
			}
		});
	}

	public <T extends Facet> void getParameterCount(final ObjectAction mockAction, final int returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).getParameterCount();
				will(returnValue(returns));
			}
		});
	}

	public void getType(final ObjectAction mockAction, final ObjectActionType returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).getType();
				will(returnValue(returns));
			}
		});
	}

	public void getIdentifier(Mockery context,
			final ObjectAction mockAction, final Identifier returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).getIdentifier();
				will(returnValue(returns));
			}
		});
	}

	public void getOnType(final ObjectAction mockAction, final ObjectSpecification returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).getOnType();
				will(returnValue(returns));
			}
		});
	}

	public void isVisible(final ObjectAction mockAction, final boolean returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).isVisible(with(any(AuthenticationSession.class)), with(any(ObjectAdapter.class)));
				will(returnValue(ConsentAbstract.allowIf(returns)));
			}
		});
	}

	public void isUsable(final ObjectAction mockAction, final boolean returns) {
		context.checking(new Expectations() {
			{
				allowing(mockAction).isUsable(with(any(AuthenticationSession.class)), with(any(ObjectAdapter.class)));
				will(returnValue(ConsentAbstract.allowIf(returns)));
			}
		});
	}


}