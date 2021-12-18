package org.casc.lang.lexer

import org.casc.lang.ast.Position
import org.casc.lang.ast.Token
import org.casc.lang.ast.TokenType
import org.casc.lang.compilation.Error
import org.casc.lang.compilation.Report

// TODO: Multi-Threaded Lexing Process
class Lexer(val chunkedSource: List<String>) {
    private var lineNumber: Int = 1
    private var pos: Int = 0

    private fun skip(offset: Int = 1): Int {
        val currentPos = pos
        pos += offset
        return currentPos
    }

    fun lex(): Pair<List<Report>, List<Token>> {
        val reports = mutableListOf<Report>()
        val tokens = mutableListOf<Token>()

        for (source in chunkedSource) {
            while (pos < source.length) {
                // Number Literals
                if (source[pos].isDigit()) {
                    var endsWithDot = false
                    var isFloatingPointNumber = false
                    val start = pos

                    while (pos < source.length && source[pos].isDigit())
                        pos++

                    if (pos != source.length) {
                        if (source[pos] == '.') {

                            isFloatingPointNumber = true
                            pos++

                            while (pos < source.length && source[pos].isDigit()) {
                                if (!endsWithDot) endsWithDot = true
                                pos++
                            }
                        }

                        if (pos != source.length) {
                            when (source[pos]) {
                                'B', 'S', 'I', 'L' -> {
                                    if (isFloatingPointNumber) {
                                        reports += Error(
                                            Position(lineNumber, pos),
                                            "Cannot declare a floating point number literal as non-floating type",
                                            "Consider removing type suffix"
                                        )
                                    }

                                    pos++
                                }
                                'F', 'D' -> {
                                    isFloatingPointNumber = true
                                    pos++
                                }
                            }
                        }
                    }

                    tokens += Token(
                        source.substring(start until pos - if (endsWithDot) 1 else 0),
                        if (isFloatingPointNumber) TokenType.FloatLiteral
                        else TokenType.IntegerLiteral,
                        Position(lineNumber, start, pos)
                    )
                    continue
                }

                // Whitespace & Tabs
                if (source[pos].isWhitespace()) {
                    val start = pos

                    while (pos < source.length && source[pos].isWhitespace())
                        pos++

                    // Discard Whitespace tokens
                    continue
                }

                // Identifiers (Including Keywords)
                if (!isSymbol(source[pos])) {
                    val start = pos

                    while (pos < source.length && !isSymbol(source[pos]))
                        pos++

                    tokens += Token(
                        source.substring(start until pos),
                        TokenType.Identifier,
                        Position(lineNumber, start, pos)
                    )
                    continue
                }

                // Operators etc.
                when (source[pos]) {
                    '{' -> tokens.charToken(source, TokenType.OpenBrace)
                    '}' -> tokens.charToken(source, TokenType.CloseBrace)
                    '[' -> tokens.charToken(source, TokenType.OpenBracket)
                    ']' -> tokens.charToken(source, TokenType.CloseBracket)
                    '(' -> tokens.charToken(source, TokenType.OpenParenthesis)
                    ')' -> tokens.charToken(source, TokenType.CloseParenthesis)
                    ':' -> when (source[pos + 1]) {
                        ':' ->
                            tokens += Token(
                                source.substring(pos..pos + 1),
                                TokenType.DoubleColon,
                                Position(lineNumber, pos, skip(2) + 1)
                            )
                        '=' ->
                            tokens += Token(
                                source.substring(pos..pos + 1),
                                TokenType.ColonEqual,
                                Position(lineNumber, pos, skip(2) + 1)
                            )
                        else -> tokens.charToken(source, TokenType.Colon)
                    }
                    ',' -> tokens.charToken(source, TokenType.Comma)
                    '=' -> tokens.charToken(source, TokenType.Equal)
                    '+' -> tokens.charToken(source, TokenType.Plus)
                    '-' -> tokens.charToken(source, TokenType.Minus)
                    '*' -> tokens.charToken(source, TokenType.Star)
                    '/' -> tokens.charToken(source, TokenType.Slash)
                    '%' -> tokens.charToken(source, TokenType.Percentage)
                    else ->
                        reports += Report.Error(
                            Position(lineNumber, pos),
                            "Unexpected character ${source[pos++]}"
                        )
                }
            }

            pos = 0
            lineNumber++
        }

        return reports to tokens
    }

    private fun MutableList<Token>.charToken(source: String, type: TokenType) =
        this.add(Token(source[pos], type, Position(lineNumber, pos++)))

    private fun isSymbol(char: Char): Boolean {
        val charCode = char.code

        return when {
            char.isLetter() -> false
            char.isWhitespace() -> true
            charCode > 127 -> false     // Not Ascii Character
            charCode in 33..47 -> true
            charCode in 58..64 -> true
            charCode in 91..96 -> true
            charCode in 123..126 -> true
            else -> false
        }
    }

}