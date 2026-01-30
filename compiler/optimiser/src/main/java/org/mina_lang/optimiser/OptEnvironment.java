/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.stack.MutableStack;
import org.mina_lang.common.*;

public record OptEnvironment(MutableStack<Scope<Meta<Attributes>>> scopes) implements Environment<Meta<Attributes>, Scope<Meta<Attributes>>> {

    public static OptEnvironment empty() {
        return new OptEnvironment(Stacks.mutable.empty());
    }

    public static OptEnvironment withScope(TopLevelScope<Attributes> scope) {
        var environment = OptEnvironment.empty();
        environment.pushScope(scope);
        return environment;
    }
}
