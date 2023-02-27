package org.mina_lang.main;

import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

public non-sealed interface ParallelPhase<A, B> extends Phase<B> {
    ParallelFlux<A> inputFlux();

    void consumeInput(A inputNode);

    default Mono<B> runPhase() {
        return inputFlux()
                .doOnNext(this::consumeInput)
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
