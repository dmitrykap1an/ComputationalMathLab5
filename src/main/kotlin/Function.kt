import java.math.BigDecimal

data class Function(
    private var f: (x: BigDecimal) -> BigDecimal,
    private var stringFunction: String,
    private var interval: Pair<BigDecimal, BigDecimal>? = null,
    private var numberOfDots: Int? = null
) {

    fun getF() = f;

    fun setF(f: (x: BigDecimal) -> BigDecimal){
        this.f = f
    }

    fun getStringFunction() =
        stringFunction

    fun setStringFunction(stringFunction: String){
        this.stringFunction = stringFunction
    }

    fun getInterval() =
        interval

    fun setInterval(interval: Pair<BigDecimal, BigDecimal>){
        this.interval = interval
    }

    fun getNumberOfDots() =
        numberOfDots

    fun setNumberOfDots(numberOfDots: Int){
        this.numberOfDots = numberOfDots
    }
}