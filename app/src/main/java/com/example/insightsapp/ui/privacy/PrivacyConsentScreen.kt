package com.example.insightsapp.ui.privacy

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun PrivacyConsentScreenPreview() {
    PrivacyConsentScreen(
        phoneNumber = "+919876543210",
        onPermissionsGranted = {},
        viewModel = PrivacyConsentViewModel()
    )
}

@Composable
fun PrivacyConsentScreen(
    phoneNumber: String,
    onPermissionsGranted: () -> Unit,
    viewModel: PrivacyConsentViewModel = viewModel()
) {
    val isConsentGiven by viewModel.isConsentGiven.collectAsState()
    val permissions by viewModel.permissions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ✅ Permission launcher with detailed logging
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        println("Permission results:")
        var allGranted = true
        var someGranted = false

        permissionsMap.forEach { (permission, granted) ->
            println("$permission: $granted")
            viewModel.updatePermissionGranted(permission, granted)
            if (granted) someGranted = true
            if (!granted) allGranted = false
        }

        // ✅ Enhanced logging - shows what happened
        when {
            allGranted -> println("All permissions granted!")
            someGranted -> println("Some permissions granted, some denied")
            else -> println("All permissions denied")
        }

        // ✅ Always proceed (partial permissions are ok for now)
        scope.launch {
            try {
                println("Permissions handled, proceeding to next screen")
                onPermissionsGranted()
            } catch (e: Exception) {
                println("Error navigating: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F8))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title with shield icon next to it
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = com.example.insightsapp.R.drawable.ic_shield),
                contentDescription = "Privacy Shield",
                tint = Color(0xFF57C6A9),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Your Privacy, Your Control",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }

        // Subtitle
        Text(
            text = "We collect only the information necessary for your participation in surveys. Please review and agree before continuing. You can withdraw consent anytime from the Privacy Hub.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Data Collection Section
        ExpandableSection(
            title = "What Data We Collect",
            subtitle = "Name, Age Range, Location...",
            icon = com.example.insightsapp.R.drawable.ic_data_collection,
            content = "• Phone number for verification\n• Basic demographic information\n• Survey responses\n• App usage patterns\n• Device information for compatibility"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Why We Collect Section
        ExpandableSection(
            title = "Why We Collect It",
            subtitle = "To match you with relevant surveys...",
            icon = com.example.insightsapp.R.drawable.ic_question_circle,
            content = "• Match you with surveys that fit your profile\n• Ensure survey quality and prevent fraud\n• Improve our app and user experience\n• Provide personalized recommendations"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // How We Use Section
        ExpandableSection(
            title = "How We Use It",
            subtitle = "Your data is shared only with selected brands for agreed purposes.",
            icon = com.example.insightsapp.R.drawable.ic_settings_gear,
            content = "• Data is anonymized when possible\n• Shared only with approved survey partners\n• Used for research and market insights\n• Never sold to third parties\n• Stored securely with encryption"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Consent Row with Switch on the right and bold text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleConsent() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "I agree to the collection and processing of my personal data for the purposes stated above.",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isConsentGiven,
                onCheckedChange = { viewModel.toggleConsent() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF57C6A9),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = {
                println("Continue button clicked")
                println("Consent given: $isConsentGiven")
                println("Context: $context")

                if (!isConsentGiven) {
                    println("Consent not given, button should be disabled")
                    return@Button
                }

                // ✅ Check if context is Activity
                val activity = context as? Activity
                if (activity == null) {
                    println("Error: Context is not an Activity")
                    return@Button
                }

                println("Activity found: $activity")

                // Request the permissions
                val permissionsToRequest = arrayOf(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
                )

                println("Requesting permissions: ${permissionsToRequest.joinToString()}")

                try {
                    permissionLauncher.launch(permissionsToRequest)
                    println("Permission request launched successfully")
                } catch (e: Exception) {
                    println("Error launching permissions: ${e.message}")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            enabled = isConsentGiven && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF57C6A9),
                disabledContainerColor = Color.Gray
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Policy Link
        Text(
            text = "You may withdraw your consent or request data deletion anytime in the Privacy Hub. View our Privacy Policy.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExpandableSection(
    title: String,
    subtitle: String,
    icon: Int,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = Color(0xFF57C6A9),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = if (expanded) content else subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = if (expanded) Int.MAX_VALUE else 2
                    )
                }
                Icon(
                    painter = painterResource(id = if (expanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float),
                    contentDescription = "Expand",
                    tint = Color(0xFF57C6A9)
                )
            }
        }
    }
}
