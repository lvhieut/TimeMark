package com.example.timemarkbase.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class PolicyDisclaimerDialog(
    private val onUnderstood: () -> Unit
) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Chính sách sử dụng")
            .setMessage(
                "Phần mềm được tạo ra chỉ nhằm hỗ trợ chỉnh sửa ảnh trong trường hợp ảnh không có thông tin định vị hoặc thời gian.\n\n" +
                    "Người dùng tự chịu trách nhiệm về mục đích sử dụng, nội dung chỉnh sửa và việc chia sẻ hình ảnh sau khi xuất từ ứng dụng. Nhà phát triển không chịu trách nhiệm đối với mọi hành vi sử dụng sai mục đích, gian lận, gây hiểu nhầm hoặc vi phạm quy định của bên thứ ba."
            )
            .setPositiveButton("Đã hiểu") { dialog, _ ->
                dialog.dismiss()
                onUnderstood()
            }
            .create()
    }
}
