/*
 * Copyright (c) 2024. Andrea Giulianelli
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

/**
 * This class represent a triple without the subject.
 * It can be useful when we want to represent triples for an external defined subject.
 */
public final class RdfUnSubjectedTriple {
    private final RdfProperty triplePredicate;
    private final RdfNode tripleObject;

    /**
     * Default constructor.
     * @param triplePredicate the predicate of the triple.
     * @param tripleObject the object of the triple.
     */
    public RdfUnSubjectedTriple(final RdfProperty triplePredicate, final RdfNode tripleObject) {
        this.triplePredicate = triplePredicate;
        this.tripleObject = tripleObject;
    }

    /**
     * Get the triple predicate.
     * @return the predicate of the triple.
     */
    public RdfProperty getTriplePredicate() {
        return this.triplePredicate;
    }

    /**
     * Get the triple object.
     * @return the object of the triple.
     */
    public RdfNode getTripleObject() {
        return this.tripleObject;
    }
}
