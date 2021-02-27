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

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

internal fun ProcessingEnvironment.error(element: Element, message: String) {
    messager.printMessage(Diagnostic.Kind.ERROR, message, element)
}

internal fun ProcessingEnvironment.note(element: Element, message: String) {
    messager.printMessage(Diagnostic.Kind.NOTE, message, element)
}
