package org.opentoolset.poc.vaadin7.custom.client.component1;

import com.google.gwt.user.client.ui.Label;

public class Component1Widget extends Label {

	public Component1Widget() {
		setStyleName(Component1Widget.class.getName());
		setText("Hello World!");
	}
}
