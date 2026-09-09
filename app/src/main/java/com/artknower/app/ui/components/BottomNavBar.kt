package com.artknower.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.R
import com.artknower.app.ui.theme.DarkText
import com.artknower.app.ui.theme.GrayText
import com.artknower.app.ui.theme.NavSelectedPill
import com.artknower.app.ui.theme.RustTerracotta

enum class HomeTab(@StringRes val titleRes: Int, val icon: ImageVector, val route: String) {
    HOME(R.string.tab_home, Icons.Default.Home, "home"),
    PRODUCTS(R.string.tab_products, Icons.Default.ShoppingBag, "products"),
    ADD(R.string.add_product, Icons.Default.Add, "add_product"),
    MARKETING(R.string.tab_marketing, Icons.Default.Campaign, "marketing_hub"),
    ORDERS(R.string.tab_orders, Icons.AutoMirrored.Filled.ListAlt, "orders")
}

@Composable
fun AppBottomBar(
    currentTab: HomeTab? = null,
    onTabSelected: (HomeTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeTab.values().forEach { tab ->
                    val isSelected = currentTab == tab

                    if (tab == HomeTab.ADD) {
                        // Floating Terracotta (+) Button with glow ring
                        Box(
                            modifier = Modifier
                                .offset(y = (-10).dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    ambientColor = RustTerracotta.copy(alpha = 0.4f),
                                    spotColor = RustTerracotta.copy(alpha = 0.6f)
                                )
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(RustTerracotta)
                                .clickable { onTabSelected(tab) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(tab.titleRes),
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        // Regular nav tab with animated pill indicator
                        val pillColor by animateColorAsState(
                            targetValue = if (isSelected) NavSelectedPill else Color.Transparent,
                            animationSpec = tween(durationMillis = 200),
                            label = "pill_color"
                        )
                        val iconTint by animateColorAsState(
                            targetValue = if (isSelected) RustTerracotta else GrayText,
                            animationSpec = tween(durationMillis = 200),
                            label = "icon_tint"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
                                ) { onTabSelected(tab) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Pill background indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pillColor)
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.titleRes),
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(tab.titleRes),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = iconTint,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
