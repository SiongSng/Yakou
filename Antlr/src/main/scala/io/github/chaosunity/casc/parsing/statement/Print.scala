package io.github.chaosunity.casc.parsing.statement

import io.github.chaosunity.casc.parsing.expression.Expression

class Print(val expression: Expression, val neg: Boolean) extends Statement {
}
