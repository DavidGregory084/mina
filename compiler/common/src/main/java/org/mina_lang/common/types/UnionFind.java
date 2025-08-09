/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class UnionFind<A> {
    private final Map<A, A> representative = new HashMap<>();
    private final Map<A, A> parent = new HashMap<>();
    private final Object2IntMap<A> rank = new Object2IntOpenHashMap<>();
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
            .collect(Collectors.toSet());
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

    public void updateWith(BiFunction<? super A, ? super A, ? extends A> func) {
        representative.forEach((k, v) -> {
            representative.computeIfPresent(k, func);
        });
    }

    public void remove(A element) {
        var root = root(element);

        // Remove the element from the set
        parent.remove(element);

        var children = parent.entrySet().stream()
            .collect(Collectors.groupingBy(
                Map.Entry::getValue,
                Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        // If it is a parent node, we need to figure out what to do with its children
        if (rank.getInt(element) > 1) {
            var elementChildren = children.get(element);

            if (!elementChildren.isEmpty()) {
                // It's the root of a set
                if (root.equals(element)) {
                    // Find the child with highest rank and make it a new root
                    var newRoot = elementChildren.stream()
                        .max(Comparator.comparingInt(rank::getInt))
                        .get();
                    elementChildren.forEach(child -> {
                        parent.put(child, newRoot);
                        if (!child.equals(newRoot)) {
                            rank.computeInt(newRoot, (k, i) -> i == null ? 1 :  i + rank.getInt(child));
                        }
                    });
                } else {
                    // Assign each child directly to the existing root
                    elementChildren.forEach(child -> {
                        parent.put(child, root);
                        if (!child.equals(root)) {
                            rank.computeInt(root, (k, i) -> i == null ? 1 :  i + rank.getInt(child));
                        }
                    });
                }
            }
        }

        rank.remove(element);

        // If it is a representative of the set, choose a new rep
        if (element.equals(representative.get(root)) && children.get(root) != null) {
            representative.put(
                root,
                children.get(root).stream().reduce(chooseRep).orElse(root));
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
            if (rank.getInt(leftRoot) < rank.getInt(rightRoot)) {
                parent.put(leftRoot, rightRoot);
                rank.compute(
                    rightRoot,
                    (k, i) -> i == null ? 1 : i + rank.getInt(leftRoot));
                representative.compute(
                    rightRoot,
                    (k, rightRep) -> rightRep == null ? rightRoot : chooseRep.apply(rightRep, representative.getOrDefault(leftRoot, leftRoot)));
            } else {
                parent.put(rightRoot, leftRoot);
                rank.compute(
                    leftRoot,
                    (k, i) -> i == null ? 1 : i + rank.getInt(rightRoot));
                representative.compute(
                    leftRoot,
                    (k, leftRep) -> leftRep == null ? leftRoot : chooseRep.apply(leftRep, representative.getOrDefault(rightRoot, rightRoot)));
            }
        }
    }
}
