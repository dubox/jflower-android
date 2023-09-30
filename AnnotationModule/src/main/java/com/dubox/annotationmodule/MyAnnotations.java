package com.dubox.annotationmodule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class MyAnnotations {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface onClick {
        int value() default 0;
    }
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface onClick2 {
        int value() default 0;
    }


}
