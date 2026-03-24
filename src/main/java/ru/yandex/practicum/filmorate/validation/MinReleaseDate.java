package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MinReleaseDateValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinReleaseDate {
    String message() default "Release date must not be earlier than 1895-12-28";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}