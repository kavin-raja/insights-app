package com.example.insightsapp.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.remote.RemoteDatabaseProvider

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        phoneNumber = "+919876543210"
    )
}

@Composable
fun ProfileScreen(
    phoneNumber: String,
    onNavigateToMyCoupons: () -> Unit = {}
) {
    val context = LocalContext.current
    val databaseProvider = RemoteDatabaseProvider.getInstance(context)

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(databaseProvider, phoneNumber)
    )

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 0.dp)
    ) {
        // Header
        ProfileHeader()

        // User Info Section
        if (uiState.user != null) {
            UserInfoSection(
                user = uiState.user!!,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }

        // Notifications Toggle
        NotificationToggle(
            isEnabled = uiState.notificationsEnabled,
            onToggle = { viewModel.toggleNotifications(it) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items
        MenuSection(
            onPrivacyClick = { viewModel.onPrivacyClick() },
            onCouponsClick = { onNavigateToMyCoupons() },
            onBankAccountClick = { viewModel.onBankAccountClick() },
            onHelpClick = { viewModel.onHelpClick() }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ProfileHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = com.example.insightsapp.R.drawable.ic_arrow_back),
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(24.dp)
                .clickable { /* Handle back navigation */ }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun UserInfoSection(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.example.insightsapp.R.drawable.ic_person),
                    contentDescription = "Profile Picture",
                    tint = Color(0xFF57C6A9),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = if (user.fullName.isNotBlank()) user.fullName else "User",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Phone Number
            Text(
                text = user.phoneNumber,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NotificationToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF57C6A9),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun MenuSection(
    onPrivacyClick: () -> Unit,
    onCouponsClick: () -> Unit,
    onBankAccountClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        MenuItemCard(
            icon = com.example.insightsapp.R.drawable.ic_privacy_shield,
            title = "Privacy & Data Consent",
            onClick = onPrivacyClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        MenuItemCard(
            icon = com.example.insightsapp.R.drawable.ic_coupon,
            title = "My Coupons",
            onClick = onCouponsClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        MenuItemCard(
            icon = com.example.insightsapp.R.drawable.ic_bank,
            title = "Link Bank Account",
            onClick = onBankAccountClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        MenuItemCard(
            icon = com.example.insightsapp.R.drawable.ic_help,
            title = "Help & Support",
            onClick = onHelpClick
        )
    }
}

@Composable
fun MenuItemCard(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(0xFF57C6A9),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = com.example.insightsapp.R.drawable.ic_chevron_right),
                contentDescription = "Arrow",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
