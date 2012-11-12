package frostillicus;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.*;
import java.lang.reflect.Method;
import javax.faces.context.FacesContext;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import lotus.domino.*;
import lotus.domino.local.NotesBase;

public abstract class Util {
	// Via http://stackoverflow.com/questions/12740889/what-is-the-least-expensive-way-to-test-if-a-view-has-been-recycled
	public static boolean isRecycled(Base object) {
		if(!(object instanceof NotesBase)) {
			// No reason to test non-NotesBase objects -> isRecycled = true
			return true;
		}

		try {
			NotesBase notesObject = (NotesBase)object;
			Method isDead = notesObject.getClass().getSuperclass().getDeclaredMethod("isDead");
			isDead.setAccessible(true);

			return (Boolean)isDead.invoke(notesObject);
		} catch (Throwable exception) { }

		return true;
	}

	@SuppressWarnings("unchecked")
	public static void addConfirmationMessage(String message) {
		Map<String, Object> flashScope = (Map<String, Object>)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "flashScope");
		List<String> messages = (List<String>)flashScope.get("confirmationMessages");
		if(messages == null) {
			messages = new ArrayList<String>();
			flashScope.put("confirmationMessages", messages);
		}
		messages.add(message);
	}

	public static Serializable restoreState(Document doc, String itemName) throws Exception {
		Session session = ExtLibUtil.getCurrentSession();
		boolean convertMime = session.isConvertMime();
		session.setConvertMime(false);

		Serializable result = null;
		Stream mimeStream = session.createStream();
		MIMEEntity entity = doc.getMIMEEntity(itemName);
		entity.getContentAsBytes(mimeStream);

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		mimeStream.getContents(streamOut);
		mimeStream.recycle();

		byte[] stateBytes = streamOut.toByteArray();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(stateBytes);
		ObjectInputStream objectStream;
		if(entity.getHeaders().toLowerCase().contains("content-encoding: gzip")) {
			GZIPInputStream zipStream = new GZIPInputStream(byteStream);
			objectStream = new ObjectInputStream(zipStream);
		} else {
			objectStream = new ObjectInputStream(byteStream);
		}
		Serializable restored = (Serializable)objectStream.readObject();
		result = restored;


		entity.recycle();

		session.setConvertMime(convertMime);

		return result;
	}
	public static void saveState(Serializable object, Document doc, String itemName) throws NotesException {
		try {
			Session session = ExtLibUtil.getCurrentSession();

			boolean convertMime = session.isConvertMime();
			session.setConvertMime(false);

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(new GZIPOutputStream(byteStream));
			objectStream.writeObject(object);
			objectStream.flush();
			objectStream.close();

			MIMEEntity entity = null;
			MIMEEntity previousState = doc.getMIMEEntity(itemName);
			if (previousState == null) {
				entity = doc.createMIMEEntity(itemName);
			} else {
				entity = previousState;
			}

			Stream mimeStream = session.createStream();
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteStream.toByteArray());
			mimeStream.setContents(byteIn);
			entity.setContentFromBytes(mimeStream, "application/x-java-serialized-object", MIMEEntity.ENC_NONE);

			MIMEHeader header = entity.getNthHeader("Content-Encoding");
			if(header == null) { header = entity.createHeader("Content-Encoding"); }
			header.setHeaderVal("gzip");

			header.recycle();
			entity.recycle();
			mimeStream.recycle();

			session.setConvertMime(convertMime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String strLeft(String input, String delimiter) {
		return input.substring(0, input.indexOf(delimiter));
	}
	public static String strRight(String input, String delimiter) {
		return input.substring(input.indexOf(delimiter) + delimiter.length());
	}
	public static String strLeftBack(String input, String delimiter) {
		return input.substring(0, input.lastIndexOf(delimiter));
	}
	public static String strRightBack(String input, String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}
}
