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


package org.apache.isis.bytecode.javassist.persistence.objectfactory.internal;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.lang.ArrayUtils;
import org.apache.isis.metamodel.java5.ImperativeFacet;
import org.apache.isis.metamodel.java5.ImperativeFacetUtils;
import org.apache.isis.metamodel.java5.ImperativeFacetUtils.ImperativeFacetFlags;
import org.apache.isis.metamodel.spec.JavaSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.bytecode.ObjectResolveAndObjectChangedEnhancerAbstract;
import org.apache.isis.runtime.persistence.objectfactory.ObjectChanger;
import org.apache.isis.runtime.persistence.objectfactory.ObjectResolver;

public class ObjectResolveAndObjectChangedEnhancer extends ObjectResolveAndObjectChangedEnhancerAbstract {
	
	private MethodHandler methodHandler;

	public ObjectResolveAndObjectChangedEnhancer(
			final ObjectResolver objectResolver, 
			final ObjectChanger objectChanger, 
			final SpecificationLoader specificationLoader) {
		super(objectResolver, objectChanger, specificationLoader);
		
		createCallback();
	}

	protected void createCallback() {
		this.methodHandler = new MethodHandler() {
		    public Object invoke(
		    		final Object proxied, Method proxyMethod, 
		    		Method proxiedMethod, Object[] args) throws Throwable {

		    	boolean ignore = proxyMethod.getDeclaringClass().equals(Object.class);
				ImperativeFacetFlags flags = null;
		    	
				if (!ignore) {
					final JavaSpecification targetObjSpec = getJavaSpecificationOfOwningClass(proxiedMethod); 
	
					final ObjectMember member = targetObjSpec.getMember(proxiedMethod);
					flags = ImperativeFacetUtils.getImperativeFacetFlags(member, proxiedMethod);
					
					if (flags.impliesResolve()) {
						objectResolver.resolve(proxied, member.getName());
					}
				}

		        Object proxiedReturn = proxiedMethod.invoke(proxied, args);  // execute the original method.
		        
				if (!ignore && flags.impliesObjectChanged()) {
					objectChanger.objectChanged(proxied);
				}
			
				return proxiedReturn;
		    }
		};
	}
	
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> cls) {

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setSuperclass(cls);
		proxyFactory.setInterfaces(ArrayUtils.combine(
				cls.getInterfaces(), new Class<?>[]{JavassistEnhanced.class}));
		
		proxyFactory.setFilter(new MethodFilter() {
		     public boolean isHandled(Method m) {
		         // ignore finalize()
		         return !m.getName().equals("finalize");
		     }
		});
		
		Class<T> proxySubclass = proxyFactory.createClass();
		try {
			T newInstance = proxySubclass.newInstance();
			ProxyObject proxyObject = (ProxyObject)newInstance;
			proxyObject.setHandler(methodHandler);
			
			return newInstance;
		} catch (InstantiationException e) {
			throw new IsisException(e);
		} catch (IllegalAccessException e) {
			throw new IsisException(e);
		}
	}

}