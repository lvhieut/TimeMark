package com.example.timemarkbase

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.timemarkbase.databinding.FragmentBottomSheetBinding
import com.example.timemarkbase.time_mark.TimeMarkStyle
import com.example.timemarkbase.utils.MainFeaturePrefs
import com.example.timemarkbase.view_model.MainFeatureViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
            updateNameInputVisibility(isChecked)
            featureViewModel.setEnableFullName(isChecked)
        }

        binding?.btnSwitchEnableNameCompany?.setOnCheckedChangeListener { _, isChecked ->
            featureViewModel.setEnableFullNameCompany(isChecked)
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

        featureViewModel.companyName.observe(viewLifecycleOwner) {
            binding?.textEditCompany?.setText(it)
        }

        featureViewModel.address.observe(viewLifecycleOwner) {
            binding?.textEditAddres?.setText(it)
        }

        featureViewModel.latLon.observe(viewLifecycleOwner) {
            binding?.textLatitude?.setText(it.first.toString())
            binding?.textLongitude?.setText(it.second.toString())
        }


        featureViewModel.isEnableFullName.observe(viewLifecycleOwner) {
            binding?.btnSwitchEnableName?.isChecked = it
            updateNameInputVisibility(it)
        }

        featureViewModel.isEnableFullNameCompany.observe(viewLifecycleOwner) {
            binding?.btnSwitchEnableNameCompany?.isChecked = it
            updateCompanyInputVisibility()
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

        featureViewModel.selectedTimeMarkStyle.observe(viewLifecycleOwner) {
            updateCompanyInputVisibility()
        }
    }

    private fun updateNameInputVisibility(isVisible: Boolean) {
        binding?.textEditNameLayout?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun updateCompanyInputVisibility() {
        val isCompact = featureViewModel.selectedTimeMarkStyle.value == TimeMarkStyle.COMPACT
        val isCompanyEnabled = featureViewModel.isEnableFullNameCompany.value ?: true
        binding?.enableNameCompany?.visibility = if (isCompact) View.VISIBLE else View.GONE
        binding?.textEditCompanyLayout?.visibility =
            if (isCompact && isCompanyEnabled) View.VISIBLE else View.GONE
    }

    private fun saveAction() {
        if (!saveLatLonIfValid()) return

        featureViewModel.setFullName(binding?.textEditName?.text.toString())
        featureViewModel.setCompanyName(binding?.textEditCompany?.text.toString())
        featureViewModel.setDay(binding?.textEditDay?.text.toString())
        featureViewModel.setAddress(binding?.textEditAddres?.text.toString())
        featureViewModel.setTime(binding?.textEditTime?.text.toString())
        featureViewModel.setDate(binding?.textEditDayMonth?.text.toString())

        binding?.btnSwitchEnableName?.isChecked
            ?.let { featureViewModel.setEnableFullName(it) }

        binding?.btnSwitchEnableNameCompany?.isChecked
            ?.let { featureViewModel.setEnableFullNameCompany(it) }

        binding?.btnSwitchEnableLogo?.isChecked
            ?.let { featureViewModel.setEnableLogo(it) }

        binding?.btnSwitchVerifiedText?.isChecked
            ?.let { featureViewModel.setEnableVerifiedText(it) }

        binding?.btnSwitchGoogleMap?.isChecked
            ?.let { featureViewModel.setEnableImageGoogleMap(it) }
        dismiss()
    }

    private fun saveLatLonIfValid(): Boolean {
        val latitude = binding?.textLatitude?.text
            ?.toString()
            ?.trim()
            ?.replace(',', '.')
            ?.toDoubleOrNull()
        val longitude = binding?.textLongitude?.text
            ?.toString()
            ?.trim()
            ?.replace(',', '.')
            ?.toDoubleOrNull()

        if (latitude == null || longitude == null) {
            Toast.makeText(requireContext(), "Vĩ độ hoặc kinh độ không hợp lệ", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            Toast.makeText(requireContext(), "Vĩ độ hoặc kinh độ ngoài phạm vi hợp lệ", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        featureViewModel.setLatLon(latitude, longitude)
        MainFeaturePrefs.saveLatLon(requireContext(), latitude, longitude)
        return true
    }

}
