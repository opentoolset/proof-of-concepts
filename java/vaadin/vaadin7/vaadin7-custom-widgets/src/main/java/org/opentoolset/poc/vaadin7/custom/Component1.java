package org.opentoolset.poc.vaadin7.custom;

import org.opentoolset.poc.vaadin7.custom.client.component1.Component1ServerRpc;
import org.opentoolset.poc.vaadin7.custom.client.component1.Component1State;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Notification;

public class Component1 extends AbstractComponent {

	private static final long serialVersionUID = 1L;

	private Component1ServerRpc rpc = new Component1ServerRpc() {

		private static final long serialVersionUID = 1L;

		private int clickCount = 0;

		public void clicked(String buttonName) {
			++clickCount;
			Notification.show("Clicked " + buttonName + " " + clickCount + " times!");
		}
	};

	public Component1() {
		registerRpc(rpc);
		getState().setText("Hello world from server side!");
	}

	@Override
	protected Component1State getState() {
		return (Component1State) super.getState();
	}
}
