package calculator

import java.math.BigDecimal

val numberRegex = """[+-]?\d+""".toRegex()
val wrongSignNumber = """^\d+\+|\d+-$""".toRegex() //example: 125+
val identifierRegex = "[a-zA-Z]+".toRegex()
val variableToValueMap = mutableMapOf<String, BigDecimal>()
val splitRegex = Regex("""\d+|[()+\-*/^]|[a-zA-Z]+""")

fun splitAndReplaceString(input: String): List<String> {
    val replacedInput = input
        .replace("(-{2})+".toRegex(), "+")
        .replace("(\\+)+".toRegex(), "+")
        .replace("\\+-".toRegex(), "-")

    return splitRegex.findAll(replacedInput).map { it.value }.toList().filter { it.isNotEmpty() && it.isNotBlank() }
}

fun checkStringParts(stringParts: List<String>): Boolean {
    val numbersNumber = stringParts.filter { it.matches(numberRegex) || it.matches(identifierRegex) }.size
    val operatorsNumber = stringParts.filter { it.matches("[+\\-*/^]".toRegex()) }.size

    val numberOfOpenParentheses = stringParts.count { it == "(" }
    val numberOfClosedParentheses = stringParts.count { it == ")" }

    if ((numbersNumber - 1) != operatorsNumber) {
        return false
    } else if (numberOfOpenParentheses != numberOfClosedParentheses) {
        return false
    }

    return true
}

fun processString(stringParts: List<String>): ArrayDeque<String>? {
    val processedString = ArrayDeque<String>()

    for (i in stringParts) {
        when {
            i.matches(identifierRegex) -> {
                if (!variableToValueMap.contains(i)) {
                    println("Unknown variable")
                    return null
                } else {
                    val value = variableToValueMap[i]
                    processedString.addLast(value.toString())
                }
            }

            i.matches(numberRegex) -> processedString.addLast(i)
            else -> {
                processedString.addLast(i)
            }
        }
    }

    return processedString
}

fun inputContainsEqualSign(input: String) {
    val partsOfInput = input.split("=").map { it.trim() }.filter { it.isNotEmpty() || it.isNotBlank() }
    if (partsOfInput.size != 2) {
        println("Invalid assignment")
        return
    } else {
        val variable = partsOfInput[0]
        if (checkIdentifier(variable)) {
            if (checkIdentifier(partsOfInput[1]) && variableToValueMap.contains(partsOfInput[1])) {
                val anotherVariable = partsOfInput[1]
                val valueOfAnotherVariable = variableToValueMap[anotherVariable]
                variableToValueMap[variable] = valueOfAnotherVariable!!
            } else {
                val value = partsOfInput[1].toBigDecimalOrNull()
                if (value != null) {
                    variableToValueMap[partsOfInput[0]] = partsOfInput[1].toBigDecimal()
                } else {
                    println("Invalid assignment")
                }
            }
        } else {
            println("Invalid identifier")
        }

    }
}

fun checkIdentifier(identifier: String): Boolean {
    return identifier.matches(identifierRegex)
}

fun checkIfVariableExists(variable: String): BigDecimal? {
    if (variableToValueMap.contains(variable)) {
        return variableToValueMap[variable]
    }
    return null
}

object ReversePolishNotationConvertor {
    private val operationStack = ArrayDeque<String>()

    private fun pollOperationsWhilePriorityTheSameOrLower(
        operationList: List<String>,
        outputList: ArrayDeque<String>
    ) {
        if (operationStack.isNotEmpty() && operationList.contains(operationStack.last())) {
            outputList.add(operationStack.removeLast())
            pollOperationsWhilePriorityTheSameOrLower(operationList, outputList)
        }
    }

