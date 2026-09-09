package com.artknower.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.artknower.app.data.model.MarketingContentDetail
import com.artknower.app.data.repository.MarketingRepository
import com.artknower.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingReviewScreen(
    marketingContentId: String,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var detail by remember { mutableStateOf<MarketingContentDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var publishErrorMessage by remember { mutableStateOf<String?>(null) }

    var isRegenerating by remember { mutableStateOf(false) }

    // Editable fields
    var captionText by remember { mutableStateOf("") }
    var hashtagsText by remember { mutableStateOf("") }
    var userModifiedCaption by remember { mutableStateOf(false) }
    var userModifiedHashtags by remember { mutableStateOf(false) }

    fun loadData() {
        coroutineScope.launch {
            MarketingRepository.getMarketingContentDetail(marketingContentId)
                .onSuccess { data ->
                    detail = data
                    if (!userModifiedCaption || captionText.isBlank()) {
                        captionText = data.content.caption ?: ""
                    }
                    if (!userModifiedHashtags || hashtagsText.isBlank()) {
                        hashtagsText = data.content.hashtags.joinToString(" ")
                    }
                }
                .onFailure { err ->
                    snackbarHostState.showSnackbar(err.message ?: "Failed to load marketing post.")
                }
            isLoading = false
        }
    }

    LaunchedEffect(marketingContentId) {
        loadData()
    }

    // Active polling when Instagram publishing is in progress (status == "ready")
    LaunchedEffect(marketingContentId, detail?.content?.status) {
        val currentStatus = detail?.content?.status
        if (currentStatus == "ready") {
            android.util.Log.d("MarketingRepository", "PUBLISH POLLING STARTED: marketing_content_id = $marketingContentId")
            var attempts = 0
            val maxAttempts = 60 // 60 * 2s = 120s (2 minutes max)
            while (isActive && attempts < maxAttempts && (detail?.content?.status == "ready" || detail?.content?.externalPostId.isNullOrBlank())) {
                delay(2000)
                attempts++
                MarketingRepository.getMarketingContentDetail(marketingContentId)
                    .onSuccess { data ->
                        android.util.Log.d(
                            "MarketingRepository",
                            "SUPABASE PUBLISH STATUS\nmarketing_content_id = $marketingContentId\nstatus = ${data.content.status}\nexternal_post_id = ${data.content.externalPostId}\npublished_at = ${data.content.publishedAt}"
                        )
                        detail = data
                        if (data.content.status == "published" && !data.content.externalPostId.isNullOrBlank()) {
                            android.util.Log.d(
                                "MarketingRepository",
                                "PUBLISH SUCCESS\nInstagram ID = ${data.content.externalPostId}"
                            )
                            android.util.Log.d("MarketingRepository", "PUBLISH LOADING STOPPED")
                            publishErrorMessage = null
                        }
                    }
                    .onFailure { err ->
                        android.util.Log.e("MarketingRepository", "Error checking Supabase publish status: ${err.message}")
                    }
            }
            if (attempts >= maxAttempts && detail?.content?.status == "ready") {
                android.util.Log.e("MarketingRepository", "PUBLISH FAILED\nreason = Publishing timeout after 2 minutes.")
                android.util.Log.d("MarketingRepository", "PUBLISH LOADING STOPPED")
                publishErrorMessage = "Publishing is taking longer than expected. Please check your Instagram account or tap Publish Again."
            }
        }
    }

    // Active polling when AI generation is in progress (status == "draft")
    LaunchedEffect(marketingContentId, detail?.content?.status, detail?.content?.caption, detail?.aiJob?.status) {
        val currentStatus = detail?.content?.status
        val jobStatus = detail?.aiJob?.status
        val isDraftPending = currentStatus == "draft" && (detail?.content?.caption.isNullOrBlank() || jobStatus in listOf("queued", "processing"))
        if (isDraftPending) {
            var attempts = 0
            val maxAttempts = 60
            while (isActive && attempts < maxAttempts && (detail?.content?.caption.isNullOrBlank() || detail?.aiJob?.status in listOf("queued", "processing"))) {
                delay(2500)
                attempts++
                MarketingRepository.getMarketingContentDetail(marketingContentId)
                    .onSuccess { data ->
                        detail = data
                        if (!userModifiedCaption || captionText.isBlank()) {
                            captionText = data.content.caption ?: ""
                        }
                        if (!userModifiedHashtags || hashtagsText.isBlank()) {
                            hashtagsText = data.content.hashtags.joinToString(" ")
                        }
                    }
            }
        }
    }

    val contentStatus = detail?.content?.status ?: "draft"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Marketing Post",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DarkText
                        )
                        Text(
                            text = "Instagram Campaign Asset",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                actions = {
                    // Status Chip in top bar
                    val (badgeBg, badgeText, badgeColor) = when (contentStatus) {
                        "published" -> Triple(Color(0xFFDCFCE7), "Published", Color(0xFF16A34A))
                        "ready" -> Triple(Color(0xFFDBEAFE), "Ready to Publish", Color(0xFF2563EB))
                        "archived" -> Triple(Color(0xFFF1F5F9), "Archived", Color.Gray)
                        else -> Triple(Color(0xFFFEF3C7), "Draft / Review", Color(0xFFD97706))
                    }
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleanBackground)
            )
        },
        containerColor = CleanBackground
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RustTerracotta)
            }
        } else {
            val isJobProcessing = detail?.aiJob?.status in listOf("queued", "processing") || (contentStatus == "draft" && detail?.content?.caption.isNullOrBlank())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── AI Processing Banner ──────────────────────────────────
                if (isJobProcessing) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFCA8A04),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Generating your marketing post...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF854D0E)
                                )
                                Text(
                                    text = "Crafting AI caption, hashtags and poster layout.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA16207)
                                )
                            }
                        }
                    }
                }

                // ── Published Success Banner ──────────────────────────────
                if (contentStatus == "published") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Live on Instagram!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = "Post ID: ${detail?.content?.externalPostId ?: "N/A"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }

                // ── Failure Banner (if publish or webhook failed) ─────────
                if (publishErrorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Publishing failed. Please try again.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = publishErrorMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }

                // ── Ready to Publish Banner ───────────────────────────────
                if (contentStatus == "ready" && publishErrorMessage == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF2563EB),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Publishing to Instagram...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = "Approved by artisan. Waiting for n8n & Instagram Graph API...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }
                    }
                }

                // ── Title / Headline Card (if available) ───────────────────
                val contentTitle = detail?.content?.title
                if (!contentTitle.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Marketing Headline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RustTerracotta
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = contentTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        }
                    }
                }

                // ── Poster Creative Image Card ────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = RustTerracotta,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Generated Creative Poster",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkText
                                )
                            }

                            Surface(
                                color = Color(0xFFFDF2F8),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Instagram 1:1",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFDB2777),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            val imgUrl = detail?.creativeImageUrl
                            if (!imgUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = "Marketing Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = RustTerracotta,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Generating creative poster...",
                                        fontSize = 12.sp,
                                        color = MutedText
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Linked Product Summary ────────────────────────────────
                val linkedProduct = detail?.product
                if (linkedProduct != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = RustTerracotta,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = linkedProduct.name ?: "Handcrafted Item",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkText
                                )
                                Text(
                                    text = "${linkedProduct.craftType ?: "Craft"} • ₹${linkedProduct.price?.toInt() ?: 0}",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }

                // ── Caption Section (Editable) ────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Instagram Caption",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkText
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Caption", captionText))
                                    Toast.makeText(context, "Caption copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Caption",
                                    tint = RustTerracotta,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = captionText,
                            onValueChange = {
                                captionText = it
                                userModifiedCaption = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("AI generating caption...", fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RustTerracotta,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }

                // ── Hashtags Section (Editable) ───────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hashtags",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkText
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Hashtags", hashtagsText))
                                    Toast.makeText(context, "Hashtags copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Hashtags",
                                    tint = RustTerracotta,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = hashtagsText,
                            onValueChange = {
                                hashtagsText = it
                                userModifiedHashtags = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("#Handmade #IndianArtisans ...", fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RustTerracotta,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }

                // ── Action Buttons ────────────────────────────────────────
                Spacer(modifier = Modifier.height(4.dp))

                when (contentStatus) {
                    "draft" -> {
                        // Approve & Publish Button (Terracotta)
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isPublishing = true
                                    publishErrorMessage = null
                                    // 1. Save edits first
                                    val tagsList = hashtagsText.split(" ", ",").filter { it.isNotBlank() }
                                    MarketingRepository.updateMarketingContent(marketingContentId, captionText, tagsList)
                                    // 2. Approve content & trigger n8n publishing webhook
                                    MarketingRepository.approveMarketingContent(marketingContentId)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar("Content approved! Triggered n8n publishing workflow...")
                                            loadData()
                                        }
                                        .onFailure { err ->
                                            publishErrorMessage = err.message ?: "Failed to reach publishing webhook."
                                            snackbarHostState.showSnackbar(err.message ?: "Approval/webhook dispatch failed.")
                                            loadData()
                                        }
                                    isPublishing = false
                                }
                            },
                            enabled = !isPublishing && !isSaving && !isRegenerating,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (isPublishing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Approve & Publish to Instagram", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        // Save Changes Button (Outlined)
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    isSaving = true
                                    val tagsList = hashtagsText.split(" ", ",").filter { it.isNotBlank() }
                                    MarketingRepository.updateMarketingContent(marketingContentId, captionText, tagsList)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar("Draft changes saved.")
                                            loadData()
                                        }
                                        .onFailure { err ->
                                            snackbarHostState.showSnackbar(err.message ?: "Failed to save draft.")
                                        }
                                    isSaving = false
                                }
                            },
                            enabled = !isSaving && !isRegenerating,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, RustTerracotta),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = RustTerracotta)
                            } else {
                                Text("Save Draft Edits", color = RustTerracotta, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Reject / Regenerate Option
                        TextButton(
                            onClick = {
                                val prodId = detail?.content?.productId ?: return@TextButton
                                coroutineScope.launch {
                                    isRegenerating = true
                                    MarketingRepository.generateMarketingContent(prodId)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar("Regeneration requested! Crafting new content...")
                                            userModifiedCaption = false
                                            userModifiedHashtags = false
                                            loadData()
                                        }
                                        .onFailure { err ->
                                            snackbarHostState.showSnackbar(err.message ?: "Failed to request regeneration.")
                                        }
                                    isRegenerating = false
                                }
                            },
                            enabled = !isRegenerating && !isPublishing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isRegenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MutedText)
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = MutedText)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text("Reject & Regenerate with AI", color = MutedText, fontSize = 13.sp)
                        }
                    }

                    "ready" -> {
                        // Publish Again / Retry Publishing Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isPublishing = true
                                    publishErrorMessage = null
                                    MarketingRepository.retryPublishWebhook(marketingContentId)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar("Publishing request sent to n8n! Awaiting Instagram confirmation...")
                                            loadData()
                                        }
                                        .onFailure { err ->
                                            publishErrorMessage = err.message ?: "Failed to reach n8n publishing webhook."
                                            snackbarHostState.showSnackbar(err.message ?: "Failed to reach n8n publishing webhook.")
                                            loadData()
                                        }
                                    isPublishing = false
                                }
                            },
                            enabled = !isPublishing,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)), // Instagram Pink
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (isPublishing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Publish Again", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    "published" -> {
                        Button(
                            onClick = onDone,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
