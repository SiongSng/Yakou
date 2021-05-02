package io.github.chaosunity.casc.parsing.node.expression

import io.github.chaosunity.casc.parsing.type.Type

data class Value(override val type: Type, override val negative: Boolean, val value: String) : Expression<Value>
