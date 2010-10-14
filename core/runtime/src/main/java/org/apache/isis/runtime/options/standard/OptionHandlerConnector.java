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


package org.apache.isis.runtime.options.standard;

import static org.apache.isis.runtime.runner.Constants.CONNECTOR_LONG_OPT;
import static org.apache.isis.runtime.runner.Constants.CONNECTOR_OPT;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.isis.commons.lang.ListUtils;
import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.runtime.installers.InstallerRepository;
import org.apache.isis.runtime.runner.BootPrinter;
import org.apache.isis.runtime.runner.Constants;
import org.apache.isis.runtime.runner.options.OptionHandlerAbstract;
import org.apache.isis.runtime.system.SystemConstants;
import org.apache.isis.runtime.viewer.IsisViewerInstaller;

public class OptionHandlerConnector extends OptionHandlerAbstract {

	private InstallerRepository installerRepository;
	private List<String> connectorNames;
	
	public OptionHandlerConnector(final InstallerRepository installerRepository) {
		this.installerRepository = installerRepository;
	}

	@SuppressWarnings("static-access")
	public void addOption(Options options) {
        Object[] connectors = installerRepository.getInstallers(IsisViewerInstaller.class);
        Option option = OptionBuilder.withArgName("name|class name").hasArg().withLongOpt(CONNECTOR_LONG_OPT).withDescription(
                "connector to use for client requests, or for server to listen on: " + availableInstallers(connectors)).create(
                CONNECTOR_OPT);
        options.addOption(option);
		
	}

	public boolean handle(CommandLine commandLine, BootPrinter bootPrinter, Options options) {
		connectorNames = getOptionValues(commandLine, Constants.CONNECTOR_OPT);
		return true;
	}
	
	public void primeConfigurationBuilder(
			ConfigurationBuilder configurationBuilder) {
		configurationBuilder.add(SystemConstants.CLIENT_CONNECTION_KEY, ListUtils.listToString(connectorNames));
	}
	
	public List<String> getConnectorNames() {
		return connectorNames;
	}

}