package org.mina_lang.main;

import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

public non-sealed interface ParallelPhase<A, B> extends Phase<B> {
    ParallelFlux<A> inputFlux();

    void consumeInput(A inputNode);

    default Mono<Void> runPhase() {
        return inputFlux()
                .doOnNext(this::consumeInput)
                .then();
    }
}
