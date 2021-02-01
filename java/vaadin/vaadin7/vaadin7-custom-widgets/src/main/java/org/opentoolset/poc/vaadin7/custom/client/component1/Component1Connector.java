package org.opentoolset.poc.vaadin7.custom.client.component1;

import org.opentoolset.poc.vaadin7.custom.Component1;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.Connect;

@Connect(Component1.class)
public class Component1Connector extends AbstractComponentConnector {

	private static final long serialVersionUID = 1L;

	public Component1Connector() {
		RpcProxy.create(Component1ServerRpc.class, this);
		getWidget().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final MouseEventDetails mouseDetails = MouseEventDetailsBuilder.buildMouseEventDetails(event.getNativeEvent(), getWidget().getElement());
				Component1ServerRpc rpc = getRpcProxy(Component1ServerRpc.class);
				rpc.clicked(mouseDetails.getButtonName());
			}
		});
	}

	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);
		String text = getState().getText();
		getWidget().setText(text);
	}

	@Override
	public Component1Widget getWidget() {
		return (Component1Widget) super.getWidget();
	}

	@Override
	public Component1State getState() {
		return (Component1State) super.getState();
	}
}
