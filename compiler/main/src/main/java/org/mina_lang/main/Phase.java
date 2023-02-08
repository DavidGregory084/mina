package org.mina_lang.main;

import java.util.function.Function;

import reactor.core.publisher.Mono;

public sealed interface Phase<A> permits GraphPhase, ParallelPhase {
    Mono<Void> runPhase();
    A transformedData();

    static <A, B> Mono<B> sequence(Mono<? extends Phase<A>> phaseMono, Function<A, B> nextPhaseFn) {
        return phaseMono.flatMap(phase -> {
            return Phase.sequence(phase, nextPhaseFn);
        });
    }

    static <A, B> Mono<B> sequence(Phase<A> phase, Function<A, B> nextPhaseFn) {
        return phase.runPhase().then(Mono.fromSupplier(() -> {
            return nextPhaseFn.apply(phase.transformedData());
        }));
    }

    static <A, B> Mono<B> sequenceMono(Phase<A> phase, Function<A, Mono<B>> nextPhaseFn) {
        return phase.runPhase().then(Mono.defer(() -> {
            return nextPhaseFn.apply(phase.transformedData());
        }));
    }

    static <A> Mono<Void> runMono(Mono<? extends Phase<A>> phaseMono) {
        return phaseMono.flatMap(Phase::runPhase);
    }
}
