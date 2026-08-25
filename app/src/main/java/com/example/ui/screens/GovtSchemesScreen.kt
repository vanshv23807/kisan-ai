package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GovtNewsItem
import com.example.data.model.GovtScheme
import com.example.ui.components.KisanTopAppBar
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

@Composable
fun GovtSchemesScreen(
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All Schemes") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSchemeForDetails by remember { mutableStateOf<GovtScheme?>(null) }
    var applicationSubmittedMessage by remember { mutableStateOf<String?>(null) }

    val newsList = remember {
        listOf(
            GovtNewsItem(
                id = "news_1",
                title = "Cabinet approves MSP Hike for Kharif Crops 2026-27 (Paddy MSP ₹2,300/Quintal)",
                dateDisplay = "Today, 09:30 AM",
                category = "MSP Update",
                summary = "The Union Cabinet chaired by PM Narendra Modi has approved higher Minimum Support Prices (MSP) for all mandated Kharif crops to ensure remunerative prices to growers."
            ),
            GovtNewsItem(
                id = "news_2",
                title = "PM-KISAN 17th Installment Disbursement Notice Issued",
                dateDisplay = "Yesterday",
                category = "Direct Transfer",
                summary = "Over ₹20,000 Crores will be transferred directly to eligible farmers' bank accounts via e-KYC verified DBT link."
            ),
            GovtNewsItem(
                id = "news_3",
                title = "80% Subsidy on Solar Agriculture Pumps extended in Punjab & Haryana",
                dateDisplay = "Aug 16, 2026",
                category = "PM-KUSUM Subsidy",
                summary = "Farmers with tube-wells can register online for 3HP to 7.5HP solar pumps with 80% combined Central and State govt subsidy."
            )
        )
    }

    val schemesList = remember {
        listOf(
            GovtScheme(
                id = "scheme_1",
                title = "PM-KISAN Samman Nidhi Yojana",
                shortDescription = "Direct income support of ₹6,000 per year directly into farmers' bank accounts in 3 equal installments.",
                category = "Direct Transfer",
                subsidyAmount = "₹6,000 / Year",
                eligibility = "All landholding farmer families across India holding cultivate land in their name.",
                requiredDocuments = listOf("Aadhaar Card", "Land Khatauni / Khasra Proof", "Bank Passbook with IFSC", "Aadhaar-Linked Mobile Number"),
                applicationProcess = "Step 1: Visit PM-KISAN official portal or nearest CSC Center.\nStep 2: Enter Aadhaar number & Land Registration ID.\nStep 3: Complete OTP e-KYC verification.",
                officialPortalUrl = "https://pmkisan.gov.in"
            ),
            GovtScheme(
                id = "scheme_2",
                title = "PM-KUSUM Solar Pump Subsidy Scheme",
                shortDescription = "80% financial subsidy on installation of stand-alone solar water pumps for irrigation.",
                category = "Subsidies",
                subsidyAmount = "Up to 80% Subsidy",
                eligibility = "Individual farmers, water user associations, and cooperatives having borewell or open well.",
                requiredDocuments = listOf("Land Ownership Document", "Borewell / Tube-well NOC", "Bank Account Details", "Aadhaar Card"),
                applicationProcess = "Apply through State Renewable Energy Agency (PEDA) online portal with land coordinates.",
                officialPortalUrl = "https://pmkusum.mnre.gov.in"
            ),
            GovtScheme(
                id = "scheme_3",
                title = "Pradhan Mantri Fasal Bima Yojana (PMFBY)",
                shortDescription = "Comprehensive crop insurance cover for natural calamities, pests, and unseasonal rainfall.",
                category = "Insurance",
                subsidyAmount = "1.5% to 2% Low Premium",
                eligibility = "All farmers growing notified crops in notified areas including sharecroppers.",
                requiredDocuments = listOf("Crop Sowing Certificate", "Aadhaar Card", "Land Bank Document", "Cancelled Bank Cheque"),
                applicationProcess = "Register through bank branch, insurance intermediary, or PMFBY mobile app within 14 days of sowing.",
                officialPortalUrl = "https://pmfby.gov.in"
            ),
            GovtScheme(
                id = "scheme_4",
                title = "Kisan Credit Card (KCC) Scheme",
                shortDescription = "Collateral-free agricultural credit limit up to ₹3,00,000 at a low effective interest rate of 4%.",
                category = "Loans & Credit",
                subsidyAmount = "4% Interest Rate",
                eligibility = "Farmers, tenant farmers, oral lessees, sharecroppers, and livestock/poultry keepers.",
                requiredDocuments = listOf("Filled KCC Application Form", "Pahani / Land Records", "2 Passport Photos", "Aadhaar & PAN"),
                applicationProcess = "Submit application at any commercial bank branch or PACS cooperative society.",
                officialPortalUrl = "https://agricoop.gov.in"
            ),
            GovtScheme(
                id = "scheme_5",
                title = "Sub-Mission on Agricultural Mechanization (SMAM)",
                shortDescription = "50% to 80% subsidy for purchasing Tractors, Rotavators, Combine Harvesters, and Agri-Drones.",
                category = "Subsidies",
                subsidyAmount = "50% - 80% Subsidy",
                eligibility = "Small and marginal farmers, women farmers, and Custom Hiring Centers (CHCs).",
                requiredDocuments = listOf("Aadhaar Card", "Land Ownership Proof", "Quotation from Authorized Dealer", "Bank Account"),
                applicationProcess = "Apply online on DBT Agriculture portal (agrimachinery.nic.in).",
                officialPortalUrl = "https://agrimachinery.nic.in"
            ),
            GovtScheme(
                id = "scheme_6",
                title = "Animal Husbandry Infrastructure Development Fund (AHIDF)",
                shortDescription = "3% interest subvention and 75% credit guarantee for dairy processing, poultry, and animal feed plants.",
                category = "Loans & Credit",
                subsidyAmount = "3% Interest Subvention",
                eligibility = "Farmers, FPOs, MSMEs, Private Companies, and Dairy Cooperatives.",
                requiredDocuments = listOf("Detailed Project Report (DPR)", "Land Lease / Purchase Agreement", "GST / PAN", "Bank Appraisal"),
                applicationProcess = "Submit DPR online via ahidf.udyamimitra.in portal.",
                officialPortalUrl = "https://ahidf.udyamimitra.in"
            )
        )
    }

    val filteredSchemes = schemesList.filter { scheme ->
        val matchesCategory = (selectedCategory == "All Schemes") || (scheme.category == selectedCategory)
        val matchesSearch = scheme.title.contains(searchQuery, ignoreCase = true) ||
                scheme.shortDescription.contains(searchQuery, ignoreCase = true) ||
                scheme.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = "Govt Schemes & Agri News",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search schemes (e.g. Solar Pump, PM KISAN, KCC)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("govt_scheme_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Category Filter Chips
            item {
                val categories = listOf("All Schemes", "Direct Transfer", "Subsidies", "Loans & Credit", "Insurance")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Latest Government Agriculture News Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Newspaper, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Latest Government News & Circulars",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }

                        newsList.forEach { news ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = news.category,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                        Text(
                                            text = news.dateDisplay,
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = news.title,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = news.summary,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Government Schemes Catalog Section
            item {
                Text(
                    text = "Active Government Schemes (${filteredSchemes.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredSchemes) { scheme ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSchemeForDetails = scheme }
                        .testTag("govt_scheme_card_${scheme.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = scheme.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = PrimaryGreen,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = scheme.subsidyAmount,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = scheme.shortDescription,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category: ${scheme.category}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryGreen
                            )

                            Button(
                                onClick = { selectedSchemeForDetails = scheme },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("View Details & Apply", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Detailed Scheme Modal Dialog
    if (selectedSchemeForDetails != null) {
        val scheme = selectedSchemeForDetails!!
        AlertDialog(
            onDismissRequest = { selectedSchemeForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(scheme.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            color = SecondaryContainerGreen,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Subsidy / Benefit Highlight", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                Text(scheme.subsidyAmount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            }
                        }
                    }

                    item {
                        Text("Eligibility Criteria", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(scheme.eligibility, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    item {
                        Text("Required Documents", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        scheme.requiredDocuments.forEach { doc ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(doc, fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        Text("How to Apply Step-by-Step", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(scheme.applicationProcess, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
                    }

                    if (applicationSubmittedMessage != null) {
                        item {
                            Text(
                                text = applicationSubmittedMessage!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        applicationSubmittedMessage = "Application initiated! Pre-filling land documents from KisanAI vault..."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Apply on Official Portal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSchemeForDetails = null }) {
                    Text("Close", color = PrimaryGreen)
                }
            }
        )
    }
}
