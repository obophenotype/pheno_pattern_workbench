package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PropertyUsage {
    String o;
    OWLObjectProperty p;
    String expressiontype;
    OWLClass object;
    OWLClass subject;

    PropertyUsage(String o, OWLObjectProperty p, String expressiontype, OWLClass object, OWLClass subject) {
        this.o = o;
        this.p = p;
        this.expressiontype = expressiontype;
        this.object = object;
        this.subject = subject;
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (!(o1 instanceof PropertyUsage)) return false;
        PropertyUsage that = (PropertyUsage) o1;
        return Objects.equals(o, that.o) &&
                Objects.equals(p, that.p) &&
                Objects.equals(expressiontype, that.expressiontype) &&
                Objects.equals(object, that.object) &&
                Objects.equals(subject, that.subject);
    }

    @Override
    public int hashCode() {

        return Objects.hash(o, p, expressiontype, object, subject);
    }

    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("o", o);
        data.put("p", p.getIRI().toString());
        data.put("expressiontype", expressiontype);
        data.put("object", object.getIRI().toString());
        data.put("subject", subject.getIRI().toString());
        return data;
    }


}
