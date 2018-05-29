package pl.softech.validation;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Validator {

    public static <Builder> Validation<List<String>, Builder> validate(Builder builder, Function<Builder, Validation<String, Builder>>... properties) {

        List<Validation<String, Builder>> result = asList(properties)
                .stream()
                .map(p -> p.apply(builder)).collect(Collectors.toList());

        List<String> errors = result.stream()
                .filter(Validation::isError)
                .map(Validation::getError)
                .collect(Collectors.toList());

        return errors.isEmpty() ? Validation.success(result.get(0).getSuccess()) : Validation.error(errors);

    }

    public static class Property<In, PropertyType, Builder> implements Function<Builder, Validation<String, Builder>> {

        private Function<Builder, Function<PropertyType, Builder>> builderProvider;
        private String name;
        private In value;
        private Converter<In, PropertyType, String> converter;

        public Property(Function<Builder, Function<PropertyType, Builder>> builderProvider, String name, In value, Converter<In, PropertyType, String> converter) {
            this.builderProvider = builderProvider;
            this.name = name;
            this.value = value;
            this.converter = converter;
        }

        public static <In, PropertyType, Builder> Property<In, PropertyType, Builder> of(
                Function<Builder, Function<PropertyType, Builder>> builderProvider,
                String name, In value, Converter<In, PropertyType, String> converter) {
            return new Property<>(builderProvider, name, value, converter);
        }

        @Override
        public Validation<String, Builder> apply(Builder builder) {
            return converter.apply(value, name).map(success -> builderProvider.apply(builder).apply(success));
        }
    }

}
