package monarch.ontology.phenoworkbench.browser.candident;

import java.lang.reflect.Method;

import com.vaadin.event.ConnectorEventListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.util.ReflectTools;

public interface GridChangedListener extends ConnectorEventListener {
	public static final Method clickMethod = ReflectTools
            .findMethod(GridChangedListener.class, "gridChange", GridChangeEvent.class);

    /**
     * 
     * @param event
     *            An event containing information about the click.
     */
    public void gridChange(GridChangeEvent event);
}
