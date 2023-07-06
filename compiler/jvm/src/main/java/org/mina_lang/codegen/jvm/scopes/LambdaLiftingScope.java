/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import java.util.concurrent.atomic.AtomicInteger;

public interface LambdaLiftingScope extends JavaMethodScope {
    AtomicInteger nextLambdaId();
}
