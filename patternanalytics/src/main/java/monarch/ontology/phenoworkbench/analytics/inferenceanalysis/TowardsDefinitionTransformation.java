package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class TowardsDefinitionTransformation implements DefinitionTransformer {
OWLDataFactory df = OWLManager.getOWLDataFactory();
    OWLObjectChanger changer = new OWLTowardsObjectChanger(df);

    public TowardsDefinitionTransformation() {
    }

    @Override
    public DefinitionSet get(DefinitionSet basicDefinitions) {
        DefinitionSet out = new DefinitionSet();
        Set<DefinedClass> definedclasses = new HashSet<>();
        for(DefinedClass d:basicDefinitions.getDefinitions()) {
            DefinedClass changed = transform(d);
            definedclasses.add(changed);
            System.out.println("----------------");
            System.out.println("BEFORE: "+d);
            System.out.println("AFTER: "+changed);
            new Scanner(System.in);

        }
        out.setDefinitions(definedclasses);
        return out;
    }

    private DefinedClass transform(DefinedClass d) {
        DefinedClass tranformed = new DefinedClass(d.getOWLClass(),transformClassExpression(d.getDefiniton()));
        return tranformed;
    }


    private OWLClassExpression transformClassExpression(OWLClassExpression definiton) {
        if(definiton instanceof OWLObjectIntersectionOf) {
            return changer.visit((OWLObjectIntersectionOf)definiton);
        } else if(definiton instanceof OWLObjectSomeValuesFrom) {
            return changer.visit((OWLObjectSomeValuesFrom)definiton);
        }
        else {
            return definiton;
        }
    }
}
