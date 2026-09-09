package com.artknower.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import coil.compose.AsyncImage
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.StoryRepository
import com.artknower.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlePreviewScreen(
    artisanName: String,
    location: String,
    craftType: String,
    storyText: String,
    imageUri: Uri?,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CleanBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background Image
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Craft Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .background(Brush.verticalGradient(listOf(RustTerracottaDark, RustTerracotta))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = LightText.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Dark gradient overlay for text legibility at the bottom of the image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 300f
                        )
                    )
            )

            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PillTerracotta.copy(alpha = 0.95f)
                ) {
                    Text(
                        "AI Story",
                        color = RustTerracotta,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(240.dp))
                
                // Header Info over Image
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = artisanName.ifEmpty { "Unknown Artisan" }.trim(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = location.ifEmpty { "Location Not Provided" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = craftType.ifEmpty { "Artisan Craft" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // The Article Card
                Surface(
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = CardSurface,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 500.dp) // Ensure it fills the screen bottom
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Text(
                            text = "\"My Craft, My Story\"",
                            style = MaterialTheme.typography.titleLarge,
                            color = RustTerracotta,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Beautiful typography paragraph
                        Text(
                            text = storyText.ifEmpty { "This artisan hasn't shared their story yet." }.trim(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkText,
                            lineHeight = 28.sp,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Justify
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        HorizontalDivider(color = ThinBorderColor, thickness = 1.dp)
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Extra sections
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = RustTerracotta,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "ABOUT THE CRAFT",
                                style = MaterialTheme.typography.labelLarge,
                                color = GrayText,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = "${craftType.ifEmpty { "This craft" }} is a beautiful tradition preserved by artisans like ${artisanName.ifEmpty { "this one" }} in ${location.ifEmpty { "our communities" }}. Support local craftsmanship by discovering their authentic products.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkText.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Button(
                            enabled = !isSaving,
                            onClick = {
                                val profileId = AuthRepository.currentArtisanProfileId
                                if (!profileId.isNullOrBlank() && storyText.isNotBlank()) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        val result = StoryRepository.saveStory(
                                            artisanProfileId = profileId,
                                            storyText = storyText
                                        )
                                        isSaving = false
                                        result.onSuccess {
                                            onDone()
                                        }.onFailure { err ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Failed to publish: ${err.message}")
                                            }
                                        }
                                    }
                                } else {
                                    onDone()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta)
                        ) {
                            Text(
                                text = if (isSaving) "Publishing..." else "Publish to Profile",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
