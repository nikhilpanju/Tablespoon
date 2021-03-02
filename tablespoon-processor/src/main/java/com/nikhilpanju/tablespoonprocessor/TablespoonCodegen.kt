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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.TypeElement

internal class TablespoonCodegen(
    private val attrClassName: String,
    packageName: String,
    data: TypeElement,
    private val attrs: MutableList<AttrBindingData>,
) {
    private val viewName = ClassName(packageName, data.simpleName.toString())
    private val attrSetName = ClassName("android.util", "AttributeSet")
    private val typedArrayName = ClassName("android.content.res", "TypedArray")
    private val getDrawableSafeName = MemberName("com.nikhilpanju.tablespoon", "getDrawableSafe")

    fun build(): TypeSpec = TypeSpec.classBuilder(attrClassName).run {

        val lambdaType: TypeName = LambdaTypeName
            .get(
                receiver = typedArrayName,
                returnType = Unit::class.asClassName(),
            )
            .copy(nullable = true)

        val primaryCtor = FunSpec.constructorBuilder().run {
            addParameter("view", viewName)
            addParameter("attrs", attrSetName.copy(nullable = true))
            addParameter("styleableRes", IntArray::class)
            addParameter(
                ParameterSpec.builder("defStyleAttr", Int::class)
                    .defaultValue("0")
                    .build()
            )
            addParameter(
                ParameterSpec.builder("defStyleRes", Int::class)
                    .defaultValue("0")
                    .build()
            )
            addParameter(
                ParameterSpec.builder("action", lambdaType)
                    .defaultValue("null")
                    .build()
            )

            addStatement("val a = view.context.theme.obtainStyledAttributes(attrs, styleableRes, defStyleAttr, defStyleRes)")
            beginControlFlow("try")
            attrs.forEach { addAttrStatement(it) }
            addStatement("action?.let { a.it() }")
            endControlFlow()
            beginControlFlow("finally")
            addStatement("a.recycle()")
            endControlFlow()
            build()
        }

        primaryConstructor(primaryCtor)

        addProperty(
            PropertySpec.builder("view", viewName)
                .initializer("view")
                .build()
        )
        addProperty(
            PropertySpec.builder("attrs", attrSetName.copy(nullable = true))
                .initializer("attrs")
                .build()
        )
        addProperty(
            PropertySpec.builder("styleableRes", IntArray::class)
                .initializer("styleableRes")
                .build()
        )
        addProperty(
            PropertySpec.builder("defStyleAttr", Int::class)
                .initializer("defStyleAttr")
                .build()
        )
        addProperty(
            PropertySpec.builder("defStyleRes", Int::class)
                .initializer("defStyleRes")
                .build()
        )
        addProperty(
            PropertySpec.builder("action", lambdaType)
                .initializer("action")
                .build()
        )

        build()
    }

    private fun FunSpec.Builder.addAttrStatement(attr: AttrBindingData) = when (attr.type) {
        is AttrBindingType.Bool -> addStatement("view.%L = a.getBoolean(%L, view.%L)", attr.name, attr.id.code, attr.name)
        is AttrBindingType.Color -> addStatement("view.%L = a.getColor(%L, view.%L)", attr.name, attr.id.code, attr.name)
        is AttrBindingType.ColorState -> addStatement("view.%L = a.getColorStateList(%L) ?: view.%L", attr.name, attr.id.code, attr.name)
        is AttrBindingType.DimenInt -> addStatement("view.%L = a.getDimension(%L, view.%L.toFloat()).toInt()", attr.name, attr.id.code, attr.name)
        is AttrBindingType.DimenFloat -> addStatement("view.%L = a.getDimension(%L, view.%L)", attr.name, attr.id.code, attr.name)
        is AttrBindingType.Drawable -> addStatement("view.%L = a.%M(view.context, %L, view.%L)", attr.name, getDrawableSafeName, attr.id.code, attr.name)
        is AttrBindingType.Float -> addStatement("view.%L = a.getFloat(%L, view.%L)", attr.name, attr.id.code, attr.name)
        is AttrBindingType.Int -> addStatement("view.%L = a.getInt(%L, view.%L)", attr.name, attr.id.code, attr.name)
        is AttrBindingType.ResourceId -> addStatement("view.%L = a.getResourceId(%L, view.%L)", attr.name, attr.id.code, attr.name)
        is AttrBindingType.StringData -> addStatement("view.%L = a.getString(%L) ?: view.%L", attr.name, attr.id.code, attr.name)
    }
}
