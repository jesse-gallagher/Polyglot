package frostillicus.controller;

import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.webapp.XspHttpServletResponse;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import lotus.domino.*;
import java.io.PrintWriter;
import java.util.*;
import frostillicus.Util;
import frostillicus.polyglot.JSFBindings;

import javax.script.*;

import com.ibm.xsp.extlib.interpreter.UIControlFactory;
import com.ibm.xsp.extlib.interpreter.interpreter.MarkupControlBuilder;

public class ScriptRunnerController implements XPageController {
	private static final long serialVersionUID = 1L;

	public ScriptRunnerController() { }

	@SuppressWarnings("unchecked")
	public void beforeRenderResponse() throws Exception {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		UIViewRoot root = facesContext.getViewRoot();

		Database database = ExtLibUtil.getCurrentDatabase();
		View scripts = database.getView("Scripts");
		scripts.setAutoUpdate(false);
		Document script = scripts.getDocumentByKey((String)getParam().get("scriptName"));

		String language = script.getItemValueString("Language");

		if(language.equals("XSP")) {
			String scriptBody = (String)Util.restoreState(script, "Body");

			try {
				UIControlFactory controlFactory = new UIControlFactory();
				controlFactory.setId("controlFactory1");
				MarkupControlBuilder builder = new MarkupControlBuilder();
				builder.setComponent(controlFactory);
				builder.setXmlMarkup(scriptBody);
				controlFactory.setControlBuilder(builder);
				root.getChildren().add(controlFactory);
				controlFactory.setParent(root);
				controlFactory.createChildren((FacesContextEx)FacesContext.getCurrentInstance());
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			root.setRendered(false);
		}
	}
	public void afterRenderResponse() throws Exception {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		UIViewRoot root = facesContext.getViewRoot();

		Database database = ExtLibUtil.getCurrentDatabase();
		View scripts = database.getView("Scripts");
		scripts.setAutoUpdate(false);
		Document script = scripts.getDocumentByKey((String)getParam().get("scriptName"));

		String language = script.getItemValueString("Language");
		if(!language.equals("XSP")) {
			root.setRendered(false);
			ResponseWriter responseWriter = facesContext.getResponseWriter();
			PrintWriter writer = new PrintWriter(responseWriter);
			XspHttpServletResponse response = (XspHttpServletResponse)facesContext.getExternalContext().getResponse();
			response.setContentType("text/plain");

			try {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine scriptEngine = manager.getEngineByName(language);
				ScriptContext scriptContext = scriptEngine.getContext();
				scriptContext.setErrorWriter(writer);
				scriptContext.setWriter(writer);

				Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
				scriptContext.setBindings(new JSFBindings(bindings), ScriptContext.ENGINE_SCOPE);

				scriptEngine.eval((String)Util.restoreState(script, "Body"));


			} catch(Exception e) {
				e.printStackTrace(new PrintWriter(writer));
				e.printStackTrace();
			}

			responseWriter.endDocument();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getParam() {
		return (Map<String, Object>)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "param");
	}
}
