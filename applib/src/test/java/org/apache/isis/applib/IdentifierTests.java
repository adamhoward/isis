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


package org.apache.isis.applib;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.applib.Identifier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


@RunWith(JMock.class)
public class IdentifierTests {

    @SuppressWarnings("unused")
	private final Mockery mockery = new JUnit4Mockery();

    private Identifier identifier;

    @Before
    public void setUp() {
    }

    @Test
    public void canInstantiateClassIdentifier() {
        identifier = Identifier.classIdentifier(SomeDomainClass.class);
        assertThat(identifier, is(not(nullValue())));
    }

    @Test
    public void classIdentifierClassNameIsSet() {
        Class<?> domainClass = SomeDomainClass.class;
        String domainClassFullyQualifiedName = domainClass.getCanonicalName();
        identifier = Identifier.classIdentifier(domainClass);
        assertThat(identifier.getClassName(), is(domainClassFullyQualifiedName));
    }

}