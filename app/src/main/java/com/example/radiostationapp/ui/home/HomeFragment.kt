package com.example.radiostationapp.ui.home

import android.R
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.radiostationapp.RadioBrowserApi
import com.example.radiostationapp.RadioStationsResponse
import com.example.radiostationapp.databinding.FragmentHomeBinding
import com.example.radiostationapp.services.MediaService
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    var mediaPlayer: MediaPlayer? = null
    var mediaService: MediaService? = null
    var mediaConnection: ServiceConnection? = null
    private val binding get() = _binding!!

    companion object {
        var currentTrackIndex = 0
        var stationsList: List<RadioStationsResponse>? = null
        var selectedItem: RadioStationsResponse? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mediaPlayer = MediaPlayer()
        mediaConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mediaService = (service as MediaService.MediaBinder).getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://at1.api.radio-browser.info/json/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val radioBrowserApi = retrofit.create(RadioBrowserApi::class.java)
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }


        GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val stations = radioBrowserApi.searchStations()
            stationsList = stations
            val stationNames = stations.map { it.name }
            val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, stationNames)
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            withContext(Dispatchers.Main) {
                binding.spinnerAudio.adapter = adapter
            }
        }

        binding.spinnerAudio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedItem = stationsList?.get(position)
                Glide.with(requireContext())
                    .load(selectedItem?.favicon)
                    .into(binding.albumCover)
                binding.songName.text = selectedItem?.name
                currentTrackIndex = position
                mediaService?.playNext()
                binding.playPauseButton.setImageResource(com.example.radiostationapp.R.drawable.play)
                //setUpSeekBar()
                Log.e("icon ", selectedItem?.favicon!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.playPauseButton.setOnClickListener {
            mediaService?.let {
                it.play()
                if (it.isPlaying) {
                    binding.playPauseButton.setImageResource(com.example.radiostationapp.R.drawable.play)
                } else {
                    binding.playPauseButton.setImageResource(com.example.radiostationapp.R.drawable.pause)
                }
            }
        }

        return root
    }

    private val TIME_TO_WAIT:Long = 2000
    private fun setUpSeekBar() {
        val handler = Handler()
        binding.seekBar.max = 100 * 5

        binding.seekBar.progress = 0

        handler.postDelayed(object : Runnable {
            override fun run() {
                binding.seekBar.progress += 1
                setUpTimeTextView(binding.seekBar.progress.toLong())
                handler.postDelayed(this, TIME_TO_WAIT)
            }
        }, TIME_TO_WAIT)
    }

    private fun setUpTimeTextView(timeInSeconds: Long) {
        val minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds)
        val seconds =
            timeInSeconds - TimeUnit.MINUTES.toSeconds(minutes)
        val formattedMinutes = String.format("%02d", minutes)
        val formattedSeconds = String.format("%02d", seconds)
        binding.timerTextView.text = "$formattedMinutes:$formattedSeconds"
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireActivity(), MediaService::class.java)
        mediaConnection?.let {
            requireActivity().bindService(
                intent,
                it,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onStop() {
        super.onStop()
        mediaConnection?.let { requireActivity().unbindService(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}