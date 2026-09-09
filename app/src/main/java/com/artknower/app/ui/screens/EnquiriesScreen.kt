package com.artknower.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QuestionAnswer
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
import com.artknower.app.data.model.CustomerEnquiryDto
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.EnquiryRepository
import com.artknower.app.ui.components.AppBottomBar
import com.artknower.app.ui.components.HomeTab
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
fun EnquiriesScreen(
    onNavigateToTab: (HomeTab) -> Unit
) {
    var enquiries by remember { mutableStateOf<List<CustomerEnquiryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedEnquiryForResponse by remember { mutableStateOf<CustomerEnquiryDto?>(null) }
    var closingEnquiryId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun loadEnquiries() {
        val artisanId = AuthRepository.currentArtisanProfileId
        if (!artisanId.isNullOrBlank()) {
            scope.launch {
                isLoading = true
                EnquiryRepository.getCustomerEnquiries(artisanId).onSuccess { list ->
                    enquiries = list
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Failed to load enquiries: ${err.message}")
                }
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadEnquiries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.customer_enquiries_title), fontWeight = FontWeight.Bold, color = DarkText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleanBackground)
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = HomeTab.HOME,
                onTabSelected = onNavigateToTab
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CleanBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = RustTerracotta)
            } else if (enquiries.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.no_enquiries), color = DarkText, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(enquiries) { enquiry ->
                        EnquiryCard(
                            enquiry = enquiry,
                            isClosing = closingEnquiryId == enquiry.id,
                            onRespondClick = {
                                if (enquiry.status.lowercase() != "closed") {
                                    selectedEnquiryForResponse = enquiry
                                }
                            },
                            onCloseClick = {
                                if (closingEnquiryId == null) {
                                    scope.launch {
                                        closingEnquiryId = enquiry.id
                                        EnquiryRepository.closeEnquiry(enquiry.id)
                                            .onSuccess {
                                                snackbarHostState.showSnackbar("Enquiry marked as closed.")
                                                loadEnquiries()
                                            }
                                            .onFailure { err ->
                                                snackbarHostState.showSnackbar("Failed to close enquiry: ${err.message}")
                                            }
                                        closingEnquiryId = null
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Response Dialog
    selectedEnquiryForResponse?.let { enquiry ->
        EnquiryResponseDialog(
            enquiry = enquiry,
            onDismiss = { selectedEnquiryForResponse = null },
            onSubmitResponse = { responseText, onComplete ->
                scope.launch {
                    EnquiryRepository.respondToEnquiry(enquiry.id, responseText).onSuccess {
                        snackbarHostState.showSnackbar("Response sent to customer!")
                        selectedEnquiryForResponse = null
                        loadEnquiries()
                    }.onFailure { err ->
                        snackbarHostState.showSnackbar("Failed to send response: ${err.message}")
                    }
                    onComplete()
                }
            }
        )
    }
}

@Composable
fun EnquiryCard(
    enquiry: CustomerEnquiryDto,
    isClosing: Boolean = false,
    onRespondClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val statusLower = enquiry.status.lowercase()
    val isClosed = statusLower == "closed"
    val isResponded = statusLower == "responded" || (!isClosed && !enquiry.artisanResponse.isNullOrBlank())
    val isOpen = statusLower == "open" && enquiry.artisanResponse.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer Enquiry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkText
                )

                val (badgeText, statusColor) = when {
                    isClosed -> "CLOSED" to Color.Gray
                    isResponded -> "RESPONDED" to Color(0xFF2E7D32)
                    else -> "OPEN" to Color(0xFFE65100)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            enquiry.product?.let { prod ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Product: ${prod.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RustTerracotta
                )
            }

            enquiry.subject?.let { subj ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subj,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = enquiry.message,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkText
            )

            enquiry.artisanResponse?.let { resp ->
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Your Response:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RustTerracotta
                        )
                        Text(
                            text = resp,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkText
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rawDate = enquiry.createdAt ?: ""
                val formattedDate = try {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val formatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                    val date = if (rawDate.isNotBlank()) parser.parse(rawDate) else null
                    if (date != null) formatter.format(date) else rawDate
                } catch (e: Exception) {
                    rawDate
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isOpen) {
                        Button(
                            onClick = onRespondClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Respond", fontSize = 12.sp)
                        }
                    } else if (isResponded && !isClosed) {
                        OutlinedButton(
                            onClick = onCloseClick,
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isClosing,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF616161)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (isClosing) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text("Closing...", fontSize = 12.sp)
                            } else {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Close Enquiry", fontSize = 12.sp)
                            }
                        }
                    } else if (isClosed) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.LightGray.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Closed", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnquiryResponseDialog(
    enquiry: CustomerEnquiryDto,
    onDismiss: () -> Unit,
    onSubmitResponse: (responseText: String, onComplete: () -> Unit) -> Unit
) {
    var responseText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Respond to Enquiry", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Enquiry ID: ${enquiry.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
                Text(
                    text = "\"${enquiry.message}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    label = { Text("Your Response") },
                    placeholder = { Text("Write details, pricing, or custom requirements...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !isSubmitting
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (responseText.isNotBlank() && !isSubmitting) {
                        isSubmitting = true
                        onSubmitResponse(responseText.trim()) {
                            isSubmitting = false
                        }
                    }
                },
                enabled = responseText.isNotBlank() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Sending...", fontSize = 13.sp)
                } else {
                    Text("Send Response")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancel")
            }
        }
    )
}

