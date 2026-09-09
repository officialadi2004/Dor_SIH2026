package com.artknower.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.R
import com.artknower.app.data.model.BulkOrderMessageDto
import com.artknower.app.data.model.BulkOrderRequestDto
import com.artknower.app.data.model.OrderDto
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.OrderRepository
import com.artknower.app.ui.components.AppBottomBar
import com.artknower.app.ui.components.HomeTab
import com.artknower.app.ui.theme.CardSurface
import com.artknower.app.ui.theme.CleanBackground
import com.artknower.app.ui.theme.DarkText
import com.artknower.app.ui.theme.GrayText
import com.artknower.app.ui.theme.RustTerracotta
import com.artknower.app.ui.theme.StatusBlue
import com.artknower.app.ui.theme.StatusBlueBg
import com.artknower.app.ui.theme.StatusGreen
import com.artknower.app.ui.theme.StatusGreenBg
import com.artknower.app.ui.theme.StatusRed
import com.artknower.app.ui.theme.StatusRedBg
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

enum class OrderFilterTab {
    BULK_REQUESTS,
    CONFIRMED_ORDERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onNavigateToTab: (HomeTab) -> Unit
) {
    var selectedTab by remember { mutableStateOf(OrderFilterTab.BULK_REQUESTS) }
    var bulkRequests by remember { mutableStateOf<List<BulkOrderRequestDto>>(emptyList()) }
    var confirmedOrders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedRequestForDetail by remember { mutableStateOf<BulkOrderRequestDto?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun loadData() {
        val artisanId = AuthRepository.currentArtisanProfileId
        if (!artisanId.isNullOrBlank()) {
            scope.launch {
                isLoading = true
                OrderRepository.getBulkOrderRequests(artisanId).onSuccess { list: List<BulkOrderRequestDto> ->
                    bulkRequests = list.sortedByDescending { it.requestedAt }
                }
                OrderRepository.getConfirmedOrders(artisanId).onSuccess { list: List<OrderDto> ->
                    confirmedOrders = list
                }
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CleanBackground)) {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.business_orders),
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CleanBackground)
                )

                // Pill-style segmented tab control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1EDE8)),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    listOf(
                        OrderFilterTab.BULK_REQUESTS to stringResource(R.string.bulk_requests_tab, bulkRequests.size),
                        OrderFilterTab.CONFIRMED_ORDERS to stringResource(R.string.confirmed_orders_tab, confirmedOrders.size)
                    ).forEach { (tab, label) ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) RustTerracotta
                                    else Color.Transparent
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else GrayText,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            AppBottomBar(
                currentTab = HomeTab.ORDERS,
                onTabSelected = onNavigateToTab
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CleanBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = RustTerracotta)
            } else if (selectedTab == OrderFilterTab.BULK_REQUESTS) {
                if (bulkRequests.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.BusinessCenter, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.no_orders), color = DarkText)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(bulkRequests) { request ->
                            BulkRequestCard(
                                request = request,
                                onClick = {
                                    android.util.Log.d("B2B_CHAT_DEBUG", "BulkRequestCard CLICKED: passing bulk_order_requests.id = '${request.id}', product = '${request.product?.name}'")
                                    selectedRequestForDetail = request
                                }
                            )
                        }
                    }
                }
            } else {
                if (confirmedOrders.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.no_confirmed_orders), color = DarkText)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(confirmedOrders) { order ->
                            ConfirmedOrderCard(order = order)
                        }
                    }
                }
            }
        }
    }

    // Full screen chat & negotiation view
    selectedRequestForDetail?.let { req ->
        BulkChatScreen(
            request = req,
            onBack = { selectedRequestForDetail = null },
            onStatusUpdated = {
                selectedRequestForDetail = null
                loadData()
            }
        )
    }
}

