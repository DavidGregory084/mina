/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

import java.io.IOException;

public non-sealed interface ParallelPhase<A, B> extends Phase<B> {
    ParallelFlux<A> inputFlux();

    void consumeInput(A inputNode) throws IOException;

    default Mono<B> runPhase() {
        return inputFlux()
                .flatMap(input  -> {
                    try {
                        consumeInput(input);
                        return Mono.empty();
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                })
                .then()
                .then(Mono.defer(() -> {
                    // Mono is not allowed to contain null,
                    // so this is the only way to deal with Mono<Void>
                    return transformedData() == null
                            ? Mono.empty()
                            : Mono.just(transformedData());
                }));
    }
}
