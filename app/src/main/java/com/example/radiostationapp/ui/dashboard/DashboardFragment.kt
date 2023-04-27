package com.example.radiostationapp.ui.dashboard

import android.R
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.radiostationapp.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            resources.getStringArray(com.example.radiostationapp.R.array.video_names)
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVideo.adapter = adapter

        binding.spinnerVideo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedVideoName =
                    resources.getStringArray(com.example.radiostationapp.R.array.video_names)[position]
                val selectedVideoUrl =
                    resources.getStringArray(com.example.radiostationapp.R.array.video_urls)[position]
                playSelectedVideo(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun playSelectedVideo(position: Int) {
        val videoUrls = resources.getStringArray(com.example.radiostationapp.R.array.video_urls)
        val selectedVideoUrl = videoUrls[position]
        binding.videoView.setVideoURI(Uri.parse(selectedVideoUrl))
        binding.videoView.start()
    }

}