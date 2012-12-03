package frostillicus;

import java.io.Serializable;

import javax.script.*;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.factory.FactoryLookup;

public class ScriptBindingAttacher implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public ScriptBindingAttacher() {
		// Attach ALL the scripting engines
		try {
			ApplicationEx app = ApplicationEx.getInstance();
			FactoryLookup facts = app.getFactoryLookup();

			ScriptEngineManager manager = new ScriptEngineManager();
			for(ScriptEngineFactory scriptFactory : manager.getEngineFactories()) {
				GenericBindingFactory fac = new GenericBindingFactory(scriptFactory.getLanguageName());
				if(facts.getFactory(fac.getPrefix()) == null) {
					facts.setFactory(fac.getPrefix(), fac);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
