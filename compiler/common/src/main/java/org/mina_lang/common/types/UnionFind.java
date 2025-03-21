/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;

import java.util.Set;
import java.util.function.BinaryOperator;

public class UnionFind<A> {
    private final MutableMap<A, A> representative = Maps.mutable.empty();
    private final MutableMap<A, A> parent = Maps.mutable.empty();
    private final MutableObjectIntMap<A> rank = ObjectIntMaps.mutable.empty();
    private final BinaryOperator<A> chooseRep;

    UnionFind(BinaryOperator<A> chooseRep) {
        this.chooseRep = chooseRep;
    }

    public static <A> UnionFind<A> of(BinaryOperator<A> chooseRepresentative, A element) {
        var uf = new UnionFind<>(chooseRepresentative);
        uf.add(element);
        return uf;
    }

    @SafeVarargs
    public static <A> UnionFind<A> of(BinaryOperator<A> chooseRepresentative, A... elements) {
        var uf = new UnionFind<>(chooseRepresentative);
        for (var element : elements) {
            uf.add(element);
        }
        return uf;
    }

    public static <A> UnionFind<A> ofAll(BinaryOperator<A> chooseRepresentative, Iterable<? extends A> elements) {
        var uf = new UnionFind<>(chooseRepresentative);
        for (var element : elements) {
            uf.add(element);
        }
        return uf;
    }

    public Set<A> elements() {
        return parent.keySet();
    }

    public Set<A> representatives() {
        return elements().stream()
                .map(this::find)
                .collect(Collectors2.toSet());
    }

    public boolean contains(A element) {
        return parent.containsKey(element);
    }

    public void add(A element) {
        if (!contains(element)) {
            parent.put(element, element);
            rank.put(element, 1);
            representative.put(element, element);
        }
    }

    public void updateWith(Function2<? super A, ? super A, ? extends A> func) {
        representative.forEachKeyValue((k, v) -> {
            representative.updateValueWith(k, () -> v, func, k);
        });
    }

    public void remove(A element) {
        var root = root(element);

        // Remove the element from the set
        parent.remove(element);

        var children = parent.flip();

        // If it is a parent node, we need to figure out what to do with its children
        if (rank.get(element) > 1)  {
            var elementChildren = children.get(element);

            if (!elementChildren.isEmpty()) {
                // It's the root of a set
                if (root.equals(element)) {
                    // Find the child with highest rank and make it a new root
                    var newRoot = elementChildren.maxBy(rank::get);
                    elementChildren.forEach(child -> {
                        parent.put(child, newRoot);
                        if (!child.equals(newRoot)) {
                            rank.updateValue(newRoot, 1, i -> i + rank.get(child));
                        }
                    });
                } else {
                    // Assign each child directly to the existing root
                    elementChildren.forEach(child -> {
                        parent.put(child, root);
                        if (!child.equals(root)) {
                            rank.updateValue(root, 1, i -> i + rank.get(child));
                        }
                    });
                }
            }
        }

        rank.remove(element);

        // If it is a representative of the set, choose a new rep
        if (element.equals(representative.get(root))) {
            representative.put(
                root,
                children.get(root).reduce(chooseRep).orElse(root));
        }
    }

    public A find(A element) {
        var elementParent = parent.getOrDefault(element, element);
        while (!element.equals(elementParent)) {
            var elementGrandparent = parent.getOrDefault(elementParent, elementParent);
            parent.put(element, elementGrandparent);
            element = elementParent;
            elementParent = parent.getOrDefault(element, element);
        }
        return representative.getOrDefault(element, element);
    }

    private A root(A element) {
        var elementParent = parent.getOrDefault(element, element);
        while (!element.equals(elementParent)) {
            element = elementParent;
            elementParent = parent.getOrDefault(element, element);
        }
        return element;
    }

    public void union(A left, A right) {
        A leftRoot = root(left);
        A rightRoot = root(right);

        if (!leftRoot.equals(rightRoot)) {
            if (rank.get(leftRoot) < rank.get(rightRoot)) {
                parent.put(leftRoot, rightRoot);
                rank.updateValue(
                        rightRoot, 1,
                        i -> i + rank.get(leftRoot));
                representative.updateValue(
                        rightRoot,
                        () -> rightRoot,
                        rightRep -> chooseRep.apply(rightRep, representative.getOrDefault(leftRoot, leftRoot)));
            } else {
                parent.put(rightRoot, leftRoot);
                rank.updateValue(
                        leftRoot, 1,
                        i -> i + rank.get(rightRoot));
                representative.updateValue(
                        leftRoot,
                        () -> leftRoot,
                        leftRep -> chooseRep.apply(leftRep, representative.getOrDefault(rightRoot, rightRoot)));
            }
        }
    }
}
