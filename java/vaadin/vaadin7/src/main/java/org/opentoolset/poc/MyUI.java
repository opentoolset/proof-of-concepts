package org.opentoolset.poc;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.diagrambuilder.DiagramBuilder;
import org.vaadin.diagrambuilder.DiagramBuilder.StateCallback;
import org.vaadin.diagrambuilder.DiagramStateEvent;
import org.vaadin.diagrambuilder.Node;
import org.vaadin.diagrambuilder.NodeType;
import org.vaadin.diagrambuilder.Transition;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * This UI is the application entry point. A UI may either represent a browser window (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@JavaScript("http://cdn.alloyui.com/2.5.0/aui/aui-min.js")
@StyleSheet("http://cdn.alloyui.com/2.5.0/aui-css/css/bootstrap.min.css")
@SpringUI
public class MyUI extends UI {

	private static final long serialVersionUID = 1L;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();

		final TextField name = new TextField();
		name.setCaption("Type your name here:");

		DiagramBuilder diagramBuilder = new DiagramBuilder();

		Button button = new Button("Click Me");
		button.addClickListener(e -> {
			layout.addComponent(new Label("Thanks " + name.getValue() + ", it works!"));

			diagramBuilder.getDiagramState(new StateCallback() {

				@Override
				public void onStateReceived(DiagramStateEvent event) {
					List<Node> nodes = event.getNodes();
					System.out.println(nodes);
				}
			});
		});

		List<NodeType> nodeTypes = new ArrayList<>();
		nodeTypes.add(new NodeType("diagram-node-start-icon", "Start", "start"));
		nodeTypes.add(new NodeType("diagram-node-fork-icon", "Fork", "fork"));
		nodeTypes.add(new NodeType("diagram-node-condition-icon", "Condition", "condition"));

		List<Node> nodes = new ArrayList<>();
		nodes.add(new Node("StartNode", "start", 10, 10));
		nodes.add(new Node("Condition", "condition", 260, 16));

		diagramBuilder.setAvailableFields(nodeTypes.toArray(new NodeType[] { }));
		diagramBuilder.setFields(nodes.toArray(new Node[] { }));

		diagramBuilder.setTransitions(new Transition("StartNode", "Condition", "TaskConnector1"));
		diagramBuilder.setSizeFull();

		layout.addComponents(name, button, diagramBuilder);
		layout.setExpandRatio(diagramBuilder, 1);
		layout.setMargin(true);
		layout.setSpacing(true);

		setContent(layout);
	}

	// @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	// @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
	// public static class MyUIServlet extends VaadinServlet {
	//
	// private static final long serialVersionUID = 1L;
	// }
}
