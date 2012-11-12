package frostillicus.controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.webapp.XspHttpServletResponse;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import lotus.domino.*;
import java.io.PrintWriter;
import java.util.*;

import frostillicus.Util;
import javax.script.*;

public class ScriptRunnerController implements XPageController {
	private static final long serialVersionUID = 1L;

	public ScriptRunnerController() { }

	public void afterRenderResponse() throws Exception {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.getViewRoot().setRendered(false);
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		PrintWriter writer = new PrintWriter(responseWriter);
		XspHttpServletResponse response = (XspHttpServletResponse)facesContext.getExternalContext().getResponse();

		response.setContentType("text/plain");

		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			View scripts = database.getView("Scripts");
			scripts.setAutoUpdate(false);
			Document script = scripts.getDocumentByKey((String)getParam().get("scriptName"));

			String language = script.getItemValueString("Language");

			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine scriptEngine = manager.getEngineByName(language);
			ScriptContext scriptContext = scriptEngine.getContext();
			scriptContext.setErrorWriter(writer);
			scriptContext.setWriter(writer);
			//			Bindings bindings = new JSFBindings(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE));
			//			bindings.put("database", database);
			//			bindings.put("facesContext", FacesContext.getCurrentInstance());
			//			scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			scriptEngine.eval((String)Util.restoreState(script, "Body"));


		} catch(Exception e) {
			e.printStackTrace(new PrintWriter(writer));
		}

		responseWriter.endDocument();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getParam() {
		return (Map<String, Object>)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "param");
	}

	public class JSFBindings implements Bindings {
		private final Bindings proxyBindings;

		public JSFBindings(Bindings proxyBindings) {
			this.proxyBindings = proxyBindings;
		}

		public boolean containsKey(Object paramObject) {
			System.out.println("bindings.containsKey(" + paramObject + ")");
			return proxyBindings.containsKey(paramObject);
		}

		public Object get(Object paramObject) {
			System.out.println("bindings.get(" + paramObject + ")");
			return proxyBindings.get(paramObject);
		}

		public Object put(String paramString, Object paramObject) {
			System.out.println("bindings.put(" + paramString + ", " + paramObject + ")");
			return proxyBindings.put(paramString, paramObject);
		}

		public void putAll(Map<? extends String, ? extends Object> paramMap) {
			System.out.println("bindings.putAll(" + paramMap + ")");
			proxyBindings.putAll(paramMap);
		}

		public Object remove(Object paramObject) {
			System.out.println("bindings.remove(" + paramObject + ")");
			return proxyBindings.remove(paramObject);
		}

		public void clear() {
			System.out.println("bindings.clear()");
			proxyBindings.clear();
		}

		public boolean containsValue(Object paramObject) {
			System.out.println("bindings.containsValue(" + paramObject + ")");
			return proxyBindings.containsValue(paramObject);
		}

		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			System.out.println("bindings.entrySet()");
			return proxyBindings.entrySet();
		}

		public boolean isEmpty() {
			System.out.println("bindings.isEmpty()");
			return proxyBindings.isEmpty();
		}

		public Set<String> keySet() {
			System.out.println("bindings.keySet()");
			return proxyBindings.keySet();
		}

		public int size() {
			System.out.println("bindings.size()");
			return proxyBindings.size();
		}

		public Collection<Object> values() {
			System.out.println("bindings.values()");
			return proxyBindings.values();
		}


	}
}
