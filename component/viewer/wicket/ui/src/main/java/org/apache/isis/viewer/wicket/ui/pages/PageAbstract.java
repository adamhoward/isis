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

package org.apache.isis.viewer.wicket.ui.pages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.ApplicationActionsModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.about.AboutPage;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;

import org.apache.log4j.Logger;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Convenience adapter for {@link WebPage}s built up using {@link ComponentType}
 * s.
 */
public abstract class PageAbstract extends WebPage {

    private static Logger LOG = Logger.getLogger(PageAbstract.class);

    private static final long serialVersionUID = 1L;
    
    private static final String ID_BOOKMARKED_PAGES = "breadcrumbs";
    private static final String ID_HOME_PAGE_LINK = "homePageLink";
    private static final String ID_APPLICATION_NAME = "applicationName";
    private static final String ID_USER_NAME = "userName";
    
    private static final String ID_PAGE_TITLE = "pageTitle";
    
    public static final String ID_MENU_LINK = "menuLink";
    public static final String ID_LOGOUT_LINK = "logoutLink";
    public static final String ID_ABOUT_LINK = "aboutLink";

    private final List<ComponentType> childComponentIds;
    private final PageParameters pageParameters;
    
    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationName")
    private String applicationName;
    
    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationCss")
    private String applicationCss;
    
    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationJs")
    private String applicationJs;

    public PageAbstract(final PageParameters pageParameters, final ComponentType... childComponentIds) {
        try {
            addApplicationActionsComponent();
            this.childComponentIds = Collections.unmodifiableList(Arrays.asList(childComponentIds));
            this.pageParameters = pageParameters;
            addHomePageLinkAndApplicationName();
            addUserName();
            addLogoutLink();
            addAboutLink();
            add(new Label(ID_PAGE_TITLE, PageParameterNames.PAGE_TITLE.getStringFrom(pageParameters, applicationName)));
        } catch(RuntimeException ex) {
            
            LOG.error("Failed to construct page, going back to sign in page", ex);
            // hack for IE
            getSession().invalidate();
            throw new RestartResponseAtInterceptPageException(WicketSignInPage.class);
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if(applicationCss != null) {
            response.render(CssReferenceHeaderItem.forUrl(applicationCss));
        }
        if(applicationJs != null) {
            response.render(JavaScriptReferenceHeaderItem.forUrl(applicationJs));
        }
    }
    
    private void addHomePageLinkAndApplicationName() {
        // this is a bit hacky, but it'll do...
        ExternalLink homePageLink = new ExternalLink(ID_HOME_PAGE_LINK, "/wicket/");
        homePageLink.setContextRelative(true);
        add(homePageLink);
        homePageLink.add(new Label(ID_APPLICATION_NAME, applicationName));
    }
    
    private void addUserName() {
        add(new Label(ID_USER_NAME, getAuthenticationSession().getUserName()));
    }

    private void addLogoutLink() {
        add(new Link<Object>(ID_LOGOUT_LINK) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                getSession().invalidate();
                throw new RestartResponseAtInterceptPageException(WicketSignInPage.class);
            }
        });
    }

    private void addAboutLink() {
        add(new Link<Object>(ID_ABOUT_LINK) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(AboutPage.class);
            }
        });
    }


    /**
     * As provided in the {@link #PageAbstract(ComponentType) constructor}.
     * 
     * <p>
     * This superclass doesn't do anything with this property directly, but
     * requiring it to be provided enforces standardization of the
     * implementation of the subclasses.
     */
    public List<ComponentType> getChildModelTypes() {
        return childComponentIds;
    }

    @Override
    public PageParameters getPageParameters() {
        return pageParameters;
    }

    private void addApplicationActionsComponent() {
        final ApplicationActionsModel model = new ApplicationActionsModel();
        addComponent(ComponentType.APPLICATION_ACTIONS, model);
    }

    /**
     * For subclasses to call.
     * 
     * <p>
     * Should be called in the subclass' constructor.
     * 
     * @param model
     *            - used to find the best matching {@link ComponentFactory} to
     *            render the model.
     */
    protected void addChildComponents(final IModel<?> model) {
        for (final ComponentType componentType : getChildModelTypes()) {
            addComponent(componentType, model);
        }
    }

    private void addComponent(final ComponentType componentType, final IModel<?> model) {
        getComponentFactoryRegistry().addOrReplaceComponent(this, componentType, model);
    }


    ////////////////////////////////////////////////////////////////
    // bookmarked pages
    ////////////////////////////////////////////////////////////////

    /**
     * Convenience for subclasses
     */
    protected void addBookmarkedPages() {
        getComponentFactoryRegistry().addOrReplaceComponent(this, ID_BOOKMARKED_PAGES, ComponentType.BOOKMARKED_PAGES, getBookmarkedPagesModel());
    }

    protected void bookmarkPage(BookmarkableModel<?> model) {
        getBookmarkedPagesModel().bookmarkPage(model);
    }

    private BookmarkedPagesModel getBookmarkedPagesModel() {
        BookmarkedPagesModelProvider application = (BookmarkedPagesModelProvider) getApplication();
        return application.getBookmarkedPagesModel();
    }



    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        final ComponentFactoryRegistryAccessor cfra = (ComponentFactoryRegistryAccessor) getApplication();
        return cfra.getComponentFactoryRegistry();
    }

    // ///////////////////////////////////////////////////
    // System components
    // ///////////////////////////////////////////////////

    protected ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }
    
    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }
}
