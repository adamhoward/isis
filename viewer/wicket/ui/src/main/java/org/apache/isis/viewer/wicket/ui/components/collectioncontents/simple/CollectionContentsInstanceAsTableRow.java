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


package org.apache.isis.viewer.wicket.ui.components.collectioncontents.simple;

import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * Renders a single entity instance within the HTML table.
 * 
 * <p>
 * TODO: it ought to be possible to in-line this into {@link CollectionContentsAsSimpleTable}.
 */
class CollectionContentsInstanceAsTableRow extends PanelAbstract<EntityModel> {

	private static final long serialVersionUID = 1L;

	public CollectionContentsInstanceAsTableRow(String id, EntityModel model) {
		super(id, model);

		addTableRow(model);
	}

	private void addTableRow(EntityModel model) {
		
		ObjectAdapter adapter = model.getObject();
		ObjectSpecification typeOfSpec = model.getTypeOfSpecification();
		List<? extends ObjectAssociation> propertyList = typeOfSpec
				.getAssociationList(ObjectAssociationFilters.PROPERTIES);

		add(new Label("title", adapter.titleString()));

		RepeatingView propertyValues = new RepeatingView("propertyValue");
		add(propertyValues);

		for (ObjectAssociation property : propertyList) {
			ObjectAdapter propertyValueAdapter = property.get(adapter);
			Component component;
			if (propertyValueAdapter == null) {
				component = new Label(property.getId(), "(null)");
			} else {
				if (propertyValueAdapter.getSpecification().getFacet(
						ValueFacet.class) == null) {
					// TODO: make more sophisticated, eg with Links if an object
					component = new Label(property.getId(),
							propertyValueAdapter.titleString());
				} else {
					component = new Label(property.getId(),
							propertyValueAdapter.titleString());
				}
			}
			propertyValues.add(component);
		}
	}

}