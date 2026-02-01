package com.example.timemarkbase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.timemarkbase.databinding.FragmentBottomSheetBinding
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
            featureViewModel.setEnableFullName(isChecked)
        }

        binding?.btnSwitchEnableLogo?.setOnCheckedChangeListener { _, isChecked ->
            featureViewModel.setEnableLogo(isChecked)
        }
    }

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

        featureViewModel.isEnableFullName.observe(viewLifecycleOwner) {
            binding?.btnSwitchEnableName?.isChecked = it
        }

        featureViewModel.isEnableLogo.observe(viewLifecycleOwner) {
            binding?.btnSwitchEnableLogo?.isChecked = it
        }
    }

    private fun saveAction() {
        featureViewModel.setFullName(binding?.textEditName?.text.toString())
        featureViewModel.setDay(binding?.textEditDay?.text.toString())
        featureViewModel.setAddress(binding?.textEditAddres?.text.toString())
        featureViewModel.setTime(binding?.textEditTime?.text.toString())
        featureViewModel.setDate(binding?.textEditDayMonth?.text.toString())
        binding?.btnSwitchEnableName?.isChecked
            ?.let { featureViewModel.setEnableFullName(it) }

        binding?.btnSwitchEnableLogo?.isChecked
            ?.let { featureViewModel.setEnableLogo(it) }
        dismiss()
    }


}