package com.artknower.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.data.model.BulkOrderMessageDto
import com.artknower.app.data.model.BulkOrderRequestDto
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.OrderRepository
import com.artknower.app.ui.theme.CardSurface
import com.artknower.app.ui.theme.CleanBackground
import com.artknower.app.ui.theme.DarkText
import com.artknower.app.ui.theme.GrayText
import com.artknower.app.ui.theme.RustTerracotta
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkChatScreen(
    request: BulkOrderRequestDto,
    onBack: () -> Unit,
    onStatusUpdated: () -> Unit
) {
    var messages by remember { mutableStateOf<List<BulkOrderMessageDto>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Agreement fields
    var finalQtyStr by remember { mutableStateOf((request.finalQuantity ?: request.requestedQuantity).toString()) }
    var finalPriceStr by remember { mutableStateOf((request.finalUnitPrice ?: request.requestedUnitPrice ?: 0.0).toString()) }
    var notesStr by remember { mutableStateOf(request.artisanNotes ?: "") }
    var isUpdatingStatus by remember { mutableStateOf(false) }
    var isConfirmingOrder by remember { mutableStateOf(false) }
    var showTermsSheet by remember { mutableStateOf(false) }
    var confirmationSuccessMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val currentUserId = AuthRepository.currentUserId
    val effectiveUserId = remember(request, currentUserId) {
        currentUserId?.ifBlank { null }
            ?: AuthRepository.currentArtisanProfile?.userId
            ?: AuthRepository.currentProfile?.id
            ?: request.artisanProfileId
    }

    suspend fun loadMessages(showLoading: Boolean = true) {
        if (showLoading && messages.isEmpty()) {
            isLoading = true
        }
        try {
            android.util.Log.d("B2B_CHAT_DEBUG", "BulkChatScreen: loadMessages called (showLoading=$showLoading) for requestId = '${request.id}'")
            OrderRepository.getBulkOrderMessages(request.id).onSuccess { list ->
                android.util.Log.d("B2B_CHAT_DEBUG", "BulkChatScreen: loadMessages SUCCESS, received ${list.size} messages")
                messages = list
                errorMessage = null
            }.onFailure { err ->
                android.util.Log.e("B2B_CHAT_DEBUG", "BulkChatScreen: loadMessages FAILURE: ${err.message}", err)
                if (messages.isEmpty()) {
                    errorMessage = "Unable to load messages. Please try again."
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("B2B_CHAT_DEBUG", "BulkChatScreen: loadMessages EXCEPTION: ${e.message}", e)
            if (messages.isEmpty()) {
                errorMessage = "Unable to load messages. Please try again."
            }
        } finally {
            isLoading = false
            android.util.Log.d("B2B_CHAT_DEBUG", "BulkChatScreen: loadMessages FINALLY, isLoading set to false")
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            try {
                listState.scrollToItem(messages.size - 1)
            } catch (_: Exception) {}
        }
    }

    fun triggerFetch(showLoading: Boolean = true) {
        scope.launch {
            loadMessages(showLoading)
        }
    }

    fun handleConfirmOrder() {
        if (isConfirmingOrder) return
        val uid = effectiveUserId
        val qty = finalQtyStr.toIntOrNull() ?: request.finalQuantity ?: request.requestedQuantity
        val price = finalPriceStr.toDoubleOrNull() ?: request.finalUnitPrice ?: request.requestedUnitPrice ?: 0.0

        if (qty <= 0) {
            errorMessage = "Final quantity must be greater than 0"
            return
        }

        scope.launch {
            isConfirmingOrder = true
            errorMessage = null
            try {
                // Ensure final terms are saved on the request first
                OrderRepository.updateBulkOrderStatus(
                    requestId = request.id,
                    status = if (request.status.equals("accepted", ignoreCase = true)) request.status else "accepted",
                    finalQuantity = qty,
                    finalUnitPrice = price,
                    artisanNotes = notesStr.ifBlank { null }
                )

                // Execute safe atomic/idempotent bulk order confirmation and inventory reservation
                OrderRepository.confirmBulkOrder(request.id, uid)
                    .onSuccess {
                        confirmationSuccessMessage = "Order confirmed successfully! Inventory reserved."
                        showTermsSheet = false
                        onStatusUpdated()
                    }
                    .onFailure { err ->
                        errorMessage = err.message ?: "Failed to confirm order."
                    }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An unexpected error occurred."
            } finally {
                isConfirmingOrder = false
            }
        }
    }

    LaunchedEffect(request.id) {
        val sessionUserId = AuthRepository.currentUserId
        val isLoggedIn = sessionUserId != null
        android.util.Log.d("B2B_CHAT_DEBUG", "BulkChatScreen OPENED | bulkOrderRequestId = '${request.id}', sessionUserId = '$sessionUserId', effectiveUserId = '$effectiveUserId', isLoggedIn = $isLoggedIn")
        loadMessages(showLoading = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = request.businessProfile?.businessName ?: "Bulk Buyer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkText
                        )
                        Text(
                            text = "Product: ${request.product?.name ?: "Item"} • Status: ${request.status.uppercase()}",
                            fontSize = 11.sp,
                            color = GrayText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                actions = {
                    IconButton(onClick = { triggerFetch(showLoading = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = RustTerracotta)
                    }
                    Button(
                        onClick = { showTermsSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Terms & Status", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleanBackground)
            )
        },
        containerColor = CleanBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Requested: ${request.requestedQuantity} units @ ₹${request.requestedUnitPrice ?: 0.0}/unit",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = DarkText
                            )
                            if (request.buyerNotes != null) {
                                Text(
                                    text = "Buyer Note: \"${request.buyerNotes}\"",
                                    fontSize = 11.sp,
                                    color = GrayText
                                )
                            }
                        }
                        if (request.finalQuantity != null || request.finalUnitPrice != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RustTerracotta.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Agreed: ${request.finalQuantity ?: request.requestedQuantity} @ ₹${request.finalUnitPrice ?: 0.0}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RustTerracotta,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Direct Confirm Action if not yet confirmed
                    if (!request.status.equals("confirmed", ignoreCase = true) &&
                        !request.status.equals("rejected", ignoreCase = true) &&
                        !request.status.equals("cancelled", ignoreCase = true)) {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { handleConfirmOrder() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            enabled = !isConfirmingOrder && !isUpdatingStatus,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isConfirmingOrder) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Reserving Stock & Confirming...", fontSize = 13.sp, color = Color.White)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Confirm & Create Order (${finalQtyStr} units @ ₹${finalPriceStr})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (request.status.equals("confirmed", ignoreCase = true)) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Order Confirmed • Inventory Reserved", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Success feedback banner
            confirmationSuccessMessage?.let { msg ->
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = msg, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
            }

            // Error banner if any when messages are already present
            if (errorMessage != null && messages.isNotEmpty()) {
                Surface(
                    color = Color.Red.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = errorMessage ?: "", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                }
            }

            // Chat Messages list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = RustTerracotta)
                } else if (errorMessage != null && messages.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Unable to load messages. Please try again.", fontSize = 14.sp, color = Color.Red)
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { triggerFetch(showLoading = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                } else if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = GrayText, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No messages yet. Start the conversation.", fontSize = 14.sp, color = GrayText, fontWeight = FontWeight.Medium)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(messages) { msg ->
                            val isMe = (msg.senderUserId == effectiveUserId) ||
                                       (currentUserId != null && msg.senderUserId == currentUserId) ||
                                       (msg.senderUserId == request.artisanProfileId)
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (isMe) 14.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 14.dp
                                    ),
                                    color = if (isMe) RustTerracotta else Color.White,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (isMe) "You (Artisan)" else (request.businessProfile?.businessName ?: "Buyer"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.8f) else RustTerracotta
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = msg.displayMessageText,
                                            fontSize = 14.sp,
                                            color = if (isMe) Color.White else DarkText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type message to buyer...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val textToSend = messageText.trim()
                            val senderId = effectiveUserId
                            if (textToSend.isNotEmpty() && !isSending && senderId != null) {
                                scope.launch {
                                    isSending = true
                                    OrderRepository.sendBulkOrderMessage(request.id, senderId, textToSend)
                                        .onSuccess {
                                            messageText = ""
                                            loadMessages(showLoading = false)
                                        }
                                        .onFailure { err ->
                                            android.util.Log.e("BulkChatScreen", "Send message error: ${err.message}", err)
                                            errorMessage = "Message could not be sent. Please try again."
                                        }
                                    isSending = false
                                }
                            }
                        },
                        enabled = messageText.trim().isNotEmpty() && !isSending,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (messageText.trim().isNotEmpty() && !isSending) RustTerracotta else GrayText.copy(alpha = 0.3f))
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
    // Modal Sheet to update Terms & Status
    if (showTermsSheet) {
        AlertDialog(
            onDismissRequest = { showTermsSheet = false },
            title = { Text("Update Request Terms & Status", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = finalQtyStr,
                            onValueChange = { finalQtyStr = it },
                            label = { Text("Final Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = finalPriceStr,
                            onValueChange = { finalPriceStr = it },
                            label = { Text("Price/Unit (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("Artisan Notes") },
                        placeholder = { Text("Add custom terms or timeline details...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { handleConfirmOrder() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        enabled = !isConfirmingOrder && !isUpdatingStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isConfirmingOrder) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Confirming...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Confirm & Create Order")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isUpdatingStatus = true
                                    val qty = finalQtyStr.toIntOrNull()
                                    val price = finalPriceStr.toDoubleOrNull()
                                    OrderRepository.updateBulkOrderStatus(request.id, "rejected", qty, price, notesStr).onSuccess {
                                        showTermsSheet = false
                                        onStatusUpdated()
                                    }
                                    isUpdatingStatus = false
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            enabled = !isUpdatingStatus && !isConfirmingOrder
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reject")
                        }

                        Spacer(Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isUpdatingStatus = true
                                    val qty = finalQtyStr.toIntOrNull()
                                    val price = finalPriceStr.toDoubleOrNull()
                                    OrderRepository.updateBulkOrderStatus(request.id, "accepted", qty, price, notesStr).onSuccess {
                                        showTermsSheet = false
                                        onStatusUpdated()
                                    }
                                    isUpdatingStatus = false
                                }
                            },
                            enabled = !isUpdatingStatus && !isConfirmingOrder
                        ) {
                            Text("Save Terms")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTermsSheet = false },
                    enabled = !isConfirmingOrder && !isUpdatingStatus
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
