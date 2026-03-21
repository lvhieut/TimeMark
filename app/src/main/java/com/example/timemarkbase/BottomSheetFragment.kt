package com.example.timemarkbase

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.timemarkbase.databinding.FragmentBottomSheetBinding
import com.example.timemarkbase.utils.PrefHelper
import com.example.timemarkbase.view_model.MainFeatureViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class BottomSheetFragment : BottomSheetDialogFragment() {

    private var binding: FragmentBottomSheetBinding? = null
    private val featureViewModel: MainFeatureViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        binding?.btbSaveAction?.setOnClickListener {
            saveAction()
        }
        binding?.btnSwitchEnableName?.setOnCheckedChangeListener { _, isChecked ->
            featureViewModel.setEnableFullName(isChecked)
        }

        binding?.btnSwitchEnableLogo?.setOnCheckedChangeListener { _, isChecked ->
            featureViewModel.setEnableLogo(isChecked)
        }

        binding?.btnSwitchVerifiedText?.setOnCheckedChangeListener { _, isChecked ->
            featureViewModel.setEnableVerifiedText(isChecked)
        }

        binding?.btnSwitchGoogleMap?.setOnCheckedChangeListener { _, isChecked ->
            featureViewModel.setEnableImageGoogleMap(isChecked)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {
        featureViewModel.time.observe(viewLifecycleOwner) {
            binding?.textEditTime?.setText(it)
        }

        featureViewModel.day.observe(viewLifecycleOwner) {
            binding?.textEditDay?.setText(it)
        }

        featureViewModel.date.observe(viewLifecycleOwner) {
            binding?.textEditDayMonth?.setText(it)
        }

        featureViewModel.fullName.observe(viewLifecycleOwner) {
            binding?.textEditName?.setText(it)
        }

        featureViewModel.address.observe(viewLifecycleOwner) {
            binding?.textEditAddres?.setText(it)
        }
        val (lat, lon) = PrefHelper.getLocation(requireContext())
        if (lat != null && lon != null) {
            featureViewModel.setLatLon(lat, lon)
        }

        featureViewModel.latLon.observe(viewLifecycleOwner) {
            binding?.textLatitude?.setText(it.first.round6().toString())
            binding?.textLongitude?.setText(it.second.round6().toString())
        }


        featureViewModel.isEnableFullName.observe(viewLifecycleOwner) {
            binding?.btnSwitchEnableName?.isChecked = it
        }

        featureViewModel.isEnableLogo.observe(viewLifecycleOwner) {
            binding?.btnSwitchEnableLogo?.isChecked = it
        }

        featureViewModel.isEnableVerifiedText.observe(viewLifecycleOwner) {
            binding?.btnSwitchVerifiedText?.isChecked = it
        }

        featureViewModel.isEnableGoogleMap.observe(viewLifecycleOwner) {
            binding?.btnSwitchGoogleMap?.isChecked = it

            if (it) {
                binding?.textLat?.visibility = View.VISIBLE
                binding?.textLon?.visibility = View.VISIBLE
            } else {
                binding?.textLat?.visibility = View.GONE
                binding?.textLon?.visibility = View.GONE
            }
        }
    }

    private fun saveAction() {
        featureViewModel.setFullName(binding?.textEditName?.text.toString())
        featureViewModel.setDay(binding?.textEditDay?.text.toString())
        featureViewModel.setAddress(binding?.textEditAddres?.text.toString())
        featureViewModel.setTime(binding?.textEditTime?.text.toString())
        featureViewModel.setDate(binding?.textEditDayMonth?.text.toString())
        val lat = binding?.textLatitude?.text
            ?.toString()
            ?.replace(",", ".")
            ?.toDouble()

        val lon = binding?.textLongitude?.text
            ?.toString()
            ?.replace(",", ".")
            ?.toDouble()

        if (lat != null && lon != null) {
            featureViewModel.setLatLon(
                lat,
                lon
            )
        }
        PrefHelper.saveLocation(
            requireContext(),
            binding?.textLatitude?.text.toString().toDouble().round6(),
            binding?.textLongitude?.text.toString().toDouble().round6()
        )
        binding?.btnSwitchEnableName?.isChecked
            ?.let { featureViewModel.setEnableFullName(it) }

        binding?.btnSwitchEnableLogo?.isChecked
            ?.let { featureViewModel.setEnableLogo(it) }

        binding?.btnSwitchVerifiedText?.isChecked
            ?.let { featureViewModel.setEnableVerifiedText(it) }

        binding?.btnSwitchGoogleMap?.isChecked
            ?.let { featureViewModel.setEnableImageGoogleMap(it) }
        dismiss()
    }

    fun Double.round6(): Double {
        return String.format(Locale.US, "%.6f", this).toDouble()
    }


}