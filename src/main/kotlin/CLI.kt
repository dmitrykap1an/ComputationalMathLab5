import interpolation.GaussPolynomial
import interpolation.LagrangePolynomial
import org.jetbrains.letsPlot.geom.geomArea
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.letsPlot
import java.io.*
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.math.*

object CLI {

    private val functions = mapOf(
        1 to Function({ x: BigDecimal -> x * x }, "x^2"),
        2 to Function({ x: BigDecimal -> x * x * x }, "x^3")
    )

    private lateinit var input: () -> String
    private lateinit var bw: BufferedWriter
    private val br = BufferedReader(FileReader("src/files/tasks/task1.txt"))
    private var visible = true
    private var dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>? = null


    fun interpolation() {
        askInputOption()
        val argument = askArgument()
        askOutputOption()
        val lagrange = LagrangePolynomial.solve(dataTable!!, argument)
        val gauss = GaussPolynomial.solve(dataTable!!, argument)
        if (gauss == null) {
            bw.write("Невозожно найти интерполяционный многочлены Гаусса, так как узлы не равноотстающие")
            bw.newLine()
            printResult(lagrange)
            showPlot(dataTable!!, lagrange)
        } else {
            val table = GaussPolynomial.getTableOfFiniteDifferences(dataTable!!.second)
            //lagrange.errorEstimation = ((gauss.errorEstimation!! + gauss.value) - lagrange.value)
            printTable(table)
            printResult(lagrange)
            printResult(gauss)
            showPlot(dataTable!!, lagrange, gauss)
        }

    }

    private fun printTable(table: ArrayList<ArrayList<BigDecimal>>) {
        println("ТАБЛИЦА КОНЕЧНЫХ РАЗНОСТЕЙ")
        println("---" * (table[0].size + 5))
        val y = table[0]
        print("yi: ")
        y.forEach { print("$it ") }
        println()
        for (i in 1 until table.size) {
            print("dy^$i: ")
            table[i].forEach { print("$it ") }
            println()
        }
        println("---" * (table[0].size + 5))

    }

    private fun askArgument(): BigDecimal {
        ask("Введите координату x, в которой найти значение функции: ")
        while (true) {
            try {
                return input().toBigDecimal()
            } catch (e: NumberFormatException) {
                askln("Координата должна быть представлена числом!")
            }
        }

    }

    private fun getDataTable(): Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>> {
        val numberOfDots = askNumberOfDots()
        askln("Введите таблицу данных:")
        return getTable(numberOfDots, 'x') to getTable(numberOfDots, 'y')
    }

    private fun askOutputOption() {
        print("Записать результат в файл? Д/н ")
        val str = readln()
        bw = when (str.lowercase()) {
            "д", "\n", "l" -> {
                createFileAndWriteResult()

            }

            else -> {
                BufferedWriter(OutputStreamWriter(System.out))
            }
        }
    }

    private fun getTable(numberOfDots: Int, char: Char): ArrayList<BigDecimal> {
        while (true) {
            try {
                askln("Введите $numberOfDots чисел соответсвующих $char:")
                val data = ArrayList(input().split(" ").map { it.toBigDecimal() })
                if (data.size == numberOfDots) return data
                else ask("Введите $numberOfDots чисел!")
            } catch (e: NumberFormatException) {
                ask("Точки должны быть представлена числом!\n")
            }
        }


    }


    private fun askInputOption() {
        print("Прочитать данные из файла? Д/н ")
        val str = readln()
        when (str.lowercase()) {
            "д", "\n", "l" -> {
                visible = false
                input = { br.readLine() }
                dataTable = getDataTable()
            }

            else -> {
                visible = true
                input = { readln() }
                askVisibleInputOption()
            }
        }
    }

    private fun askVisibleInputOption() {
        askln("Выберете формат ввода данных:")
        askln("1)Ввести данные таблицей")
        askln("2)Выбрать функцию")
        while (true) {
            try {
                val n = readln().toInt()
                dataTable = when (n) {
                    1 -> getDataTable()

                    2 -> selectFunction()

                    else -> {
                        askln("Ответ должен быть в промежутке от 1 до 2!")
                        continue
                    }
                }
                break;
            } catch (e: NumberFormatException) {
                askln("Ответ должен быть представлен числом!")
            }
        }
    }

    private fun selectFunction(): Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>> {
        askln("Выберете функцию для интерполяции: ")
        functions.forEach {
            askln("${it.key}) ${it.value.getStringFunction()}")
        }
        while (true) {
            try {
                val n = readln().toInt()
                if (n in 1..functions.size) {
                    val f = functions[n]!!
                    f.setInterval(askInterval())
                    f.setNumberOfDots(askNumberOfDots())
                    return generateDataTable(functions[n]!!)
                } else askln("Ответ должен быть в промежутке от 1 до ${functions.size}")
            } catch (e: NumberFormatException) {
                askln("Ответ должен быть представлен числом!")
            }
        }
    }

