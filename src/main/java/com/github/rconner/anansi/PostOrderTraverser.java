/*
 * Copyright (c) 2012 Ray A. Conner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.rconner.anansi;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Package implementation of the Traverser returned by {@link Traversers#postOrder(Traverser)}.
 *
 * @param <V>
 * @param <E>
 */
final class PostOrderTraverser<V, E> implements Traverser<V, E> {
    private final Traverser<V, E> adjacency;

    PostOrderTraverser( final Traverser<V, E> adjacency ) {
        this.adjacency = adjacency;
    }

    @Override
    public Iterable<Walk<V, E>> apply( final V start ) {
        return new Iterable<Walk<V, E>>() {
            @Override
            public Iterator<Walk<V, E>> iterator() {
                return new PostOrderIterator<V, E>( start, adjacency );
            }
        };
    }

    private static final class PostOrderIterator<V, E> implements Iterator<Walk<V, E>> {
        /**
         * The supplied adjacency function.
         */
        private final Traverser<V, E> adjacency;

        /**
         * A stack of Iterators.
         */
        private final LinkedList<Iterator<Walk<V, E>>> iteratorStack = Lists.newLinkedList();

        /**
         * Collects adjacency walks and produces the compound walks to return.
         */
        private final Walk.Builder<V, E> builder;

        PostOrderIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            iteratorStack.addFirst( Iterators.singletonIterator( Walk.<V, E>empty( start ) ) );
            builder = Walk.from( start );
        }

        @Override
        public boolean hasNext() {
            return !builder.isEmpty() || iteratorStack.getFirst().hasNext();
        }

        @Override
        public Walk<V, E> next() {
            Iterator<Walk<V, E>> top = iteratorStack.getFirst();

            if( !top.hasNext() ) {
                if( builder.isEmpty() ) {
                    throw new NoSuchElementException();
                }
                iteratorStack.removeFirst();
                final Walk<V, E> result = builder.build();
                builder.pop();
                return result;
            }

            while( true ) {
                final Walk<V, E> walk = top.next();
                top = adjacency.apply( walk.getTo() ).iterator();
                builder.add( walk );
                if( !top.hasNext() ) {
                    final Walk<V, E> result = builder.build();
                    builder.pop();
                    return result;
                }
                iteratorStack.addFirst( top );
            }
        }

        @Override
        public void remove() {
            Preconditions.checkState( !iteratorStack.isEmpty() );
            iteratorStack.getFirst().remove();
        }
    }
}
