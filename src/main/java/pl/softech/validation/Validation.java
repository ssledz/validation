package pl.softech.validation;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Validation<E, S> {

    boolean isSuccess();

    default boolean isError() {
        return !isSuccess();
    }

    E getError();

    S getSuccess();

    Optional<S> toOptional();

    static <E, S> Validation<E, S> error(E error) {
        return new Error<>(error);
    }

    static <E, S> Validation<E, S> success(S success) {
        return new Success<>(success);
    }

    default <T> Validation<E, T> map(Function<S, T> mapper) {
        return flatMap(success -> success(mapper.apply(success)));
    }

    default Validation<E, S> filter(Predicate<S> predicate, Supplier<E> error) {
        if (isSuccess()) {
            return predicate.test(getSuccess()) ? this : error(error.get());
        } else {
            return this;
        }
    }

    default Validation<E, S> compose(Validation<E, S> other, BinaryOperator<S> f) {

        if (isSuccess()) {

            if (other.isSuccess()) {
                return success(f.apply(getSuccess(), other.getSuccess()));
            }

            return other;

        } else {

            return this;

        }

    }

    default <T> Validation<E, T> flatMap(Function<S, Validation<E, T>> mapper) {
        if (isSuccess()) {
            return mapper.apply(getSuccess());

        } else {
            return (Validation<E, T>) this;
        }
    }

    default Validation<E, S> orElse(Validation<E, S> other) {
        return orElseGet(() -> other);
    }

    default Validation<E, S> orElseGet(Supplier<Validation<E, S>> other) {
        if (isSuccess()) {
            return this;
        } else {
            return other.get();
        }
    }

    class Success<E, S> implements Validation<E, S> {

        private final S success;

        private Success(S success) {
            this.success = success;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public E getError() {
            throw new IllegalStateException();
        }

        @Override
        public S getSuccess() {
            return success;
        }

        @Override
        public Optional<S> toOptional() {
            return Optional.ofNullable(success);
        }

        @Override
        public String toString() {
            return new StringBuilder("Success[")
                    .append(success)
                    .append(']')
                    .toString();
        }
    }

    class Error<E, S> implements Validation<E, S> {

        private final E error;

        private Error(E error) {
            this.error = error;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public E getError() {
            return error;
        }

        @Override
        public S getSuccess() {
            throw new IllegalStateException();
        }

        @Override
        public Optional<S> toOptional() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return new StringBuilder("Error[")
                    .append(error)
                    .append(']')
                    .toString();
        }
    }

}
