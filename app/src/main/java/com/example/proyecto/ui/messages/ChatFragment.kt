package com.example.proyecto.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentChatBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        val ownerName = arguments?.getString("ownerName") ?: "Chat"
        binding.tvChatName.text = ownerName
        binding.etMessage.hint  = "Message ${ownerName.split(" ").first()}..."

        val defaultMessage = arguments?.getString("defaultMessage") ?: ""
        if (defaultMessage.isNotEmpty()) {
            binding.etMessage.setText(defaultMessage)
            binding.etMessage.setSelection(defaultMessage.length)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = ChatAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        binding.btnAttachment.setOnClickListener { showAttachmentMenu() }

        binding.btnSend.setOnClickListener { sendMessage() }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("bookShared")
            ?.observe(viewLifecycleOwner) { shared ->
                if (shared == true) {
                    messages.add(
                        ChatMessage("", getCurrentTime(), isSent = true, type = ChatMessage.TYPE_BOOK_SHARED)
                    )
                    adapter.notifyItemInserted(messages.size - 1)
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                    findNavController().currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("bookShared")
                }
            }
    }

    private fun showAttachmentMenu() {
        val sheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_attachment, null)

        view.findViewById<View>(R.id.optionShareBook).setOnClickListener {
            sheet.dismiss()
            findNavController().navigate(R.id.action_chat_to_shared)
        }

        view.findViewById<View>(R.id.optionDeliverBook).setOnClickListener {
            sheet.dismiss()
            findNavController().navigate(R.id.action_chat_to_return)
        }

        sheet.setContentView(view)
        sheet.show()
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val time = getCurrentTime()
        messages.add(ChatMessage(text, time, isSent = true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
        binding.etMessage.text.clear()
    }

    private fun getCurrentTime(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour   = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val amPm   = if (hour < 12) "AM" else "PM"
        val hour12 = when {
            hour == 0  -> 12
            hour > 12  -> hour - 12
            else       -> hour
        }
        return "$hour12:${minute.toString().padStart(2, '0')} $amPm"
    }

    override fun onDestroyView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        super.onDestroyView()
        _binding = null
    }
}
