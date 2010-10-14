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


package org.apache.isis.extensions.dnd.view.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.SubviewDecorator;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;


public abstract class AbstractViewBuilder implements ViewBuilder {
    private final List<SubviewDecorator> subviewDecorators = new ArrayList<SubviewDecorator>();

    public abstract void build(final View view, Axes axes);

    public void createAxes(Axes axes, Content content) {
        for (SubviewDecorator decorator : subviewDecorators) {
            axes.add(decorator.createAxis(content));
        }        
    }

    public void addSubviewDecorator(SubviewDecorator decorator) {
        if (decorator != null) {
            subviewDecorators.add(decorator);
        }
    }

    public View decorateSubview(Axes axes, final View child) {
        View view = child;
        for (SubviewDecorator decorator : subviewDecorators) {
            view = decorator.decorate(axes, view);
        }
        return view;
    }

    public boolean isOpen() {
        return false;
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isSubView() {
        return false;
    }

    public boolean canDragView() {
        return true;
    }
    
    @Override
    public String toString() {
        final String name = getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
    
    public void viewMenuOptions(UserActionSet options, View view) {}
}