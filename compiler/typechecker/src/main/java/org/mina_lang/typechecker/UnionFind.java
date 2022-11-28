package org.mina_lang.typechecker;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.primitive.ObjectIntMaps;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;

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

    public A find(A element) {
        var elementParent = parent.getOrDefault(element, element);
        while (!element.equals(elementParent)) {
            parent.put(element, parent.getOrDefault(elementParent, elementParent));
            element = elementParent;
            elementParent = parent.getOrDefault(element, element);
        }
        return representative.get(element);
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
