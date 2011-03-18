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


package org.apache.isis.runtimes.dflt.runtime.fixturesinstaller;

import java.util.List;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserService;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserServiceAware;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.runtimes.dflt.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtimes.dflt.runtime.authentication.standard.fixture.AuthenticationRequestLogonFixture;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionManager;


public class SwitchUserServiceImpl implements SwitchUserService {

    public SwitchUserServiceImpl() {
    }

    @Override
    public void switchUser(final String username, final List<String> roles) {
        switchUser(new LogonFixture(username, roles));
    }
    
    @Override
    public void switchUser(final String username, final String... roles) {
        switchUser(new LogonFixture(username, roles));
    }

    private void switchUser(LogonFixture logonFixture) {
        getTransactionManager().endTransaction();
    	IsisContext.closeSession();
    	AuthenticationRequestLogonFixture authRequest = new AuthenticationRequestLogonFixture(logonFixture);
    	AuthenticationSession session = getAuthenticationManager().authenticate(authRequest);
        IsisContext.openSession(session);
        getTransactionManager().startTransaction();
    }

	public void injectInto(Object fixture) {
    	if (fixture instanceof SwitchUserServiceAware) {
    		SwitchUserServiceAware serviceAware = (SwitchUserServiceAware) fixture;
    		serviceAware.setService(this);
    	}
	}

	
	protected AuthenticationManager getAuthenticationManager() {
		return IsisContext.getAuthenticationManager();
	}
	
	protected IsisTransactionManager getTransactionManager() {
		return IsisContext.getTransactionManager();
	}
	
}
