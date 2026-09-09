package com.artknower.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.R
import com.artknower.app.ui.theme.*

@Composable
fun BusinessStatsSection(
    totalProducts: Int = 12,
    totalEarnings: String = "₹ 8,450",
    productViews: String = "1.2K",
    followers: Int = 248
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayText,
                        fontWeight = FontWeight.Medium
                    )
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GrayText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Grid of White Elevated Cards matching reference design
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlanceCard(
                    title = stringResource(R.string.stats_total_products),
                    value = totalProducts.toString(),
                    growth = "+33%",
                    icon = Icons.Default.ShoppingBag,
                    modifier = Modifier.weight(1f)
                )
                GlanceCard(
                    title = stringResource(R.string.stats_total_earnings),
                    value = totalEarnings,
                    growth = "+27%",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlanceCard(
                    title = stringResource(R.string.stats_product_views),
                    value = productViews,
                    growth = "+40%",
                    icon = Icons.Default.RemoveRedEye,
                    modifier = Modifier.weight(1f)
                )
                GlanceCard(
                    title = stringResource(R.string.stats_followers),
                    value = followers.toString(),
                    growth = "+18%",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GlanceCard(
    title: String,
    value: String,
    growth: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = RustTerracotta,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontSize = 18.sp
                )
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = GrayText,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = TextGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = growth,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
