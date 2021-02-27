/*
 * Copyright 2020 Nikhil Panju.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nikhilpanju.tablespoon

import com.nikhilpanju.tablespoon.annotations.BooleanAttr
import com.nikhilpanju.tablespoon.annotations.ColorAttr
import com.nikhilpanju.tablespoon.annotations.DimensionAttr
import com.nikhilpanju.tablespoon.annotations.DrawableAttr
import com.nikhilpanju.tablespoon.annotations.FloatAttr
import com.nikhilpanju.tablespoon.annotations.IntAttr
import com.nikhilpanju.tablespoon.annotations.ResourceIdAttr
import com.nikhilpanju.tablespoon.annotations.StringAttr
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

internal class AttrValidator(private val env: ProcessingEnvironment) {

    internal fun validateElement(
        element: Element,
        annotation: Class<out Annotation?>,
    ): Boolean = when (annotation) {
        BooleanAttr::class.java -> validate(element, annotation, env, "'Boolean") {
            element.asType().kind == TypeKind.BOOLEAN
                || element.asType().toString() == DYNAMIC_BOOLEAN_PROP
        }
        ColorAttr::class.java -> validate(element, annotation, env, "'Int' or 'ColorStateList'") {
            element.asType().kind == TypeKind.INT
                || element.asType().toString() == COLOR_STATE_LIST_TYPE
                || element.asType().toString() == DYNAMIC_INT_PROP
                || element.asType().toString() == DYNAMIC_COLOR_STATE_PROP
        }
        DimensionAttr::class.java -> validate(element, annotation, env, "'Int' or 'Float'") {
            element.asType().kind == TypeKind.INT
                || element.asType().kind == TypeKind.FLOAT
                || element.asType().toString() == DYNAMIC_INT_PROP
                || element.asType().toString() == DYNAMIC_FLOAT_PROP
        }
        DrawableAttr::class.java -> validate(element, annotation, env, "'Drawable'") {
            element.asType().toString() == DRAWABLE_TYPE
                || element.asType().toString() == DYNAMIC_DRAWABLE_PROP
        }
        FloatAttr::class.java -> validate(element, annotation, env, "'Float'") {
            element.asType().kind == TypeKind.FLOAT
                || element.asType().toString() == DYNAMIC_FLOAT_PROP
        }
        IntAttr::class.java -> validate(element, annotation, env, "'Int'") {
            element.asType().kind == TypeKind.INT
                || element.asType().toString() == DYNAMIC_INT_PROP
        }
        ResourceIdAttr::class.java -> validate(element, annotation, env, "'Int'") {
            element.asType().kind == TypeKind.INT
                || element.asType().toString() == DYNAMIC_INT_PROP
        }
        StringAttr::class.java -> validate(element, annotation, env, "'String'") {
            element.asType().toString() == STRING_TYPE
                || element.asType().toString() == DYNAMIC_STRING_PROP
        }
        else -> throw IllegalStateException("Unknown annotation used")
    }

    private fun validate(
        element: Element,
        annotation: Class<out Annotation?>,
        env: ProcessingEnvironment,
        types: String,
        validateCondition: () -> Boolean,
    ): Boolean {
        if (!validateCondition()) {
            val enclosingElement = element.enclosingElement as TypeElement
            val msg = "@${annotation.simpleName} field type must be $types. " +
                "(${enclosingElement.qualifiedName}.${element.simpleName})"
            env.messager.printMessage(Diagnostic.Kind.ERROR, msg, element)
            return false
        }
        return true
    }

    internal fun verifyCommonRestrictions(element: Element, annotationClass: Class<out Annotation?>): Boolean {
        return !isInaccessibleViaGeneratedCode(annotationClass, element)
            && !isBindingInWrongPackage(annotationClass, element)
    }

    private fun isInaccessibleViaGeneratedCode(
        annotationClass: Class<out Annotation?>,
        element: Element,
    ): Boolean {
        var hasError = false
        val enclosingElement = element.enclosingElement as TypeElement

        // Verify field or method modifiers.
        val modifiers = element.modifiers
        //TODO
        if (/*modifiers.contains(Modifier.PRIVATE) ||*/ modifiers.contains(Modifier.STATIC)) {
            env.error(
                element,
                "@${annotationClass.simpleName} fields must not be private or static. " +
                    "(${enclosingElement.qualifiedName}.${element.simpleName})"
            )
            hasError = true
        }

        // Verify containing type.
        if (enclosingElement.kind != ElementKind.CLASS) {
            env.error(
                element,
                "@${annotationClass.simpleName} fields may only be contained in classes. " +
                    "(${enclosingElement.qualifiedName}.${element.simpleName})"
            )
            hasError = true
        }

        // Verify containing class visibility is not private.
        if (enclosingElement.modifiers.contains(Modifier.PRIVATE)) {
            env.error(
                element,
                "@${annotationClass.simpleName} fields may not be contained in private classes. " +
                    "(${enclosingElement.qualifiedName}.${element.simpleName})"
            )
            hasError = true
        }
        return hasError
    }

    private fun isBindingInWrongPackage(
        annotationClass: Class<out Annotation?>,
        element: Element,
    ): Boolean {
        val enclosingElement = element.enclosingElement as TypeElement
        val qualifiedName = enclosingElement.qualifiedName.toString()

        if (qualifiedName.startsWith("android.")) {
            env.error(
                element,
                "@${annotationClass.simpleName}-annotated class incorrectly " +
                    "in Android framework package. ($qualifiedName)"
            )
            return true
        }
        if (qualifiedName.startsWith("java.")) {
            env.error(
                element,
                "@${annotationClass.simpleName}-annotated class incorrectly " +
                    "in Java framework package. ($qualifiedName)"
            )
            return true
        }
        return false
    }
}
