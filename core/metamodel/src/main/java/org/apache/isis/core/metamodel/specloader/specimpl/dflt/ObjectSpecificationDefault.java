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

package org.apache.isis.core.metamodel.specloader.specimpl.dflt;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.profiles.Perspective;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.commons.lang.ListUtils;
import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.ImperativeFacetUtils;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacetInferred;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.layout.MemberLayoutArranger;
import org.apache.isis.core.metamodel.layout.OrderSet;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.SpecificationContext;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.specimpl.CreateObjectContext;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilder;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionContext;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionImpl;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationImpl;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationImpl;

public class ObjectSpecificationDefault extends ObjectSpecificationAbstract implements DebuggableWithTitle, FacetHolder {

    private final static Logger LOG = Logger.getLogger(ObjectSpecificationDefault.class);

    private static String determineShortName(final Class<?> introspectedClass) {
        String name = introspectedClass.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    // //////////////////////////////////////////////////////////////
    // fields
    // //////////////////////////////////////////////////////////////


    private boolean isService;

    
    /**
     * Lazily built by {@link #getMember(Method)}.
     */
    private Map<Method, ObjectMember> membersByMethod = null;

    private final ObjectMemberContext objectMemberContext;
    private final IntrospectionContext introspectionContext;
    private final CreateObjectContext createObjectContext;

    private FacetedMethodsBuilder facetedMethodsBuilder;

    
    // //////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////


    public ObjectSpecificationDefault(
        final Class<?> correspondingClass, 
        final FacetedMethodsBuilderContext facetedMethodsBuilderContext,
        final IntrospectionContext introspectionContext,
        final SpecificationContext specContext, 
        final ObjectMemberContext objectMemberContext,
        final CreateObjectContext createObjectContext 
        ) {
        super(correspondingClass, determineShortName(correspondingClass), specContext);
        
        this.facetedMethodsBuilder = new FacetedMethodsBuilder(this, facetedMethodsBuilderContext);
        
        this.introspectionContext = introspectionContext;
        this.createObjectContext = createObjectContext;
        this.objectMemberContext = objectMemberContext;
    }

    
    @Override
    public void introspectTypeHierarchyAndMembers() {
        if (facetedMethodsBuilder == null) {
            throw new MetaModelException("Introspection already taken place, cannot introspect again");
        }

        // class
        facetedMethodsBuilder.introspectClass();

        // names
        addNamedFacetAndPluralFacetIfRequired();

        // superclass
        final Class<?> superclass = getCorrespondingClass().getSuperclass();
        setSuperclass(superclass);

        // go no further if required
        final boolean skipFurtherIntrospection =
            JavaClassUtils.isJavaClass(getCorrespondingClass()) || isAppLibValue(getCorrespondingClass());
        if (skipFurtherIntrospection) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("skipping introspection of interfaces, properties, actions and interfaces for "
                    + getFullIdentifier() + " (java.xxx or applib value class)");
            }
            return;
        }

        // walk superinterfaces

        // REVIEW: the processing here isn't quite the same as with superclasses,
        // in that with superclasses the superclass adds this type as its subclass,
        // whereas here this type defines itself as the subtype.
        // it'd be nice to push the responsibility for adding subclasses to
        // the interface type... needs some tests around it, though, before
        // making that refactoring.
        final Class<?>[] interfaceTypes = getCorrespondingClass().getInterfaces();
        List<ObjectSpecification> interfaceSpecList = Lists.newArrayList();
        for (Class<?> interfaceType : interfaceTypes) {
            Class<?> substitutedInterfaceType = getClassSubstitutor().getClass(interfaceType);
            if (substitutedInterfaceType != null) {
                ObjectSpecification interfaceSpec =
                    getSpecificationLookup().loadSpecification(substitutedInterfaceType);
                interfaceSpecList.add(interfaceSpec);
            }
        }
        addAsSubclassTo(interfaceSpecList);
        addInterfaces(interfaceSpecList);

