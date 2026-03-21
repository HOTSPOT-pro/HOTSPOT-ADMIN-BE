package hotspot.admin.policy.controller.request.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimePolicyRequestValidator.class)
public @interface ValidTimePolicyRequest {

    String message() default "INVALID_TIME_POLICY_REQUEST";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
