package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

class BucketGridPanel extends VerticalLayout {

    /**
     *
     */

    private final BucketGrid grid = new BucketGrid();
    private final Button bt_save_bucket = new Button("Save");
    private final Button bt_load_bucket = new Button("Load");


    BucketGridPanel() {
        setSizeFull();
        setMargin(false);

        addComponent(layoutHeader());
        addComponent(grid);
    }
    
   private Component layoutHeader() {
        HorizontalLayout vl = new HorizontalLayout();
        vl.setMargin(false);
        vl.setWidth("100%");
        vl.addComponent(LabelManager.htmlLabel("Buckets"));
        bt_save_bucket.setWidth("80px");
        bt_load_bucket.setWidth("80px");
        vl.addComponent(bt_load_bucket);
        vl.addComponent(bt_save_bucket);
        return vl;
    }

}
