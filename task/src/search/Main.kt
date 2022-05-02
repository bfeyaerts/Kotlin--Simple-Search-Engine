package search

import java.io.File

enum class Strategy(
    val mergeFunction: (Set<Int>, Set<Int>) -> Set<Int>,
    val resultInitializer: (List<String>) -> Set<Int> = {_: List<String> -> setOf()},
) {
    ALL({ s1: Set<Int>, s2: Set<Int> -> s1.intersect(s2) }),
    ANY({ s1: Set<Int>, s2: Set<Int> -> s1.union(s2) }),
    NONE({ s1: Set<Int>, s2: Set<Int> -> s1.minus(s2) }, { list: List<String> -> list.indices.toSet() }),
    ;
    fun init(dataset: List<String>) = resultInitializer(dataset)
    fun merge(set1: Set<Int>, set2: Set<Int>) = mergeFunction(set1, set2)
}

enum class Option(val string: String) {
    DATA("--data");
    companion object {
        fun getValueOrNull(string: String): Option? {
            values().forEach { if (it.string == string) return it }
            return null
        }
    }
}

enum class MenuOption(val string: String, val code: Int) {
    FIND_A_RECORD("Find a record", 1),
    PRINT_ALL_RECORDS("Print all records", 2),
    EXIT("Exit", 0);
    companion object {
        fun getValueOrNull(int: Int): MenuOption? {
            values().forEach { if (it.code == int) return it }
            return null
        }
        fun printMenu() {
            println("=== Menu ===")
            values().forEach { println("${it.code}. ${it.string}") }
        }
    }
}

lateinit var dataset: List<String>

fun main(args: Array<String>) {
    val invertedIndex = mutableMapOf<String, MutableSet<Int>>()

    val options = args.toMutableList()
    while (options.isNotEmpty()) {
        val option = options.removeAt(0)
        when (Option.getValueOrNull(option)) {
            Option.DATA -> {
                val file = File(options.removeAt(0))
                dataset = file.readLines().map(String::trim)
                dataset.mapIndexed { index, s ->
                    s.lowercase().split("\\s+".toRegex()).forEach {
                        if (invertedIndex.containsKey(it)) {
                            invertedIndex[it]!!.add(index)
                        } else {
                            invertedIndex[it] = mutableSetOf(index)
                        }
                    }
                }
            }
        }
    }

    while (true) {
        println()
        MenuOption.printMenu()
        val menuOption = MenuOption.getValueOrNull(readLine()!!.toInt())
        println()
        when (menuOption) {
            MenuOption.FIND_A_RECORD -> {
                println("Select a matching strategy: ${Strategy.values().joinToString()}")
                val strategy = Strategy.valueOf(readLine()!!)

                println("Enter the query:")
                val query = readLine()!!.split("\\s+".toRegex()).map(String::trim).map(String::lowercase)
                var indices = strategy.init(dataset)
                query.forEach {
                    val newIndices = invertedIndex.computeIfAbsent(it) { mutableSetOf() }
                    indices = strategy.merge(indices, newIndices)
                }
                val records = indices.map { dataset[it] }
                if (records.isNotEmpty()) {
                    println("${indices.size} record${ if (indices.size == 1) "" else "s" } found:")
                    records.forEach(::println)
                } else println("No matching records found.")
                println()
            }
            MenuOption.PRINT_ALL_RECORDS -> {
                println("=== List of records ===")
                dataset.forEach(::println)
            }
            MenuOption.EXIT -> {
                println("Bye!")
                return
            }
            else -> println("Incorrect option! Try again.")
        }
    }
}
