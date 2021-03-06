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
import java.util.*;
import lotus.domino.*;

public class FormulaBindingFactory implements BindingFactory {
	public static final String LANGUAGE_NAME = "formula";

	@SuppressWarnings("unchecked")
	public MethodBinding createMethodBinding(Application arg0, String arg1, Class[] arg2) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new FormulaMethodBinding(script);
	}

	public ValueBinding createValueBinding(Application arg0, String arg1) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new FormulaValueBinding(script);
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
	@SuppressWarnings("unchecked")
	protected static Object evaluate(String formula) throws NotesException {
		Document currentDoc = getCurrentDocument();
		List<Object> result;
		if(currentDoc != null) {
			result = ExtLibUtil.getCurrentSession().evaluate(formula, currentDoc);
		} else {
			result = ExtLibUtil.getCurrentSession().evaluate(formula);
		}
		List<Object> translated = new ArrayList<Object>();
		for(Object element : result) {
			if(element instanceof DateTime) {
				DateTime dt = (DateTime)element;
				translated.add(dt.toJavaDate());
				dt.recycle();
			} else {
				translated.add(element);
			}
		}
		return translated;
	}

	public static class FormulaMethodBinding extends MethodBindingEx implements Serializable, StateHolder {
		private static final long serialVersionUID = 1L;
		private String content;

		public FormulaMethodBinding() { super(); }
		public FormulaMethodBinding(String content) {
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
				return evaluate(content);
			} catch(NotesException ne) {
				throw new EvaluationException(ne);
			}
		}

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
	public static class FormulaValueBinding extends ValueBindingEx implements Serializable, StateHolder {
		private static final long serialVersionUID = 1L;
		private String content;

		public FormulaValueBinding() { super(); }
		public FormulaValueBinding(String content) {
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
				return evaluate(content);
			} catch(NotesException ne) {
				throw new EvaluationException(ne);
			}
		}

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
