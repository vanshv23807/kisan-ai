package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialTransaction
import com.example.ui.components.KisanTopAppBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    transactions: List<FinancialTransaction> = emptyList(),
    onAddTransaction: (title: String, category: String, amount: Double, isIncome: Boolean) -> Unit = { _, _, _, _ -> },
    onDeleteTransaction: (FinancialTransaction) -> Unit = {},
    onOpenSoilReport: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var isIncomeType by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf("All Months") }

    val months = listOf("All Months", "August 2026", "July 2026", "June 2026", "May 2026", "April 2026")

    // Filter transactions by selected month
    val filteredTransactions = if (selectedMonth == "All Months") {
        transactions
    } else {
        transactions.filter { tx ->
            val monthAbbr = selectedMonth.split(" ")[0].take(3)
            tx.date.contains(monthAbbr, ignoreCase = true) || tx.date.contains("Today", ignoreCase = true)
        }
    }

    // Dynamic calculations - NO STATIC DATA
    val totalRevenue = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpense

    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = "Farm Finance & Accounting",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
        ) {
            // Prominent Action Buttons Header at Top
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isIncomeType = false
                            showAddDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("btn_top_add_expense"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("- Add Expense", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            isIncomeType = true
                            showAddDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("btn_top_add_income"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Profit / Revenue", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Month Selector Bar
            item {
                Column {
                    Text("Select Ledger Month", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(months) { month ->
                            FilterChip(
                                selected = selectedMonth == month,
                                onClick = { selectedMonth = month },
                                label = { Text(month, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Net Profit Card (Dynamic calculation for selected month)
            item {
                Card(
                    modifier = Modifier
                        .testTag("finance_net_profit_card")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NET PROFIT ($selectedMonth)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (netProfit >= 0) SecondaryContainerGreen else AlertRedContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (netProfit >= 0) "Profitable" else "Loss",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netProfit >= 0) PrimaryGreen else AlertRedText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "₹${String.format("%,.0f", netProfit)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) PrimaryGreen else AlertRed
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Revenue",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${String.format("%,.0f", totalRevenue)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Monthly Expenses",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${String.format("%,.0f", totalExpense)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                            }
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ledger Entries (${filteredTransactions.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedMonth,
                        fontSize = 12.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryContainerGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = "No Ledger Entries for $selectedMonth",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Use the buttons at the top to log farm expenses (fertilizer, diesel, feed) or crop revenue.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        onDelete = { onDeleteTransaction(tx) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(if (isIncomeType) "Crop Harvest Sale" else "Seeds & Fertilizers") }
        var amountText by remember { mutableStateOf("") }

        val categories = if (isIncomeType) {
            listOf("Crop Harvest Sale", "Milk & Dairy Sale", "Egg Sale", "Govt Subsidy / PM-KISAN", "Equipment Rental Income", "Other Revenue")
        } else {
            listOf("Seeds & Fertilizers", "Pesticides & Chemicals", "Diesel & Tractor Fuel", "Cattle Feed & Fodder", "Labor Wages", "Irrigation & Electricity", "Machinery Repair", "Veterinary & Medicine")
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (isIncomeType) "Log Revenue / Profit Entry" else "Log Expense Entry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncomeType) PrimaryGreen else AlertRed
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Description") },
                        placeholder = { Text(if (isIncomeType) "e.g. Sold 50 Quintals Wheat" else "e.g. Bought 2 Bags Nano Urea") },
                        modifier = Modifier
                            .testTag("finance_title_input")
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (₹)") },
                        placeholder = { Text("e.g. 12500") },
                        modifier = Modifier
                            .testTag("finance_amount_input")
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isIncomeType) PrimaryGreen else AlertRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amt > 0) {
                            onAddTransaction(title, category, amt, isIncomeType)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isIncomeType) PrimaryGreen else AlertRed),
                    modifier = Modifier.testTag("finance_save_button")
                ) {
                    Text("Save Entry", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun TransactionItemRow(
    transaction: FinancialTransaction,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag("transaction_item_${transaction.id}")
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (transaction.isIncome) SecondaryContainerGreen else AlertRedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (transaction.isIncome) PrimaryGreen else AlertRedText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${transaction.category} • ${transaction.date}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (transaction.isIncome) "+" else "-"}₹${String.format("%,.0f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (transaction.isIncome) PrimaryGreen else AlertRed
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
