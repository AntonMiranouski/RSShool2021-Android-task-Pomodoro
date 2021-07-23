package anton.miranouski.pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    var period: Long
)
