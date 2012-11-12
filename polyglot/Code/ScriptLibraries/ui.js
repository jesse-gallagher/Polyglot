function create_code_mirror(id, mode) {
	var editor = dojo.byId(id)
	document["codemirror-" + id] = CodeMirror.fromTextArea(editor, {
		lineNumbers: true,
		onKeyEvent: editor.onkeypress,
		mode: mode
	})
}