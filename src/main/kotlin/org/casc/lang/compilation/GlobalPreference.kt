package org.casc.lang.compilation

import org.casc.lang.asm.BytecodeClassLoader
import java.io.File
import java.net.URLClassLoader

object GlobalPreference : AbstractPreference() {
    override var enableColor = true
    override var classLoader: BytecodeClassLoader? = null
    override var compileAndRun = false

    fun reset() {
        enableColor = true
        classLoader = null
        outputDir = File(System.getProperty("user.dir"))
        compileAndRun = false
    }
}