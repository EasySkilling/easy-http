package com.easyhttp.core.annotations.methods;

import com.easyhttp.core.enums.BodyForm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Put {
    String url() default "";
    BodyForm form() default BodyForm.FORM;
    boolean urlEncode() default false;
}
