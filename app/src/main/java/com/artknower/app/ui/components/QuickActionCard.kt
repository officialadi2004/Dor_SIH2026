package com.artknower.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.R
import com.artknower.app.ui.theme.*

data class QuickActionPill(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val containerColor: Color,
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsSection(
    onTellStoryClick: () -> Unit,
    onEnquiriesClick: () -> Unit,
    onMarketingClick: () -> Unit = {}
) {
    val items = listOf(
        QuickActionPill(
            title = stringResource(R.string.action_tell_story),
            subtitle = stringResource(R.string.action_tell_story_sub),
            icon = Icons.Default.Mic,
            containerColor = PillTerracotta,
            iconColor = RustTerracotta,
            onClick = onTellStoryClick
        ),
        QuickActionPill(
            title = stringResource(R.string.action_enquiries),
            subtitle = stringResource(R.string.action_enquiries_sub),
            icon = Icons.Default.QuestionAnswer,
            containerColor = Color(0xFFE0F2FE),
            iconColor = Color(0xFF0284C7),
            onClick = onEnquiriesClick
        ),
        QuickActionPill(
            title = stringResource(R.string.action_marketing),
            subtitle = stringResource(R.string.action_marketing_sub),
            icon = Icons.Default.Campaign,
            containerColor = Color(0xFFFCE7F3),
            iconColor = Color(0xFFDB2777),
            onClick = onMarketingClick
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                QuickActionPillItem(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickActionPillItem(item: QuickActionPill, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(124.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = item.iconColor.copy(alpha = 0.15f),
                spotColor = item.iconColor.copy(alpha = 0.1f)
            )
            .clickable { item.onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = item.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = GrayText
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
