package com.artknower.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.artknower.app.R
import com.artknower.app.ui.theme.DarkText
import com.artknower.app.ui.theme.GrayText
import com.artknower.app.ui.theme.PillGreen
import com.artknower.app.ui.theme.RustTerracotta
import com.artknower.app.ui.theme.TextGreen

@Composable
fun HomeHeader(
    artisanName: String,
    profilePhotoUrl: String? = null,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Subtle background mandala motif
        HeaderMandalaMotif(modifier = Modifier.matchParentSize())

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.namaste),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = DarkText,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    Text(
                        text = artisanName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = DarkText,
                            fontWeight = FontWeight.Bold
                        )
                    )

                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Language Switcher Pill (EN / HI)
                    val currentLocales = AppCompatDelegate.getApplicationLocales()
                    val isHindi = !currentLocales.isEmpty && currentLocales[0]?.language == "hi"

                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(Color.White)
                            .clickable {
                                val newLocale = if (isHindi) "en" else "hi"
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                            }
                            .padding(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "EN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isHindi) DarkText else GrayText
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "HI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHindi) DarkText else GrayText
                                )
                            }
                        }

                        // Sliding thumb knob
                        Box(
                            modifier = Modifier
                                .align(if (isHindi) Alignment.CenterEnd else Alignment.CenterStart)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(RustTerracotta),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isHindi) "हि" else "EN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Profile Avatar (Photo from Database or Initials)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RustTerracotta.copy(alpha = 0.12f))
                            .border(1.5.dp, RustTerracotta.copy(alpha = 0.5f), CircleShape)
                            .clickable { onProfileClick() },
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RustTerracotta
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Verified Badge pill from reference design
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PillGreen,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = TextGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.artisan_verified),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
