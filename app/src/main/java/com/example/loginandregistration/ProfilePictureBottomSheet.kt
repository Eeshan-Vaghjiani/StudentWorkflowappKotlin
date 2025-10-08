package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.loginandregistration.databinding.BottomSheetProfilePictureBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Bottom sheet dialog for selecting profile picture source.
 * Provides options to take a photo or choose from gallery.
 */
class ProfilePictureBottomSheet(
    private val onTakePhoto: () -> Unit,
    private val onChooseGallery: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProfilePictureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProfilePictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTakePhoto.setOnClickListener {
            onTakePhoto()
            dismiss()
        }

        binding.btnChooseGallery.setOnClickListener {
            onChooseGallery()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
