package com.artknower.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.artknower.app.R

@Composable
fun PosterHeader() {
    Image(
        painter = painterResource(id = R.drawable.dor_header),
        contentDescription = "Dor Header",
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}