        // associations and actions
        final List<FacetedMethod> associationFacetedMethods = facetedMethodsBuilder.getAssociationFacetedMethods();
        // actions
        final List<FacetedMethod> actionFacetedMethods = facetedMethodsBuilder.getActionFacetedMethods();

        // ordering
        final OrderSet associationOrderSet = getMemberLayoutArranger().createAssociationOrderSetFor(this, associationFacetedMethods);
        addAssociations(asAssociations(associationOrderSet));

        OrderSet actionOrderSet = getMemberLayoutArranger().createActionOrderSetFor(this, actionFacetedMethods);
        addObjectActions(asObjectActions(actionOrderSet));

        updateFromFacetValues();
        
        facetedMethodsBuilder = null;
        setIntrospected(true);
    }



    private void addNamedFacetAndPluralFacetIfRequired() {
        NamedFacet namedFacet = getFacet(NamedFacet.class);
        if (namedFacet == null) {
            namedFacet = new NamedFacetInferred(NameUtils.naturalName(getShortIdentifier()), this);
            addFacet(namedFacet);
        }

        PluralFacet pluralFacet = getFacet(PluralFacet.class);
        if (pluralFacet == null) {
            pluralFacet = new PluralFacetInferred(NameUtils.pluralName(namedFacet.value()), this);
            addFacet(pluralFacet);
        }
    }

    /**
     * TODO: review this, should be more general and check for value facet, surely?
     */
    private boolean isAppLibValue(Class<?> type) {
        return type.getName().startsWith("org.apache.isis.applib.value.");
    }

    private List<ObjectAssociation> asAssociations(final OrderSet orderSet) {
        if(orderSet == null) {
            return null;
        }
        final List<ObjectAssociation> associations = Lists.newArrayList();
        for (Object element : orderSet) {
            if (element instanceof FacetedMethod) {
                FacetedMethod facetMethod = (FacetedMethod) element;
                if (facetMethod.getFeatureType().isCollection()) {
                    associations.add(createCollection(facetMethod));
                } else if (facetMethod.getFeatureType().isProperty()) {
                    associations.add(createProperty(facetMethod));
                }
            } else if (element instanceof OrderSet) {
                // Not supported at present
            } else {
                throw new UnknownTypeException(element);
            }
        }

        return associations;
    }

    private List<ObjectAction> asObjectActions(final OrderSet orderSet) {
        if(orderSet == null) {
            return null;
        }
        final List<ObjectAction> actions = Lists.newArrayList();
        for (Object element : orderSet) {
            if (element instanceof FacetedMethod) {
                final FacetedMethod facetedMethod = (FacetedMethod) element;
                if (facetedMethod.getFeatureType().isAction()) {
                    actions.add(createAction(facetedMethod));
                }
            } else if (element instanceof OrderSet) {
                final OrderSet set = ((OrderSet) element);
                actions.add(createObjectActionSet(set));
            } else {
                throw new UnknownTypeException(element);
            }
        }
        return actions;
    }

    private OneToOneAssociationImpl createProperty(final FacetedMethod facetedMethod) {
        return new OneToOneAssociationImpl(facetedMethod, objectMemberContext);
    }

    private OneToManyAssociationImpl createCollection(final FacetedMethod facetedMethod) {
        return new OneToManyAssociationImpl(facetedMethod, objectMemberContext);
    }

    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        return new ObjectActionImpl(facetedMethod, objectMemberContext, getServicesProvider());
    }

    private ObjectActionSet createObjectActionSet(final OrderSet set) {
        return new ObjectActionSet("", set.getGroupFullName(), asObjectActions(set));
    }


    /**
     * Added to try to track down a race condition.
     */
    @Override
    public boolean isIntrospected() {
        return facetedMethodsBuilder == null;
    }

    // //////////////////////////////////////////////////////////////////////
    // Whether a service or not
    // //////////////////////////////////////////////////////////////////////

    @Override
    public boolean isService() {
        return isService;
    }

    /**
     * TODO: should ensure that service has at least one user action; fix when specification knows of its hidden
     * methods.
     * 
     * <pre>
     * if (objectActions != null &amp;&amp; objectActions.length == 0) {
     *     throw new ObjectSpecificationException(&quot;Service object &quot; + getFullName() + &quot; should have at least one user action&quot;);
     * }
     * </pre>
     */
    @Override
    public void markAsService() {
        ensureServiceHasNoAssociations();
        isService = true;
    }

    private void ensureServiceHasNoAssociations() {
        final List<ObjectAssociation> associations = getAssociations();
        StringBuilder buf = new StringBuilder();
        for (ObjectAssociation association : associations) {
            final String name = association.getId();
            // services are allowed to have one association, called 'id'
            if (!isValidAssociationForService(name)) {
                appendAssociationName(buf, name);
            }
        }
        if (buf.length() > 0) {
            throw new ObjectSpecificationException("Service object " + getFullIdentifier()
                + " should have no fields, but has: " + buf);
        }
    }

    /**
     * Services are allowed to have one association, called 'id'.
     * 
     * <p>
     * This is used for {@link Perspective}s (user profiles).
     */
    private boolean isValidAssociationForService(final String associationId) {
        return "id".indexOf(associationId) != -1;
    }

    private void appendAssociationName(StringBuilder fieldNames, final String name) {
        fieldNames.append(fieldNames.length() > 0 ? ", " : "");
        fieldNames.append(name);
    }

    // //////////////////////////////////////////////////////////////////////
    // Actions
    // //////////////////////////////////////////////////////////////////////

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id,
        final List<ObjectSpecification> parameters) {
        final List<ObjectAction> availableActions =
            ListUtils.combine(getObjectActionsAll(), getContributedActions(type));
        return getAction(availableActions, type, id, parameters);
    }

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String nameParmsIdentityString) {
        final List<ObjectAction> availableActions =
            ListUtils.combine(getObjectActionsAll(), getContributedActions(type));
        return getAction(availableActions, type, nameParmsIdentityString);
    }

    private ObjectAction getAction(final List<ObjectAction> availableActions, final ActionType type,
        final String actionName, final List<ObjectSpecification> parameters) {
        outer: for (int i = 0; i < availableActions.size(); i++) {
            final ObjectAction action = availableActions.get(i);
            if (action.getActions().size() > 0) {
                // deal with action set
                final ObjectAction a = getAction(action.getActions(), type, actionName, parameters);
                if (a != null) {
                    return a;
                }
            } else {
                // regular action
                if (!action.getType().equals(type)) {
                    continue outer;
                }
                if (actionName != null && !actionName.equals(action.getId())) {
                    continue outer;
                }
                if (action.getParameters().size() != parameters.size()) {
                    continue outer;
                }
                for (int j = 0; j < parameters.size(); j++) {
                    if (!parameters.get(j).isOfType(action.getParameters().get(j).getSpecification())) {
                        continue outer;
                    }
                }
                return action;
            }
        }
        return null;
    }

    private ObjectAction getAction(final List<ObjectAction> availableActions, final ActionType type,
        final String nameParmsIdentityString) {
        if (nameParmsIdentityString == null) {
            return null;
        }
        outer: for (int i = 0; i < availableActions.size(); i++) {
            final ObjectAction action = availableActions.get(i);
            if (action.getActions().size() > 0) {
                // deal with action set
                final ObjectAction a = getAction(action.getActions(), type, nameParmsIdentityString);
                if (a != null) {
                    return a;
                }
            } else {
                // regular action
                if (!sameActionTypeOrNotSpecified(type, action)) {
                    continue outer;
                }
                if (!nameParmsIdentityString.equals(action.getIdentifier().toNameParmsIdentityString())) {
                    continue outer;
                }
                return action;
            }
        }
        return null;
    }

    @Override
    public boolean isCollectionOrIsAggregated() {
        return isCollection() || isValueOrIsAggregated();
    }

    // //////////////////////////////////////////////////////////////////////
    // createObject
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Object createObject(CreationMode creationMode) {
        if (getCorrespondingClass().isArray()) {
            return Array.newInstance(getCorrespondingClass().getComponentType(), 0);
        }

        try {
            Object object = getObjectInstantiator().instantiate(getCorrespondingClass());

            if (creationMode == CreationMode.INITIALIZE) {
                final ObjectAdapter adapter = getAdapterMap().adapterFor(object);

                // initialize new object
                final List<ObjectAssociation> fields = adapter.getSpecification().getAssociations();
                for (int i = 0; i < fields.size(); i++) {
                    fields.get(i).toDefault(adapter);
                }
                getDependencyInjector().injectDependenciesInto(object);

                CallbackUtils.callCallback(adapter, CreatedCallbackFacet.class);
            }
            return object;
        } catch (final ObjectInstantiationException e) {
            throw new IsisException("Failed to create instance of type " + getFullIdentifier(), e);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // getMember, catalog... (not API)
    // //////////////////////////////////////////////////////////////////////

    public ObjectMember getMember(final Method method) {
        if (membersByMethod == null) {
            final HashMap<Method, ObjectMember> membersByMethod = Maps.newHashMap();
            cataloguePropertiesAndCollections(membersByMethod);
            catalogueActions(membersByMethod);
            this.membersByMethod = membersByMethod;
        }
        return membersByMethod.get(method);
    }

    private void cataloguePropertiesAndCollections(final Map<Method, ObjectMember> membersByMethod) {
        Filter<ObjectAssociation> noop = Filters.anyOfType(ObjectAssociation.class);
        final List<ObjectAssociation> fields = getAssociations(noop);
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final Facet[] facets = field.getFacets(ImperativeFacet.FILTER);
            for (int j = 0; j < facets.length; j++) {
                final ImperativeFacet facet = ImperativeFacetUtils.getImperativeFacet(facets[j]);
                for (Method imperativeFacetMethod : facet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, field);
                }
            }
        }
    }

    private void catalogueActions(final Map<Method, ObjectMember> membersByMethod) {
        final List<ObjectAction> userActions = getObjectActions(ActionType.USER);
        for (int i = 0; i < userActions.size(); i++) {
            final ObjectAction userAction = userActions.get(i);
            final Facet[] facets = userAction.getFacets(ImperativeFacet.FILTER);
            for (int j = 0; j < facets.length; j++) {
                final ImperativeFacet facet = ImperativeFacetUtils.getImperativeFacet(facets[j]);
                for (Method imperativeFacetMethod : facet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, userAction);
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Debug, toString
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugString debug) {
        debug.blankLine();
        debug.appendln("Title", getFacet(TitleFacet.class));
        IconFacet iconFacet = getFacet(IconFacet.class);
        if (iconFacet != null) {
            debug.appendln("Icon", iconFacet);
        }
        debug.unindent();
    }

    @Override
    public String debugTitle() {
        return "NO Member Specification";
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullIdentifier());
        str.append("type", (isCollection() ? "Collection" : "Object"));
        str.append("persistable", persistability());
        str.append("superclass", superclass() == null ? "Object" : superclass().getFullIdentifier());
        return str.toString();
    }

    // //////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // //////////////////////////////////////////////////////////////////

    protected AdapterMap getAdapterMap() {
        return createObjectContext.getAdapterMap();
    }

    protected DependencyInjector getDependencyInjector() {
        return createObjectContext.getDependencyInjector();
    }

    private ClassSubstitutor getClassSubstitutor() {
        return introspectionContext.getClassSubstitutor();
    }

    private MemberLayoutArranger getMemberLayoutArranger() {
        return introspectionContext.getMemberLayoutArranger();
    }

}