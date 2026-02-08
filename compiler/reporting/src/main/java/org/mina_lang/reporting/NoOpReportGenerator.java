/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.reporting;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.ina.Namespace;
import org.mina_lang.syntax.NamespaceNode;

public enum NoOpReportGenerator implements ReportGenerator {
    INSTANCE;

    @Override
    public void reportNamespaceGraph(Graph<NamespaceName, DefaultEdge> namespaceGraph) {
    }

    @Override
    public void reportNamespace(NamespaceNode<?> namespaceNode) {
    }

    @Override
    public void reportIntermediate(Namespace namespace) {
    }
}
