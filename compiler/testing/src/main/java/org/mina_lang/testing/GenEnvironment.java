/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

public record GenEnvironment(
    Deque<GenScope> scopes,
    AtomicInteger localVarIndex
) implements Environment<Attributes, GenScope> {
    public GenEnvironment() {
        this(new ArrayDeque<>(), new AtomicInteger(0));
        scopes.add(new GenScope());
    }
    public GenEnvironment withScope(GenScope scope) {
        var newScopes = new ArrayDeque<GenScope>();
        newScopes.push(scope);
        return new GenEnvironment(newScopes, localVarIndex);
    }
}
