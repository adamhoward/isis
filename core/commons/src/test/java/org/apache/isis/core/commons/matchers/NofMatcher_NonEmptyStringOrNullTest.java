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


package org.apache.isis.core.commons.matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.core.commons.matchers.NofMatchers;


public class NofMatcher_NonEmptyStringOrNullTest {

    private Matcher<String> fooMatcher;

    @Before
    public void setUp() {
        fooMatcher = NofMatchers.nonEmptyStringOrNull();
    }

    @Test
    public void shouldMatchNonEmptyString() {
        assertThat(fooMatcher.matches("foo"), is(true));
    }

    @Test
    public void shouldNotMatchEmptyString() {
        assertThat(fooMatcher.matches(""), is(false));
    }

    @Test
    public void shouldMatchNullString() {
        assertThat(fooMatcher.matches(null), is(true));
    }

}