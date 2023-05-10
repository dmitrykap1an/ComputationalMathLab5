package interpolation

import Result
import exceptions.UnevenlySpacedException

import java.math.BigDecimal

object GaussPolynomial : Interpolation() {
    override fun solve(dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>, argument: BigDecimal): Result? {
        return try {
            val (x, y) = dataTable
            val h = checkUniformity(x)
            val a = x[(x.size - 1) / 2]
            val n = x.size - 1
            val result = chooseMethod(dataTable, argument, a, h)
            result

        } catch (e: UnevenlySpacedException) {
            null
        }
    }

    private fun chooseMethod(
        dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>,
        argument: BigDecimal,
        a: BigDecimal,
        h: BigDecimal
    ): Result {
        return if (argument > a) firstGaussInterpolation(dataTable, argument, a, h)
        else secondGaussInterpolation(dataTable, argument, a, h)
    }


    private fun firstGaussInterpolation(
        dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>,
        argument: BigDecimal,
        a: BigDecimal,
        h: BigDecimal
    ): Result {
        val (xd, y) = dataTable
        val n = xd.size - 1
        val table = getTableOfFiniteDifferences(y)
        val f = { x: BigDecimal ->
            val t = (x - a) / h
            var j = 0
            var index = xd.indexOf(a)
            var sum = table[j][index]
            var tsum = 1.0.toBigDecimal()
            var cnt = 0.0.toBigDecimal()
            j++
            for (i in 1..n) {
                if(index > 0 && j < table.size){
                    if(i % 2 == 1){
                        tsum *= (t + cnt)
                        cnt++
                    }
                    else{
                        tsum *= (t - cnt)
                        index--
                    }
                    sum += (table[j][index] * tsum)/GaussPolynomial.factorial(i).toBigDecimal()
                    j++
                } else break
            }
            sum
        }

        return Result(f(argument), f, "Первая интерполяционная формула Гаусса", errorEstimation(table, a, n/2, (argument - a) / h))
    }

    private fun secondGaussInterpolation(
        dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>,
        argument: BigDecimal,
        a: BigDecimal,
        h: BigDecimal
    ): Result {
        val (x, y) = dataTable
        val n = x.size - 1
        val table = getTableOfFiniteDifferences(y)
        var index = x.indexOf(a)
        val t = (argument - a) / h
        val f = { x: BigDecimal ->
            var j = 0
            var sum = table[j][index]
            var tsum = 1.0.toBigDecimal()
            var cnt = 0.0.toBigDecimal()
            j++;
            for (i in 1..n) {
                if(index >= 0 && j < table.size){
                    if(i % 2 == 1){
                        tsum *= (t - cnt)
                        cnt++
                        index--
                    }
                    else{
                        tsum *= (t + cnt)
                    }
                    sum += table[j][index] * tsum/GaussPolynomial.factorial(i).toBigDecimal()
                    j++
                } else break
            }
            sum
        }

        return Result(f(argument), f, "Первая интерполяционная формула Гаусса")
    }

    private fun getTableOfFiniteDifferences(y: ArrayList<BigDecimal>): ArrayList<ArrayList<BigDecimal>> {
        val table = ArrayList<ArrayList<BigDecimal>>()
        table.add(y)
        val n = y.size - 1
        for (j in 0..n) {
            val arrList = ArrayList<BigDecimal>()
            for (i in 0 until table[j].lastIndex) {
                arrList.add(table[j][i + 1] - table[j][i])
            }
            table.add(arrList)
        }

        return table

    }

    private fun errorEstimation(table: ArrayList<ArrayList<BigDecimal>>, y0: BigDecimal, n: Int, t: BigDecimal): BigDecimal{
        var pr = 1.0.toBigDecimal()
        (0..n).forEach { pr *= t - it.toBigDecimal() }

        return (table[n][n] * pr)/factorial(n + 1).toBigDecimal()
        //return 0.0.toBigDecimal()
    }
    private fun checkUniformity(x: ArrayList<BigDecimal>): BigDecimal {
        val h = x[1] - x[0]
        for (i in 1..x.lastIndex) {
            if (x[i] - x[i - 1] != h) throw UnevenlySpacedException()
        }
        return h
    }
}