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

import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.LinkedHashMap

object TableSpoon {
    private val cache = LinkedHashMap<Class<*>, Constructor<out Any>?>()

    fun init(
        view: View,
        attrs: AttributeSet?,
        styleableRes: IntArray,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
        action: (TypedArray.() -> Unit)? = null,
    ) {
        val constructor = findBindingConstructorForClass(view::class.java)
            ?: throw IllegalStateException("${view::class.simpleName} must contain at least one Tablespoon annotation")

        try {
            constructor.newInstance(view, attrs, styleableRes, defStyleAttr, defStyleRes, action)
        } catch (e: IllegalAccessException) {
            throw java.lang.RuntimeException("Unable to invoke $constructor", e)
        } catch (e: InstantiationException) {
            throw java.lang.RuntimeException("Unable to invoke $constructor", e)
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause is java.lang.RuntimeException || cause is Error) {
                throw cause
            }
            throw RuntimeException("Unable to create binding instance.", cause)
        }
    }

    private fun findBindingConstructorForClass(cls: Class<*>?): Constructor<out Any>? {
        if (cache.containsKey(cls)) return cache[cls]

        val clsName = cls!!.name
        if (clsName.startsWith("android.")
            || clsName.startsWith("java.")
            || clsName.startsWith("androidx.")
        ) {
            return null
        }
        val constructor: Constructor<out Any>? = try {
            val bindingClass = cls.classLoader!!.loadClass(clsName + "_AttrBinding")
            bindingClass.constructors.firstOrNull()
        } catch (e: ClassNotFoundException) {
            findBindingConstructorForClass(cls.superclass)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Unable to find binding constructor for $clsName", e)
        }

        cache[cls] = constructor
        return constructor
    }
}
