package frostillicus.controller;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import lotus.domino.*;
import frostillicus.Util;

public class ScriptDocumentController extends AbstractDocumentController {
	private static final long serialVersionUID = 1L;

	public ScriptDocumentController(String documentName) {
		super(documentName);
	}

	@Override
	public void afterPageLoad() throws Exception {
		Document doc = this.getDominoDocument().getDocument();
		if(doc.hasItem("Body")) {
			UIInput bodyField = (UIInput)ExtLibUtil.getComponentFor(FacesContext.getCurrentInstance().getViewRoot(), "Body");
			bodyField.setValue(Util.restoreState(doc, "Body"));
		}

	}

	@Override
	public String save() throws NotesException {
		Document doc = this.getDominoDocument().getDocument(true);
		UIInput bodyField = (UIInput)ExtLibUtil.getComponentFor(FacesContext.getCurrentInstance().getViewRoot(), "Body");
		Util.saveState((String)bodyField.getValue(), doc, "Body");
		return super.save();
	}
}
