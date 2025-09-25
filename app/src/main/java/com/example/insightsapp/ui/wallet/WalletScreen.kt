package com.example.insightsapp.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightsapp.data.database.Transaction
import java.text.SimpleDateFormat
import java.util.*

@Preview(showBackground = true)
@Composable
fun WalletScreenPreview() {
    val mockTransactions = listOf(
        Transaction(
            id = 1,
            phoneNumber = "+919876543210",
            type = "CREDIT",
            amount = 500.0,
            description = "Signup Reward",
            timestamp = System.currentTimeMillis()
        ),
        Transaction(
            id = 2,
            phoneNumber = "+919876543210",
            type = "CREDIT",
            amount = 100.0,
            description = "Brand 2 Survey Reward",
            timestamp = System.currentTimeMillis() - 86400000
        ),
        Transaction(
            id = 3,
            phoneNumber = "+919876543210",
            type = "DEBIT",
            amount = 200.0,
            description = "Gift Card Redemption",
            timestamp = System.currentTimeMillis() - 172800000
        )
    )

    WalletScreen(
        currentBalance = 500.0,
        transactions = mockTransactions,
        onWithdrawClick = {},
        onRedeemClick = {}
    )
}

@Composable
fun WalletScreen(
    currentBalance: Double,
    transactions: List<Transaction>,
    onWithdrawClick: () -> Unit,
    onRedeemClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Cash") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F8))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                // ✅ Use XML wallet icon for header
                painter = painterResource(id = com.example.insightsapp.R.drawable.ic_wallet_nav),
                contentDescription = "Menu",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF57C6A9)
            )

            Text(
                text = "My Wallet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Icon(
                painter = painterResource(id = com.example.insightsapp.R.drawable.ic_profile),
                contentDescription = "Profile",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF57C6A9)
            )
        }

        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Balance",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "₹${String.format("%.0f", currentBalance)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Cash/Points Toggle
                Row(
                    modifier = Modifier
                        .background(
                            Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(4.dp)
                ) {
                    listOf("Cash", "Points").forEach { tab ->
                        Text(
                            text = tab,
                            modifier = Modifier
                                .clickable { selectedTab = tab }
                                .background(
                                    if (selectedTab == tab) Color(0xFF57C6A9) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            color = if (selectedTab == tab) Color.White else Color.Gray,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onWithdrawClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF57C6A9))
            ) {
                Icon(
                    painter = painterResource(id = com.example.insightsapp.R.drawable.ic_arrow_up),
                    contentDescription = "Withdraw",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Withdraw",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onRedeemClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF57C6A9))
            ) {
                Icon(
                    painter = painterResource(id = com.example.insightsapp.R.drawable.ic_coin),
                    contentDescription = "Redeem",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF57C6A9)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Redeem",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF57C6A9)
                )
            }
        }

        // Recent Transactions
        Text(
            text = "Recent Transactions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { transaction ->
                TransactionCard(transaction = transaction)
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())
    val isCredit = transaction.type == "CREDIT"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = if (isCredit)
                        com.example.insightsapp.R.drawable.ic_arrow_down
                    else
                        com.example.insightsapp.R.drawable.ic_arrow_up
                ),
                contentDescription = transaction.type,
                modifier = Modifier.size(24.dp),
                tint = if (isCredit) Color(0xFF4CAF50) else Color(0xFFFF4081)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = dateFormatter.format(Date(transaction.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "${if (isCredit) "+" else "-"}₹${transaction.amount.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) Color(0xFF4CAF50) else Color(0xFFFF4081)
            )
        }
    }
}
