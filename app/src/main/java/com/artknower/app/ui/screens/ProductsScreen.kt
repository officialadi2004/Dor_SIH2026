package com.artknower.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.R
import com.artknower.app.data.model.ProductDto
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.ProductRepository
import com.artknower.app.ui.components.AppBottomBar
import com.artknower.app.ui.components.HomeTab
import com.artknower.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onNavigateToTab: (HomeTab) -> Unit,
    onAddProductClick: () -> Unit,
    onMarketProductClick: (productId: String) -> Unit = {}
) {
    var products by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val artisanId = AuthRepository.currentArtisanProfileId
        if (!artisanId.isNullOrBlank()) {
            ProductRepository.getProducts(artisanId).onSuccess { list ->
                products = list
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.my_products),
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleanBackground)
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = HomeTab.PRODUCTS,
                onTabSelected = onNavigateToTab
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProductClick,
                containerColor = RustTerracotta,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        stringResource(R.string.add_product),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            )
        },
        containerColor = CleanBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = RustTerracotta,
                    strokeWidth = 3.dp
                )
            } else if (products.isEmpty()) {
                // Premium empty state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PillTerracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = RustTerracotta,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        stringResource(R.string.no_products),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.no_products_sub),
                        style = MaterialTheme.typography.bodyMedium.copy(color = GrayText),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddProductClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.add_product),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onMarketClick = { onMarketProductClick(product.id ?: "") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductDto,
    onMarketClick: () -> Unit
) {
    val isPublished = product.status.lowercase() == "published"

    val (statusBg, statusText, statusColor) = when (product.status.lowercase()) {
        "published" -> Triple(StatusGreenBg, "Published", StatusGreen)
        "draft"     -> Triple(StatusAmberBg, "Draft", StatusAmber)
        "rejected"  -> Triple(StatusRedBg, "Rejected", StatusRed)
        else        -> Triple(Color(0xFFF1F5F9), product.status.replaceFirstChar { it.uppercase() }, MutedText)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar matching status
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        color = statusColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.name ?: stringResource(R.string.unnamed_product),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkText,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Status badge pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    stringResource(R.string.price_label, product.price ?: "N/A"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkText,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                if (isPublished && !product.id.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = ThinBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onMarketClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_market),
                            color = RustTerracotta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
}
