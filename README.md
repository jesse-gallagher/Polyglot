Polyglot
========

Polyglot is an experiment with making all JSR-223-compliant scripting languages present on a server available in several ways in XPages.

Currently, it exposes the languages in two ways:

- An improved version of the generic-scripting-language functionality from Ruby-in-XPages, which allows for all available languages to be used in value and method bindings in XPages.
- A document-based script editor (/Scripts.xsp et al) that lets you create and execute standalone scripts in available languages in the context of the /ScriptRunner.xsp XPage.

Generic Binding
---------------

Like Ruby-in-XPages, the generic binding feature allows the use of scripting languages in any context where #{javascript: ... } bindings are allowed. Additionally, it allows the use of script libraries by placing a file in "WebContent/WEB-INF/#{languageName}/", where "languageName" is the canonical name from the script factory (Home.xsp contains a table of available languages, and this is the "Language" column). For example:

	<xp:this.resources>
		<xp:script clientSide="false" type="application/x-ruby" src="/testclass.rb"/>
	</xp:this.resources>
	
	<xp:text value="#{ruby: TestClass.new.someMethod }"/>

The scope of the runtime is set to the current request but can be switched to the application via the RUNTIME scope property of frostillicus.GenericBindingFactory.

Scripting languages are made available using any of the names in the "Names" column on Home.xsp (except Rhino's "javascript", which is used for SSJS instead). The "type" specified for Script Libraries can be any of the values in the "MIME Types" column or "text/x-script-#{languageName}" as a fallback for languages without MIME types listed.

Script Runner
-------------

The Script Runner is a mostly-proof-of-concept page for editing and executing document-stored scripts using either the JSR-223 available languages or XSP.