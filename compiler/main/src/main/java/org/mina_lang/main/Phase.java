/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import reactor.core.publisher.Mono;

import java.util.function.Function;

public sealed interface Phase<A> permits GraphPhase, ParallelPhase {
    Mono<A> runPhase();
    A transformedData();

    static <A, B> Mono<B> andThen(Mono<? extends Phase<A>> phaseMono, Function<A, B> nextPhaseFn) {
        return phaseMono.flatMap(phase -> Phase.andThen(phase, nextPhaseFn));
    }

    static <A, B> Mono<B> andThen(Phase<A> phase, Function<A, B> nextPhaseFn) {
        return phase.runPhase().map(nextPhaseFn::apply);
    }

    static <A> Mono<A> runMono(Mono<? extends Phase<A>> phaseMono) {
        return phaseMono.flatMap(Phase::runPhase);
    }
}
