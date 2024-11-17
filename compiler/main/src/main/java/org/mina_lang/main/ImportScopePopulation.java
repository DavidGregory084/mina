/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import com.opencastsoftware.prettier4j.Doc;
import com.opencastsoftware.yvette.Range;
import org.eclipse.collections.impl.tuple.Tuples;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.TopLevelScope;
import org.mina_lang.common.diagnostics.NamespaceDiagnosticReporter;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.syntax.ImportQualifiedNode;
import org.mina_lang.syntax.ImportSymbolsNode;
import org.mina_lang.syntax.NamespaceNode;

import java.util.Optional;

public interface ImportScopePopulation<A, B extends Scope<A>> {
    Optional<NamespaceNode<A>> getNamespaceNode(NamespaceName namespaceName);

    NamespaceDiagnosticReporter getNamespaceDiagnostics(NamespaceName namespaceName);

    Optional<Scope<Attributes>> getClasspathScope(NamespaceName namespaceName);

    Named getName(Meta<A> meta);

    Meta<A> transformMeta(Meta<Attributes> meta);

    private void namespaceNotFound(Range range, NamespaceName namespace, NamespaceName importedNamespace) {
        var message = Doc.text("The namespace")
            .appendSpace(
                Doc.text("'")
                    .append(Doc.text(importedNamespace.canonicalName()))
                    .append(Doc.text("'")))
            .appendSpace(Doc.text("could not be found."))
            .render(80);

        getNamespaceDiagnostics(namespace).reportError(range, message);
    }

    private void symbolNotFound(Range range, NamespaceName namespace, NamespaceName importedNamespace, String symbol) {
        var message = Doc.text("The symbol")
            .appendSpace(
                Doc.text("'")
                    .append(Doc.text(symbol))
                    .append(Doc.text("'")))
            .appendSpace(Doc.text("could not be found in namespace"))
            .appendSpace(
                Doc.text("'")
                    .append(Doc.text(importedNamespace.canonicalName()))
                    .append(Doc.text("'.")))
            .render(80);

        getNamespaceDiagnostics(namespace).reportError(range, message);
    }

    default Optional<B> populateImportScope(NamespaceNode<?> namespaceNode, B scope) {
        var nsName = namespaceNode.getName();

        for (var importNode : namespaceNode.imports()) {
            var importedNamespace = importNode.namespace().getName();

            var importedScope = getNamespaceNode(importedNamespace).map(NamespaceNode::getScope)
                .or(() -> getClasspathScope(importedNamespace).map(this::transformScope))
                .orElse(null);

            if (importedScope == null) {
                namespaceNotFound(
                    importNode.namespace().range(),
                    nsName, importedNamespace);
                continue;
            }

            if (importNode instanceof ImportQualifiedNode qualifiedNode) {
                qualifiedNode.alias().ifPresentOrElse(alias -> {
                    populateQualifiedImport(importedScope, scope, alias);
                }, () -> {
                    populateQualifiedImport(importedScope, scope, qualifiedNode.namespace().ns());
                });
            } else if (importNode instanceof ImportSymbolsNode symbolsNode) {
                for (var importedSymbol : symbolsNode.symbols()) {
                    importedSymbol.alias().ifPresentOrElse(alias -> {
                        populateImportSymbol(
                            importedSymbol.range(),
                            nsName, importedNamespace,
                            importedScope, scope,
                            importedSymbol.symbol(), alias);
                    }, () -> {
                        populateImportSymbol(
                            importedSymbol.range(),
                            nsName, importedNamespace,
                            importedScope, scope,
                            importedSymbol.symbol(), importedSymbol.symbol());
                    });
                }
            }
        }

        var nsDiagnostics = getNamespaceDiagnostics(nsName);

        if (nsDiagnostics.hasErrors()) {
            return Optional.empty();
        } else {
            return Optional.of(scope);
        }
    }

    default Scope<A> transformScope(Scope<Attributes> scope) {
        var values = scope.values().collect((key, value) -> Tuples.pair(key, transformMeta(value)));
        var types = scope.types().collect((key, value) -> Tuples.pair(key, transformMeta(value)));
        var fields = scope.fields().collect((constr, constrFields) -> {
            var transformedFields = constrFields.collect((key, value) -> Tuples.pair(key, transformMeta(value)));
            return Tuples.pair(constr, transformedFields);
        });
        return new TopLevelScope<>(values, types, fields);
    }

    private void populateQualifiedImport(Scope<A> namespaceScope, B importScope, String namespaceAlias) {
        namespaceScope.types().forEach(meta -> {
            var name = getName(meta);
            importScope.putTypeIfAbsent(namespaceAlias + "." + name.localName(), meta);
        });
        namespaceScope.values().forEach(meta -> {
            var name = getName(meta);
            importScope.putValueIfAbsent(namespaceAlias + "." + name.localName(), meta);
            if (name instanceof ConstructorName constrName) {
                addConstructorFields(namespaceScope, importScope, constrName);
            }
        });
    }

    private void populateImportSymbol(
        Range range,
        NamespaceName namespace,
        NamespaceName importedNamespace,
        Scope<A> namespaceScope, B importScope,
        String symbol, String alias) {
        var type = namespaceScope.lookupType(symbol);
        var value = namespaceScope.lookupValue(symbol);

        type.ifPresent(meta -> {
            importScope.putTypeIfAbsent(alias, meta);
        });

        value.ifPresent(meta -> {
            importScope.putValueIfAbsent(alias, meta);
            if (getName(meta) instanceof ConstructorName constrName) {
                addConstructorFields(namespaceScope, importScope, constrName);
            }
        });

        if (type.isEmpty() && value.isEmpty()) {
            symbolNotFound(range, namespace, importedNamespace, symbol);
        }
    }

    private void addConstructorFields(Scope<A> namespaceScope, B importScope, ConstructorName constr) {
        var fields = namespaceScope.fields().get(constr);
        if (fields != null) {
            fields.forEach((fieldName, meta) -> {
                importScope.putFieldIfAbsent(constr, fieldName, meta);
            });
        }
    }
}
