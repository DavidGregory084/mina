/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import org.eclipse.collections.api.stack.MutableStack;
import org.eclipse.collections.impl.factory.Stacks;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;

import java.util.concurrent.atomic.AtomicInteger;

public record GenEnvironment(
    MutableStack<GenScope> scopes,
    AtomicInteger localVarIndex
) implements Environment<Attributes, GenScope> {
    public GenEnvironment() {
        this(Stacks.mutable.of(new GenScope()), new AtomicInteger(0));
    }
    public GenEnvironment withScope(GenScope scope) {
        var newScopes = Stacks.mutable.ofAll(scopes);
        newScopes.push(scope);
        return new GenEnvironment(newScopes, localVarIndex);
    }
}
