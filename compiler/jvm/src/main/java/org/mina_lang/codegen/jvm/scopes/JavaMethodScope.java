/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.map.ImmutableMap;
import org.mina_lang.codegen.jvm.LocalVar;
import org.mina_lang.common.names.Named;

public interface JavaMethodScope extends VarBindingScope {
    ImmutableMap<Named, LocalVar> methodParams();

    default public void visitMethodParams() {
        methodParams()
                .toSortedListBy(LocalVar::index)
                .forEach(param -> {
                    methodWriter().visitLocalVariable(
                            param.name(),
                            param.descriptor(),
                            param.signature(),
                            param.startLabel(),
                            param.endLabel(),
                            param.index());
                });
    }

    default void finaliseMethod() {
        methodWriter().returnValue();
        methodWriter().visitLabel(endLabel());
        visitMethodParams();
        methodWriter().endMethod();
    }
}
