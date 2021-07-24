package anton.miranouski.pomodoro

import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import anton.miranouski.pomodoro.databinding.StopwatchItemBinding
import anton.miranouski.pomodoro.service.displayTime

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        if (stopwatch.isFinished) {
            binding.stopwatchTimer.text = ZERO_TIME
        } else {
            binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        }

        binding.progressBar.setPeriod(stopwatch.period)
        binding.progressBar.setCurrent(stopwatch.period - stopwatch.currentMs)

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer()
        }

        initButtonListeners(stopwatch)
    }

    private fun initButtonListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener {
            listener.delete(stopwatch.id)
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = "Stop"

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        binding.startPauseButton.text = "Start"

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentMs, UNIT_MS) {

            val interval = UNIT_MS

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.progressBar.setCurrent(stopwatch.period - stopwatch.currentMs)
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }

            override fun onFinish() {
                stopwatch.isFinished = true
                stopTimer()
                listener.stop(stopwatch.id, stopwatch.period)
                Toast.makeText(itemView.context, "Timer finished!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private companion object {

        private const val ZERO_TIME = "00:00:00"
        private const val UNIT_MS = 1000L
    }
}


