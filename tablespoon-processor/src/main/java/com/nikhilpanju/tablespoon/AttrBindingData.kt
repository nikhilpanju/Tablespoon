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

internal class AttrBindingData(
    val id: Id,
    _name: String,
    val type: AttrBindingType,
) {
    val name = _name.removeSuffix("\$delegate")
}

internal sealed class AttrBindingType {
    internal object Bool : AttrBindingType()
    internal object Color : AttrBindingType()
    internal object ColorState : AttrBindingType()
    internal object DimenInt : AttrBindingType()
    internal object DimenFloat : AttrBindingType()
    internal object Drawable : AttrBindingType()
    internal object Float : AttrBindingType()
    internal object Int : AttrBindingType()
    internal object ResourceId : AttrBindingType()
    internal object StringData : AttrBindingType()
}
