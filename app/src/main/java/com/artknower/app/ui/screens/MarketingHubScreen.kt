package com.artknower.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.artknower.app.R
import com.artknower.app.data.model.MarketingContentDetail
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.MarketingRepository
import com.artknower.app.ui.components.AppBottomBar
import com.artknower.app.ui.components.HomeTab
import com.artknower.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingHubScreen(
    onBack: () -> Unit,
    onNavigateToTab: (HomeTab) -> Unit = {},
    onMarketNewProductClick: () -> Unit,
    onContentClick: (marketingContentId: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var contents by remember { mutableStateOf<List<MarketingContentDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Review, 2: Ready, 3: Published

    val tabs = listOf(
        stringResource(R.string.tab_all),
        stringResource(R.string.tab_review),
        stringResource(R.string.tab_ready),
        stringResource(R.string.tab_published)
    )

    fun loadMarketingContents() {
        val artisanId = AuthRepository.currentArtisanProfileId ?: return
        coroutineScope.launch {
            isLoading = true
            MarketingRepository.getMarketingContents(artisanId)
                .onSuccess { list ->
                    contents = list
                }
            isLoading = false
        }
    }

    LaunchedEffect(AuthRepository.currentArtisanProfileId) {
        loadMarketingContents()
    }

    val filteredContents = remember(contents, selectedTab) {
        when (selectedTab) {
            1 -> contents.filter { it.content.status.lowercase() == "draft" }
            2 -> contents.filter { it.content.status.lowercase() == "ready" }
            3 -> contents.filter { it.content.status.lowercase() == "published" }
            else -> contents.filter { it.content.status.lowercase() != "archived" }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.tab_marketing),
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = DarkText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleanBackground)
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = HomeTab.MARKETING,
                onTabSelected = onNavigateToTab
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onMarketNewProductClick,
                containerColor = RustTerracotta,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                text = { Text(stringResource(R.string.btn_market), fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = CleanBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Hero Banner ──────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.marketing_amplify_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.marketing_amplify_subtitle),
                            fontSize = 12.sp,
                            color = MutedText,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onMarketNewProductClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(stringResource(R.string.marketing_new_post), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Pill-style Tab Row ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1EDE8)),
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) RustTerracotta else Color.Transparent
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MutedText,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Content List / Empty State ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = RustTerracotta
                    )
                } else if (filteredContents.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFCE7F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFDB2777),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (contents.isEmpty())
                                stringResource(R.string.marketing_no_campaigns)
                            else stringResource(R.string.marketing_no_tab_posts),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (contents.isEmpty())
                                stringResource(R.string.marketing_no_campaigns_sub)
                            else stringResource(R.string.marketing_no_tab_posts_sub),
                            fontSize = 13.sp,
                            color = MutedText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (contents.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onMarketNewProductClick,
                                colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.marketing_market_now))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredContents, key = { it.content.id ?: "" }) { item ->
                            MarketingContentCard(
                                item = item,
                                onClick = {
                                    val contentId = item.content.id ?: return@MarketingContentCard
                                    onContentClick(contentId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketingContentCard(
    item: MarketingContentDetail,
    onClick: () -> Unit
) {
    val status = item.content.status.lowercase()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Creative / Product Thumbnail
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    val imgUrl = item.creativeImageUrl ?: item.productImageUrl
                    if (!imgUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = item.product?.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(28.dp).align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.product?.name ?: "Marketed Craft",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Status Badge
                        val (bg, txt, color) = when (status) {
                            "published" -> Triple(Color(0xFFDCFCE7), "Published", Color(0xFF16A34A))
                            "ready" -> Triple(Color(0xFFDBEAFE), "Ready", Color(0xFF2563EB))
                            "archived" -> Triple(Color(0xFFF1F5F9), "Archived", Color.Gray)
                            else -> Triple(Color(0xFFFEF3C7), "Review", Color(0xFFD97706))
                        }
                        Surface(
                            color = bg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = txt,
                                color = color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.content.caption ?: "AI generated caption & hashtags ready for review.",
                        fontSize = 12.sp,
                        color = MutedText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFFE1306C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.marketing_instagram_post),
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (status) {
                            "published" -> stringResource(R.string.marketing_view_post)
                            "ready" -> stringResource(R.string.marketing_publish_now)
                            else -> stringResource(R.string.marketing_review_edit)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RustTerracotta
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = RustTerracotta,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
