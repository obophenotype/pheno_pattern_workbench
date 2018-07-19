package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.data.HasValue;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.*;
import monarch.ontology.phenoworkbench.browser.basic.LayoutUtils;
import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.util.PatternClass;
import monarch.ontology.phenoworkbench.util.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.UberOntology;

public class ReconciliationInfoBox extends HorizontalLayout {

    /**
     *
     */
    private static final long serialVersionUID = 2254864769616615100L;
    private final OntologyClassInfoBox b1 = new OntologyClassInfoBox();
    private final OntologyClassInfoBox b2 = new OntologyClassInfoBox();
    private final PatternReconciliationCandidate prc;
    private final TowardsDefinitionTransformation towardsTransform = new TowardsDefinitionTransformation(UberOntology.instance().getRender(),UberOntology.instance().getPatoterms(),UberOntology.instance().getAllClassesInBranches(true));
    private final PhenotypeDefinitionTransformer hasPartTransform = new HasPartToPhenotypeDefinitionTransformer(UberOntology.instance().getRender(),UberOntology.instance().getPatoterms(),UberOntology.instance().getAllClassesInBranches(true));
    private final PhenotypeDefinitionTransformer hasQualityTransform = new HasQualityDefinitionTransformation(UberOntology.instance().getRender(),UberOntology.instance().getPatoterms(),UberOntology.instance().getAllClassesInBranches(true));
    //private final PhenotypeDefinitionTransformer inheresinPartofTransform = new InheresInPartOfDefinitionTransformation(UberOntology.instance().getRender(),UberOntology.instance().getBranches("pato"),UberOntology.instance().getAllClassesInBranches(true));

    public ReconciliationInfoBox(PatternReconciliationCandidate recon) {
        prc = recon;
        setWidth("100%");
        setHeightUndefined();
        setMargin(true);
        setSpacing(true);

        b1.setDefinition(recon.getP1());
        b2.setDefinition(recon.getP2());


        addComponent(LayoutUtils.preparePanel(LayoutUtils.hlNoMarginNoSpacingNoSize(b1),
                "Definiton 1"));
        addComponent(LayoutUtils.preparePanel(LayoutUtils.hlNoMarginNoSpacingNoSize(b2),
                "Definiton 2"));
        addComponent(LayoutUtils.preparePanel(LayoutUtils.hlNoMarginNoSpacingNoSize(new ReconciliationInfoControlBox(recon)),
                "Info / Controls"));
    }

    class OntologyClassInfoBox extends VerticalLayout {

        OntologyClassInfoBox() {
            setSpacing(false);
            setWidth("100%");
            setHeight("100%");
        }

        OntologyClass cl = null;

        public void setDefinition(OntologyClass c) {
        		this.cl = c;
            removeAllComponents();
            Label l = LabelManager.htmlLabel(HTMLRenderUtils.renderOntologyClass(c));
            l.setWidth("100%");
            addComponent(l);
        }

        public OntologyClass getOntologyClass() {
            return cl;
        }
    }

    class ReconciliationInfoControlBox extends VerticalLayout {
        private final ComboBox<TransformationItem> cb_apply = new ComboBox<>("Apply filter");
        private final Button bt_reset = new Button("Reset");

        ReconciliationInfoControlBox(PatternReconciliationCandidate recon) {
        	setSpacing(true);
        	setWidth("100%");
            String sb = "<div><ul>"
                    + "<li>Complexity of reconciliation: " + recon.getReconciliationComplexity() + "</li>"
                    + "<li>Logical equivalence: " + recon.isLogicallyEquivalent() + "</li>"
                    + "<li>Syntactic equivalence: " + recon.isSyntacticallyEquivalent() + "</li>"
                    + "<li>Grammatical equivalence: " + recon.isSyntacticallyEquivalent() + "</li>"
                    + "<li>Common ancestors: <ol>";
            for (OntologyClass c : recon.getCommonAncestors()) {
                sb += "<li>" + HTMLRenderUtils.renderOLSLinkout(c) + "</li>";
            }
            sb = sb + "</ol></li>"
                    + "<li>Impact: " + recon.getReconciliationEffect() + "</li>"
                    + "</ul></div>";
            Label l_info = LabelManager.htmlLabel(sb);
            addComponent(cb_apply);
            cb_apply.setWidth("100%");
            cb_apply.setItems(new TransformationItem(towardsTransform),new TransformationItem(hasPartTransform),new TransformationItem(hasQualityTransform));
            addComponent(bt_reset);
            bt_reset.addClickListener(this::resetInfoBoxes);
            cb_apply.addValueChangeListener(this::applyFilterToDefinition);
            addComponent(l_info);
        }

        private void applyFilterToDefinition(HasValue.ValueChangeEvent valueChangeEvent) {
        	System.out.println("Triggered Value Change");
            if(!cb_apply.isEmpty()) {
            	System.out.println("A: "+b1.getOntologyClass().getClass().getName()+ " " + b2.getOntologyClass().getClass().getName());
                if(b1.getOntologyClass() instanceof DefinedClass) {
                	System.out.println("B");
                    b1.setDefinition(cb_apply.getValue().transformer.transform((DefinedClass)b1.getOntologyClass()));
                }
                if(b2.getOntologyClass() instanceof DefinedClass) {
                	System.out.println("B");
                    b2.setDefinition(cb_apply.getValue().transformer.transform((DefinedClass)b2.getOntologyClass()));
                }
            }
        }


        private void resetInfoBoxes(Button.ClickEvent clickEvent) {
            b1.setDefinition(prc.getP1());
            b2.setDefinition(prc.getP2());
        }
    }

    class TransformationItem {
        PhenotypeDefinitionTransformer transformer;
        TransformationItem(PhenotypeDefinitionTransformer transformer) {
        this.transformer = transformer;
    }

        @Override
        public String toString() {
            return transformer.getClass().getSimpleName();
        }
    }

}
