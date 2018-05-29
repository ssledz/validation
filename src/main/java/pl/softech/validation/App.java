package pl.softech.validation;

import pl.softech.validation.Validator.Property;

import java.util.List;

import static pl.softech.validation.Validation.error;
import static pl.softech.validation.Validation.success;

/**
 * Hello world!
 */
public class App {

    public static class Email {
        private final String address;

        public Email(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Email{");
            sb.append("address='").append(address).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public static class User {

        private final String firstName;
        private final String lastName;
        private final Email email;

        private User(String firstName, String lastName, Email email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("User{");
            sb.append("firstName='").append(firstName).append('\'');
            sb.append(", lastName='").append(lastName).append('\'');
            sb.append(", email=").append(email);
            sb.append('}');
            return sb.toString();
        }

        public static UserBuilder builder() {
            return new UserBuilder();
        }

        public static class UserBuilder {

            private String firstName;
            private String lastName;
            private Email email;

            public UserBuilder firstName(String firstName) {
                this.firstName = firstName;
                return this;
            }

            public UserBuilder lastName(String lastName) {
                this.lastName = lastName;
                return this;
            }

            public UserBuilder email(Email email) {
                this.email = email;
                return this;
            }

            public User build() {
                return new User(firstName, lastName, email);
            }
        }

    }

    static <In> Converter<In, In, String> notNull() {
        return (value, name) -> value == null ? error(String.format("%s should not be null", name)) : success(value);
    }

    static Converter<String, String, String> strNotNull() {
        return notNull();
    }

    static Converter<String, String, String> maxLength(int maxLen) {
        return (str, name) -> str.length() > maxLen ?
                error(String.format("%s is too long (len(value) < %d)", name, maxLen)) : success(str);
    }

    static Converter<String, Email, String> email() {
        return (str, name) -> str != null && str.split("@").length == 2 ? success(new Email(str)) :
                error(String.format("%s: %s is not a valid email", name, str));
    }

    public static void main(String[] args) {

        System.out.println(success(2).map(i -> i + 1));
        System.out.println(error("Invalid value"));
        System.out.println(success(2).compose(error("Invalid value"), (i, j) -> i + j));
        System.out.println(success(2).compose(success(3), (i, j) -> i + j));
        System.out.println(success(2).compose(success(3), (i, j) -> i + j).filter(i -> i > 5, () -> "Invalid value"));

        User.UserBuilder builder = User.builder();

        Validation<List<String>, User.UserBuilder> validation = Validator.validate(builder,
                Property.of(b -> b::firstName, "firstName", "Slavik", strNotNull().andThen(maxLength(2))),
                Property.of(b -> b::lastName, "lastName", null, strNotNull())
        );
        System.out.println(validation);

        validation = Validator.validate(builder,
                Property.of(b -> b::firstName, "firstName", "Slavik", strNotNull().andThen(maxLength(10))),
                Property.of(b -> b::lastName, "lastName", "XXX", strNotNull()),
                Property.of(b -> b::email, "email", "user@wp.pl", email())
        );

        System.out.println(validation.toOptional().map(User.UserBuilder::build));

    }
}
