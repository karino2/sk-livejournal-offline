package com.googlecode.karino.var;

import java.io.Serializable;

/**
 * Android doesn't have a javax.xml.namespace.QName so this is my version of
 * this
 * 
 * @author fi41051
 * 
 */

public class QName implements Serializable {

	public QName(String localPart) {

	}

	public QName(String namespaceURI, String localPart) {

	}

	public QName(String namespaceURI, String localPart, String prefix) {

	}

	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

	public boolean equals(Object objectToTest) {
		return true;
	}

	/**
	 * Get the local part of this QName.
	 */

	public String getLocalPart() {
		return null;
	}

	/**
	 * Get the Namespace URI of this QName.
	 * 
	 * @return
	 */

	public String getNamespaceURI() {
		return null;
	}

	/**
	 * Get the prefix of this QName.
	 */

	public String getPrefix() {
		return null;
	}

	/**
	 * QName derived from parsing the formatted String.
	 * 
	 * @param qNameAsString
	 * @return
	 */

	public static QName valueOf(String qNameAsString) {
		return null;
	}

}
