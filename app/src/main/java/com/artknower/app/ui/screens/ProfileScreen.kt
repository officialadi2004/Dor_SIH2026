package com.artknower.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.artknower.app.data.MockDataSource
import com.artknower.app.data.model.ArtisanProfileDto
import com.artknower.app.data.model.ProfileDto
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.ProductRepository
import com.artknower.app.data.repository.ProductStats
import com.artknower.app.ui.components.HomeTab
import com.artknower.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToTab: (HomeTab) -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Live state from AuthRepository or fallback to MockDataSource
    var artisanProfile by remember { mutableStateOf(AuthRepository.currentArtisanProfile) }
    var userProfile by remember { mutableStateOf(AuthRepository.currentProfile) }
    var productStats by remember { mutableStateOf<ProductStats?>(null) }
    var artisanStory by remember { mutableStateOf<com.artknower.app.data.model.ArtisanStoryDto?>(null) }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    val artisanName = artisanProfile?.displayName 
        ?: userProfile?.fullName 
        ?: "Complete Your Profile"
    val artisanCraft = artisanProfile?.craftSummary 
        ?: "Complete Your Profile"
    val artisanLocation = listOfNotNull(
        artisanProfile?.locationCity,
        artisanProfile?.locationState,
        artisanProfile?.locationCountry
    ).filter { it.isNotBlank() }.joinToString(", ").ifEmpty { "Complete Your Profile" }
    val phone = userProfile?.phone ?: "Not specified"

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val artisanId = artisanProfile?.id ?: AuthRepository.currentArtisanProfileId
                if (!artisanId.isNullOrBlank()) {
                    coroutineScope.launch {
                        isUploadingPhoto = true
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bytes = inputStream?.readBytes()
                            inputStream?.close()
                            if (bytes != null && bytes.isNotEmpty()) {
                                val result = AuthRepository.uploadArtisanProfilePhoto(
                                    artisanProfileId = artisanId,
                                    imageBytes = bytes
                                )
                                result.onSuccess { path ->
                                    artisanProfile = AuthRepository.currentArtisanProfile
                                    profilePhotoUrl = AuthRepository.getProfilePhotoUrl(path)
                                    snackbarHostState.showSnackbar("Profile photo updated successfully!")
                                }.onFailure { err ->
                                    snackbarHostState.showSnackbar("Failed to upload photo: ${err.message}")
                                }
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error loading image: ${e.message}")
                        } finally {
                            isUploadingPhoto = false
                        }
                    }
                }
            }
        }
    )

    // Load live product stats and profile photo for this artisan from Supabase
    LaunchedEffect(artisanProfile?.id, artisanProfile?.profilePhotoPath) {
        val artisanId = artisanProfile?.id ?: AuthRepository.currentArtisanProfileId
        if (!artisanId.isNullOrBlank()) {
            ProductRepository.getProductStats(artisanId).onSuccess { stats ->
                productStats = stats
            }
            com.artknower.app.data.repository.StoryRepository.getStory(artisanId).onSuccess { story ->
                artisanStory = story
            }
        }
        val path = artisanProfile?.profilePhotoPath ?: userProfile?.avatarPath
        profilePhotoUrl = AuthRepository.getProfilePhotoUrl(path)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Artisan Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White
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
            // 1. Hero Artisan Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(RustTerracotta, RustTerracottaDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Avatar with Photo or Initials + Camera Badge
                    Box(
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(3.dp, RustTerracottaLight, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profilePhotoUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initials = artisanName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                    .ifBlank { "A" }

                                Text(
                                    text = initials.uppercase(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RustTerracotta
                                )
                            }

                            if (isUploadingPhoto) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }

                        // Camera Icon Action Badge
                        Surface(
                            shape = CircleShape,
                            color = RustTerracotta,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = 4.dp, y = 4.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Upload Profile Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = artisanName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = artisanCraft,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = RustTerracottaLight,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Verified Badge + Location Chip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PillGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = TextGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified Artisan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardSurface.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = artisanLocation,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. Business Catalog Performance (Supabase stats)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Catalog Activity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStatItem(
                                label = "Total Products",
                                value = (productStats?.totalProducts ?: 0).toString(),
                                icon = Icons.Default.Inventory2,
                                tint = RustTerracotta
                            )
                            ProfileStatItem(
                                label = "Published",
                                value = (productStats?.publishedCount ?: 0).toString(),
                                icon = Icons.Default.Storefront,
                                tint = TextGreen
                            )
                            ProfileStatItem(
                                label = "Drafts",
                                value = (productStats?.draftCount ?: 0).toString(),
                                icon = Icons.Default.EditNote,
                                tint = GrayText
                            )
                        }
                    }
                }

                // 3. Contact & Account Details Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Contact & Account Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        )
                        HorizontalDivider(color = ThinBorderColor)

                        DetailRow(
                            icon = Icons.Default.Phone,
                            title = "Phone Number",
                            value = phone
                        )
                        DetailRow(
                            icon = Icons.Default.LocationCity,
                            title = "Studio Location",
                            value = artisanLocation
                        )
                        DetailRow(
                            icon = Icons.Default.Category,
                            title = "Craft Specialty",
                            value = artisanCraft
                        )
                        artisanProfile?.experienceYears?.let { yrs ->
                            DetailRow(
                                icon = Icons.Default.Timeline,
                                title = "Experience",
                                value = "$yrs Years of Craftsmanship"
                            )
                        }
                        DetailRow(
                            icon = Icons.Default.Public,
                            title = "Country",
                            value = artisanProfile?.locationCountry ?: "India"
                        )
                        DetailRow(
                            icon = Icons.Default.Language,
                            title = "Preferred Language",
                            value = userProfile?.preferredLanguage?.uppercase() ?: "EN (English)"
                        )
                        DetailRow(
                            icon = Icons.Default.Shield,
                            title = "Account Status",
                            value = if (userProfile?.isActive != false) "Active & Verified" else "Inactive"
                        )
                    }
                }

                // 4. Artisan Heritage & Story Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Artisan Heritage & Story",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PillTerracotta
                            ) {
                                Text(
                                    text = if (artisanStory != null) "Published" else "AI Verified",
                                    color = RustTerracotta,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = artisanStory?.approvedStory ?: "Rooted in authentic handcrafted tradition, preserving generational craft heritage with verified identity and catalog records.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DarkText.copy(alpha = 0.8f),
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // 5. Account Settings Actions
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Account Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        )
                        HorizontalDivider(color = ThinBorderColor)

                        // Edit Profile Button
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RustTerracotta)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = RustTerracotta,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile Details", color = RustTerracotta, fontWeight = FontWeight.SemiBold)
                        }

                        // Help / Support
                        OutlinedButton(
                            onClick = {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("DOR Support: support@dor.app")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ThinBorderColor)
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = GrayText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Help & Support", color = DarkText, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // 6. Logout Button
                Button(
                    onClick = { showLogoutConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log Out",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out of Account",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ── Edit Profile Dialog ────────────────────────────────────────────────
    if (showEditDialog) {
        var editName by remember { mutableStateOf(artisanName) }
        var editCraft by remember { mutableStateOf(artisanCraft) }
        var editCity by remember { mutableStateOf(artisanProfile?.locationCity ?: "Bankura") }
        var editState by remember { mutableStateOf(artisanProfile?.locationState ?: "West Bengal") }
        var editCountry by remember { mutableStateOf(artisanProfile?.locationCountry ?: "India") }
        var editExperienceYears by remember { mutableStateOf((artisanProfile?.experienceYears ?: "").toString()) }
        var isSaving by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { if (!isSaving) showEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Edit Artisan Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    )

                    // Photo preview & change in dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(WarmCardSurface)
                                .border(2.dp, RustTerracotta, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profilePhotoUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initials = editName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                    .ifBlank { "A" }

                                Text(
                                    text = initials.uppercase(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RustTerracotta
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = RustTerracotta)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (profilePhotoUrl.isNullOrBlank()) "Upload Photo" else "Change Photo", color = RustTerracotta)
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display / Full Name") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustTerracotta,
                            unfocusedBorderColor = ThinBorderColor
                        )
                    )

                    OutlinedTextField(
                        value = editCraft,
                        onValueChange = { editCraft = it },
                        label = { Text("Craft Specialization") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustTerracotta,
                            unfocusedBorderColor = ThinBorderColor
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editCity,
                                onValueChange = { editCity = it },
                                label = { Text("City / Village") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RustTerracotta,
                                    unfocusedBorderColor = ThinBorderColor
                                )
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editState,
                                onValueChange = { editState = it },
                                label = { Text("State") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RustTerracotta,
                                    unfocusedBorderColor = ThinBorderColor
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editCountry,
                                onValueChange = { editCountry = it },
                                label = { Text("Country") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RustTerracotta,
                                    unfocusedBorderColor = ThinBorderColor
                                )
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editExperienceYears,
                                onValueChange = { editExperienceYears = it.filter { c -> c.isDigit() } },
                                label = { Text("Exp. (Years)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RustTerracotta,
                                    unfocusedBorderColor = ThinBorderColor
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showEditDialog = false },
                            enabled = !isSaving
                        ) {
                            Text("Cancel", color = GrayText)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        val result = AuthRepository.updateArtisanProfile(
                                            displayName = editName,
                                            phone = phone,
                                            city = editCity,
                                            state = editState,
                                            craftSummary = editCraft,
                                            experienceYears = editExperienceYears.toIntOrNull(),
                                            country = editCountry
                                        )
                                        isSaving = false
                                        result.onSuccess { updatedArtisan ->
                                            artisanProfile = updatedArtisan
                                            MockDataSource.updateProfile(
                                                name = updatedArtisan.displayName,
                                                craft = updatedArtisan.craftSummary ?: editCraft,
                                                location = "$editCity, $editState"
                                            )
                                            showEditDialog = false
                                            snackbarHostState.showSnackbar("Profile updated successfully in Supabase!")
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar("Update failed: ${err.message}")
                                        }
                                    }
                                }
                            },
                            enabled = !isSaving && editName.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta)
                        ) {
                            Text(if (isSaving) "Saving..." else "Save Changes")
                        }
                    }
                }
            }
        }
    }

    // ── Logout Confirmation Dialog ─────────────────────────────────────────
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = {
                Text(
                    text = "Log Out?",
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your artisan account? You can log back in anytime with your registered phone number.",
                    color = GrayText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        coroutineScope.launch {
                            AuthRepository.logout()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = CardSurface
        )
    }
}

@Composable
private fun ProfileStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = GrayText
            )
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(WarmCardSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RustTerracotta,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = GrayText)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
            )
        }
    }
}
