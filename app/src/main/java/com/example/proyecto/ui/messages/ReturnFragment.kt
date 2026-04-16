package com.example.proyecto.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ReturnFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_return, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnConfirmReturn).setOnClickListener {
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set("bookReturned", true)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        super.onDestroyView()
    }
}
