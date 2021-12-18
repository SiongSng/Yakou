package org.casc.lang.table

import org.casc.lang.ast.Function

data class Scope(
    val isGlobalScope: Boolean = true,
    var usages: MutableSet<Reference> = mutableSetOf(),
    var functions: MutableSet<FunctionSignature> = mutableSetOf(),
    var variables: MutableList<Variable> = mutableListOf()
) {
    constructor(parent: Scope) : this(false, parent.usages, parent.functions, parent.variables)

    /**
     * registerFunctionSignature must be called after checker assigned types to function object
     */
    fun registerFunctionSignature(function: Function) {
        functions += FunctionSignature(
            function.compKeyword != null,
            function.mutKeyword != null,
            function.accessor,
            function.name?.literal ?: "",
            function.parameterTypes!!.mapNotNull { it },
            function.returnType!!
        )
    }

    fun registerVariable(mutable: Boolean, name: String, type: Type?): Boolean {
        val index = variables.lastIndex + 1 + if (variables.lastOrNull() != null) {
            val lastType = variables.last().type

            if (lastType == PrimitiveType.F64 || lastType == PrimitiveType.I64) 1
            else 0
        } else 0
        val variable = Variable(
            mutable,
            name,
            type,
            index
        )

        if (variables.contains(variable)) return false

        return variables.add(variable)
    }

    fun findVariable(name: String): Variable? =
        variables.find { it.name == name }

    fun findVariableIndex(name: String): Int? {
        var index: Int? = null

        variables.forEach {
            if (it.name == name) index = it.index
        }

        return index
    }

    fun findType(reference: Reference?): Type? =
        if (reference == null) null
        else TypeUtil.asType(reference)
}