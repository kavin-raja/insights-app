package com.example.insightsapp.ui.coupons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.insightsapp.data.database.Coupon
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponsScreen(
    phoneNumber: String,  // ✅ Add phone number parameter
    onClose: () -> Unit,
    onClaimed: (Coupon) -> Unit
) {
    val context = LocalContext.current
    val databaseProvider = RemoteDatabaseProvider.getInstance(context)

    // ✅ Create ViewModel with correct parameters
    val vm: CouponsViewModel = viewModel(
        factory = CouponsViewModelFactory(databaseProvider, phoneNumber)
    )

    val coupons by vm.coupons.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val snackHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Redeem Coupons") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackHost) }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(coupons, key = { it.id }) { c ->
                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = c.imageUrl,
                                contentDescription = c.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop,
                                onError = { error ->
                                    println("❌ Image failed: ${c.imageUrl}")
                                    println("   Error: ${error.result.throwable.message}")
                                },
                                onSuccess = {
                                    println("✅ Image loaded: ${c.imageUrl}")
                                }
                            )

                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = c.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            val locked = c.claimed || !c.canBuy

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${c.points} pts",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )

                                Button(
                                    onClick = {
                                        println("🎫 Claiming coupon: ${c.title}")
                                        vm.claim(c.id)
                                        onClaimed(c)
                                        scope.launch {
                                            snackHost.showSnackbar("Coupon Purchased for ${c.points} points")
                                        }
                                    },
                                    enabled = !locked
                                ) {
                                    Text(
                                        when {
                                            c.claimed -> "Redeemed"
                                            !c.canBuy -> "Insufficient Points"
                                            else -> "Redeem"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (coupons.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No coupons available")
                        }
                    }
                }
            }
        }
    }
}
