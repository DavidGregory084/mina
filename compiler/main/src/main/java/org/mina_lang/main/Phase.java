/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import reactor.core.publisher.Mono;


public sealed interface Phase<A> permits GraphPhase, ParallelPhase {
    Mono<A> runPhase();
    A transformedData() throws Exception;
}
