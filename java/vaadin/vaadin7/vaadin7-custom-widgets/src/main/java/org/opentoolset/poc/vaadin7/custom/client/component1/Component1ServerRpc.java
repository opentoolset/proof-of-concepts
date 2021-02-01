package org.opentoolset.poc.vaadin7.custom.client.component1;

import com.vaadin.shared.communication.ServerRpc;

public interface Component1ServerRpc extends ServerRpc {

	void clicked(String buttonName);
}
