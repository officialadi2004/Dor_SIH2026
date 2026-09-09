package com.artknower.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.R
import com.artknower.app.ui.components.AppBottomBar
import com.artknower.app.ui.components.HomeTab
import com.artknower.app.ui.theme.DarkText
import com.artknower.app.ui.theme.GrayText
import com.artknower.app.ui.theme.RustTerracotta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    description: String? = null,
    currentTab: HomeTab? = null,
    onTabSelected: ((HomeTab) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val displayDesc = description ?: stringResource(R.string.coming_soon_desc)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RustTerracotta)
            )
        },
        bottomBar = {
            if (currentTab != null && onTabSelected != null) {
                AppBottomBar(
                    currentTab = currentTab,
                    onTabSelected = onTabSelected
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = RustTerracotta,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.coming_soon),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RustTerracotta
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = displayDesc,
                style = MaterialTheme.typography.bodyLarge,
                color = GrayText,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            if (onBack != null) {
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta)
                ) {
                    Text(stringResource(R.string.return_to_home), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
