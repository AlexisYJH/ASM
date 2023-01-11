package com.example.asminject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author AlexisYin
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ASMTest {
}
