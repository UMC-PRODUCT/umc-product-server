package com.umc.product.global.cache.domain;

import java.util.function.Function;

public sealed interface CacheLookup<T> permits CacheLookup.Hit, CacheLookup.Miss {

    boolean hit();

    default <R> CacheLookup<R> map(Function<T, R> mapper) {
        if (this instanceof Hit<T> hit) {
            return new Hit<>(mapper.apply(hit.value()));
        }
        return new Miss<>();
    }

    record Hit<T>(T value) implements CacheLookup<T> {
        @Override
        public boolean hit() {
            return true;
        }
    }

    record Miss<T>() implements CacheLookup<T> {
        @Override
        public boolean hit() {
            return false;
        }
    }
}
