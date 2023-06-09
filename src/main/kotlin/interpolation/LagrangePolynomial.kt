package interpolation

import Result
import max
import java.math.BigDecimal

object LagrangePolynomial : Interpolation() {
    override fun solve(dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>, argument: BigDecimal): Result{
        val n = dataTable.first.size - 1
        val (arrX, arrY) = dataTable
        val f = {
            x: BigDecimal ->
            var sum = 0.0.toBigDecimal()
            for (i in 0 .. n) {
                var pv = 1.0.toBigDecimal()
                var pn = 1.0.toBigDecimal()
                for (j in 0 .. n) {
                    if (i != j) {
                        pv *= (x - arrX[j])
                        pn *= (arrX[i] - arrX[j])
                    }
                }
                if(pn >= 0.0.toBigDecimal() && pn <= 0E-9.toBigDecimal()) pn = 0.00001.toBigDecimal()
                sum += (pv * arrY[i])/pn
            }
            sum
        }
        val value = f(argument)
        val errorEstimation = errorEstimation(arrX, f, argument )
        return Result(value, f, "Многочлен Лагранжа", errorEstimation)
    }

    private fun errorEstimation(xArray: ArrayList<BigDecimal>, f: (x: BigDecimal) -> BigDecimal, argument: BigDecimal): BigDecimal{
        val n = xArray.size - 1
        var fn1 = f
        repeat(n + 1){
            fn1 = derive(fn1)
        }
        val f2  = {
            x: BigDecimal ->
            var p = 1.0.toBigDecimal()
            for(i in 0.. n){
                p *= x - xArray[i]
            }
            p
        }

        var max = 0.0.toBigDecimal()
        xArray.forEach{
            max = max(max, fn1(it))
        }

        return max * (f2(argument)).abs()/(factorial(n + 1 )).toBigDecimal()
    }


}