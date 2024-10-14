/*
 * Copyright (c) 2023-2024. Andrea Giulianelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.webbasedwodt.model.ontology.rdf;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * It models the concept of RDF Blank Node in the context of Digital Twin Knowledge Graph.
 * A Blank Node could have an associated list of predicates.
 */
public final class RdfBlankNode implements RdfResource {
    private final String blankNodeId;
    private final List<RdfUnSubjectedTriple> tripleList;

    /**
     * Default constructor.
     * It creates a Blank Node without any triples.
     * @param blankNodeId the blank node id, used to safely update data.
     */
    public RdfBlankNode(final String blankNodeId) {
        this(blankNodeId, new ArrayList<>());
    }

    /**
     * Constructor that allows you to configure the Blank Node with existing triples.
     * @param blankNodeId the blank node id, used to safely update data.
     * @param tripleList the unsubjected triples to add
     */
    public RdfBlankNode(final String blankNodeId, final List<RdfUnSubjectedTriple> tripleList) {
        this.blankNodeId = blankNodeId;
        this.tripleList = new ArrayList<>(tripleList);
    }

    /**
     * Add a triple to the BlankNode.
     * Note that this is an immutable data structure, so it returns a new {@link RdfBlankNode}.
     * @param triple the triple to add
     * @return the modified version of the Blank Node
     */
    public RdfBlankNode addPredicate(final RdfUnSubjectedTriple triple) {
        final List<RdfUnSubjectedTriple> resultingTripleSet = new ArrayList<>(this.tripleList);
        resultingTripleSet.add(triple);
        return new RdfBlankNode(this.blankNodeId, resultingTripleSet);
    }

    /**
     * Get the blank node id.
     * @return the blank node id
     */
    public String getBlankNodeId() {
        return this.blankNodeId;
    }

    /**
     * Get the triples inside the Blank node.
     * @return the list of triples
     */
    public List<RdfUnSubjectedTriple> getPredicates() {
        return new ArrayList<>(this.tripleList);
    }

    @Override
    public Optional<URI> getUri() {
        return Optional.empty();
    }
}
