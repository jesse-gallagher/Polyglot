package frostillicus.polyglot;

import java.io.Serializable;

import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.*;

import com.ibm.xsp.binding.*;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import com.ibm.xsp.util.ValueBindingUtil;
import java.util.List;
import lotus.domino.*;

public class LotusScriptBindingFactory implements BindingFactory {
	public static final String LANGUAGE_NAME = "lotusscript";

	@SuppressWarnings("unchecked")
	public MethodBinding createMethodBinding(Application arg0, String arg1, Class[] arg2) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new LotusScriptMethodBinding(script);
	}

	public ValueBinding createValueBinding(Application arg0, String arg1) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new LotusScriptValueBinding(script);
	}

	public String getPrefix() {
		return LANGUAGE_NAME;
	}

	protected static Document getCurrentDocument() throws NotesException {
		DominoDocument dominoDocument = (DominoDocument)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "currentDocument");
		if(dominoDocument != null) {
			return dominoDocument.getDocument(true);
		}
		return null;
	}

	public static class LotusScriptMethodBinding extends MethodBindingEx implements Serializable, StateHolder {
		private static final long serialVersionUID = 1L;
		private String content;

		public LotusScriptMethodBinding() { super(); }
		public LotusScriptMethodBinding(String content) {
			super();
			this.content = content;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class getType(FacesContext arg0) throws MethodNotFoundException {
			return List.class;
		}

		@Override
		public Object invoke(FacesContext arg0, Object[] arg1) throws EvaluationException, MethodNotFoundException {
			try {
				Document currentDoc = getCurrentDocument();
				Agent evaluator = ExtLibUtil.getCurrentDatabase().getAgent("Polyglot\\Evaluate LotusScript");
				if(currentDoc == null) {
					currentDoc = ExtLibUtil.getCurrentDatabase().createDocument();
				}

				currentDoc.replaceItemValue("$$LotusScriptBody", "Sub Initialize\n" + content + "\nEnd Sub");
				evaluator.runWithDocumentContext(currentDoc);
				currentDoc.removeItem("$$LotusScriptBody");
				return currentDoc.getItemValue("$$LotusScriptResult");
			} catch(NotesException ne) { }
			return null;
		}

		@Override
		public boolean isTransient() { return false; }

		@Override
		public void restoreState(FacesContext context, Object paramObject) {
			Object[] arrayOfObject = (Object[]) paramObject;
			super.restoreState(context, arrayOfObject[0]);
			this.content = (String)arrayOfObject[1];
		}

		@Override
		public Object saveState(FacesContext context) {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = super.saveState(context);
			arrayOfObject[1] = this.content;
			return arrayOfObject;
		}
	}
	public static class LotusScriptValueBinding extends ValueBindingEx implements Serializable, StateHolder {
		private static final long serialVersionUID = 1L;
		private String content;

		public LotusScriptValueBinding() { super(); }
		public LotusScriptValueBinding(String content) {
			super();
			this.content = content;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class getType(FacesContext arg0) throws MethodNotFoundException {
			return List.class;
		}

		@Override
		public Object getValue(FacesContext arg0) throws EvaluationException, PropertyNotFoundException {
			try {
				Document currentDoc = getCurrentDocument();
				Agent evaluator = ExtLibUtil.getCurrentDatabase().getAgent("Polyglot\\Evaluate LotusScript");
				if(currentDoc == null) {
					currentDoc = ExtLibUtil.getCurrentDatabase().createDocument();
				}
				currentDoc.replaceItemValue("$$LotusScriptBody", content);
				evaluator.runWithDocumentContext(currentDoc);
				currentDoc.removeItem("$$LotusScriptBody");
				return currentDoc.getItemValue("$$LotusScriptResult");
			} catch(NotesException ne) { }
			return null;
		}

		@Override
		public boolean isTransient() { return false; }

		@Override
		public boolean isReadOnly(FacesContext arg0) throws EvaluationException, PropertyNotFoundException {
			return true;
		}
		@Override
		public void setValue(FacesContext arg0, Object arg1) throws EvaluationException, PropertyNotFoundException { }

		@Override
		public void restoreState(FacesContext context, Object paramObject) {
			Object[] arrayOfObject = (Object[]) paramObject;
			super.restoreState(context, arrayOfObject[0]);
			this.content = (String)arrayOfObject[1];
		}

		@Override
		public Object saveState(FacesContext context) {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = super.saveState(context);
			arrayOfObject[1] = this.content;
			return arrayOfObject;
		}
	}
}
