package org.apache.isis.progmodel.groovy.metamodel;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

public class RemoveGroovyMethodsFacetFactory extends FacetFactoryAbstract implements IsisConfigurationAware {
	
    private static final String DEPTH_KEY = "isis.groovy.depth";
    private static final int DEPTH_DEFAULT = 5;
    
	private IsisConfiguration configuration;

	public RemoveGroovyMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }
	
	static class MethodSpec {
		static class Builder {
			
			private MethodSpec methodSpec = new MethodSpec();
			
			public Builder param(Class<?>... paramTypes) {
				methodSpec.parameterTypes = paramTypes;
				return this;
			}
			
			public Builder ret(Class<?> returnType) {
				methodSpec.returnType = returnType;
				return this;
			}

			public MethodSpec build() {
				return methodSpec; 
			}

			public void remove(MethodRemover remover) {
				build().removeMethod(remover);
			}
		}
		static Builder specFor(String methodName) {
			Builder builder = new Builder();
			builder.methodSpec.methodName = methodName;
			return builder;
		}
		static Builder specFor(String formatStr, Object... args) {
			return specFor(String.format(formatStr, args));
		}
		private String methodName;
		private Class<?> returnType = void.class;
		private Class<?>[] parameterTypes = new Class[0];
		void removeMethod(MethodRemover methodRemover) {
			methodRemover.removeMethod(MethodScope.OBJECT, methodName, returnType, parameterTypes);
		}
	}

    @Override
    public void process(ProcessClassContext processClassContext) {
    	MethodSpec.specFor("invokeMethod").param(String.class, Object.class).ret(Object.class).remove(processClassContext);
    	MethodSpec.specFor("getMetaClass").ret(groovy.lang.MetaClass.class).remove(processClassContext);
    	MethodSpec.specFor("setMetaClass").param(groovy.lang.MetaClass.class).remove(processClassContext);
    	MethodSpec.specFor("getProperty").param(String.class).ret(Object.class).remove(processClassContext);

        int depth = determineDepth();
		for(int i=1; i<depth; i++) {
	    	MethodSpec.specFor("this$dist$invoke$%d", i).param(String.class, Object.class).ret(Object.class).remove(processClassContext);
	    	MethodSpec.specFor("this$dist$set$%d", i).param(String.class, Object.class).remove(processClassContext);
	    	MethodSpec.specFor("this$dist$get$%d", i).param(String.class).ret(Object.class).remove(processClassContext);
        }
		Method[] methods = processClassContext.getCls().getMethods();
		for(Method method: methods) {
			if (method.getName().startsWith("super$")) {
				processClassContext.removeMethod(method);
			}
		}
    }

	private int determineDepth() {
		int depth = configuration.getInteger(DEPTH_KEY, DEPTH_DEFAULT);
		return depth;
	}

	@Override
	public void setIsisConfiguration(
			IsisConfiguration configuration) {
		this.configuration = configuration;
	}

}
