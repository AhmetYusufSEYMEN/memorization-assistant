package com.seymen.ezberarkadasim.view

import android.os.Bundle
import android.view.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seymen.ezberarkadasim.R
import com.seymen.ezberarkadasim.databinding.BottomSheetDialogBinding


class BottomSheetFragment : BottomSheetDialogFragment(){
    private lateinit var binding: BottomSheetDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetDialogBinding.inflate(inflater)
        return inflater.inflate(R.layout.bottom_sheet_dialog,container,false)
    }



}