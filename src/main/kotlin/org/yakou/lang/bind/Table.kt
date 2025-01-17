package org.yakou.lang.bind

import org.yakou.lang.ast.TokenType
import org.yakou.lang.ast.Type

/**
 * Table class stores type infos and function infos in Yakou standard type format.
 * Example standard type formats:
 * - pkgA::pkgB::class   (Normal type or just package path)
 * - [pkgA::pkgB::class] (Array type)
 */
class Table {
    private val typeTable: MutableMap<String, TypeInfo> = hashMapOf()

    /**
     * fnTable stores class' / package's standardized path as key and list of its member functions
     */
    private val fnTable: MutableMap<String, MutableList<Fn>> = hashMapOf()

    fun registerFunction(fn: Fn, packageLevel: Boolean): Boolean {
        val qualifiedOwnerPath = fn.qualifiedOwnerPath
        val ownerTypeInfo = typeTable.getOrPut(qualifiedOwnerPath) {
            if (packageLevel) {
                TypeInfo.PackageClass(fn.packagePath)
            } else {
                TODO()
            }
        } as TypeInfo.Class

        return if (!fnTable.containsKey(qualifiedOwnerPath)) {
            // Create a new set of functions
            fnTable[qualifiedOwnerPath] = mutableListOf(fn)

            fn.ownerTypeInfo = ownerTypeInfo

            true
        } else if (fnTable[qualifiedOwnerPath]!!.any { it == fn }) false
        else {
            fnTable[qualifiedOwnerPath]!! += fn

            fn.ownerTypeInfo = ownerTypeInfo

            true
        }
    }

    fun findType(type: Type): TypeInfo? {
        val typeName = type.standardizeType()

        PrimitiveType.findPrimitiveType(typeName)?.let {
            return TypeInfo.Primitive(it)
        }

        if (typeName.startsWith("[")) {
            // Array type
            val arrayTypeInfo = asTypeInfo(type)

            // Check if base type is type path, and validate if type path exists
            return when ((arrayTypeInfo as TypeInfo.Array).baseType) {
                is TypeInfo.Primitive, is TypeInfo.Class -> arrayTypeInfo
                is TypeInfo.Array -> null // Unreachable
            }
        }

        return typeTable[typeName]
    }

    private fun asTypeInfo(type: Type): TypeInfo? = when (type) {
        is Type.Array -> {
            var finalArrayType: TypeInfo.Array
            var dimensionCount = 0
            var innerType = type.type

            while (innerType is Type.Array) {
                dimensionCount++
                innerType = innerType.type
            }

            val baseType = asTypeInfo(innerType)

            if (baseType != null) {
                finalArrayType = TypeInfo.Array(baseType)

                while (dimensionCount > 0) {
                    finalArrayType = TypeInfo.Array(finalArrayType)
                    dimensionCount--
                }

                finalArrayType
            } else null
        }
        is Type.TypePath -> {
            val typeName = type.standardizeType()

            PrimitiveType.findPrimitiveType(typeName)?.let {
                TypeInfo.Primitive(it)
            } ?: typeTable[typeName] ?: run {
                try {
                    val clazz = Class.forName(typeName.replace("::", "."))

                    return@run TypeInfo.fromClass(clazz)
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (e: Error) {
                    e.printStackTrace()
                }

                null
            }
        }
    }
}