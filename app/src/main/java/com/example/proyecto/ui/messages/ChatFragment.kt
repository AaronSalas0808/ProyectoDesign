package com.example.proyecto.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentChatBinding
import com.example.proyecto.network.BookRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private var conversationId: Int = -1
    private var ownerName: String = "Chat"
    private var defaultMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationId = arguments?.getInt("conversationId", -1) ?: -1
        ownerName = arguments?.getString("ownerName") ?: "Chat"
        defaultMessage = arguments?.getString("defaultMessage") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        binding.tvChatName.text = ownerName
        binding.etMessage.hint = "Message ${ownerName.split(" ").first()}..."

        binding.btnBack.setOnClickListener {
            goToMessages()
        }

        adapter = ChatAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        if (defaultMessage.isNotEmpty()) {
            binding.etMessage.setText(defaultMessage)
        }

        binding.btnAttachment.setOnClickListener { showAttachmentMenu() }
        binding.btnSend.setOnClickListener { sendMessage() }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        loadMessages()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goToMessages()
                }
            }
        )
    }

    private fun goToMessages() {
        findNavController().popBackStack()
    }

    private fun loadMessages() {
        if (conversationId == -1) return
        fetchMessages()
    }

    private fun fetchMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiMessages = BookRepository.getConversationMessages(conversationId)
                messages.clear()
                messages.addAll(apiMessages)
                adapter.notifyDataSetChanged()

                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Error cargando mensajes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAttachmentMenu() {
        val sheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_attachment, null)

        view.findViewById<View>(R.id.optionShareBook).setOnClickListener {
            sheet.dismiss()
            Toast.makeText(requireContext(), "Esta opción sigue igual por ahora", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.optionDeliverBook).setOnClickListener {
            sheet.dismiss()
            Toast.makeText(requireContext(), "Esta opción sigue igual por ahora", Toast.LENGTH_SHORT).show()
        }

        sheet.setContentView(view)
        sheet.show()
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        binding.btnSend.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (conversationId == -1) {
                    val time = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                    messages.add(ChatMessage(text = text, time = time, isSent = true))
                    adapter.notifyItemInserted(messages.size - 1)
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                    binding.etMessage.text.clear()
                    return@launch
                }

                val sent = BookRepository.postConversationMessage(conversationId, text)
                if (sent) {
                    binding.etMessage.text.clear()
                    fetchMessages()
                } else {
                    Toast.makeText(requireContext(), "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Error enviando mensaje",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnSend.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        super.onDestroyView()
        _binding = null
    }
}