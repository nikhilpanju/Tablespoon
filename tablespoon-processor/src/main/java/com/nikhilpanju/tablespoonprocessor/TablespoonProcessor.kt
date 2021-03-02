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

package com.nikhilpanju.tablespoonprocessor

import com.google.auto.common.SuperficialValidation
import com.nikhilpanju.tablespoon.annotations.BooleanAttr
import com.nikhilpanju.tablespoon.annotations.ColorAttr
import com.nikhilpanju.tablespoon.annotations.DimensionAttr
import com.nikhilpanju.tablespoon.annotations.DrawableAttr
import com.nikhilpanju.tablespoon.annotations.FloatAttr
import com.nikhilpanju.tablespoon.annotations.IntAttr
import com.nikhilpanju.tablespoon.annotations.ResourceIdAttr
import com.nikhilpanju.tablespoon.annotations.StringAttr
import com.squareup.kotlinpoet.FileSpec
import com.sun.source.util.Trees
import com.sun.tools.javac.tree.JCTree
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

internal class TablespoonProcessor : AbstractProcessor() {
    private val supportedAnnotations = mutableSetOf(
        BooleanAttr::class.java.canonicalName,
        ColorAttr::class.java.canonicalName,
        DimensionAttr::class.java.canonicalName,
        DrawableAttr::class.java.canonicalName,
        FloatAttr::class.java.canonicalName,
        IntAttr::class.java.canonicalName,
        ResourceIdAttr::class.java.canonicalName,
        StringAttr::class.java.canonicalName,
    )
    private var trees: Trees? = null
    private val rScanner by lazy { RScanner() }
    private val attrValidator by lazy { AttrValidator(processingEnv) }

    override fun init(env: ProcessingEnvironment) {
        super.init(env)

        // Initialize trees
        try {
            trees = Trees.instance(env)
        } catch (ignored: IllegalArgumentException) {
            try {
                // Get original ProcessingEnvironment from Gradle-wrapped one or KAPT-wrapped one.
                val field = env.javaClass.declaredFields
                    .firstOrNull { it.name == "delegate" || it.name == "processingEnv" }
                    ?: return

                field.isAccessible = true
                val javacEnv = field[env] as ProcessingEnvironment
                trees = Trees.instance(javacEnv)
            } catch (ignored2: Throwable) {
            }
        }
    }

