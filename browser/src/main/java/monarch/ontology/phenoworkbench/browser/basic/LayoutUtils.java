package monarch.ontology.phenoworkbench.browser.basic;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.browser.quickimpact.PatternInfoBox;

public class LayoutUtils {
	
    public static Panel preparePanel(Component c, String label) {
        Panel panel = new Panel(label);
        panel.setWidth("100%");
        panel.setHeight("100%");
        //c.setSizeUndefined();
        c.setWidth("100%");
        panel.setContent(c);
        return panel;
    }
    
    public static VerticalLayout vl100(Component content) {
        VerticalLayout vl_infobox = new VerticalLayout();
        vl_infobox.addComponent(content);
        vl_infobox.setSizeFull();
        return vl_infobox;
    }

    public static VerticalLayout hlNoMarginNoSpacingNoSize(Component content) {
        VerticalLayout vl_infobox = new VerticalLayout();
        vl_infobox.addComponent(content);
        vl_infobox.setMargin(false);
        vl_infobox.setSpacing(false);
        vl_infobox.setSizeUndefined();
        return vl_infobox;
    }
    
    public static HorizontalSplitPanel prepareSplitPanel(Component right, Component left, int heightPixels) {
        HorizontalSplitPanel split_tree = new HorizontalSplitPanel();
        split_tree.setSplitPosition(60, Unit.PERCENTAGE);
        split_tree.setWidth("100%");
        split_tree.setHeight(heightPixels+"px");
        split_tree.setFirstComponent(left);
        split_tree.setSecondComponent(right);
        return split_tree;
    }
}
