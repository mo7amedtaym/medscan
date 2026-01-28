package com.albarmajy.medscan.ui.customUi

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.ui.theme.PrimaryBlue

@Composable
fun CustomStatusSwitch(
    isActive: Boolean,
    onStatusChange: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) PrimaryBlue.copy(0.1f) else Color(0xFFEEEEEE),
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF2094f3) else Color(0xFF757575),
        label = "textColor"
    )
    
    val alignment by animateAlignmentAsState(if (isActive) Alignment.CenterEnd else Alignment.CenterStart)

    Box(
        modifier = Modifier
            .width(100.dp) // زدنا العرض قليلاً لراحة النص
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onStatusChange(!isActive) }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // النص يتحاذى عكس اتجاه الزر
        Text(
            text = if (isActive) "ACTIVE" else "INACTIVE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp
            ),
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp) // ترك مساحة كافية للزر في الطرفين
                .wrapContentWidth(
                    align = if (isActive) Alignment.Start else Alignment.End
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .shadow(if (isActive) 0.dp else 2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFF2094f3) else Color.White)
            )
        }
    }
}

@Composable
fun animateAlignmentAsState(targetAlignment: Alignment): androidx.compose.runtime.State<Alignment> {
    val horizontalBias by animateFloatAsState(
        targetValue = if (targetAlignment == Alignment.CenterEnd) 1f else -1f,
        label = "alignmentAnimation"
    )

    return remember(horizontalBias) {
        derivedStateOf {
            BiasAlignment(horizontalBias = horizontalBias, verticalBias = 0f)
        }
    }
}


@Preview
@Composable
private fun CustomStatusSwitchPreview() {
    CustomStatusSwitch(
        isActive = true,
        onStatusChange = {}

    )
}