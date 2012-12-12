/*
 * � Copyright Jesse Gallagher 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package frostillicus.polyglot;

import java.io.*;
import java.util.*;

import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;
import javax.script.*;

import com.ibm.xsp.binding.BindingFactory;
import com.ibm.xsp.binding.MethodBindingEx;
import com.ibm.xsp.binding.ValueBindingEx;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.resource.Resource;
import com.ibm.xsp.resource.ScriptResource;
import com.ibm.xsp.util.ValueBindingUtil;

public class GenericBindingFactory implements BindingFactory {
	public static RuntimeScope RUNTIME_SCOPE = RuntimeScope.REQUEST;

	private String language;

	public GenericBindingFactory(String language) {
		this.language = language;
	}

	@SuppressWarnings("unchecked")
	public MethodBinding createMethodBinding(Application arg0, String arg1, Class[] arg2) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new GenericMethodBinding(script, language);
	}

	public ValueBinding createValueBinding(Application arg0, String arg1) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new GenericValueBinding(script, language);
	}

	public String getPrefix() {
		return this.language;
	}

	/* Utility functions for the binding classes */
	protected static ScriptEngine createScriptEngine(String language) throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName(language);

		ScriptContext scriptContext = engine.getContext();
		Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
		scriptContext.setBindings(new JSFBindings(bindings), ScriptContext.ENGINE_SCOPE);

		return engine;
	}

	public static class GenericMethodBinding extends MethodBindingEx implements Serializable, StateHolder {
		private static final long serialVersionUID = 1L;
		private String content;
		private String language;

		public GenericMethodBinding() { super(); }

		public GenericMethodBinding(String expression, String language) {
			super();
			this.content = expression;
			this.language = language;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class getType(FacesContext arg0) throws MethodNotFoundException {
			return null;
		}

		@Override
		public Object invoke(FacesContext context, Object[] arg1) throws EvaluationException, MethodNotFoundException {
			Object result = null;
			try {
				ScriptEngine engine = (ScriptEngine)getScopeMap().get("_" + language + "ScriptEngine");
				if(engine == null) {
					engine = GenericBindingFactory.createScriptEngine(language);
					getScopeMap().put("_" + language + "ScriptEngine", engine);
				}

				includeScriptLibraries(engine);

				result = engine.eval(this.content);
			}
			catch(ScriptException se) { throw new EvaluationException(se); }

			if(!(result instanceof Serializable)) {
				return result.toString();
			}
			return result;
		}

		@Override
		public boolean isTransient() { return false; }

		@Override
		public void restoreState(FacesContext context, Object paramObject) {
			Object[] arrayOfObject = (Object[]) paramObject;
			super.restoreState(context, arrayOfObject[0]);
			this.content = (String)arrayOfObject[1];
			this.language = (String)arrayOfObject[2];
		}

		@Override
		public Object saveState(FacesContext context) {
			Object[] arrayOfObject = new Object[3];
			arrayOfObject[0] = super.saveState(context);
			arrayOfObject[1] = this.content;
			arrayOfObject[2] = this.language;
			return arrayOfObject;
		}
	}

	public static class GenericValueBinding extends ValueBindingEx implements Serializable, StateHolder {
		private static final long serialVersionUID = 3164174793723541283L;

		String content;
		String language;
		boolean _transient = false;

		// Empty constructor for restoreState()
		public GenericValueBinding() { super(); }

		public GenericValueBinding(String content, String language) {
			this.content = content;
			this.language = language;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class getType(FacesContext arg0) throws EvaluationException, PropertyNotFoundException {
			return Object.class;
		}

		@Override
		public Object getValue(FacesContext context) throws EvaluationException, PropertyNotFoundException {
			Object result = null;
			try {
				ScriptEngine engine = (ScriptEngine)getScopeMap().get("_" + language + "ScriptEngine");
				if(engine == null) {
					engine = GenericBindingFactory.createScriptEngine(language);
					getScopeMap().put("_" + language + "ScriptEngine", engine);
				}

				includeScriptLibraries(engine);

				result = engine.eval(this.content);
			}
			catch(ScriptException se) { throw new EvaluationException(se); }
			return result;
		}

		@Override public boolean isReadOnly(FacesContext arg0) throws EvaluationException, PropertyNotFoundException { return true; }
		@Override public void setValue(FacesContext arg0, Object arg1) throws EvaluationException, PropertyNotFoundException { }

		@Override public boolean isTransient() { return this._transient; }
		@Override public void setTransient(boolean arg0) { this._transient = arg0; }

		@Override
		public void restoreState(FacesContext context, Object paramObject) {
			Object[] arrayOfObject = (Object[]) paramObject;
			super.restoreState(context, arrayOfObject[0]);
			this.content = (String)arrayOfObject[1];
			this.language = (String)arrayOfObject[2];
		}

		@Override
		public Object saveState(FacesContext context) {
			Object[] arrayOfObject = new Object[3];
			arrayOfObject[0] = super.saveState(context);
			arrayOfObject[1] = this.content;
			arrayOfObject[2] = this.language;
			return arrayOfObject;
		}
	}

	@SuppressWarnings("unchecked")
	protected static void includeScriptLibraries(ScriptEngine engine) throws ScriptException {
		FacesContext context = FacesContext.getCurrentInstance();

		String language = engine.getFactory().getLanguageName();
		Set<String> mimeTypes = new HashSet<String>(engine.getFactory().getMimeTypes());
		mimeTypes.add("text/x-script-" + language);

		Set<String> includedLibraries = (Set<String>)getScopeMap().get("_" + language + "IncludedLibraries");
		if(includedLibraries == null) {
			includedLibraries = new HashSet<String>();
			getScopeMap().put("_" + language + "IncludedLibraries", includedLibraries);
		}

		// Now look through the view's resources for appropriate scripts and load them
		UIViewRootEx2 view = (UIViewRootEx2)context.getViewRoot();
		if(view != null) {
			for(Resource res : view.getResources()) {
				if(res instanceof ScriptResource) {
					ScriptResource script = (ScriptResource)res;
					if(script.getType() != null && mimeTypes.contains(script.getType())) {
						// Then we have a script - find its contents and run it

						String properName = (script.getSrc().charAt(0) == '/' ? "" : "/") + script.getSrc();
						if(!includedLibraries.contains(properName)) {
							InputStream is = context.getExternalContext().getResourceAsStream("/WEB-INF/" + language + properName);
							if(is != null) {
								engine.eval(new InputStreamReader(is));
							}

							includedLibraries.add(properName);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected static Map<Object, Object> getScopeMap() {
		switch(RUNTIME_SCOPE) {
		case APPLICATION:
			return FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
		case REQUEST:
			return FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
		}
		return null;
	}

	public static enum RuntimeScope {
		APPLICATION,
		REQUEST
	}
}