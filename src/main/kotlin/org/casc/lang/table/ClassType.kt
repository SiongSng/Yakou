package org.casc.lang.table

import org.casc.lang.compilation.Preference
import org.objectweb.asm.Opcodes

data class ClassType(
    override val typeName: String,
    override val internalName: String = typeName.replace('.', '/'),
    override val descriptor: String = "L$internalName;"
) : Type {
    constructor(clazz: Class<*>): this(clazz.name)

    override fun type(): Class<*>? = try {
        Preference.classLoader?.loadClass(typeName)
    } catch (e: Exception) {
        null
    }

    override val loadOpcode: Int = Opcodes.ALOAD
    override val storeOpcode: Int = Opcodes.ASTORE
}