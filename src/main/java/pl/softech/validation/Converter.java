package pl.softech.validation;

import java.util.function.BiFunction;

public interface Converter<In, Out, E> extends BiFunction<In, String, Validation<E, Out>> {

    default <Out2> Converter<In, Out2, E> andThen(Converter<Out, Out2, E> other) {
        return (value, name) -> apply(value, name).flatMap(out -> other.apply(out, name));
    }

}
