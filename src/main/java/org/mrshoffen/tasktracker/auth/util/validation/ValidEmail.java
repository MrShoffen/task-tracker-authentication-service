package org.mrshoffen.tasktracker.auth.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Size(min = 5, max = 64, message = "Длина почтового адреса должна быть между  {min} и {max} символами")
@NotBlank(message = "Почта не может быть пустой")
@Pattern(regexp = "^(?!\\.)[A-Za-z0-9+_.-]+(?<!\\.)@(?!\\.)[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$", message = "Недопустимые символы в адресе")
public @interface ValidEmail {
    String message() default "Некорректный почтовый адрес";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
