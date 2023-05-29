package app.adreal.endec.Model

data class KDFParameters(
    val salt : String,
    val iteration : String,
    val memoryCost : String,
    val hashLength : String,
    val version : String,
    val mode : String,
    val parallelism : String
)