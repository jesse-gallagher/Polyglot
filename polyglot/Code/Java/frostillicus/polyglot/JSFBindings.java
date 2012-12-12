package frostillicus.polyglot;

import java.io.Serializable;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.script.Bindings;

import com.ibm.xsp.extlib.util.ExtLibUtil;

public class JSFBindings implements Bindings, Serializable {
	private static final long serialVersionUID = 1L;

	private Bindings proxyBindings;

	public JSFBindings(Bindings proxyBindings) {
		this.proxyBindings = proxyBindings;
	}

	public boolean containsKey(Object paramObject) {

		Object result = this.get(paramObject);
		if(result != null) { return true; }

		return proxyBindings.containsKey(paramObject);
	}

	public Object get(Object paramObject) {

		if(paramObject != null) {
			Object result = ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), String.valueOf(paramObject));
			if(result != null) {
				return result;
			}
		}

		return proxyBindings.get(paramObject);
	}

	public Object put(String paramString, Object paramObject) {
		return proxyBindings.put(paramString, paramObject);
	}

	public void putAll(Map<? extends String, ? extends Object> paramMap) {
		proxyBindings.putAll(paramMap);
	}

	public Object remove(Object paramObject) {
		return proxyBindings.remove(paramObject);
	}

	public void clear() {
		proxyBindings.clear();
	}

	public boolean containsValue(Object paramObject) {
		return proxyBindings.containsValue(paramObject);
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return proxyBindings.entrySet();
	}

	public boolean isEmpty() {
		return false;
	}

	public Set<String> keySet() {
		return proxyBindings.keySet();
	}

	public int size() {
		return proxyBindings.size();
	}

	public Collection<Object> values() {
		return proxyBindings.values();
	}

}