    private fun manageOperation(
        operation: String,
        lowerPriorityOperations: List<String>,
        higherOrTheSamePriorityOperations: List<String>,
        outputList: ArrayDeque<String>
    ) {
        if (operationStack.isNotEmpty()) {
            val peek = operationStack.last()
            when {
                lowerPriorityOperations.contains(peek) -> operationStack.addLast(operation)
                higherOrTheSamePriorityOperations.contains(peek) -> {
                    pollOperationsWhilePriorityTheSameOrLower(higherOrTheSamePriorityOperations, outputList)
                    operationStack.addLast(operation)
                }
            }
        } else {
            operationStack.addLast(operation)
        }
    }

    fun transformIntoReversePolishNotation(input: List<String>): ArrayDeque<String> {
        val outputList = ArrayDeque<String>()

        for (operation in input) {
            when (operation) {
                "+", "-" -> manageOperation(operation, listOf("("), listOf("+", "-", "*", "/", "^"), outputList)
                "*", "/" -> manageOperation(operation, listOf("(", "+", "-"), listOf("*", "/", "^"), outputList)
                "^" -> manageOperation(operation, listOf("(", "+", "-", "*", "/"), listOf("^"), outputList)
                "(" -> operationStack.addLast(operation)
                ")" -> {
                    var poll: String
                    do {
                        poll = operationStack.removeLast()
                        if (poll != "(") {
                            outputList.add(poll)
                        }
                    } while (poll != "(")
                }

                else -> {
                    outputList.add(operation)
                }
            }
        }

        for (operation in operationStack.reversed()) {
            outputList.add(operation)
        }

        operationStack.clear()
        return outputList
    }

}

object ReversePolishNotationProcessor {
    fun processNotation(outputList: ArrayDeque<String>): BigDecimal {
        val arrayDeque = ArrayDeque<BigDecimal>()

        for (token in outputList) {
            when (token) {
                "+" -> {
                    val firstNumber = arrayDeque.removeFirst()
                    val secondNumber = arrayDeque.removeFirst()
                    arrayDeque.addFirst(firstNumber + secondNumber)
                }

                "-" -> {
                    val firstNumber = arrayDeque.removeFirst()
                    val secondNumber = arrayDeque.removeFirst()
                    arrayDeque.addFirst(secondNumber - firstNumber)
                }

                "*" -> {
                    val firstNumber = arrayDeque.removeFirst()
                    val secondNumber = arrayDeque.removeFirst()
                    arrayDeque.addFirst(firstNumber * secondNumber)
                }

                "/" -> {
                    val firstNumber = arrayDeque.removeFirst()
                    val secondNumber = arrayDeque.removeFirst()
                    arrayDeque.addFirst(secondNumber / firstNumber)
                }

                "^" -> {
                    val firstNumber = arrayDeque.removeFirst()
                    val secondNumber = arrayDeque.removeFirst()
                    arrayDeque.addFirst(Math.pow(secondNumber.toDouble(), firstNumber.toDouble()).toBigDecimal())
                }

                else -> arrayDeque.addFirst(token.toBigDecimal())
            }
        }
        return arrayDeque.first()
    }
}

fun main() {
    while (true) {
        val input = readln()

        when {
            input.contains("=") -> {
                inputContainsEqualSign(input)
            }

            input.matches(identifierRegex) -> {
                val value = checkIfVariableExists(input) ?: "Unknown variable"
                println(value)
            }

            input.matches(numberRegex) -> println(input.toBigDecimal())
            input.matches(wrongSignNumber) -> println("Invalid expression")
            input == "/help" -> println("The program calculates the sum of numbers")
            input == "/exit" -> break
            input.startsWith("/") -> {
                println("Unknown command")
            }

            input.isEmpty() -> continue

            else -> {
                val splitAndReplaceResult = splitAndReplaceString(input)
                if (checkStringParts(splitAndReplaceResult)) {
                    val processResult = processString(splitAndReplaceResult)
                    if (processResult != null) {
                        val rpnConversionResult =
                            ReversePolishNotationConvertor.transformIntoReversePolishNotation(processResult)
                        val finalResult = ReversePolishNotationProcessor.processNotation(rpnConversionResult)
                        println(finalResult)
                    }
                } else {
                    println("Invalid expression")
                }
            }
        }
    }
    println("Bye!")
}