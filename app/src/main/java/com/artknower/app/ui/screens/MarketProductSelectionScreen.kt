package com.artknower.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.artknower.app.R
import com.artknower.app.data.model.MarketableProduct
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.MarketingRepository
import com.artknower.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketProductSelectionScreen(
    onBack: () -> Unit,
    onNavigateToReview: (marketingContentId: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var products by remember { mutableStateOf<List<MarketableProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var generatingProductId by remember { mutableStateOf<String?>(null) }

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedAvailability by remember { mutableStateOf("All") }

    fun loadProducts() {
        val artisanId = AuthRepository.currentArtisanProfileId ?: return
        coroutineScope.launch {
            isLoading = true
            MarketingRepository.getMarketableProducts(artisanId)
                .onSuccess { list ->
                    products = list
                }
                .onFailure { error ->
                    snackbarHostState.showSnackbar(error.message ?: "Failed to load products.")
                }
            isLoading = false
        }
    }

    LaunchedEffect(AuthRepository.currentArtisanProfileId) {
        loadProducts()
    }

    // Categories list for filter chips
    val categories = remember(products) {
        listOf("All") + products.mapNotNull { it.categoryName }.distinct()
    }

    // Filtered products list
    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedAvailability) {
        products.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    (item.product.name?.contains(searchQuery, ignoreCase = true) == true) ||
                    (item.product.craftType?.contains(searchQuery, ignoreCase = true) == true) ||
                    (item.categoryName?.contains(searchQuery, ignoreCase = true) == true)

            val matchesCategory = selectedCategory == "All" || item.categoryName == selectedCategory

            val matchesAvailability = when (selectedAvailability) {
                "In Stock" -> item.quantityOnHand > 0
                "Low Stock" -> item.quantityOnHand in 1..5
                else -> true
            }

            matchesSearch && matchesCategory && matchesAvailability
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Market Your Product",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = DarkText
                        )
                        Text(
                            text = "Select a published product to promote",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkText
                        )
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
            // ── Search Box ───────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                color = CardSurface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MutedText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search products by name, craft, or category...", fontSize = 14.sp, color = MutedText)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Category Filter Chips ─────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RustTerracotta.copy(alpha = 0.15f),
                            selectedLabelColor = RustTerracotta,
                            containerColor = CardSurface,
                            labelColor = DarkText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) RustTerracotta else Color(0xFFE2E8F0),
                            selectedBorderColor = RustTerracotta,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Product List / Empty State ────────────────────────────────
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
                } else if (filteredProducts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (products.isEmpty()) "No published products found" else "No matching products",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (products.isEmpty())
                                "Only published products can be marketed. Publish your craft items first."
                            else "Try adjusting your search or category filter.",
                            fontSize = 13.sp,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProducts, key = { it.product.id ?: "" }) { item ->
                            MarketableProductCard(
                                item = item,
                                isGenerating = generatingProductId == item.product.id,
                                onMarketClick = {
                                    val prodId = item.product.id ?: return@MarketableProductCard
                                    
                                    // If already has an active marketing content, navigate directly
                                    if (!item.activeMarketingContentId.isNullOrBlank()) {
                                        onNavigateToReview(item.activeMarketingContentId)
                                        return@MarketableProductCard
                                    }

                                    coroutineScope.launch {
                                        generatingProductId = prodId
                                        MarketingRepository.generateMarketingContent(prodId)
                                            .onSuccess { result ->
                                                onNavigateToReview(result.marketingContentId)
                                            }
                                            .onFailure { err ->
                                                snackbarHostState.showSnackbar(err.message ?: "Generation request failed.")
                                            }
                                        generatingProductId = null
                                    }
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
private fun MarketableProductCard(
    item: MarketableProduct,
    isGenerating: Boolean,
    onMarketClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                if (!item.primaryImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.primaryImageUrl,
                        contentDescription = item.product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.product.name ?: "Handcrafted Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Stock badge
                    val isLowStock = item.quantityOnHand in 1..5
                    Surface(
                        color = if (isLowStock) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isLowStock) "Low Stock (${item.quantityOnHand})" else "In Stock (${item.quantityOnHand})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) Color(0xFFD97706) else Color(0xFF16A34A),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Category & Craft Type
                Text(
                    text = "${item.categoryName ?: "Craft"} • ${item.product.craftType ?: "Handmade"}",
                    fontSize = 12.sp,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price & Market Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${item.product.price?.toInt() ?: 0}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = RustTerracotta
                    )

                    Button(
                        onClick = onMarketClick,
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!item.activeMarketingContentId.isNullOrBlank()) DeepNavy else RustTerracotta
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (!item.activeMarketingContentId.isNullOrBlank()) "View Campaign" else "Market This Product",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
