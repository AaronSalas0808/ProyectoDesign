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
import com.example.proyecto.network.RetrofitClient
import com.example.proyecto.network.SendChatMessageRequestDto
import com.example.proyecto.session.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatFragment : Fragment() {

    companion object {
        private const val MESSAGES_PAGE_SIZE = 50
    }

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var sessionManager: SessionManager

    private var conversationId: String = ""
    private var ownerName: String = "Chat"
    private var defaultMessage: String = ""

    private var loadingMessages: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationId = arguments?.getString("conversationId").orEmpty()
        ownerName = arguments?.getString("ownerName") ?: "Chat"
        defaultMessage = arguments?.getString("defaultMessage") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())

        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.GONE

        binding.tvChatName.text = ownerName
        binding.etMessage.hint = "Message ${ownerName.split(" ").firstOrNull() ?: ownerName}..."

        binding.btnBack.setOnClickListener {
            goToMessages()
        }

        adapter = ChatAdapter(
            messages = messages,
            onShareBookClick = {
                findNavController().navigate(R.id.action_chat_to_shared)
            },
            onDeliverBookClick = {
                findNavController().navigate(R.id.action_chat_to_return)
            }
        )

        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        binding.rvMessages.adapter = adapter

        if (defaultMessage.isNotEmpty()) {
            binding.etMessage.setText(defaultMessage)
        }

        binding.btnAttachment.setOnClickListener {
            showAttachmentMenu()
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

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
        if (conversationId.isBlank()) {
            Toast.makeText(
                requireContext(),
                "No se encontró la conversación",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        fetchAllMessages()
        markAsRead()
    }

    private fun fetchAllMessages() {
        if (conversationId.isBlank() || loadingMessages) return

        val myId = getMyId()

        if (myId.isBlank()) {
            Toast.makeText(
                requireContext(),
                "No se encontró el id del usuario",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        loadingMessages = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val allMessages = mutableListOf<ChatMessage>()

                var page = 0
                var totalPages = 1

                do {
                    val response = RetrofitClient.api.getChatMessages(
                        chatId = conversationId,
                        page = page,
                        size = MESSAGES_PAGE_SIZE
                    )

                    totalPages = response.totalPages ?: 1

                    val mappedMessages = response.content.map { apiMessage ->
                        val content = apiMessage.content ?: ""

                        ChatMessage(
                            id = apiMessage.id ?: "",
                            text = content,
                            time = formatTime(apiMessage.sentAt),
                            isSent = apiMessage.senderId == myId,
                            type = detectMessageType(content)
                        )
                    }

                    allMessages.addAll(mappedMessages)

                    page++

                } while (page < totalPages)

                val cleanMessages = allMessages
                    .distinctBy { message ->
                        if (message.id.isNotBlank()) {
                            message.id
                        } else {
                            "${message.text}-${message.time}-${message.isSent}"
                        }
                    }

                adapter.setMessages(cleanMessages)

                scrollToBottom()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Error cargando mensajes",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                loadingMessages = false
            }
        }
    }

    private fun markAsRead() {
        val myId = getMyId()

        if (myId.isBlank() || conversationId.isBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RetrofitClient.api.markChatAsRead(
                    chatId = conversationId,
                    readerId = myId
                )
            } catch (_: Exception) {
            }
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()

        if (text.isEmpty()) return

        val myId = getMyId()

        if (myId.isBlank()) {
            Toast.makeText(
                requireContext(),
                "No se encontró el id del usuario",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (conversationId.isBlank()) {
            Toast.makeText(
                requireContext(),
                "No se encontró la conversación",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.btnSend.isEnabled = false
        binding.etMessage.text.clear()

        val tempId = "temp-${System.currentTimeMillis()}"

        val tempMessage = ChatMessage(
            id = tempId,
            text = text,
            time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            isSent = true,
            type = detectMessageType(text)
        )

        adapter.addMessage(tempMessage)
        scrollToBottom()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.sendChatMessage(
                    chatId = conversationId,
                    body = SendChatMessageRequestDto(
                        senderId = myId,
                        content = text
                    )
                )

                if (response.isSuccessful) {
                    val saved = response.body()

                    if (saved != null) {
                        val savedContent = saved.content ?: text

                        adapter.replaceMessage(
                            tempId = tempId,
                            newMessage = ChatMessage(
                                id = saved.id ?: tempId,
                                text = savedContent,
                                time = formatTime(saved.sentAt),
                                isSent = true,
                                type = detectMessageType(savedContent)
                            )
                        )

                        scrollToBottom()
                    }

                    /*
                     * Opcional, pero útil:
                     * después de enviar, recarga todo para sincronizar con la API paginada.
                     */
                    fetchAllMessages()

                } else {
                    adapter.removeMessageById(tempId)

                    Toast.makeText(
                        requireContext(),
                        "No se pudo enviar el mensaje",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                adapter.removeMessageById(tempId)

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

    private fun showAttachmentMenu() {
        val sheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_attachment, null)

        view.findViewById<View>(R.id.optionShareBook).setOnClickListener {
            sheet.dismiss()

            val time = SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            ).format(Date())

            adapter.addMessage(
                ChatMessage(
                    id = "local-share-${System.currentTimeMillis()}",
                    text = "",
                    time = time,
                    isSent = true,
                    type = ChatMessage.TYPE_BOOK_SHARED
                )
            )

            scrollToBottom()
        }

        view.findViewById<View>(R.id.optionDeliverBook).setOnClickListener {
            sheet.dismiss()

            val time = SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            ).format(Date())

            adapter.addMessage(
                ChatMessage(
                    id = "local-deliver-${System.currentTimeMillis()}",
                    text = "",
                    time = time,
                    isSent = true,
                    type = ChatMessage.TYPE_DELIVER
                )
            )

            scrollToBottom()
        }

        sheet.setContentView(view)
        sheet.show()
    }

    private fun detectMessageType(content: String): Int {
        if (content.isBlank()) return ChatMessage.TYPE_NORMAL

        return try {
            val json = JSONObject(content)

            when {
                json.optString("kind") == "request" -> ChatMessage.TYPE_BOOK_SHARED
                json.optString("kind") == "deliver" -> ChatMessage.TYPE_DELIVER
                json.optString("kind") == "return" -> ChatMessage.TYPE_DELIVER
                else -> ChatMessage.TYPE_NORMAL
            }
        } catch (_: Exception) {
            ChatMessage.TYPE_NORMAL
        }
    }

    private fun scrollToBottom() {
        if (messages.isNotEmpty()) {
            binding.rvMessages.post {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun getMyId(): String {
        val id = sessionManager.getUserId()

        if (id.isNotBlank()) {
            return id
        }

        return sessionManager.getUserEmail()
    }

    private fun formatTime(iso: String?): String {
        if (iso.isNullOrBlank()) {
            return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        }

        return try {
            val fixedIso = if (
                iso.endsWith("Z", ignoreCase = true) ||
                Regex("[+-]\\d{2}:\\d{2}$").containsMatchIn(iso)
            ) {
                iso
            } else {
                "${iso}Z"
            }

            val parser = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                Locale.getDefault()
            )
            parser.timeZone = TimeZone.getTimeZone("UTC")

            val date = try {
                parser.parse(fixedIso)
            } catch (_: Exception) {
                val parser2 = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ssX",
                    Locale.getDefault()
                )
                parser2.timeZone = TimeZone.getTimeZone("UTC")
                parser2.parse(fixedIso)
            }

            val out = SimpleDateFormat("h:mm a", Locale.getDefault())

            if (date != null) {
                out.format(date)
            } else {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            }
        } catch (_: Exception) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        }
    }

    override fun onDestroyView() {
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.VISIBLE

        super.onDestroyView()
        _binding = null
    }
}