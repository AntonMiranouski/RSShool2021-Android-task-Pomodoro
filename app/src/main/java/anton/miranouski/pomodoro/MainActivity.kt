package anton.miranouski.pomodoro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import anton.miranouski.pomodoro.databinding.ActivityMainBinding
import anton.miranouski.pomodoro.service.*

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var currentId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.picker.minValue = 1
        binding.picker.maxValue = 59

        binding.addNewStopwatchButton.setOnClickListener {
            val minutes = binding.picker.value

            stopwatches.add(Stopwatch(nextId++, minutes * 60000L, false, false, minutes * 60000L))
            stopwatchAdapter.submitList(stopwatches.toList())
        }
    }

    override fun start(id: Int) {
        currentId = id

        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long?) {
        if (id == currentId) currentId = -1

        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        if (id == currentId) currentId = -1

        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        stopwatches.replaceAll {
            if (it.id == id) {
                Stopwatch(it.id, currentMs ?: it.currentMs, isStarted, it.isFinished, it.period)
            } else {
                Stopwatch(it.id, it.currentMs, false, it.isFinished, it.period)
            }
        }
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("TAG", "onAppBackground()")
        var currentMs = -10L
        stopwatches.forEach {
            if (it.isStarted) {
                currentMs = it.currentMs
            }
        }
        if (currentMs == -10L) return

        val startTime = System.currentTimeMillis()
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
        startIntent.putExtra(CURRENT_MS, currentMs)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d("TAG", "onAppForeground()")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}