@Composable
fun BulkRequestCard(
    request: BulkOrderRequestDto,
    onClick: () -> Unit
) {
    val statusColor = when (request.status.lowercase()) {
        "accepted", "confirmed" -> StatusGreen
        "rejected", "cancelled" -> StatusRed
        "clarification_required" -> StatusBlue
        else -> Color(0xFFE65100) // under_review, requested
    }
    val statusBg = when (request.status.lowercase()) {
        "accepted", "confirmed" -> StatusGreenBg
        "rejected", "cancelled" -> StatusRedBg
        "clarification_required" -> StatusBlueBg
        else -> Color(0xFFFFF3E0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left status bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        color = statusColor,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Column(Modifier.weight(1f).padding(16.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = request.businessProfile?.businessName ?: "Web Buyer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = request.status.uppercase(),
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(stringResource(R.string.product_label, request.product?.name ?: "Craft Item"), fontWeight = FontWeight.SemiBold, color = DarkText)
                Text(stringResource(R.string.requested_quantity, request.requestedQuantity), color = DarkText)
                request.requestedUnitPrice?.let {
                    Text(stringResource(R.string.requested_unit_price, it.toString()), color = DarkText)
                }
                request.requiredByDate?.let {
                    Text(stringResource(R.string.required_by_label, it), color = DarkText, fontSize = 13.sp)
                }

                if (request.finalQuantity != null || request.finalUnitPrice != null) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RustTerracotta.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        val qty = request.finalQuantity ?: request.requestedQuantity
                        val price = request.finalUnitPrice ?: request.requestedUnitPrice ?: 0.0
                        Text(
                            text = stringResource(R.string.agreed_terms_label, qty, price.toString()),
                            fontWeight = FontWeight.Bold,
                            color = RustTerracotta,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedDate = try {
                        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val date = parser.parse(request.requestedAt)
                        if (date != null) formatter.format(date) else request.requestedAt
                    } catch (e: Exception) {
                        request.requestedAt
                    }

                    Text(
                        stringResource(R.string.requested_label, formattedDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayText
                    )

                    Text(
                        text = stringResource(R.string.tap_to_review),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = RustTerracotta
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmedOrderCard(order: OrderDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Green left accent bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        color = StatusGreen,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Column(Modifier.weight(1f).padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.businessProfile?.businessName ?: "Business Order",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusGreenBg
                    ) {
                        Text(
                            text = "CONFIRMED",
                            color = StatusGreen,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.order_id_label, order.id.take(8) + "..."),
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
                val productName = order.bulkOrderRequest?.product?.name ?: order.product?.name
                if (!productName.isNullOrBlank()) {
                    Text(stringResource(R.string.product_label, productName), fontWeight = FontWeight.SemiBold, color = DarkText)
                }

                order.bulkOrderRequest?.let { req ->
                    val qty = req.finalQuantity ?: req.requestedQuantity
                    val price = req.finalUnitPrice ?: req.requestedUnitPrice
                    Text(stringResource(R.string.confirmed_qty_label, qty.toString()), color = DarkText)
                    price?.let {
                        Text(stringResource(R.string.unit_price_label, it.toString()), color = DarkText)
                        // Show computed total value
                        val total = qty * it
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StatusGreenBg
                        ) {
                            Text(
                                text = stringResource(R.string.total_value_label, String.format("%.0f", total)),
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                val formattedDate = try {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val formatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                    val date = parser.parse(order.confirmedAt)
                    if (date != null) formatter.format(date) else order.confirmedAt
                } catch (e: Exception) {
                    order.confirmedAt
                }

                Text(
                    stringResource(R.string.confirmed_at_label, formattedDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkRequestDetailDialog(
    request: BulkOrderRequestDto,
    onDismiss: () -> Unit,
    onUpdateStatus: (newStatus: String, finalQty: Int?, finalPrice: Double?, notes: String?) -> Unit
) {
    var finalQtyStr by remember { mutableStateOf((request.finalQuantity ?: request.requestedQuantity).toString()) }
    var finalPriceStr by remember { mutableStateOf((request.finalUnitPrice ?: request.requestedUnitPrice ?: 0.0).toString()) }
    var notesStr by remember { mutableStateOf(request.artisanNotes ?: "") }

    var messages by remember { mutableStateOf<List<BulkOrderMessageDto>>(emptyList()) }
    var newMessageText by remember { mutableStateOf("") }
    var isLoadingMessages by remember { mutableStateOf(true) }
    var sendErrorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadMessages() {
        scope.launch {
            OrderRepository.getBulkOrderMessages(request.id).onSuccess { list: List<BulkOrderMessageDto> ->
                messages = list
            }
            isLoadingMessages = false
        }
    }

    LaunchedEffect(request.id) {
        loadMessages()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = RustTerracotta)
                Spacer(Modifier.width(8.dp))
                Text("Bulk Order Review", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Business: ${request.businessProfile?.businessName ?: "Buyer"}", fontWeight = FontWeight.Bold, color = DarkText)
                    Text("Product: ${request.product?.name ?: "Item"}", color = DarkText)
                    Text("Requested Qty: ${request.requestedQuantity}", color = DarkText)
                    request.requestedUnitPrice?.let { Text("Requested Unit Price: ₹$it", color = DarkText) }
                    request.buyerNotes?.let {
                        Text("Buyer Notes: \"$it\"", style = MaterialTheme.typography.bodySmall, color = GrayText, modifier = Modifier.padding(top = 4.dp))
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                }

                item {
                    Text("Counter-Offer & Final Agreement Terms", fontWeight = FontWeight.Bold, color = RustTerracotta)
                    Spacer(Modifier.height(6.dp))

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

                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("Artisan Notes") },
                        placeholder = { Text("Add delivery timeframe or custom notes...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(Modifier.padding(vertical = 10.dp))
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Message, contentDescription = null, tint = RustTerracotta, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Messages with Buyer", fontWeight = FontWeight.Bold, color = DarkText)
                    }
                    Spacer(Modifier.height(6.dp))
                }

                if (isLoadingMessages) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = RustTerracotta)
                    }
                } else if (messages.isEmpty()) {
                    item {
                        Text("No messages exchanged yet.", style = MaterialTheme.typography.bodySmall, color = GrayText)
                    }
                } else {
                    items(messages) { msg ->
                        val isMe = msg.senderUserId == AuthRepository.currentUserId
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isMe) RustTerracotta.copy(alpha = 0.15f) else Color(0xFFE0E0E0),
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Column(Modifier.padding(8.dp)) {
                                    Text(
                                        text = if (isMe) "You (Artisan)" else "Buyer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isMe) RustTerracotta else DarkText
                                    )
                                    Text(text = msg.displayMessageText, fontSize = 13.sp, color = DarkText)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            placeholder = { Text("Type message...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                val currentUid = AuthRepository.currentUserId
                                if (newMessageText.isNotBlank() && currentUid != null) {
                                    scope.launch {
                                        val txt = newMessageText
                                        newMessageText = ""
                                        OrderRepository.sendBulkOrderMessage(request.id, currentUid, txt)
                                            .onSuccess {
                                                sendErrorMsg = null
                                                loadMessages()
                                            }
                                            .onFailure { err ->
                                                sendErrorMsg = err.message ?: "Failed to send message"
                                            }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = RustTerracotta)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val qty = finalQtyStr.toIntOrNull()
                        val price = finalPriceStr.toDoubleOrNull()
                        onUpdateStatus("REJECTED", qty, price, notesStr)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_reject))
                }

                Button(
                    onClick = {
                        val qty = finalQtyStr.toIntOrNull()
                        val price = finalPriceStr.toDoubleOrNull()
                        onUpdateStatus("ACCEPTED", qty, price, notesStr)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_accept))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}
