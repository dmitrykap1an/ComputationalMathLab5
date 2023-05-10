package interpolation

import Result
import java.math.BigDecimal

sealed class Interpolation {

    private val dx: BigDecimal
        get() = 0.0001.toBigDecimal()
    abstract fun solve(dataTable: Pair<ArrayList<BigDecimal>, ArrayList<BigDecimal>>, argument: BigDecimal): Result?

    fun derive(f: (BigDecimal) -> BigDecimal): (BigDecimal) -> BigDecimal{
        return { x: BigDecimal -> (f(x + dx) - f(x)) / dx}
    }

    fun factorial(n: Int): Int{
        return if(n == 1) 1
        else n * factorial(n - 1)
    }



}

