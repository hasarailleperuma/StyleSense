package com.example.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "UK GDPR Art. 9 Shield",
                tint = Color(0xFF06D6A0),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Privacy & Data Protection",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "StyleSense is designed with strict on-device data privacy:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyBullet(
                    title = "100% On-Device Processing",
                    description = "All camera streams and photo analysis occur locally in memory and are cleared immediately after processing."
                )

                PrivacyBullet(
                    title = "Zero Photo Storage",
                    description = "No photos, face captures, or biometric images are ever saved to disk or database."
                )

                PrivacyBullet(
                    title = "Offline Operation",
                    description = "The application works completely offline with zero data sent to external servers."
                )

                PrivacyBullet(
                    title = "Anonymous Color Tokens",
                    description = "Your wardrobe closet only persists anonymous clothing color hex codes and category tags."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_close_privacy_modal")
            ) {
                Text("Understood")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun PrivacyBullet(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}
