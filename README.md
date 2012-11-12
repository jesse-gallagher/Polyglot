Polyglot
========

Polyglot is an experiment with making all JSR-223-compliant scripting languages present on a server available in several ways in XPages.

Currently, it exposes the languages in two ways:

- A document-based script editor (/Scripts.xsp et al) that lets you create and execute standalone scripts in available languages in the context of the /ScriptRunner.xsp XPage.
- An improved version of the generic-scripting-language functionality from Ruby-in-XPages, which allows for all available languages to be used in value and method bindings in XPages.