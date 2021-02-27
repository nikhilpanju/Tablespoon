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

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.code.Symbol.VarSymbol
import com.sun.tools.javac.code.Type.JCPrimitiveType
import com.sun.tools.javac.tree.JCTree.JCFieldAccess
import com.sun.tools.javac.tree.JCTree.JCIdent
import com.sun.tools.javac.tree.JCTree.JCLiteral
import com.sun.tools.javac.tree.TreeScanner
import java.util.LinkedHashMap
import java.util.Objects

internal class RScanner : TreeScanner() {
    var resourceIds: MutableMap<Int, Id> = LinkedHashMap()

    override fun visitIdent(jcIdent: JCIdent) {
        super.visitIdent(jcIdent)
        val symbol = jcIdent.sym
        if (symbol.type is JCPrimitiveType) {
            val id = parseId(symbol)
            if (id != null) {
                resourceIds[id.value] = id
            }
        }
    }

    override fun visitSelect(jcFieldAccess: JCFieldAccess) {
        val symbol = jcFieldAccess.sym
        val id = parseId(symbol)
        if (id != null) {
            resourceIds[id.value] = id
        }
    }

    private fun parseId(symbol: Symbol): Id? {
        var id: Id? = null
        if (symbol.enclosingElement != null && symbol.enclosingElement.enclosingElement != null && symbol.enclosingElement.enclosingElement.enclClass() != null) {
            try {
                val value = Objects.requireNonNull((symbol as VarSymbol).constantValue) as Int
                id = Id(value, symbol)
            } catch (ignored: Exception) {
            }
        }
        return id
    }

    override fun visitLiteral(jcLiteral: JCLiteral) {
        try {
            val value = jcLiteral.value as Int
            resourceIds[value] = Id(value)
        } catch (ignored: Exception) {
        }
    }

    fun reset() {
        resourceIds.clear()
    }
}
