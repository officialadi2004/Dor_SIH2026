package com.artknower.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.R
import com.artknower.app.ui.theme.*

data class CopilotFeatureItem(
    val title: String,
    val icon: ImageVector,
    val routeKey: String,
    val accentColor: Color = RustTerracotta
)

@Composable
fun CopilotSection(
    onFeatureClick: (routeKey: String, title: String) -> Unit
) {
    val features = listOf(
        CopilotFeatureItem(
            title = stringResource(R.string.copilot_enhance),
            icon = Icons.Default.Image,
            routeKey = "improve_image",
            accentColor = Color(0xFF0284C7)
        ),
        CopilotFeatureItem(
            title = stringResource(R.string.copilot_price),
            icon = Icons.Default.MonetizationOn,
            routeKey = "price_advice",
            accentColor = Color(0xFF059669)
        ),
        CopilotFeatureItem(
            title = stringResource(R.string.copilot_marketing),
            icon = Icons.Default.Campaign,
            routeKey = "marketing_help",
            accentColor = Color(0xFFDB2777)
        )
    )

    val copilotTitle = stringResource(R.string.copilot_title)
    val promoTitle = stringResource(R.string.copilot_promo_title)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = copilotTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            )

            TextButton(onClick = { onFeatureClick("all", copilotTitle) }) {
                Text(
                    text = stringResource(R.string.copilot_see_all),
                    color = RustTerracotta,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
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

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Row of Feature Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            features.forEach { feature ->
                CopilotFeatureCard(
                    feature = feature,
                    modifier = Modifier.weight(1f),
                    onClick = { onFeatureClick(feature.routeKey, feature.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Grow Stronger Together" promo banner with gradient
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF334155)
                            )
                        )
                    )
            ) {
                // Decorative circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.05f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.copilot_promo_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.copilot_promo_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onFeatureClick("resources", promoTitle) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.copilot_promo_btn),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LightText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CopilotFeatureCard(
    feature: CopilotFeatureItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = feature.accentColor.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = feature.accentColor.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon in a soft circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(feature.accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = feature.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = feature.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
