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

package com.nikhilpanju.tablespoonsample

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.nikhilpanju.tablespoon.TableSpoon
import com.nikhilpanju.tablespoon.annotations.BooleanAttr
import com.nikhilpanju.tablespoon.annotations.ColorAttr
import com.nikhilpanju.tablespoon.annotations.DimensionAttr
import com.nikhilpanju.tablespoon.annotations.DrawableAttr
import com.nikhilpanju.tablespoon.annotations.FloatAttr
import com.nikhilpanju.tablespoon.annotations.IntAttr
import com.nikhilpanju.tablespoon.annotations.ResourceIdAttr
import com.nikhilpanju.tablespoon.dynamicBooleanAttr
import com.nikhilpanju.tablespoon.dynamicColorStateAttr
import com.nikhilpanju.tablespoon.dynamicDrawableAttr
import com.nikhilpanju.tablespoon.dynamicFloatAttr
import com.nikhilpanju.tablespoon.dynamicIntAttr
import com.nikhilpanju.tablespoon.dynamicStringAttr

@SuppressLint("NonConstantResourceId")
class SomeOtherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr) {

    @delegate:BooleanAttr(R2.styleable.SomeOtherView_bool_attr)
    var someBool: Boolean by dynamicBooleanAttr(false)

    @delegate:ColorAttr(R2.styleable.SomeOtherView_color_state_attr)
    var colorStateListAttr: ColorStateList? by dynamicColorStateAttr(null)

    @delegate:ColorAttr(R2.styleable.SomeOtherView_color_attr)
    var colorAttr: Int by dynamicIntAttr(0xFFF1BB5A.toInt())

    @delegate:DimensionAttr(R2.styleable.SomeOtherView_dimension_int_attr)
    var dimIntAttr: Int by dynamicIntAttr(0)

    @DimensionAttr(R2.styleable.SomeOtherView_dimension_float_attr)
    var dimFloatAttr: Float = 10f

    @delegate:DrawableAttr(R2.styleable.SomeOtherView_drawable_attr)
    var drawableAttr: Drawable? by dynamicDrawableAttr(null)

    @delegate:FloatAttr(R2.styleable.SomeOtherView_float_attr)
    var floatAttr: Float by dynamicFloatAttr(4f)

    @delegate:IntAttr(R2.styleable.SomeOtherView_integer_attr)
    var intAttr: Int by dynamicIntAttr(66)

    @delegate:IntAttr(R2.styleable.SomeOtherView_enum_attr)
    var enumAttr: Int by dynamicIntAttr(0)

    @delegate:IntAttr(R2.styleable.SomeOtherView_flags_attr)
    var flagAttr: Int by dynamicIntAttr(0)

    @delegate:ResourceIdAttr(R2.styleable.SomeOtherView_reference_attr)
    var resIdAttr: Int by dynamicIntAttr(0)

    var stringAttr: String by dynamicStringAttr(
        initialValue = "default string",
        invalidate = true, // default is true
        requestLayout = false // default is true
    ) { newString ->
        // this block is called before the view is updated
        // in case some pre-operations need to be performed
    }

    init {
        TableSpoon.init(this, attrs, R.styleable.SomeOtherView, defStyleAttr, defStyleRes) {
            stringAttr = getString(R.styleable.SomeOtherView_string_attr) ?: stringAttr
        }

        setBackgroundColor(if (someBool) colorStateListAttr!!.defaultColor else colorAttr)
    }
}
