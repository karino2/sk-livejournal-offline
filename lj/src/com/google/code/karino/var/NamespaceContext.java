package com.google.code.karino.var;

import java.util.Iterator;

public interface NamespaceContext {

	public String getNamespaceURI(String prefix);

	public String getPrefix(String nameSpaceURI);

	public Iterator getPrefixes(String namespaceURI);
}
