package com.artknower.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artknower.app.ui.theme.RustTerracotta
import com.artknower.app.ui.theme.ThinBorderColor

@Composable
fun TraditionalMotifDivider(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = ThinBorderColor,
            thickness = 1.dp
        )
        Text(
            text = " ❖ ",
            color = RustTerracotta,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = ThinBorderColor,
            thickness = 1.dp
        )
    }
}
