package com.example.timemarkbase

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.example.timemarkbase.databinding.BottomSheetTimeMarkTemplateBinding
import com.example.timemarkbase.time_mark.TemplateItemSizeResolver
import com.example.timemarkbase.time_mark.TimeMarkStyle
import com.example.timemarkbase.utils.MainFeaturePrefs
import com.example.timemarkbase.view_model.MainFeatureViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TimeMarkTemplateBottomSheetFragment : BottomSheetDialogFragment() {

    private var binding: BottomSheetTimeMarkTemplateBinding? = null
    private val featureViewModel: MainFeatureViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetTimeMarkTemplateBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderTemplateItems(featureViewModel.selectedTimeMarkStyle.value ?: TimeMarkStyle.CLASSIC)
        featureViewModel.selectedTimeMarkStyle.observe(viewLifecycleOwner) {
            updateSelectedState(it)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun renderTemplateItems(selectedStyle: TimeMarkStyle) {
        val list = binding?.templateList ?: return
        list.removeAllViews()

        TimeMarkStyle.entries.forEach { style ->
            list.addView(createTemplateItem(style, style == selectedStyle))
        }
    }

    private fun createTemplateItem(style: TimeMarkStyle, selected: Boolean): View {
        val context = requireContext()
        val size = TemplateItemSizeResolver.resolve(context)
        val density = resources.displayMetrics.density
        val marginEnd = (8 * density).toInt()

        return LinearLayout(context).apply {
            tag = style
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(size.paddingPx, size.paddingPx, size.paddingPx, size.paddingPx)
            background = createItemBackground(selected)
            layoutParams = LinearLayout.LayoutParams(size.widthPx, size.heightPx).apply {
                setMargins(0, 0, marginEnd, 0)
            }
            isClickable = true
            isFocusable = true

            addView(FrameLayout(context).apply {
                setBackgroundColor(if (selected) Color.parseColor("#FFF6DA") else Color.parseColor("#F2F2F2"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    size.previewHeightPx
                )
                addView(TextView(context).apply {
                    text = style.displayName.take(1)
                    gravity = Gravity.CENTER
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#444444"))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                })
            })

            addView(TextView(context).apply {
                text = style.displayName
                gravity = Gravity.CENTER
                maxLines = 1
                textSize = size.textSizeSp
                setTextColor(if (selected) Color.parseColor("#222222") else Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (6 * density).toInt()
                }
            })

            setOnClickListener {
                featureViewModel.setSelectedTimeMarkStyle(style)
                MainFeaturePrefs.saveTimeMarkStyle(context, style)
            }
        }
    }

    private fun updateSelectedState(selectedStyle: TimeMarkStyle) {
        binding?.templateList?.children?.forEach { item ->
            val isSelected = item.tag == selectedStyle
            item.background = createItemBackground(isSelected)
            (item as? ViewGroup)?.children?.forEach { child ->
                if (child is TextView) {
                    child.setTextColor(
                        if (isSelected) Color.parseColor("#222222") else Color.parseColor("#666666")
                    )
                }
            }
        }
    }

    private fun createItemBackground(selected: Boolean) =
        android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = 8 * resources.displayMetrics.density
            setColor(Color.WHITE)
            val strokeWidth = ((if (selected) 2 else 1) * resources.displayMetrics.density).toInt()
            setStroke(
                strokeWidth.coerceAtLeast(1),
                if (selected) Color.parseColor("#F4B527") else Color.parseColor("#DDDDDD")
            )
        }
}
