package com.albarmajy.medscan.ui.customUi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.albarmajy.medscan.ui.theme.PrimaryBlue

@Composable
fun Label(text : String = "Label", color: Color = PrimaryBlue, modifier: Modifier = Modifier) {

    Box(modifier = modifier
        .clip(RoundedCornerShape(18.dp))
        .background(color.copy(0.1f))
        .padding(horizontal = 12.dp, vertical = 6.dp)

    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview
@Composable
private fun LabelPreview() {
    Label()
}