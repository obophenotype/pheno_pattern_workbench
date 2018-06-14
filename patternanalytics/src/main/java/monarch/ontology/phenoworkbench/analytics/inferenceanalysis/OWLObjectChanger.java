package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

/* This file is part of the OWL API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright 2014, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public class OWLObjectChanger implements OWLObjectVisitorEx<OWLObject> {

    protected final OWLDataFactory df;

    /**
     * Creates an object duplicator that duplicates objects using the specified
     * data factory.
     *
     * @param dataFactory
     *        The data factory to be used for the duplication.
     */
    public OWLObjectChanger(OWLDataFactory dataFactory) {
        df = dataFactory;
    }

    private <T extends OWLObject> T duplicate(T object) {
        return (T) object.accept(this);
    }

    @Override
    public OWLAxiom visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return df.getOWLAsymmetricObjectPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLClassAssertionAxiom axiom) {
        return df.getOWLClassAssertionAxiom(
                duplicate(axiom.getClassExpression()),
                duplicate(axiom.getIndividual()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDataPropertyAssertionAxiom axiom) {
        return df.getOWLDataPropertyAssertionAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getSubject()),
                duplicate(axiom.getObject()), duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDataPropertyDomainAxiom axiom) {
        return df.getOWLDataPropertyDomainAxiom(duplicate(axiom.getProperty()),
                duplicate(axiom.getDomain()), duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDataPropertyRangeAxiom axiom) {
        return df.getOWLDataPropertyRangeAxiom(duplicate(axiom.getProperty()),
                duplicate(axiom.getRange()), duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLSubDataPropertyOfAxiom axiom) {
        return df.getOWLSubDataPropertyOfAxiom(
                duplicate(axiom.getSubProperty()),
                duplicate(axiom.getSuperProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDeclarationAxiom axiom) {
        return df.getOWLDeclarationAxiom(duplicate(axiom.getEntity()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDifferentIndividualsAxiom axiom) {
        Set<OWLIndividual> inds = duplicateSet(axiom.getIndividuals());
        return df.getOWLDifferentIndividualsAxiom(inds,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDisjointClassesAxiom axiom) {
        Set<OWLClassExpression> descs = duplicateSet(axiom
                .getClassExpressions());
        return df.getOWLDisjointClassesAxiom(descs,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDisjointDataPropertiesAxiom axiom) {
        Set<OWLDataPropertyExpression> props = duplicateSet(axiom
                .getProperties());
        return df.getOWLDisjointDataPropertiesAxiom(props,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDisjointObjectPropertiesAxiom axiom) {
        Set<OWLObjectPropertyExpression> props = duplicateSet(axiom
                .getProperties());
        return df.getOWLDisjointObjectPropertiesAxiom(props,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLDisjointUnionAxiom axiom) {
        Set<OWLClassExpression> ops = duplicateSet(axiom.getClassExpressions());
        return df.getOWLDisjointUnionAxiom(duplicate(axiom.getOWLClass()), ops,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLAnnotationAssertionAxiom axiom) {
        return df.getOWLAnnotationAssertionAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getSubject()),
                duplicate(axiom.getValue()), duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {
        Set<OWLClassExpression> descs = duplicateSet(axiom
                .getClassExpressions());
        return df.getOWLEquivalentClassesAxiom(descs,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLEquivalentDataPropertiesAxiom axiom) {
        Set<OWLDataPropertyExpression> props = duplicateSet(axiom
                .getProperties());
        return df.getOWLEquivalentDataPropertiesAxiom(props,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        Set<OWLObjectPropertyExpression> props = duplicateSet(axiom
                .getProperties());
        return df.getOWLEquivalentObjectPropertiesAxiom(props,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLFunctionalDataPropertyAxiom axiom) {
        return df.getOWLFunctionalDataPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return df.getOWLFunctionalObjectPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return df.getOWLInverseFunctionalObjectPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLInverseObjectPropertiesAxiom axiom) {
        return df.getOWLInverseObjectPropertiesAxiom(
                duplicate(axiom.getFirstProperty()),
                duplicate(axiom.getSecondProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return df.getOWLIrreflexiveObjectPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        return df.getOWLNegativeDataPropertyAssertionAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getSubject()),
                duplicate(axiom.getObject()), duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return df.getOWLNegativeObjectPropertyAssertionAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getSubject()),
                duplicate(axiom.getObject()), duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {
        return df.getOWLObjectPropertyAssertionAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getSubject()),
                duplicate(axiom.getObject()), duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLSubPropertyChainOfAxiom axiom) {
        List<OWLObjectPropertyExpression> chain = new ArrayList<>();
        for (OWLObjectPropertyExpression p : axiom.getPropertyChain()) {
            chain.add(duplicate(p));
        }
        return df.getOWLSubPropertyChainOfAxiom(chain,
                duplicate(axiom.getSuperProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {
        return df.getOWLObjectPropertyDomainAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getDomain()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {
        return df.getOWLObjectPropertyRangeAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getRange()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLSubObjectPropertyOfAxiom axiom) {
        return df.getOWLSubObjectPropertyOfAxiom(
                duplicate(axiom.getSubProperty()),
                duplicate(axiom.getSuperProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return df.getOWLReflexiveObjectPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLSameIndividualAxiom axiom) {
        Set<OWLIndividual> individuals = duplicateSet(axiom.getIndividuals());
        return df.getOWLSameIndividualAxiom(individuals,
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLSubClassOfAxiom axiom) {
        return df.getOWLSubClassOfAxiom(duplicate(axiom.getSubClass()),
                duplicate(axiom.getSuperClass()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return df.getOWLSymmetricObjectPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAxiom visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return df.getOWLTransitiveObjectPropertyAxiom(
                duplicate(axiom.getProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLClassExpression visit(OWLClass ce) {
        return df.getOWLClass(getIRI(ce.getIRI()));
    }

    @Override
    public OWLClassExpression visit(OWLDataAllValuesFrom ce) {
        return df.getOWLDataAllValuesFrom(duplicate(ce.getProperty()),
                duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLDataExactCardinality ce) {
        return df.getOWLDataExactCardinality(ce.getCardinality(),
                duplicate(ce.getProperty()), duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLDataMaxCardinality ce) {
        return df.getOWLDataMaxCardinality(ce.getCardinality(),
                duplicate(ce.getProperty()), duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLDataMinCardinality ce) {
        return df.getOWLDataMinCardinality(ce.getCardinality(),
                duplicate(ce.getProperty()), duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLDataSomeValuesFrom ce) {
        return df.getOWLDataSomeValuesFrom(duplicate(ce.getProperty()),
                duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLDataHasValue ce) {
        return df.getOWLDataHasValue(duplicate(ce.getProperty()),
                duplicate(ce.getValue()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectAllValuesFrom ce) {
        return df.getOWLObjectAllValuesFrom(duplicate(ce.getProperty()),
                duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectComplementOf ce) {
        return df.getOWLObjectComplementOf(duplicate(ce.getOperand()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectExactCardinality ce) {
        return df.getOWLObjectExactCardinality(ce.getCardinality(),
                duplicate(ce.getProperty()), duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectIntersectionOf ce) {
        Set<OWLClassExpression> ops = duplicateSet(ce.getOperands());
        return df.getOWLObjectIntersectionOf(ops);
    }

    @Override
    public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
        return df.getOWLObjectMaxCardinality(ce.getCardinality(),
                duplicate(ce.getProperty()), duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectMinCardinality ce) {
        return df.getOWLObjectMinCardinality(ce.getCardinality(),
                duplicate(ce.getProperty()), duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectOneOf ce) {
        Set<OWLIndividual> inds = duplicateSet(ce.getIndividuals());
        return df.getOWLObjectOneOf(inds);
    }

    @Override
    public OWLClassExpression visit(OWLObjectHasSelf ce) {
        return df.getOWLObjectHasSelf(duplicate(ce.getProperty()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectSomeValuesFrom ce) {
        return df.getOWLObjectSomeValuesFrom(duplicate(ce.getProperty()),
                duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectUnionOf ce) {
        Set<OWLClassExpression> ops = duplicateSet(ce.getOperands());
        return df.getOWLObjectUnionOf(ops);
    }

    @Override
    public OWLClassExpression visit(OWLObjectHasValue ce) {
        return df.getOWLObjectHasValue(duplicate(ce.getProperty()),
                duplicate(ce.getValue()));
    }

    @Override
    public OWLDataRange visit(OWLDataComplementOf node) {
        return df.getOWLDataComplementOf(duplicate(node.getDataRange()));
    }

    @Override
    public OWLDataRange visit(OWLDataOneOf node) {
        Set<OWLLiteral> vals = duplicateSet(node.getValues());
        return df.getOWLDataOneOf(vals);
    }

    @Override
    public OWLDataRange visit(OWLDatatype node) {
        return df.getOWLDatatype(getIRI(node.getIRI()));
    }

    @Override
    public OWLDataRange visit(OWLDatatypeRestriction node) {
        Set<OWLFacetRestriction> restrictions = new HashSet<>();
        for (OWLFacetRestriction restriction : node.getFacetRestrictions()) {
            restrictions.add((OWLFacetRestriction) duplicate(restriction
                    .accept(this)));
        }
        return df.getOWLDatatypeRestriction(duplicate(node.getDatatype()),
                restrictions);
    }

    @Override
    public OWLFacetRestriction visit(OWLFacetRestriction node) {
        return df.getOWLFacetRestriction(node.getFacet(),
                duplicate(node.getFacetValue()));
    }

    @Override
    public OWLLiteral visit(OWLLiteral node) {
        if (node.hasLang()) {
            return df.getOWLLiteral(node.getLiteral(), node.getLang());
        } else {
            return df.getOWLLiteral(node.getLiteral(),
                    duplicate(node.getDatatype()));
        }
    }

    @Override
    public OWLDataProperty visit(OWLDataProperty property) {
        return df.getOWLDataProperty(getIRI(property.getIRI()));
    }

    @Override
    public OWLObjectProperty visit(OWLObjectProperty property) {
        return df.getOWLObjectProperty(getIRI(property.getIRI()));
    }

    @Override
    public OWLObjectPropertyExpression visit(OWLObjectInverseOf property) {
        return df.getOWLObjectInverseOf(duplicate(property.getInverse()));
    }

    @Override
    public OWLNamedIndividual visit(OWLNamedIndividual individual) {
        return df.getOWLNamedIndividual(getIRI(individual.getIRI()));
    }

    @Override
    public OWLOntology visit(OWLOntology ontology) {
        // Should we duplicate ontologies here? Probably not.
        return ontology;
    }

    @Override
    public SWRLRule visit(SWRLRule rule) {
        Set<SWRLAtom> antecedents = new HashSet<>();
        Set<SWRLAtom> consequents = new HashSet<>();
        for (SWRLAtom atom : rule.getBody()) {
            antecedents.add((SWRLAtom) duplicate(atom.accept(this)));
        }
        for (SWRLAtom atom : rule.getHead()) {
            consequents.add((SWRLAtom) duplicate(atom.accept(this)));
        }
        return df.getSWRLRule(antecedents, consequents);
    }

    @Override
    public SWRLClassAtom visit(SWRLClassAtom node) {
        return df.getSWRLClassAtom(duplicate(node.getPredicate()),
                duplicate(node.getArgument()));
    }

    @Override
    public SWRLDataRangeAtom visit(SWRLDataRangeAtom node) {
        return df.getSWRLDataRangeAtom(duplicate(node.getPredicate()),
                duplicate(node.getArgument()));
    }

    @Override
    public SWRLObjectPropertyAtom visit(SWRLObjectPropertyAtom node) {
        return df.getSWRLObjectPropertyAtom(duplicate(node.getPredicate()),
                duplicate(node.getFirstArgument()),
                duplicate(node.getSecondArgument()));
    }

    @Override
    public SWRLDataPropertyAtom visit(SWRLDataPropertyAtom node) {
        return df.getSWRLDataPropertyAtom(duplicate(node.getPredicate()),
                duplicate(node.getFirstArgument()),
                duplicate(node.getSecondArgument()));
    }

    @Override
    public SWRLBuiltInAtom visit(SWRLBuiltInAtom node) {
        List<SWRLDArgument> atomObjects = new ArrayList<>();
        for (SWRLDArgument atomObject : node.getArguments()) {
            atomObjects.add((SWRLDArgument) duplicate(atomObject.accept(this)));
        }
        return df.getSWRLBuiltInAtom(node.getPredicate(), atomObjects);
    }

    @Override
    public SWRLDifferentIndividualsAtom
    visit(SWRLDifferentIndividualsAtom node) {
        return df.getSWRLDifferentIndividualsAtom(
                duplicate(node.getFirstArgument()),
                duplicate(node.getSecondArgument()));
    }

    @Override
    public SWRLSameIndividualAtom visit(SWRLSameIndividualAtom node) {
        return df.getSWRLSameIndividualAtom(duplicate(node.getFirstArgument()),
                duplicate(node.getSecondArgument()));
    }

    @Override
    public SWRLVariable visit(SWRLVariable node) {
        return df.getSWRLVariable(getIRI(node.getIRI()));
    }

    @Override
    public SWRLIndividualArgument visit(SWRLIndividualArgument node) {
        return df.getSWRLIndividualArgument(duplicate(node.getIndividual()));
    }

    @Override
    public SWRLLiteralArgument visit(SWRLLiteralArgument node) {
        return df.getSWRLLiteralArgument(duplicate(node.getLiteral()));
    }

    @Override
    public OWLHasKeyAxiom visit(OWLHasKeyAxiom axiom) {
        Set<OWLPropertyExpression> props = duplicateSet(axiom
                .getPropertyExpressions());
        return df.getOWLHasKeyAxiom(duplicate(axiom.getClassExpression()),
                props, duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLDataIntersectionOf visit(OWLDataIntersectionOf node) {
        Set<OWLDataRange> ranges = duplicateSet(node.getOperands());
        return df.getOWLDataIntersectionOf(ranges);
    }

    @Override
    public OWLDataUnionOf visit(OWLDataUnionOf node) {
        Set<OWLDataRange> ranges = duplicateSet(node.getOperands());
        return df.getOWLDataUnionOf(ranges);
    }

    @Override
    public OWLAnnotationProperty visit(OWLAnnotationProperty property) {
        return df.getOWLAnnotationProperty(getIRI(property.getIRI()));
    }

    @Override
    public OWLAnnotationPropertyDomainAxiom visit(
            OWLAnnotationPropertyDomainAxiom axiom) {
        return df.getOWLAnnotationPropertyDomainAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getDomain()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAnnotationPropertyRangeAxiom visit(
            OWLAnnotationPropertyRangeAxiom axiom) {
        return df.getOWLAnnotationPropertyRangeAxiom(
                duplicate(axiom.getProperty()), duplicate(axiom.getRange()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLSubAnnotationPropertyOfAxiom visit(
            OWLSubAnnotationPropertyOfAxiom axiom) {
        return df.getOWLSubAnnotationPropertyOfAxiom(
                duplicate(axiom.getSubProperty()),
                duplicate(axiom.getSuperProperty()),
                duplicateAxiomAnnotations(axiom));
    }

    @Override
    public OWLAnnotation visit(OWLAnnotation node) {
        return df.getOWLAnnotation(duplicate(node.getProperty()),
                duplicate(node.getValue()));
    }

    @Override
    public OWLAnonymousIndividual visit(OWLAnonymousIndividual individual) {
        return individual;
    }

    @Override
    public IRI visit(IRI iri) {
        return getIRI(iri);
    }

    @Override
    public OWLDatatypeDefinitionAxiom visit(OWLDatatypeDefinitionAxiom axiom) {
        return df.getOWLDatatypeDefinitionAxiom(duplicate(axiom.getDatatype()),
                duplicate(axiom.getDataRange()),
                duplicateAxiomAnnotations(axiom));
    }

    /**
     * A utility function that duplicates a set of objects.
     *
     * @param objects
     *        The set of object to be duplicated
     * @return The set of duplicated objects
     */
    @SuppressWarnings("unchecked")
    protected <O extends OWLObject> Set<O> duplicateSet(Set<O> objects) {
        Set<O> dup = new HashSet<>();
        for (O o : objects) {
            dup.add((O) o.accept(this));
        }
        return dup;
    }

    protected Set<OWLAnnotation> duplicateAxiomAnnotations(OWLAxiom ax) {
        return duplicateSet(ax.getAnnotations());
    }

    protected IRI getIRI(IRI i) {
        return i;
    }
}
