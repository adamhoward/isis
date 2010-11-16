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


package org.apache.isis.core.runtime.persistence.objectfactory;

import java.lang.reflect.Method;

public class MethodUtils {

    public static boolean isGetter(Method method) {
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
        	return true;
        }
        if (name.startsWith("is") && name.length() > 2) {
        	return true;
        }
        return false;
    }

    public static boolean isSetter(Method method) {
        String name = method.getName();
        return name.startsWith("set") && name.length() > 3;
    }


}