    override fun getSupportedAnnotationTypes() = supportedAnnotations

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_8

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment,
    ): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return false

        val bindingMap = parseAnnotations(roundEnv)
        bindingMap.forEach { (enclosingElement: TypeElement, attrs: MutableList<AttrBindingData>) ->
            val className = "${enclosingElement.simpleName}_AttrBinding"

            // Create a new file in the same package as the model class,
            // and name it fileName.
            val classSpec = TablespoonCodegen(className, enclosingElement.packageName, enclosingElement, attrs).build()
            FileSpec.builder(enclosingElement.packageName, className)
                .addType(classSpec)
                .build()
                .writeTo(File(kaptKotlinGeneratedDir))
        }

        return true
    }

    /**
     * Parses all elements and their enclosing classes into a map
     *
     * @return Map where key = view class which contains annotations and
     * value = list of all the annotation info for properties in that class
     *
     */
    private fun parseAnnotations(env: RoundEnvironment): MutableMap<TypeElement, MutableList<AttrBindingData>> {
        // bindingMap will hold the [class -> list of annotated attrs] information
        val bindingMap = mutableMapOf<TypeElement, MutableList<AttrBindingData>>()

        // This would be useful fo associate superclasses, etc. But not used right now
        val erasedTargetNames = mutableSetOf<TypeElement>()

        env.parseAnnotation(BooleanAttr::class.java, bindingMap, erasedTargetNames)
        env.parseAnnotation(ColorAttr::class.java, bindingMap, erasedTargetNames)
        env.parseAnnotation(DimensionAttr::class.java, bindingMap, erasedTargetNames)
        env.parseAnnotation(DrawableAttr::class.java, bindingMap, erasedTargetNames)
        env.parseAnnotation(FloatAttr::class.java, bindingMap, erasedTargetNames)
        env.parseAnnotation(IntAttr::class.java, bindingMap, erasedTargetNames)
        env.parseAnnotation(ResourceIdAttr::class.java, bindingMap, erasedTargetNames)
        env.parseAnnotation(StringAttr::class.java, bindingMap, erasedTargetNames)

        return bindingMap
    }

    private fun RoundEnvironment.parseAnnotation(
        annotation: Class<out Annotation?>,
        attrBindingMap: MutableMap<TypeElement, MutableList<AttrBindingData>>,
        erasedTargetNames: MutableSet<TypeElement>,
    ) {
        getElementsAnnotatedWith(annotation).forEach { element ->
            var success = true

            // Since we want all errors to be printed, run through all validations
            success = success && SuperficialValidation.validateElement(element)
            success = success && attrValidator.verifyCommonRestrictions(element, annotation)
            success = success && attrValidator.validateElement(element, annotation)

            if (success) {
                try {
                    // Assemble information on the field.
                    val (attrType, attrId) = getAttrTypeAndIdForAnnotation(element, annotation)
                    attrBindingMap.addElement(element, annotation, attrType, attrId)
                    erasedTargetNames.add(element.enclosingElement as TypeElement)
                } catch (e: Exception) {
                    logParsingError(element, annotation, e)
                }
            }
        }
    }

    /**
     * @return Attribute type (field type) and id (styleable res) for a given annotated element
     */
    private fun getAttrTypeAndIdForAnnotation(
        element: Element,
        annotation: Class<out Annotation?>,
    ): Pair<AttrBindingType, Int> = when (annotation) {
        BooleanAttr::class.java -> AttrBindingType.Bool to element.getAnnotation(annotation).attrId

        ColorAttr::class.java -> when {
            element.asType().toString() == COLOR_STATE_LIST_TYPE
                || element.asType().toString() == DYNAMIC_COLOR_STATE_PROP -> AttrBindingType.ColorState
            element.asType().kind == TypeKind.INT
                || element.asType().toString() == DYNAMIC_INT_PROP -> AttrBindingType.Color
            else -> throw IllegalStateException("AttrValidator must account for this. Please report bug")
        } to element.getAnnotation(annotation).attrId

        DimensionAttr::class.java -> when {
            element.asType().kind == TypeKind.INT
                || element.asType().toString() == DYNAMIC_INT_PROP -> AttrBindingType.DimenInt
            element.asType().kind == TypeKind.FLOAT
                || element.asType().toString() == DYNAMIC_FLOAT_PROP -> AttrBindingType.DimenFloat
            else -> throw IllegalStateException("AttrValidator must account for this. Please report bug")
        } to element.getAnnotation(annotation).attrId

        DrawableAttr::class.java -> AttrBindingType.Drawable to element.getAnnotation(annotation).attrId
        FloatAttr::class.java -> AttrBindingType.Float to element.getAnnotation(annotation).attrId
        IntAttr::class.java -> AttrBindingType.Int to element.getAnnotation(annotation).attrId
        ResourceIdAttr::class.java -> AttrBindingType.ResourceId to element.getAnnotation(annotation).attrId
        StringAttr::class.java -> AttrBindingType.StringData to element.getAnnotation(annotation).attrId
        else -> throw IllegalStateException("Unknown annotation used")
    }

    /**
     * Adds [AttrBindingData] to the binding map. Binding data includes [Id],
     * element name and [AttrBindingType]
     */
    private fun MutableMap<TypeElement, MutableList<AttrBindingData>>.addElement(
        element: Element,
        annotationClass: Class<out Annotation?>,
        attrType: AttrBindingType,
        attrId: Int,
    ) {
        val resourceId: Id = element.toId(annotationClass, value = attrId)

        val enclosingElement = element.enclosingElement as TypeElement
        val list = getOrPut(enclosingElement) { mutableListOf() }
        list.add(AttrBindingData(resourceId, element.simpleName.toString(), attrType))
    }

    private fun logParsingError(
        element: Element,
        annotation: Class<out Annotation?>,
        e: Exception,
    ) {
        val stackTrace = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))
        processingEnv.error(element, "Unable to parse @${annotation.simpleName} binding.\n\n$stackTrace")
    }

    /**
     * Returns an [Id] from an [Element]
     */
    private fun Element.toId(annotation: Class<out Annotation?>, value: Int): Id {
        val tree = trees?.getTree(this, getMirror(annotation)) as? JCTree
        if (tree != null) { // tree can be null if the references are compiled types and not source
            rScanner.reset()
            tree.accept(rScanner)
            if (rScanner.resourceIds.isNotEmpty()) {
                return rScanner.resourceIds.values.iterator().next()
            }
        }
        return Id(value)
    }

    private fun Element.getMirror(annotation: Class<out Annotation?>): AnnotationMirror? =
        annotationMirrors.firstOrNull { it.annotationType.toString() == annotation.canonicalName }

    private inline val Element.packageName: String
        get() = processingEnv.elementUtils.getPackageOf(this).toString()
}
