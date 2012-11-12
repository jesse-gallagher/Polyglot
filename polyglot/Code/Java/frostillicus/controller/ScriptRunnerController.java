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

			String scriptName = script.getItemValueString("Name");
			String language = script.getItemValueString("Language");

			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine scriptEngine = manager.getEngineByName(language);
			ScriptContext scriptContext = scriptEngine.getContext();
			scriptContext.setErrorWriter(writer);
			scriptContext.setWriter(writer);
			Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.put("database", database);
			bindings.put("facesContext", FacesContext.getCurrentInstance());
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
}
