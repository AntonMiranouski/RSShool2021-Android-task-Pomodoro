package anton.miranouski.pomodoro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import anton.miranouski.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopwatchListener {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.picker.minValue = 1
        binding.picker.maxValue = 59

        binding.addNewStopwatchButton.setOnClickListener {
            val minutes = binding.picker.value

            stopwatches.add(Stopwatch(nextId++, minutes * 60000L, false, minutes * 60000L))
            stopwatchAdapter.submitList(stopwatches.toList())
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()

        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, isStarted, it.period))
            } else {
                newTimers.add(Stopwatch(it.id, it.currentMs, false, it.period))
            }
        }

        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }
}