    private fun askInterval(): Pair<BigDecimal, BigDecimal> {
        while (true) {
            try {
                ask("Введите границы области: ")
                val (a, b) = input().split(" ").map { it.toBigDecimalOrNull() }
                if (a is BigDecimal && b is BigDecimal) return Pair(min(a, b), max(a, b))
            } catch (e: IndexOutOfBoundsException) {
                ask("Введите через пробел два числа!\n")
            }

        }
    }

    private fun askNumberOfDots(): Int {
        while (true) {
            try {
                ask("Введите количество точек для таблицы: ")
                val numberOfDots = input().toInt()
                if (numberOfDots in 2..100) return numberOfDots
                else ask("Количество точек таблицы должно быть представлено числом из диапозона [2;100]!\n")
            } catch (e: NumberFormatException) {
                ask("Количество точек таблицы должно быть представлено числом из диапозона [2;100]!\n")
            }
        }

    }

    private fun generateDataTable(function: Function): Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>> {
        var (a, b) = function.getInterval()!!
        val f = function.getF()
        val step = (b - a).abs() / function.getNumberOfDots()!!.toBigDecimal()
        val x = ArrayList<BigDecimal>()
        val y = ArrayList<BigDecimal>()
        while (a <= b) {
            x.add(a)
            y.add(f(a))
            a += step
        }
        return x to y
    }

    private fun printResult(result: Result) {
        bw.write(result.method.uppercase())
        bw.newLine()
        bw.write("Значение функции: ${result.value}")
        bw.newLine()
//        bw.write("Оценка погрешности: ${result.errorEstimation}")
//        bw.newLine()
        bw.write("-----------------------------------")
        bw.newLine()
        bw.flush()
    }

    private fun showPlot(dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>, result: Result) {
        val (xd, yd) = dataTable
        val (listX, listY) = generateData(result.f, xd)
        val d = mapOf(
            xd to yd, listX to listY
        )

        val plot = letsPlot(d) + geomArea(fill = "white", color = "red") { x = xd; y = listY } +
                geomPoint(size = 3, fill = "white") { x = xd; y = yd }
        plot.show()
    }


    private fun showPlot(
        dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>,
        lagrange: Result,
        gauss: Result
    ) {
        val (xd, yd) = dataTable
        val (lagrangeX, lagrangeY) = generateData(lagrange.f, xd)
        val (gaussX, gaussY) = generateData(gauss.f, xd)
        val newX = ArrayList<BigDecimal>()
        val newY = ArrayList<BigDecimal>()
        for(i in xd.size + 1..lagrangeX.size){
            newX.add(0.0.toBigDecimal())
            newY.add(0.0.toBigDecimal())
        }
        newX.addAll(xd)
        newY.addAll(yd)
        println(newX.size)
        println(lagrangeX.size)
        val d = mapOf(
            newX to newY, lagrangeX to lagrangeY, gaussX to gaussY
        )

        val plot = letsPlot(d) + geomArea(fill = "white", color = "red") { x = lagrangeX; y = lagrangeY } +
                geomArea(fill = "white", color = "blue") { x = gaussX; y = gaussY } +
                geomPoint(size = 3, fill = "white") { x = newX; y = newY }

        plot.show()
    }

    private fun generateData(f: (BigDecimal) -> BigDecimal, x: ArrayList<BigDecimal>): Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>> {
        val y = ArrayList<BigDecimal>()
        val xd = ArrayList<BigDecimal>()
        var start = x.first().toDouble()
        var finish = x.last().toDouble()
        var h = (finish - start)/40.0
        while(start < finish){
            y.add(f(start.toBigDecimal()))
            xd.add(start.toBigDecimal())
            start += h;
        }

        return xd to y
    }


    private fun ask(text: String) {
        if (visible) print(text)
    }

    private fun askln(text: String? = null) {
        if (visible) println(text)
    }


    private fun createFileAndWriteResult(): BufferedWriter {
        val date = LocalDateTime.now()
        val file = File(
            "/home/newton/IdeaProjects/Math/comp_math/lab4/src/files/results/" +
                    "${date.dayOfMonth}_${date.month}_${date.hour}:${date.minute}.${date.second}"
        )

        return BufferedWriter(FileWriter(file))
    }


}

private operator fun String.times(i: Int): String {
    val stringBuilder = StringBuilder()
    for (i in 1..i) {
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}

fun min(x: BigDecimal, y: BigDecimal): BigDecimal {
    return if (x <= y) x
    else y
}

fun max(x: BigDecimal, y: BigDecimal): BigDecimal {
    return if (x >= y) x
    else y
}
