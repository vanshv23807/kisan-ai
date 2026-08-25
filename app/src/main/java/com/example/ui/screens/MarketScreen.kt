package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

enum class MarketTabSection(val label: String, val iconName: String) {
    BUY_ONLINE("Buy Online", "shopping_cart"),
    BUY_OFFLINE("Buy Offline", "store"),
    SELL_ONLINE("Sell Online", "trending_up"),
    SELL_OFFLINE("Sell Offline", "account_balance"),
    MARKET_RATES("Market Rates", "bar_chart"),
    SOIL_TESTING("Book Soil Test", "biotech"),
    VET_DOCTORS("Book Vet Doctor", "pets"),
    VET_CLINICS("Vet Clinics & Tests", "local_hospital")
}

@Composable
fun MarketScreen(
    prices: List<MarketPrice>,
    userProfile: UserProfile? = null,
    parcels: List<LandParcel> = emptyList(),
    products: List<AgriProduct> = emptyList(),
    cartItems: Map<String, Int> = emptyMap(),
    shops: List<AgriShop> = emptyList(),
    cropListings: List<CropSellListing> = emptyList(),
    buyerBids: List<CropBuyerBid> = emptyList(),
    physicalMandis: List<PhysicalMandi> = emptyList(),
    soilPackages: List<SoilTestingPackage> = emptyList(),
    onAddToCart: (String, Int) -> Unit = { _, _ -> },
    onUpdateCartQty: (String, Int) -> Unit = { _, _ -> },
    onPlaceOrder: (AgriProduct, Int, String, String, String) -> String = { _, _, _, _, _ -> "" },
    onListCropForSale: (String, String, Double, Int, Double, String) -> Unit = { _, _, _, _, _, _ -> },
    onAcceptBid: (CropSellListing, CropBuyerBid) -> Unit = { _, _ -> },
    onBookSoilTest: (SoilTestingPackage, String, String, String, String, String) -> String = { _, _, _, _, _, _ -> "" },
    onOpenNotifications: () -> Unit = {},
    onSelectPrice: (MarketPrice) -> Unit = {}
) {
    var activeSection by remember { mutableStateOf(MarketTabSection.BUY_ONLINE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Dialog States
    var selectedProductForDetail by remember { mutableStateOf<AgriProduct?>(null) }
    var productForCheckout by remember { mutableStateOf<AgriProduct?>(null) }
    var isCartDialogOpen by remember { mutableStateOf(false) }
    var isSellHarvestDialogOpen by remember { mutableStateOf(false) }
    var selectedListingForBids by remember { mutableStateOf<CropSellListing?>(null) }
    var selectedSoilPackageForBooking by remember { mutableStateOf<SoilTestingPackage?>(null) }
    var confirmedOrderTrackingId by remember { mutableStateOf<String?>(null) }
    var confirmedSoilBookingId by remember { mutableStateOf<String?>(null) }

    val totalCartCount = cartItems.values.sum()

    val categories = listOf("All", "Fertilizers", "Seeds", "Pesticides", "Organic", "Cattle Feed", "Equipment")

    val filteredProducts = products.filter {
        val matchesCategory = (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.brand.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val filteredPrices = prices.filter {
        searchQuery.isBlank() || it.cropName.contains(searchQuery, ignoreCase = true) || it.mandiName.contains(searchQuery, ignoreCase = true)
    }

    val filteredShops = shops.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true)
    }

    val filteredMandis = physicalMandis.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.district.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Top Header with Cart & Notification Quick Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Agri Market & Services",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Buy Inputs, Sell Harvest, Mandi Rates & Soil Test",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cart Badge Button
                Box {
                    IconButton(
                        onClick = {
                            if (cartItems.isNotEmpty()) {
                                // Open first item in cart for checkout
                                val firstId = cartItems.keys.firstOrNull()
                                val prod = products.find { it.id == firstId }
                                if (prod != null) productForCheckout = prod
                            } else {
                                isCartDialogOpen = true
                            }
                        },
                        modifier = Modifier
                            .testTag("market_cart_button")
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shopping Cart",
                            tint = PrimaryGreen
                        )
                    }

                    if (totalCartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .background(AlertRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = totalCartCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .testTag("market_search_input")
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                placeholder = {
                    val hint = when (activeSection) {
                        MarketTabSection.BUY_ONLINE -> "Search fertilizers, seeds, pesticides..."
                        MarketTabSection.BUY_OFFLINE -> "Search nearest Krishi Kendras & dealers..."
                        MarketTabSection.SELL_ONLINE -> "Search buyers & active crop bids..."
                        MarketTabSection.SELL_OFFLINE -> "Search APMC Mandis & procurement centers..."
                        MarketTabSection.MARKET_RATES -> "Search crop rates (Wheat, Mustard, Paddy)..."
                        MarketTabSection.SOIL_TESTING -> "Search soil & lab testing packages..."
                        MarketTabSection.VET_DOCTORS -> "Search vet doctors, livestock specialists..."
                        MarketTabSection.VET_CLINICS -> "Search vet clinics & animal health tests..."
                    }
                    Text(hint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = PrimaryGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )
        }

        // Main Navigation Section Tabs (6 Primary Options)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MarketTabSection.values()) { section ->
                    val isSelected = section == activeSection
                    Box(
                        modifier = Modifier
                            .testTag("market_tab_${section.name}")
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(22.dp)
                            )
                            .clickable { activeSection = section }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val icon = when (section) {
                                MarketTabSection.BUY_ONLINE -> Icons.Default.ShoppingCart
                                MarketTabSection.BUY_OFFLINE -> Icons.Default.Store
                                MarketTabSection.SELL_ONLINE -> Icons.Default.TrendingUp
                                MarketTabSection.SELL_OFFLINE -> Icons.Default.AccountBalance
                                MarketTabSection.MARKET_RATES -> Icons.Default.BarChart
                                MarketTabSection.SOIL_TESTING -> Icons.Default.Biotech
                                MarketTabSection.VET_DOCTORS -> Icons.Default.Pets
                                MarketTabSection.VET_CLINICS -> Icons.Default.LocalHospital
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = section.label,
                                tint = if (isSelected) Color.White else PrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = section.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // OPTION 1: BUY ONLINE (E-COMMERCE SHOPPING)
        // ==========================================
        if (activeSection == MarketTabSection.BUY_ONLINE) {
            // Category Pills
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isCatSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .testTag("cat_chip_$cat")
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isCatSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isCatSelected) PrimaryGreen else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCatSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Products Grid / Card List
            items(filteredProducts) { product ->
                ProductShoppingCard(
                    product = product,
                    onViewDetails = { selectedProductForDetail = product },
                    onBuyNow = { productForCheckout = product },
                    onAddToCart = { onAddToCart(product.id, 1) }
                )
            }
        }

        // ==========================================
        // OPTION 2: BUY OFFLINE (NEAREST SHOPS)
        // ==========================================
        else if (activeSection == MarketTabSection.BUY_OFFLINE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Verified Nearest Agri Input Stores", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                            Text("Live stock availability for Urea, DAP, Seeds & Tools", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            items(filteredShops) { shop ->
                OfflineShopCard(shop = shop)
            }
        }

        // ==========================================
        // OPTION 3: SELL ONLINE (DIGITAL HARVEST)
        // ==========================================
        else if (activeSection == MarketTabSection.SELL_ONLINE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Sell Harvest Directly to Millers", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
                                Text("Zero middleman commission • Free farmgate pickup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { isSellHarvestDialogOpen = true },
                            modifier = Modifier
                                .testTag("list_crop_button")
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ List Harvest for Live Bids", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "My Active Harvest Listings (${cropListings.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(cropListings) { listing ->
                CropListingCard(
                    listing = listing,
                    onViewBids = { selectedListingForBids = listing }
                )
            }
        }

        // ==========================================
        // OPTION 4: SELL OFFLINE (PHYSICAL MANDIS)
        // ==========================================
        else if (activeSection == MarketTabSection.SELL_OFFLINE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Nearby APMC Mandis & MSP Centers", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                            Text("Daily arrival volumes, gate hours & direct MSP counters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(filteredMandis) { mandi ->
                PhysicalMandiCard(mandi = mandi)
            }
        }

        // ==========================================
        // OPTION 5: MARKET RATES (EXPLORER)
        // ==========================================
        else if (activeSection == MarketTabSection.MARKET_RATES) {
            // KisanAI Market Forecast Insight Card
            item {
                Card(
                    modifier = Modifier
                        .testTag("market_ai_forecast_card")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KisanAI Market Forecast",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Market prices for Wheat are expected to rise by 5% next week due to high procurement demand. Consider holding 60% of stock if your dry storage allows.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Prices List
            items(filteredPrices) { price ->
                Card(
                    modifier = Modifier
                        .testTag("mandi_price_card_${price.id}")
                        .fillMaxWidth()
                        .clickable { onSelectPrice(price) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = price.cropName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (price.isBestPrice) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Best Price",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SecondaryContainerGreen)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${price.mandiName} • ${price.distanceKm} km away",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${price.pricePerQuintal}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "per Quintal",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day Trend: ${price.trend7Day}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = if (price.changePercent >= 0) "+${price.changePercent}% ▲" else "${price.changePercent}% ▼",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (price.changePercent >= 0) PrimaryGreen else AlertRed
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // OPTION 6: SOIL & LAB TESTING AT HOME
        // ==========================================
        else if (activeSection == MarketTabSection.SOIL_TESTING) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Biotech, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Doorstep Soil & Water Lab Testing", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                            Text("Agronomist collects samples at your field • Digital report in 2-3 days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            items(soilPackages) { pkg ->
                SoilTestingPackageCard(
                    pkg = pkg,
                    onBookNow = { selectedSoilPackageForBooking = pkg }
                )
            }
        }

        // ==========================================
        // OPTION 7: VET DOCTORS
        // ==========================================
        else if (activeSection == MarketTabSection.VET_DOCTORS) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Verified Veterinary Doctors", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                            Text("Doorstep farm visits • Instant phone consultation for cattle & poultry", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            val vetDoctors = listOf(
                Triple("Dr. Harpreet Singh (MVSc)", "Cattle & Dairy Specialist • 14 Yrs Exp", "3.2 km • Ludhiana Vet Hospital"),
                Triple("Dr. Sunita Sharma (Ph.D Vet)", "Poultry & Avian Disease Expert • 10 Yrs Exp", "5.8 km • Regional Animal Care"),
                Triple("Dr. Rajesh Verma (BVSc)", "Goat, Sheep & Swine Specialist • 8 Yrs Exp", "4.1 km • Rural Livestock Clinic")
            )

            items(vetDoctors) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryContainerGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = PrimaryGreen)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.first, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(doc.second, fontSize = 12.sp, color = PrimaryGreen)
                                Text(doc.third, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { /* Book Visit */ },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Book Visit (₹300)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // OPTION 8: VET CLINICS & ANIMAL HEALTH TESTS
        // ==========================================
        else if (activeSection == MarketTabSection.VET_CLINICS) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Nearest Vet Clinics & Animal Diagnostic Labs", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                            Text("Mastitis, Brucellosis, Milk Purity & FMD Vaccination Centers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            val vetClinics = listOf(
                Triple("Govt Veterinary Polyclinic", "Open 24x7 • Govt Subsidized Emergency", "Samrala Road, 2.4 km"),
                Triple("Pashu Swasthya Diagnostics", "Cattle Milk Testing & Blood Panel", "Ludhiana Mandi Area, 4.5 km"),
                Triple("Apex Vet Hospital & AI Center", "Artificial Insemination & Ultrasound", "GT Road, 6.1 km")
            )

            items(vetClinics) { clinic ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(clinic.first, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(clinic.second, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("📍 ${clinic.third}", fontSize = 11.5.sp, color = PrimaryGreen)
                        }
                        Button(
                            onClick = { /* Open clinic */ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Directions", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MODALS & DIALOGS
    // ==========================================

    // 1. Product Detail Dialog
    selectedProductForDetail?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { selectedProductForDetail = null },
            onBuyNow = {
                selectedProductForDetail = null
                productForCheckout = product
            },
            onAddToCart = {
                onAddToCart(product.id, 1)
                selectedProductForDetail = null
            }
        )
    }

    // 2. Checkout / Place Order Dialog
    productForCheckout?.let { product ->
        CheckoutOrderDialog(
            product = product,
            userProfile = userProfile,
            onDismiss = { productForCheckout = null },
            onConfirmOrder = { pack, address, payment ->
                val trackId = onPlaceOrder(product, 1, pack, address, payment)
                productForCheckout = null
                confirmedOrderTrackingId = trackId
            }
        )
    }

    // 3. Order Success Dialog
    confirmedOrderTrackingId?.let { trackId ->
        OrderSuccessDialog(
            trackingId = trackId,
            onDismiss = { confirmedOrderTrackingId = null }
        )
    }

    // 4. Sell Harvest Dialog
    if (isSellHarvestDialogOpen) {
        SellHarvestModalDialog(
            userProfile = userProfile,
            onDismiss = { isSellHarvestDialogOpen = false },
            onSubmitListing = { crop, variety, qty, price, moisture, loc ->
                onListCropForSale(crop, variety, qty, price, moisture, loc)
                isSellHarvestDialogOpen = false
            }
        )
    }

    // 5. View Buyer Bids Dialog
    selectedListingForBids?.let { listing ->
        BuyerBidsModalDialog(
            listing = listing,
            bids = buyerBids,
            onDismiss = { selectedListingForBids = null },
            onAcceptBid = { bid ->
                onAcceptBid(listing, bid)
                selectedListingForBids = null
            }
        )
    }

    // 6. Book Soil Test Dialog
    selectedSoilPackageForBooking?.let { pkg ->
        BookSoilTestModalDialog(
            pkg = pkg,
            userProfile = userProfile,
            parcels = parcels,
            onDismiss = { selectedSoilPackageForBooking = null },
            onConfirmBooking = { parcel, date, slot, address, notes ->
                val bookingId = onBookSoilTest(pkg, parcel, date, slot, address, notes)
                selectedSoilPackageForBooking = null
                confirmedSoilBookingId = bookingId
            }
        )
    }

    // 7. Soil Booking Success Dialog
    confirmedSoilBookingId?.let { bookingId ->
        SoilBookingSuccessDialog(
            bookingId = bookingId,
            onDismiss = { confirmedSoilBookingId = null }
        )
    }

    // 8. Empty Cart Dialog
    if (isCartDialogOpen) {
        AlertDialog(
            onDismissRequest = { isCartDialogOpen = false },
            title = { Text("Shopping Cart", fontWeight = FontWeight.Bold) },
            text = { Text("Your cart is currently empty. Browse the 'Buy Online' tab to add fertilizers, seeds, and equipment.") },
            confirmButton = {
                TextButton(onClick = { isCartDialogOpen = false }) {
                    Text("Continue Shopping", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// COMPOSABLE UI CARDS
// ==========================================

@Composable
fun ProductShoppingCard(
    product: AgriProduct,
    onViewDetails: () -> Unit,
    onBuyNow: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag("product_card_${product.id}")
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Image(
                        painter = painterResource(id = product.imageDrawableRes),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (product.discountPercent > 0) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(AlertRed, RoundedCornerShape(bottomEnd = 8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.brand.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${product.rating} (${product.reviewsCount})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(product.packSize, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${product.price}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.originalPrice}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Cart", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBuyNow,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Buy Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OfflineShopCard(shop: AgriShop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = shop.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = shop.shopType,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "${shop.address} (${shop.distanceKm} km away)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryContainerGreen)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${shop.distanceKm} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Available In Stock:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(shop.availableStock) { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("✓ $item", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Call dealer */ },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Shop", color = PrimaryGreen, fontSize = 12.sp)
                }

                Button(
                    onClick = { /* Open Directions */ },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CropListingCard(
    listing: CropSellListing,
    onViewBids: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${listing.quantityQuintals} Qtl ${listing.cropName}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Variety: ${listing.variety} • Moisture: ${listing.moisturePercent}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Location: ${listing.pickupLocation}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (listing.status == "Active") SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = listing.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (listing.status == "Active") PrimaryGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Asking Price: ₹${listing.askingPricePerQuintal}/Qtl", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Top Bid: ₹${listing.highestBidPrice}/Qtl (${listing.activeBidsCount} bids)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }

                Button(
                    onClick = onViewBids,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("View Bids (${listing.activeBidsCount})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PhysicalMandiCard(mandi: PhysicalMandi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(mandi.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${mandi.district}, ${mandi.state} • ${mandi.distanceKm} km away", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Open Hours: ${mandi.openHours}", fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${mandi.modalPriceToday}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Modal Price/Qtl", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today's Arrivals: ${mandi.todayArrivalsQuintals} Qtl", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${mandi.activeTradersCount} Active Commission Agents", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SoilTestingPackageCard(
    pkg: SoilTestingPackage,
    onBookNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = pkg.imageDrawableRes),
                    contentDescription = pkg.title,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(pkg.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(pkg.sampleCollectionType, fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${pkg.price}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("₹${pkg.originalPrice}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                        if (pkg.isGovtSubsidized) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Govt Subsidized", fontSize = 10.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Parameters Tested:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(pkg.parametersTested) { param ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(param, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBookNow,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Book Doorstep Field Visit", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// MODAL DIALOG IMPLEMENTATIONS
// ==========================================

@Composable
fun ProductDetailDialog(
    product: AgriProduct,
    onDismiss: () -> Unit,
    onBuyNow: () -> Unit,
    onAddToCart: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Image(
                        painter = painterResource(id = product.imageDrawableRes),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(product.brand.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                Text(product.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${product.price}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MRP ₹${product.originalPrice}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${product.discountPercent}% OFF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                }

                Text("Description:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(product.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)

                Text("Active Composition:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(product.composition, fontSize = 12.sp, color = PrimaryGreen)

                Text("Recommended Dosage:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(product.recommendedDosage, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAddToCart,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PrimaryGreen)
                    ) {
                        Text("Add to Cart", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onBuyNow,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Buy Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutOrderDialog(
    product: AgriProduct,
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onConfirmOrder: (packSize: String, address: String, paymentMethod: String) -> Unit
) {
    var selectedPack by remember { mutableStateOf(product.packSize) }
    var deliveryAddress by remember {
        mutableStateOf(
            if (userProfile != null) "${userProfile.houseNo}, ${userProfile.village}, ${userProfile.district}, ${userProfile.state} - ${userProfile.pinCode}"
            else "Plot 14-B, Samrala, Ludhiana, Punjab - 141114"
        )
    }
    var selectedPaymentMethod by remember { mutableStateOf("Cash on Delivery (COD)") }

    val paymentOptions = listOf("Cash on Delivery (COD)", "UPI / QR Code", "Kisan Credit Card (KCC)")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Confirm Agri Input Order", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)

                // Item Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = product.imageDrawableRes),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("1 Unit • ₹${product.price}", fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Pack Size Selection
                Text("Select Pack Size:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(product.packSizesAvailable) { pack ->
                        val isSel = pack == selectedPack
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedPack = pack }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(pack, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Delivery Address
                Text("Delivery Address:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = PrimaryGreen
                    ),
                    maxLines = 2
                )

                // Payment Mode
                Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                paymentOptions.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = method }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (method == selectedPaymentMethod),
                            onClick = { selectedPaymentMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Text(method, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Price Total & Place Order Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${product.price}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { onConfirmOrder(selectedPack, deliveryAddress, selectedPaymentMethod) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Place Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSuccessDialog(
    trackingId: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(48.dp))
        },
        title = { Text("Order Placed Successfully!", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tracking ID: #$trackingId", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                Text("Your order details and delivery status have been added to your app notifications and financial transactions.")
                Text("Delivery expected tomorrow by 4:00 PM.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun SellHarvestModalDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSubmitListing: (crop: String, variety: String, qty: Double, price: Int, moisture: Double, loc: String) -> Unit
) {
    var cropName by remember { mutableStateOf("Wheat (Kanak)") }
    var variety by remember { mutableStateOf("HD-3086 Sharbati") }
    var quantityText by remember { mutableStateOf("50") }
    var priceText by remember { mutableStateOf("2450") }
    var moistureText by remember { mutableStateOf("11.5") }
    var location by remember {
        mutableStateOf(
            if (userProfile != null) "${userProfile.village}, ${userProfile.district}" else "Samrala, Ludhiana"
        )
    }

    val cropChoices = listOf("Wheat (Kanak)", "Mustard (Sarson)", "Paddy (Basmati)", "Cotton (Narma)", "Soybean", "Maize (Makki)", "Tomato", "Potato")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("List Harvest for Direct Sale", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)

                Text("Select Crop:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cropChoices) { crop ->
                        val isSel = crop == cropName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { cropName = crop }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(crop, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Qty (Quintals)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Asking ₹/Qtl") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = variety,
                        onValueChange = { variety = it },
                        label = { Text("Crop Variety") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = moistureText,
                        onValueChange = { moistureText = it },
                        label = { Text("Moisture %") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Farm Pickup Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val qty = quantityText.toDoubleOrNull() ?: 10.0
                            val price = priceText.toIntOrNull() ?: 2400
                            val moisture = moistureText.toDoubleOrNull() ?: 12.0
                            onSubmitListing(cropName, variety, qty, price, moisture, location)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Post Listing", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerBidsModalDialog(
    listing: CropSellListing,
    bids: List<CropBuyerBid>,
    onDismiss: () -> Unit,
    onAcceptBid: (CropBuyerBid) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Live Bids for ${listing.quantityQuintals} Qtl ${listing.cropName}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)

                bids.forEach { bid ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bid.buyerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(bid.buyerType, fontSize = 11.sp, color = PrimaryGreen)
                                }
                                Text("₹${bid.bidPricePerQuintal}/Qtl", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Total: ₹${bid.totalAmount} • ${bid.pickupPromise}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onAcceptBid(bid) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("Accept Bid & Schedule Truck", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun BookSoilTestModalDialog(
    pkg: SoilTestingPackage,
    userProfile: UserProfile?,
    parcels: List<LandParcel>,
    onDismiss: () -> Unit,
    onConfirmBooking: (parcel: String, date: String, slot: String, address: String, notes: String) -> Unit
) {
    var selectedParcel by remember { mutableStateOf(parcels.firstOrNull()?.name ?: "Main Field (Field 1)") }
    var selectedDate by remember { mutableStateOf("Tomorrow") }
    var selectedSlot by remember { mutableStateOf("Morning (9:00 AM - 12:00 PM)") }
    var address by remember {
        mutableStateOf(
            if (userProfile != null) "${userProfile.village}, ${userProfile.district}, ${userProfile.state}"
            else "Samrala, Ludhiana, Punjab"
        )
    }

    val dates = listOf("Tomorrow", "Day After Tomorrow", "This Weekend")
    val slots = listOf("Morning (9:00 AM - 12:00 PM)", "Afternoon (2:00 PM - 5:00 PM)")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Schedule Doorstep Field Visit", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                Text(pkg.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Text("Select Farm Parcel / Field:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val parcelOptions = if (parcels.isNotEmpty()) parcels.map { it.name } else listOf("Field 1 (North Plot)", "Field 2 (South Plot)")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(parcelOptions) { p ->
                        val isSel = p == selectedParcel
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedParcel = p }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(p, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Text("Select Date:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dates) { d ->
                        val isSel = d == selectedDate
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedDate = d }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(d, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Text("Select Time Slot:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                slots.forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSlot = s }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (s == selectedSlot),
                            onClick = { selectedSlot = s },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Text(s, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Field Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Booking Fee", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${pkg.price}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }

                    Button(
                        onClick = { onConfirmBooking(selectedParcel, selectedDate, selectedSlot, address, "") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Confirm Visit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SoilBookingSuccessDialog(
    bookingId: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(48.dp))
        },
        title = { Text("Field Visit Booked!", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Booking Reference: #$bookingId", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                Text("A certified agronomist has been assigned for sample collection. Digital Soil Health Card will be generated in 2-3 days.")
                Text("Notification alert has been added to your app task manager.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Done")
            }
        }
    )
}
