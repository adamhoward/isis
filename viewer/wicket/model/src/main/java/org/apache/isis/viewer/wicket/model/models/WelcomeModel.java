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


package org.apache.isis.viewer.wicket.model.models;


/**
 * Model providing welcome text.
 */
public class WelcomeModel extends ModelAbstract<String> {
	
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MESSAGE = 
		"Apache Isis' Wicket Viewer combines the power of Apache Wicket " +
		"for web UIs with Apache Isis for domain modelling.  " +
		"Out-of-the box you get a fully-functional webapp just from " +
		"your domain objects; you can then customize the UI by " +
		"writing custom Wicket components, replacing the page layouts or " +
		"simply by altering the CSS";
	
	private String welcomeMessage;

	public WelcomeModel(final String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	public WelcomeModel() {
		this(DEFAULT_MESSAGE);
	}

	@Override
	protected String load() {
		return welcomeMessage;
	}

	@Override
	public void setObject(String welcomeMessage) {
		super.setObject(welcomeMessage);
		this.welcomeMessage = welcomeMessage;
	}
	
}