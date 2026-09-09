package com.artknower.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.artknower.app.data.model.EnhancedImageItem
import com.artknower.app.data.model.PricingRecommendResponse
import com.artknower.app.data.model.ProductCategoryDto
import com.artknower.app.data.model.ProductDto
import com.artknower.app.data.model.ProductPricingRequest
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.ImageEnhanceRepository
import com.artknower.app.data.repository.PricingRepository
import com.artknower.app.data.repository.ProductRepository
import com.artknower.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

// ── Status options aligned to the DB enum ──────────────────────────────────
private val STATUS_OPTIONS = listOf("draft", "published")

// ── Craft types for the picker ─────────────────────────────────────────────
private val CRAFT_TYPES = listOf(
    "Pottery", "Weaving", "Embroidery", "Woodcraft",
    "Metalwork", "Jewellery", "Painting", "Bamboo", "Handicrafts", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    onProductSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Persistent Product UUID across the flow ────────────────────────────
    val productUuid = remember { mutableStateOf(UUID.randomUUID().toString()) }

    // ── Categories from DB ──────────────────────────────────────────────────
    var categories by remember { mutableStateOf<List<ProductCategoryDto>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<ProductCategoryDto?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ProductRepository.getCategories()
            .onSuccess { cats ->
                categories = cats
                if (cats.isNotEmpty() && selectedCategory == null) {
                    selectedCategory = cats.firstOrNull()
                }
            }
    }

    // ── Form state aligned to products & inventory tables ──────────────────
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCraftType by remember { mutableStateOf(CRAFT_TYPES[0]) }
    var material by remember { mutableStateOf("") }
    var dimensions by remember { mutableStateOf("") }
    var primaryColour by remember { mutableStateOf("") }
    var secondaryColour by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockQuantityText by remember { mutableStateOf("1") }
    var lowStockThresholdText by remember { mutableStateOf("5") }
    var selectedStatus by remember { mutableStateOf(STATUS_OPTIONS[0]) }

    // ── Cost Input Fields for ML Pricing ───────────────────────────────────
    var rawMaterialCostText by remember { mutableStateOf("") }
    var labourCostText by remember { mutableStateOf("") }
    var inputCostText by remember { mutableStateOf("") }

    // ── Pricing Recommendation State ───────────────────────────────────────
    var isEstimatingPrice by remember { mutableStateOf(false) }
    var priceRecommendation by remember { mutableStateOf<PricingRecommendResponse?>(null) }
    var pricingErrorMessage by remember { mutableStateOf<String?>(null) }

    fun estimatePrice() {
        val artisanId = AuthRepository.currentArtisanProfileId
        if (artisanId.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Please log in to estimate price.") }
            return
        }

        isEstimatingPrice = true
        pricingErrorMessage = null

        scope.launch {
            try {
                val prodId = productUuid.value
                val parsedTags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val currentPrice = priceText.toDoubleOrNull() ?: 0.0

                val draftDto = ProductDto(
                    id = prodId,
                    artisanProfileId = artisanId,
                    categoryId = selectedCategory?.id,
                    name = productName.trim().ifBlank { "Handcrafted Product" },
                    description = description.trim().ifBlank { "" },
                    material = material.trim().ifBlank { "" },
                    dimensions = dimensions.trim().ifBlank { "" },
                    primaryColour = primaryColour.trim().ifBlank { "" },
                    secondaryColour = secondaryColour.trim().ifBlank { "" },
                    weight = weightText.trim().ifBlank { "" },
                    craftType = selectedCraftType,
                    tags = parsedTags,
                    additionalNotes = additionalNotes.trim().ifBlank { "" },
                    price = currentPrice,
                    currency = "INR",
                    status = "draft",
                    publishedAt = null
                )

                // 1. Save/upsert draft product in Supabase
                ProductRepository.saveDraftProduct(draftDto)
                    .onFailure { err ->
                        android.util.Log.w("AddProductScreen", "Draft save notice: ${err.message}")
                    }

                // 2. Build full pricing request matching the API input fields
                val pricingRequest = ProductPricingRequest(
                    category = selectedCategory?.name ?: selectedCraftType,
                    craftType = selectedCraftType,
                    material = material.trim().ifBlank { "Handcrafted" },
                    dimensions = dimensions.trim().ifBlank { null },
                    stateOrLocation = AuthRepository.currentArtisanProfile?.locationState
                        ?: AuthRepository.currentArtisanProfile?.locationCity
                        ?: "India",
                    rawMaterialCost = rawMaterialCostText.toDoubleOrNull() ?: 0.0,
                    laborCost = labourCostText.toDoubleOrNull() ?: 0.0,
                    inputCost = inputCostText.toDoubleOrNull() ?: 0.0
                )

                // 3. Call deployed Render Pricing ML API
                PricingRepository.recommendPrice(pricingRequest)
                    .onSuccess { rec ->
                        priceRecommendation = rec
                        if (rec.recommendedMin != null && rec.recommendedMax != null) {
                            snackbarHostState.showSnackbar(
                                "Suggested Price Range: ₹${rec.recommendedMin.toInt()} – ₹${rec.recommendedMax.toInt()}"
                            )
                        }
                    }
                    .onFailure { err ->
                        pricingErrorMessage = err.message ?: "Failed to get price recommendation."
                        snackbarHostState.showSnackbar("Pricing API: ${err.message}")
                    }
            } catch (e: Exception) {
                pricingErrorMessage = e.localizedMessage
                snackbarHostState.showSnackbar("Pricing estimate error: ${e.message}")
            } finally {
                isEstimatingPrice = false
            }
        }
    }

    // ── Image & AI Enhancement state ───────────────────────────────────────
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var originalStoragePath by remember { mutableStateOf<String?>(null) }
    var enhancedVariants by remember { mutableStateOf<List<EnhancedImageItem>>(emptyList()) }
    var isEnhancing by remember { mutableStateOf(false) }
    var enhanceStatusMessage by remember { mutableStateOf("AI enhancing your product image...") }
    var enhanceError by remember { mutableStateOf<String?>(null) }

    fun triggerImageEnhancement(uri: Uri) {
        val userId = AuthRepository.currentUserId
        if (userId.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Please log in to upload product images.") }
            return
        }

        selectedImageUri = uri
        enhanceError = null
        enhancedVariants = emptyList()

        scope.launch {
            isEnhancing = true
            enhanceStatusMessage = "AI enhancing your product image..."

            // Sequential progress message ticker
            val tickerJob = launch {
                delay(2000L)
                enhanceStatusMessage = "Processing image quality and lighting..."
                delay(2500L)
                enhanceStatusMessage = "Removing unwanted objects..."
            }

            try {
                val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    // Step 1: Upload original image to product-media bucket
                    val origResult = ImageEnhanceRepository.uploadOriginalImage(
                        userId = userId,
                        productId = productUuid.value,
                        imageBytes = imageBytes,
                        filename = "artisan-1.jpg"
                    )

                    if (origResult.isSuccess) {
                        originalStoragePath = origResult.getOrNull()

                        // Step 2: Call AI enhancement API
                        val apiResult = ImageEnhanceRepository.callEnhancementApi(
                            productId = productUuid.value,
                            imageBytes = imageBytes,
                            filename = "artisan-1.jpg"
                        )

                        if (apiResult.isSuccess) {
                            val variants = apiResult.getOrNull()?.allImages ?: emptyList()

                            // Step 3: Download and upload enhanced variants to Supabase Storage
                            val uploadVariantsResult = ImageEnhanceRepository.downloadAndUploadEnhancedVariants(
                                userId = userId,
                                productId = productUuid.value,
                                variants = variants
                            )

                            if (uploadVariantsResult.isSuccess) {
                                enhancedVariants = uploadVariantsResult.getOrNull() ?: emptyList()
                            } else {
                                enhanceError = "Enhanced variants could not be saved to storage. Original image is preserved."
                            }
                        } else {
                            val errMsg = apiResult.exceptionOrNull()?.message ?: "AI Enhancement unavailable"
                            enhanceError = "AI Enhancement notice: $errMsg. Original image is preserved."
                        }
                    } else {
                        val errMsg = origResult.exceptionOrNull()?.message ?: "Upload failed"
                        enhanceError = "Failed to upload original image: $errMsg"
                    }
                } else {
                    enhanceError = "Could not read image file."
                }
            } catch (e: Exception) {
                enhanceError = "Enhancement error: ${e.localizedMessage}"
            } finally {
                tickerJob.cancel()
                isEnhancing = false
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                triggerImageEnhancement(uri)
            }
        }
    )

    // ── Speech-to-Text for Description ─────────────────────────────────────
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                description = if (description.isBlank()) spokenText else "$description $spokenText"
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak product description...")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Voice recognition not available on device.", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Microphone permission is required for voice description.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceInput() {
        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak product description...")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Voice recognition not available.", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // ── UI state ───────────────────────────────────────────────────────────
    var isSubmitting by remember { mutableStateOf(false) }
    var craftDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // ── Validation ─────────────────────────────────────────────────────────
    val isFormValid = productName.isNotBlank() && priceText.isNotBlank()

    fun submitProduct() {
        if (isSubmitting || isEnhancing) return

        val artisanId = AuthRepository.currentArtisanProfileId
        val userId = AuthRepository.currentUserId
        if (artisanId.isNullOrBlank() || userId.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Session expired. Please log in again.") }
            return
        }
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            scope.launch { snackbarHostState.showSnackbar("Please enter a valid price.") }
            return
        }
        val stockQty = stockQuantityText.toIntOrNull() ?: 1
        val lowStockLimit = lowStockThresholdText.toIntOrNull() ?: 5

        // Parse tags from comma-separated input
        val parsedTags = tagsInput.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        isSubmitting = true
        scope.launch {
            val prodId = productUuid.value

            val dto = ProductDto(
                id = prodId,
                artisanProfileId = artisanId,
                categoryId = selectedCategory?.id,
                name = productName.trim(),
                description = description.trim().ifBlank { "" },
                material = material.trim().ifBlank { "" },
                dimensions = dimensions.trim().ifBlank { "" },
                primaryColour = primaryColour.trim().ifBlank { "" },
                secondaryColour = secondaryColour.trim().ifBlank { "" },
                weight = weightText.trim().ifBlank { "" },
                craftType = selectedCraftType,
                tags = parsedTags,
                additionalNotes = additionalNotes.trim().ifBlank { "" },
                price = price,
                currency = "INR",
                status = "draft",
                publishedAt = null
            )

            val origPath = originalStoragePath ?: ""
            val keptPaths = enhancedVariants.filter { it.isKept }.map { it.storagePath }

            val saveResult = if (origPath.isNotBlank()) {
                ImageEnhanceRepository.saveProductWithImages(
                    product = dto,
                    initialStock = stockQty,
                    lowStockThreshold = lowStockLimit,
                    originalStoragePath = origPath,
                    keptEnhancedPaths = keptPaths
                )
            } else {
                ProductRepository.createProduct(
                    product = dto,
                    initialStock = stockQty,
                    lowStockThreshold = lowStockLimit
                )
            }

            saveResult.onSuccess { insertedProd ->
                val finalProdId = insertedProd.id ?: prodId

                // Publication validation and transition if requested
                if (selectedStatus == "published") {
                    ProductRepository.publishProduct(finalProdId, artisanId).onSuccess {
                        snackbarHostState.showSnackbar("Product published successfully!")
                        onProductSaved()
                    }.onFailure { err ->
                        snackbarHostState.showSnackbar("Saved as draft. Publishing note: ${err.message}")
                        onProductSaved()
                    }
                } else {
                    snackbarHostState.showSnackbar("Product saved as draft!")
                    onProductSaved()
                }
            }.onFailure { e ->
                snackbarHostState.showSnackbar("Error saving product: ${e.message}")
            }

            isSubmitting = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Product",
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LightText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RustTerracotta)
            )
        },
        containerColor = CleanBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header Banner ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(RustTerracotta, RustTerracottaDark)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        "List Your Craft",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = LightText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Provide verified details for the marketplace catalog & inventory.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LightText.copy(alpha = 0.85f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Form Card ────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // SECTION 1: Basic Identity & Taxonomy
                    FormSectionHeader(icon = Icons.Default.Inventory2, label = "Basic Information")

                    ProductFormField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = "Product Name *",
                        placeholder = "e.g. Terracotta Horse Figurine",
                        icon = Icons.Default.Label
                    )

                    // Controlled Product Category Dropdown from product_categories
                    if (categories.isNotEmpty()) {
                        DropdownFormField(
                            label = "Product Category",
                            selected = selectedCategory?.name ?: "Select Category",
                            options = categories.map { it.name },
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = it },
                            onOptionSelected = { catName ->
                                selectedCategory = categories.firstOrNull { it.name == catName }
                                categoryDropdownExpanded = false
                            },
                            icon = Icons.Default.Category
                        )
                    }

                    // Craft Type Dropdown
                    DropdownFormField(
                        label = "Craft Type",
                        selected = selectedCraftType,
                        options = CRAFT_TYPES,
                        expanded = craftDropdownExpanded,
                        onExpandedChange = { craftDropdownExpanded = it },
                        onOptionSelected = {
                            selectedCraftType = it
                            craftDropdownExpanded = false
                        },
                        icon = Icons.Default.Palette
                    )

                    ProductFormField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description",
                        placeholder = "Describe heritage, artisanal technique, and cultural significance...",
                        icon = Icons.Default.Notes,
                        trailingIcon = {
                            IconButton(
                                onClick = { startVoiceInput() },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input for Description",
                                    tint = RustTerracotta,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        singleLine = false,
                        minLines = 3
                    )

                    HorizontalDivider(color = ThinBorderColor)

                    // SECTION 2: Physical Specifications
                    FormSectionHeader(icon = Icons.Default.Straighten, label = "Materials & Dimensions")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductFormField(
                                value = material,
                                onValueChange = { material = it },
                                label = "Material",
                                placeholder = "e.g. Clay, Brass",
                                icon = Icons.Default.Texture
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ProductFormField(
                                value = dimensions,
                                onValueChange = { dimensions = it },
                                label = "Dimensions",
                                placeholder = "e.g. 15 × 8 cm",
                                icon = Icons.Default.Straighten
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductFormField(
                                value = primaryColour,
                                onValueChange = { primaryColour = it },
                                label = "Primary Colour",
                                placeholder = "e.g. Terracotta Red",
                                icon = Icons.Default.ColorLens
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ProductFormField(
                                value = secondaryColour,
                                onValueChange = { secondaryColour = it },
                                label = "Secondary Colour",
                                placeholder = "e.g. Gold / White",
                                icon = Icons.Default.FormatColorFill
                            )
                        }
                    }

                    ProductFormField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = "Weight",
                        placeholder = "e.g. 450 grams, 1.2 kg",
                        icon = Icons.Default.FitnessCenter
                    )

                    HorizontalDivider(color = ThinBorderColor)

                    // SECTION 3: Tags & Additional Notes
                    FormSectionHeader(icon = Icons.Default.Tag, label = "Discovery & Notes")

                    ProductFormField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = "Tags (comma-separated)",
                        placeholder = "e.g. handmade, festive, eco-friendly, pottery",
                        icon = Icons.Default.LocalOffer
                    )

                    ProductFormField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        label = "Additional Notes",
                        placeholder = "e.g. Fragile, keep away from direct moisture",
                        icon = Icons.Default.BookmarkBorder,
                        singleLine = false,
                        minLines = 2
                    )

                    HorizontalDivider(color = ThinBorderColor)

                    // SECTION 4: Pricing & Inventory
                    FormSectionHeader(icon = Icons.Default.CurrencyRupee, label = "Pricing & Inventory")

                    // Estimate Price Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                        border = BorderStroke(1.dp, RustTerracottaLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = RustTerracotta,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "ML Price Intelligence",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText
                                    )
                                }

                                Button(
                                    onClick = { estimatePrice() },
                                    enabled = !isEstimatingPrice && !isSubmitting,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    if (isEstimatingPrice) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Estimating...", fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Estimate Price", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Production Costs (Optional for accurate ML pricing):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkText
                            )
                            Spacer(Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductFormField(
                                        value = rawMaterialCostText,
                                        onValueChange = { rawMaterialCostText = it.filter { c -> c.isDigit() || c == '.' } },
                                        label = "Raw Materials (₹)",
                                        placeholder = "0.00",
                                        keyboardType = KeyboardType.Decimal
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductFormField(
                                        value = labourCostText,
                                        onValueChange = { labourCostText = it.filter { c -> c.isDigit() || c == '.' } },
                                        label = "Labour Cost (₹)",
                                        placeholder = "0.00",
                                        keyboardType = KeyboardType.Decimal
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductFormField(
                                        value = inputCostText,
                                        onValueChange = { inputCostText = it.filter { c -> c.isDigit() || c == '.' } },
                                        label = "Other Costs (₹)",
                                        placeholder = "0.00",
                                        keyboardType = KeyboardType.Decimal
                                    )
                                }
                            }

                            // Suggested Price Range Display
                            val recMin = priceRecommendation?.recommendedMin
                            val recMax = priceRecommendation?.recommendedMax

                            if (recMin != null && recMax != null) {
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = RustTerracottaLight.copy(alpha = 0.5f))
                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Suggested Price Range",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MutedText
                                        )
                                        Text(
                                            text = "₹${recMin.toInt()} – ₹${recMax.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RustTerracotta,
                                            fontSize = 18.sp
                                        )
                                    }

                                    Text(
                                        text = "Market estimate. Set your own price below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText,
                                        fontSize = 11.sp,
                                        modifier = Modifier.widthIn(max = 140.dp)
                                    )
                                }
                            } else if (pricingErrorMessage != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Estimate unavailable: $pricingErrorMessage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    ProductFormField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = "Price (INR) *",
                        placeholder = "0.00",
                        icon = Icons.Default.CurrencyRupee,
                        keyboardType = KeyboardType.Decimal
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductFormField(
                                value = stockQuantityText,
                                onValueChange = { stockQuantityText = it.filter { c -> c.isDigit() } },
                                label = "Quantity on Hand",
                                placeholder = "1",
                                icon = Icons.Default.Warehouse,
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ProductFormField(
                                value = lowStockThresholdText,
                                onValueChange = { lowStockThresholdText = it.filter { c -> c.isDigit() } },
                                label = "Low Stock Limit",
                                placeholder = "5",
                                icon = Icons.Default.Warning,
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    HorizontalDivider(color = ThinBorderColor)

                    // SECTION 5: Product Image Selection & AI Enhancement
                    FormSectionHeader(icon = Icons.Default.AddAPhoto, label = "Product Photo & AI Enhancement")

                    if (selectedImageUri != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, ThinBorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Original Photo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        enabled = !isEnhancing && !isSubmitting,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Change Photo", fontSize = 11.sp)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected Product Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // AI Enhancement Loading State
                                if (isEnhancing) {
                                    Spacer(Modifier.height(14.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = WarmCardSurface,
                                        border = BorderStroke(1.dp, RustTerracottaLight)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = RustTerracotta,
                                                strokeWidth = 2.5.dp
                                            )
                                            Text(
                                                text = enhanceStatusMessage,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = DarkText
                                                )
                                            )
                                        }
                                    }
                                }

                                // AI Enhanced Variants List with Keep / Discard controls
                                if (enhancedVariants.isNotEmpty()) {
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = RustTerracotta,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "AI Enhanced Variations",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkText
                                        )
                                    }
                                    Text(
                                        text = "Choose which enhanced variations to keep for your product listing:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayText
                                    )
                                    Spacer(Modifier.height(10.dp))

                                    enhancedVariants.forEachIndexed { index, variant ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (variant.isKept) WarmCardSurface else Color(0xFFF1F5F9)
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (variant.isKept) RustTerracotta else ThinBorderColor
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(10.dp)
                                                    .fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                // Enhanced image preview from Supabase Storage
                                                Box(
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE2E8F0)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AsyncImage(
                                                        model = variant.displayUrl,
                                                        contentDescription = "Enhanced Variant ${index + 1}",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Variation #${index + 1}",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DarkText
                                                    )
                                                    Text(
                                                        text = if (variant.isKept) "Status: Kept" else "Status: Discarded",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (variant.isKept) TextGreen else GrayText
                                                    )
                                                }

                                                // Keep / Discard Control Button
                                                Button(
                                                    onClick = {
                                                        enhancedVariants = enhancedVariants.mapIndexed { idx, item ->
                                                            if (idx == index) item.copy(isKept = !item.isKept) else item
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (variant.isKept) RustTerracotta else Color(0xFF94A3B8),
                                                        contentColor = Color.White
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (variant.isKept) Icons.Default.Check else Icons.Default.Close,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = if (variant.isKept) "Keep" else "Discard",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Enhancement Error / Retry State
                                if (enhanceError != null && !isEnhancing) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = PillTerracotta.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, RustTerracottaLight)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = enhanceError ?: "",
                                                style = MaterialTheme.typography.bodySmall.copy(color = RustTerracottaDark),
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(
                                                onClick = {
                                                    selectedImageUri?.let { triggerImageEnhancement(it) }
                                                }
                                            ) {
                                                Text("Retry", fontWeight = FontWeight.Bold, color = RustTerracotta)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.5.dp, ThinBorderColor, RoundedCornerShape(14.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(RustTerracottaLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Upload Photo",
                                        tint = RustTerracotta,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "Add Product Photo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Tap to select photo from gallery for AI enhancement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayText
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Select Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = ThinBorderColor)

                    // SECTION 6: Listing Status
                    FormSectionHeader(icon = Icons.Default.Visibility, label = "Listing Status")

                    DropdownFormField(
                        label = "Initial Status",
                        selected = if (selectedStatus == "draft") "Draft (Visible only to you)" else "Published (Live on marketplace)",
                        options = listOf("Draft (Visible only to you)", "Published (Live on marketplace)"),
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = it },
                        onOptionSelected = { opt ->
                            selectedStatus = if (opt.startsWith("Draft")) "draft" else "published"
                            statusDropdownExpanded = false
                        },
                        icon = Icons.Default.ToggleOn
                    )

                    Spacer(Modifier.height(8.dp))

                    // ── Submit / Save Button ─────────────────────────────
                    Button(
                        onClick = { submitProduct() },
                        enabled = isFormValid && !isSubmitting && !isEnhancing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RustTerracotta,
                            disabledContainerColor = RustTerracotta.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = LightText,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Saving Product...",
                                color = LightText,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isEnhancing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = LightText,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Enhancing Image...",
                                color = LightText,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = if (selectedStatus == "published") Icons.Default.CloudUpload else Icons.Default.Save,
                                contentDescription = null,
                                tint = LightText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (selectedStatus == "published") "Publish Product" else "Save as Draft",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightText
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormSectionHeader(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RustTerracotta,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    icon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = GrayText.copy(alpha = 0.6f)) },
        leadingIcon = icon?.let {
            { Icon(it, contentDescription = null, tint = RustTerracotta, modifier = Modifier.size(20.dp)) }
        },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RustTerracotta,
            unfocusedBorderColor = ThinBorderColor,
            focusedLabelColor = RustTerracotta
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownFormField(
    label: String,
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    icon: ImageVector? = null
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = icon?.let {
                { Icon(it, contentDescription = null, tint = RustTerracotta, modifier = Modifier.size(20.dp)) }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RustTerracotta,
                unfocusedBorderColor = ThinBorderColor,
                focusedLabelColor = RustTerracotta
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}
