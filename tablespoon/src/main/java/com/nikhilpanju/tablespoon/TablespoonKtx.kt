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

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

fun View.dynamicBooleanAttr(
    initialValue: Boolean,
    invalidate: Boolean = true,
    requestLayout: Boolean = true,
    onChange: ((Boolean) -> Unit)? = null,
) = DynamicBooleanProperty(this, initialValue, invalidate, requestLayout, onChange)

fun <T : ColorStateList?> View.dynamicColorStateAttr(
    initialValue: T,
    invalidate: Boolean = true,
    requestLayout: Boolean = true,
    onChange: ((T) -> Unit)? = null,
) = DynamicColorStateListProperty(this, initialValue, invalidate, requestLayout, onChange)

fun <T : Drawable?> View.dynamicDrawableAttr(
    initialValue: T,
    invalidate: Boolean = true,
    requestLayout: Boolean = true,
    onChange: ((T) -> Unit)? = null,
) = DynamicDrawableProperty(this, initialValue, invalidate, requestLayout, onChange)

fun View.dynamicFloatAttr(
    initialValue: Float,
    invalidate: Boolean = true,
    requestLayout: Boolean = true,
    onChange: ((Float) -> Unit)? = null,
) = DynamicFloatProperty(this, initialValue, invalidate, requestLayout, onChange)

fun View.dynamicIntAttr(
    initialValue: Int,
    invalidate: Boolean = true,
    requestLayout: Boolean = true,
    onChange: ((Int) -> Unit)? = null,
) = DynamicIntProperty(this, initialValue, invalidate, requestLayout, onChange)

fun <T : String?> View.dynamicStringAttr(
    initialValue: T,
    invalidate: Boolean = true,
    requestLayout: Boolean = true,
    onChange: ((T) -> Unit)? = null,
) = DynamicStringProperty(this, initialValue, invalidate, requestLayout, onChange)

fun <T> View.dynamicAttr(
    initialValue: T,
    invalidate: Boolean = true,
    requestLayout: Boolean = true,
    onChange: ((T) -> Unit)? = null,
) = DynamicAttrProperty(this, initialValue, invalidate, requestLayout, onChange)

class DynamicBooleanProperty(
    view: View,
    initialValue: Boolean,
    invalidate: Boolean,
    requestLayout: Boolean,
    onChange: ((Boolean) -> Unit)? = null,
) :
    DynamicAttrProperty<Boolean>(view, initialValue, invalidate, requestLayout, onChange)

class DynamicColorStateListProperty<T : ColorStateList?>(
    view: View,
    initialValue: T,
    invalidate: Boolean,
    requestLayout: Boolean,
    onChange: ((T) -> Unit)? = null,
) :
    DynamicAttrProperty<T>(view, initialValue, invalidate, requestLayout, onChange)

class DynamicDrawableProperty<T : Drawable?>(
    view: View,
    initialValue: T,
    invalidate: Boolean,
    requestLayout: Boolean,
    onChange: ((T) -> Unit)? = null,
) :
    DynamicAttrProperty<T>(view, initialValue, invalidate, requestLayout, onChange)

class DynamicFloatProperty(
    view: View,
    initialValue: Float,
    invalidate: Boolean,
    requestLayout: Boolean,
    onChange: ((Float) -> Unit)? = null,
) :
    DynamicAttrProperty<Float>(view, initialValue, invalidate, requestLayout, onChange)

class DynamicIntProperty(
    view: View,
    initialValue: Int,
    invalidate: Boolean,
    requestLayout: Boolean,
    onChange: ((Int) -> Unit)? = null,
) :
    DynamicAttrProperty<Int>(view, initialValue, invalidate, requestLayout, onChange)

class DynamicStringProperty<T : String?>(
    view: View,
    initialValue: T,
    invalidate: Boolean,
    requestLayout: Boolean,
    onChange: ((T) -> Unit)? = null,
) :
    DynamicAttrProperty<T>(view, initialValue, invalidate, requestLayout, onChange)

open class DynamicAttrProperty<T>(
    private val view: View,
    initialValue: T,
    private val invalidate: Boolean = true,
    private val requestLayout: Boolean = true,
    private val onChange: ((T) -> Unit)? = null,
) : ObservableProperty<T>(initialValue) {

    // var initDone = false

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        // Calling invalidate and requestLayout during init is okay
        /*if (!initDone) {
            initDone = true
            return
        }*/
        onChange?.invoke(newValue)
        if (invalidate) view.invalidate()
        if (requestLayout) view.requestLayout()
    }
}

fun TypedArray.getDrawableSafe(
    context: Context,
    @StyleableRes res: Int,
    defaultDrawable: Drawable? = null,
): Drawable? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        val resourceId = getResourceId(res, -1)
        if (resourceId != -1) AppCompatResources.getDrawable(context, resourceId) else null
    } else {
        getDrawable(res)
    } ?: defaultDrawable
