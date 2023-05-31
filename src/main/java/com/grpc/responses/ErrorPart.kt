package responses

class ErrorPart(
    public val text: String,
    public val type: String,
)

class Error(
    public val error: ArrayList<ErrorPart>
)
