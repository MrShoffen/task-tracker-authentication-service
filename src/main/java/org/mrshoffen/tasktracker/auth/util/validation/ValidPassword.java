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
@Size(min = 5, max = 20, message = "Длина пароля должна быть между  {min} и {max} символами")
@NotBlank(message = "Пароль не должен быть пустым")
@Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?:{}|<>/`~+=-_';]*$", message = "Недопустимые символы в пароле")
public @interface ValidPassword {
    String message() default "Incorrect password";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
