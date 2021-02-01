package org.opentoolset.poc.vaadin7.custom.client.component1;

import com.vaadin.shared.AbstractComponentState;

public class Component1State extends AbstractComponentState {

	private static final long serialVersionUID = 1L;

	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
