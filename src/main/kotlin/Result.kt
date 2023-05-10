import java.math.BigDecimal

data class Result(
    val value: BigDecimal,
    val f: (x: BigDecimal) -> BigDecimal,
    val method: String,
    var errorEstimation: BigDecimal? = null,
)