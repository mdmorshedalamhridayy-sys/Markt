package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreApp(viewModel: StoreViewModel) {
    // Collect Reactive State Streams
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val productsAll by viewModel.products.collectAsStateWithLifecycle(initialValue = emptyList())
    val ordersAll by viewModel.allOrdersList.collectAsStateWithLifecycle(initialValue = emptyList())
    val usersAll by viewModel.allUsersList.collectAsStateWithLifecycle(initialValue = emptyList())
    val notificationsAll by viewModel.allNotifications.collectAsStateWithLifecycle(initialValue = emptyList())
    val cartList by viewModel.cart.collectAsStateWithLifecycle()

    // Search and filters
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    // Local Interactive Navigation & View States
    var systemMode by remember { mutableStateOf("UserStore") } // "UserStore" or "AdminPanel"
    var selectedTab by remember { mutableStateOf("Store") } // "Store", "Notifications", "Orders"
    var selectedAdminTab by remember { mutableStateOf("Products") } // "Products", "Orders", "Notifications", "Users"

    // Dialog & Detail Trigger States
    var showAuthDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddNotificationDialog by remember { mutableStateOf(false) }
    var selectedProductDetails by remember { mutableStateOf<com.example.data.Product?>(null) }

    // Screen Safe Area Margin Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Brand Icon Rounded Square
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF005AC1), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AS",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Text(
                                text = "AI Studio Store",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1B1B1F)
                            )
                            Text(
                                text = "Pro Admin Dashboard Enabled",
                                fontSize = 10.sp,
                                color = Color(0xFF44474E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Quick profile / sign-in indicator
                    if (currentUser == null) {
                        Button(
                            onClick = { showAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD8E2FF),
                                contentColor = Color(0xFF001A41)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp).testTag("sign_in_open_btn")
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = "Sign In", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Switcher button for Admin Modes
                        if (currentUser?.isAdmin == true) {
                            IconButton(
                                onClick = {
                                    systemMode = if (systemMode == "UserStore") "AdminPanel" else "UserStore"
                                },
                                modifier = Modifier.testTag("admin_toggle_mode_btn")
                            ) {
                                Icon(
                                    imageVector = if (systemMode == "UserStore") Icons.Filled.AdminPanelSettings else Icons.Filled.Storefront,
                                    contentDescription = "Switch View",
                                    tint = if (systemMode == "AdminPanel") Color(0xFF93000A) else Color(0xFF005AC1)
                                )
                            }
                        }

                        // Geometric Profile avatar bubble
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .background(Color(0xFFE1E2EC), CircleShape)
                                .clickable { showAuthDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 16.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFDFBFF) // GeoSurface
                )
            )
        },
        bottomBar = {
            if (systemMode == "UserStore") {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.height(64.dp)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == "Store",
                        onClick = { selectedTab = "Store" },
                        icon = { Icon(Icons.Outlined.Storefront, contentDescription = "Store") },
                        label = { Text("Store", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_store_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == "Notifications",
                        onClick = { selectedTab = "Notifications" },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (notificationsAll.isNotEmpty()) {
                                        Badge { Text(notificationsAll.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Alerts")
                            }
                        },
                        label = { Text("Alerts", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_alerts_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == "Orders",
                        onClick = { selectedTab = "Orders" },
                        icon = {
                            val pendingCount = ordersAll.filter { it.userEmail == currentUser?.email && it.status == "Pending" }.size
                            BadgedBox(
                                badge = {
                                    if (pendingCount > 0) {
                                        Badge { Text(pendingCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders")
                            }
                        },
                        label = { Text("Orders", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_orders_tab")
                    )
                }
            } else {
                // Admin Mode Navigation Bar
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                    modifier = Modifier.height(64.dp)
                ) {
                    NavigationBarItem(
                        selected = selectedAdminTab == "Products",
                        onClick = { selectedAdminTab = "Products" },
                        icon = { Icon(Icons.Filled.Apps, contentDescription = "Products Admin") },
                        label = { Text("Apps", fontSize = 10.sp) },
                        modifier = Modifier.testTag("admin_apps_tab")
                    )
                    NavigationBarItem(
                        selected = selectedAdminTab == "Orders",
                        onClick = { selectedAdminTab = "Orders" },
                        icon = {
                            val activePending = ordersAll.filter { it.status == "Pending" }.size
                            BadgedBox(
                                badge = {
                                    if (activePending > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) { Text(activePending.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Verify Orders")
                            }
                        },
                        label = { Text("Claims", fontSize = 10.sp) },
                        modifier = Modifier.testTag("admin_claims_tab")
                    )
                    NavigationBarItem(
                        selected = selectedAdminTab == "Notifications",
                        onClick = { selectedAdminTab = "Notifications" },
                        icon = { Icon(Icons.Filled.Campaign, contentDescription = "Control Alerts") },
                        label = { Text("Offers", fontSize = 10.sp) },
                        modifier = Modifier.testTag("admin_offers_tab")
                    )
                    NavigationBarItem(
                        selected = selectedAdminTab == "Users",
                        onClick = { selectedAdminTab = "Users" },
                        icon = { Icon(Icons.Filled.Group, contentDescription = "Users Access") },
                        label = { Text("Users", fontSize = 10.sp) },
                        modifier = Modifier.testTag("admin_users_tab")
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Screen switcher
            AnimatedContent(
                targetState = systemMode,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "SystemModeAnimation"
            ) { mode ->
                when (mode) {
                    "UserStore" -> {
                        UserStoreView(
                            selectedTab = selectedTab,
                            viewModel = viewModel,
                            productsAll = productsAll,
                            ordersAll = ordersAll,
                            notificationsAll = notificationsAll,
                            cartList = cartList,
                            onOpenDetails = { selectedProductDetails = it },
                            onOpenCheckout = { showCheckoutDialog = true },
                            currentUser = currentUser
                        )
                    }
                    "AdminPanel" -> {
                        AdminPanelView(
                            selectedTab = selectedAdminTab,
                            viewModel = viewModel,
                            productsAll = productsAll,
                            ordersAll = ordersAll,
                            notificationsAll = notificationsAll,
                            usersAll = usersAll,
                            onOpenAddProduct = { showAddProductDialog = true },
                            onOpenAddNotification = { showAddNotificationDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet / Drawers implement as dialogs
    if (showAuthDialog) {
        AuthDialog(
            viewModel = viewModel,
            currentUser = currentUser,
            onDismiss = { showAuthDialog = false }
        )
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            viewModel = viewModel,
            cartList = cartList,
            onDismiss = { showCheckoutDialog = false }
        )
    }

    selectedProductDetails?.let { product ->
        ProductDetailsDialog(
            product = product,
            viewModel = viewModel,
            currentUser = currentUser,
            onAddToCart = { viewModel.addToCart(product) },
            isInCart = cartList.any { it.id == product.id },
            onDismiss = { selectedProductDetails = null }
        )
    }

    if (showAddProductDialog) {
        AddNewProductDialog(
            viewModel = viewModel,
            onDismiss = { showAddProductDialog = false }
        )
    }

    if (showAddNotificationDialog) {
        AddNewNotificationDialog(
            viewModel = viewModel,
            onDismiss = { showAddNotificationDialog = false }
        )
    }
}

// ==========================================
// STORE VIEW (USER SIDE)
// ==========================================
@Composable
fun UserStoreView(
    selectedTab: String,
    viewModel: StoreViewModel,
    productsAll: List<com.example.data.Product>,
    ordersAll: List<com.example.data.PurchaseOrder>,
    notificationsAll: List<com.example.data.NotificationAlert>,
    cartList: List<com.example.data.Product>,
    onOpenDetails: (com.example.data.Product) -> Unit,
    onOpenCheckout: () -> Unit,
    currentUser: com.example.data.UserAccount?
) {
    when (selectedTab) {
        "Store" -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Promotional banner or special announcement ticker in Geometric Balance styling
                if (notificationsAll.isNotEmpty()) {
                    val featured = notificationsAll.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF005AC1) // Geometric Balance brand Blue
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "FLASH OFFER",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = featured.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = featured.message,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = { /* Simple CTA actions */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF005AC1)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Claim Offer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val offerEmoji = when (featured.type) {
                                    "discount" -> "💸"
                                    "announcement" -> "📢"
                                    "update" -> "🚀"
                                    else -> "📺"
                                }
                                Text(offerEmoji, fontSize = 32.sp)
                            }
                        }
                    }
                }

                // Search & Filter header
                val query by viewModel.searchQuery.collectAsStateWithLifecycle()
                val filterCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
                val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search premium apps...", fontSize = 13.sp, color = Color(0xFF44474E)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon", tint = Color(0xFF44474E)) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Color(0xFF44474E))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("store_search_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE1E2EC),
                            unfocusedContainerColor = Color(0xFFE1E2EC),
                            focusedIndicatorColor = Color(0xFF005AC1),
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF1B1B1F),
                            unfocusedTextColor = Color(0xFF1B1B1F)
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    // Categories pills scroll row (Horizontal Scrolling enabled & Styled)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("All", "Regular", "Subscription", "Mod")
                        categories.forEach { cat ->
                            val isSelected = filterCat == cat
                            val label = when (cat) {
                                "All" -> "All Apps"
                                "Regular" -> "Utility"
                                "Subscription" -> "Subscriptions"
                                else -> "Mod Apps"
                            }
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = if (isSelected) Color(0xFFD8E2FF) else Color(0xFFE1E2EC),
                                contentColor = if (isSelected) Color(0xFF001A41) else Color(0xFF44474E),
                                modifier = Modifier
                                    .clickable { viewModel.selectedCategory.value = cat }
                                    .testTag("chip_cat_$cat")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Sorters selector
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Filter: $filterCat Category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sort", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            var showSortMenu by remember { mutableStateOf(false) }
                            Box {
                                Text(
                                    text = sortBy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { showSortMenu = true }
                                        .padding(4.dp)
                                        .testTag("sort_menu_trigger")
                                )
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    listOf("Popularity", "New Arrivals", "Price Asc", "Price Desc").forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt, fontSize = 12.sp) },
                                            onClick = {
                                                viewModel.sortBy.value = opt
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Filter & Sort actual list in memory
                val filteredProductsList = remember(productsAll, query, filterCat, sortBy) {
                    var list = productsAll.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true)
                    }
                    if (filterCat != "All") {
                        list = list.filter { it.category == filterCat }
                    }
                    when (sortBy) {
                        "Popularity" -> list = list.sortedByDescending { it.popularity }
                        "New Arrivals" -> list = list.sortedBy { !it.isNewArrival }
                        "Price Asc" -> list = list.sortedBy { it.price }
                        "Price Desc" -> list = list.sortedByDescending { it.price }
                    }
                    list
                }

                // Show Grid list of products
                if (filteredProductsList.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Filled.SearchOff,
                        title = "No Apps Found",
                        subtitle = "We couldn't find any products matching your active filters. Try adjusting your search query."
                    )
                } else {
                    Box(modifier = Modifier.weight(1f)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredProductsList) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { onOpenDetails(product) },
                                    onAddToCart = { viewModel.addToCart(product) },
                                    isInCart = cartList.any { it.id == product.id }
                                )
                            }
                        }

                        // Floating Basket Cart Button if cart is not empty
                        if (cartList.isNotEmpty()) {
                            ExtendedFloatingActionButton(
                                onClick = onOpenCheckout,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .testTag("checkout_fab"),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "View Basket")
                                Spacer(Modifier.width(8.dp))
                                Text("Checkout (${cartList.size})")
                            }
                        }
                    }
                }
            }
        }
        "Notifications" -> {
            // General notification list UI
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(
                    text = "System Alerts & Offers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (notificationsAll.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Filled.NotificationsNone,
                        title = "All Caught Up!",
                        subtitle = "Currently there are no special alerts, discounts, or addition events configured."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notificationsAll) { alert ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (alert.type) {
                                        "discount" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                        "new_app" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    val icon = when (alert.type) {
                                        "discount" -> Icons.Filled.LocalOffer
                                        "new_app" -> Icons.Filled.NewReleases
                                        else -> Icons.Filled.Campaign
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "Notification type Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = alert.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = alert.message,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        "Orders" -> {
            // User order states view
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(
                    text = "Your Submitted Claims",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "All submitted orders undergo administrator payment verification.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (currentUser == null) {
                    EmptyStateWithButton(
                        icon = Icons.Filled.Lock,
                        title = "Sign In Required",
                        subtitle = "Please sign in or register to view your custom app purchases, subscriptions, and transaction statuses.",
                        buttonText = "Sign In Now",
                        onButtonClick = onOpenCheckout // This opens sign-in workflow elegantly
                    )
                } else {
                    val userOrders = ordersAll.filter { it.userEmail == currentUser.email }
                    if (userOrders.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Filled.ReceiptLong,
                            title = "No Purchases Yet",
                            subtitle = "Add items from the Store section and process payments to claim regular, mod, or subscription apps!"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(userOrders) { order ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = order.productName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            // Order verification status badge
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when (order.status) {
                                                    "Verified" -> Color(0xFF2E7D32)
                                                    "Rejected" -> MaterialTheme.colorScheme.error
                                                    else -> Color(0xFFE65100)
                                                },
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = order.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Divider(Modifier.padding(vertical = 8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Method: ${order.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("TxID: ${order.transactionId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Price: \$${order.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (order.status == "Verified") "📥 Ready to Download" else "Verification pending",
                                                    fontSize = 10.sp,
                                                    color = if (order.status == "Verified") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (order.status == "Verified") {
                                            Spacer(Modifier.height(8.dp))
                                            Button(
                                                onClick = { /* Simulated dynamic download trigger */ },
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                            ) {
                                                Icon(Icons.Filled.Download, contentDescription = "Download")
                                                Spacer(Modifier.width(6.dp))
                                                Text("Download Package File [APK]", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper Cards (Geometric Balance Style)
@Composable
fun ProductCard(
    product: com.example.data.Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    isInCart: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("app_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F0F4)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2EC))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Icon box and category badge
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE1E2EC), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val emoji = when (product.visualType) {
                        1 -> "🛡️" // VPN
                        2 -> "🏃" // Fitness / Running
                        3 -> "🌧️" // Sounds / Ambience
                        4 -> "✍️" // Grammar / Productivity
                        5 -> "💬" // Chat / Social
                        else -> "🎨" // Photo / Design
                    }
                    Text(
                        text = emoji,
                        fontSize = 22.sp
                    )
                }

                // App Category Badge
                val badgeBgColor = when (product.category) {
                    "Mod" -> Color(0xFFE8F5E9)
                    "Subscription" -> Color(0xFFE3F2FD)
                    else -> Color(0xFFF3E5F5)
                }
                val badgeTextColor = when (product.category) {
                    "Mod" -> Color(0xFF2E7D32)
                    "Subscription" -> Color(0xFF1565C0)
                    else -> Color(0xFF7B1FA2)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBgColor,
                    contentColor = badgeTextColor
                ) {
                    Text(
                        text = if (product.category == "Mod") "Mod App" else if (product.category == "Subscription") "Sub App" else "Regular",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1B1B1F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = product.description,
                fontSize = 11.sp,
                color = Color(0xFF44474E),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                modifier = Modifier.heightIn(min = 28.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "\$${product.price}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color(0xFF005AC1)
                )

                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isInCart) Color(0xFF005AC1) else Color(0xFFD8E2FF),
                            shape = RoundedCornerShape(10.dp)
                        ).testTag("add_to_cart_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isInCart) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = "Add Product to Basket",
                        tint = if (isInCart) Color.White else Color(0xFF001A41),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Reviews",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = String.format("%.1f", product.ratingAvg),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${product.popularity}+ users",
                    fontSize = 10.sp,
                    color = Color(0xFF44474E)
                )
            }
        }
    }
}

// Placeholder Views
@Composable
fun EmptyStateView(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Empty visual representation",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}

@Composable
fun EmptyStateWithButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Active lock representation",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onButtonClick) {
            Text(buttonText)
        }
    }
}

// ==========================================
// ADMIN DASHBOARD VIEW
// ==========================================
@Composable
fun AdminPanelView(
    selectedTab: String,
    viewModel: StoreViewModel,
    productsAll: List<com.example.data.Product>,
    ordersAll: List<com.example.data.PurchaseOrder>,
    notificationsAll: List<com.example.data.NotificationAlert>,
    usersAll: List<com.example.data.UserAccount>,
    onOpenAddProduct: () -> Unit,
    onOpenAddNotification: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f))) {
        // Red Administrative Alert strip
        Surface(
            color = MaterialTheme.colorScheme.error,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Security, contentDescription = "Security Alert", modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Administrator Control panel view active", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        when (selectedTab) {
            "Products" -> {
                // Product Management Dashboard
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active App Inventory (${productsAll.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Button(
                            onClick = onOpenAddProduct,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("admin_add_app_btn")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add New Release")
                            Spacer(Modifier.width(4.dp))
                            Text("Add App", fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(productsAll) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Visual color marker
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE57373)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(product.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Category: ${product.category} • \$${product.price}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    // Actions - Delete app directly from system DB
                                    IconButton(
                                        onClick = { viewModel.deleteProduct(product) },
                                        modifier = Modifier.testTag("delete_app_btn_${product.id}")
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete App", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Orders" -> {
                // Verify pending user orders / cash / trx tokens
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Text("Payment Verification claims", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Verify Transaction IDs submitted by users across payment gateways.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(10.dp))

                    if (ordersAll.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Filled.Wallet,
                            title = "No Incoming Transactions",
                            subtitle = "Currently there are no dynamic transaction verification claims in the database queue."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(ordersAll) { order ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(order.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Buyer: ${order.userEmail}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = when (order.status) {
                                                    "Verified" -> Color(0xFF2E7D32)
                                                    "Rejected" -> MaterialTheme.colorScheme.error
                                                    else -> Color(0xFFE65100)
                                                },
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = order.status,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Divider(Modifier.padding(vertical = 8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Receipt Price: \$${order.price}", fontSize = 11.sp)
                                                Text("Wallet: ${order.paymentMethod}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("Transaction ID: ${order.transactionId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                            }

                                            // Verification confirmation actions
                                            if (order.status == "Pending") {
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Button(
                                                        onClick = { viewModel.updateOrderStatus(order.id, "Verified") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                                        modifier = Modifier.height(32.dp).testTag("verify_order_btn_${order.id}")
                                                    ) {
                                                        Icon(Icons.Filled.Done, contentDescription = "Approve", modifier = Modifier.size(12.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Verify", fontSize = 10.sp)
                                                    }
                                                    Button(
                                                        onClick = { viewModel.updateOrderStatus(order.id, "Rejected") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                                        modifier = Modifier.height(32.dp).testTag("reject_order_btn_${order.id}")
                                                    ) {
                                                        Icon(Icons.Filled.Close, contentDescription = "Reject", modifier = Modifier.size(12.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Reject", fontSize = 10.sp)
                                                    }
                                                }
                                            } else {
                                                ExtendedFloatingActionButton(
                                                    onClick = { viewModel.updateOrderStatus(order.id, "Pending") },
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Reset to Pending", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Notifications" -> {
                // Post announcements and specific details
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Alert Bulletins", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Button(
                            onClick = onOpenAddNotification,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("admin_post_notification_btn")
                        ) {
                            Icon(Icons.Filled.Campaign, contentDescription = "Post Notification")
                            Spacer(Modifier.width(4.dp))
                            Text("Post Broadcast", fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notificationsAll) { alert ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Campaign, contentDescription = "Broadcasting", tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(alert.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteNotification(alert.id) },
                                        modifier = Modifier.testTag("delete_notif_btn_${alert.id}")
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete alert", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Users" -> {
                // User Account blocking system
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Text("User Access Control", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Block or activate buyers within the local App Market store systems.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(usersAll) { person ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (person.isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                                            contentDescription = "User Identity Icon",
                                            tint = if (person.status == "blocked") Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = person.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (person.status == "blocked") Color.Gray else Color.Unspecified
                                            )
                                            Text("Email: ${person.email} • Ph: ${person.phone}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }

                                    // Switch Blocked/Active block status
                                    Button(
                                        onClick = { viewModel.toggleUserStatus(person.email) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (person.status == "blocked") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(32.dp).testTag("toggle_status_btn_${person.email}")
                                    ) {
                                        Text(
                                            text = if (person.status == "blocked") "Unblock" else "Block",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DETAILED MODAL DIALOGS
// ==========================================
@Composable
fun AuthDialog(
    viewModel: StoreViewModel,
    currentUser: com.example.data.UserAccount?,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentUser == null) "Sign In / Register Account" else "Active Connection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (currentUser != null) {
                    // Profile details logged user
                    Icon(Icons.Filled.AccountCircle, contentDescription = "User", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(currentUser.email, fontSize = 12.sp, color = Color.Gray)
                    Text("Role: ${if (currentUser.isAdmin) "Administrator Mode" else "Regular Buyer"}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.logout()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().testTag("logout_action_btn")
                    ) {
                        Text("Sign Out")
                    }
                } else {
                    // Sign-in form simulating multi auth
                    Text(
                        text = "Enter any email to create or resume an account. Select administrator status to access the Verification Claims panel.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Simulated admin role checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAdmin,
                            onCheckedChange = { isAdmin = it },
                            modifier = Modifier.testTag("auth_admin_checkbox")
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Sign in as Administrator Role", fontSize = 12.sp)
                    }

                    loginError?.let { err ->
                        Spacer(Modifier.height(8.dp))
                        Text(err, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.authenticate(email, name, phone, isAdmin)
                                if (loginError == null && email.isNotBlank()) {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("auth_submit_btn")
                        ) {
                            Text("Unlock")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    viewModel: StoreViewModel,
    cartList: List<com.example.data.Product>,
    onDismiss: () -> Unit
) {
    val totalCost = cartList.sumOf { it.price }
    var selectedGateway by remember { mutableStateOf("bKash") }
    var transactionId by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val paymentGateways = listOf("bKash", "Nagad", "Rocket", "PayPal", "Stripe", "Google Pay")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Checkout Gateway Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (!submitted) {
                    Text(
                        text = "Items in Checkout:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))

                    // Simple layout items in checkout
                    Box(modifier = Modifier.heightIn(max = 120.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(cartList) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.name, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("\$${item.price}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Divider(Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = String.format("\$%.2f", totalCost),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Select Payment Gateway Channel:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    // Gateways selector list
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentGateways.forEach { gateway ->
                            val active = selectedGateway == gateway
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedGateway = gateway }
                                    .testTag("gateway_pills_$gateway"),
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(
                                    text = gateway,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Submit Cash Transfer Receipt ID:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Send \$${String.format("%.2f", totalCost)} using your mobile wallet/card then enter the generated TxID below:",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        placeholder = { Text("Example: TRK209192839", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("checkout_txid_input"),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                if (transactionId.isNotBlank()) {
                                    viewModel.submitCheckout(selectedGateway, transactionId) {
                                        submitted = true
                                    }
                                }
                            },
                            enabled = transactionId.isNotBlank(),
                            modifier = Modifier.weight(1f).testTag("checkout_submit_btn")
                        ) {
                            Text("Submit TxID ($selectedGateway)")
                        }
                    }
                } else {
                    // Success validation feedback
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Icon(Icons.Filled.Verified, contentDescription = "Order Claimed", tint = Color(0xFF2E7D32), modifier = Modifier.size(54.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Claim Received!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Your purchase is submitted for verification registry. Once our system admin approves the Transaction ID ($transactionId), download packages will deploy in your Orders panel.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().testTag("checkout_ok_btn")
                        ) {
                            Text("Got It")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailsDialog(
    product: com.example.data.Product,
    viewModel: StoreViewModel,
    currentUser: com.example.data.UserAccount?,
    onAddToCart: () -> Unit,
    isInCart: Boolean,
    onDismiss: () -> Unit
) {
    val reviews by viewModel.getProductReviews(product.id).collectAsStateWithLifecycle(initialValue = emptyList())

    // New review input parameters
    var userRating by remember { mutableFloatStateOf(5.0f) }
    var userComment by remember { mutableStateOf("") }
    var reviewsCountLimit by remember { mutableStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(0xFF1E88E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
                    Text(
                        text = product.category,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).testTag("dismiss_details_btn")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Scroll contents
                LazyColumn(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                    item {
                        Text(
                            text = "Product Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = product.description,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "App Price: \$${product.price}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = {
                                    onAddToCart()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInCart) Color.Gray else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("details_cart_btn")
                            ) {
                                Text(if (isInCart) "In Basket" else "Add to Basket")
                            }
                        }

                        Divider(Modifier.padding(vertical = 8.dp))

                        // Ratings & Reviews section
                        Text(
                            text = "Customer Feedback & Reviews",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = "Feedback Star", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", product.ratingAvg)} Average rating out of 5.0",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                    }

                    // User Feedback comment entry form (Only if user signed in)
                    if (currentUser != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Post rating comment:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(Modifier.height(4.dp))

                                    // Interactive Star count slider / buttons
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf(1f, 2f, 3f, 4f, 5f).forEach { star ->
                                            Icon(
                                                imageVector = if (userRating >= star) Icons.Filled.Star else Icons.Filled.StarOutline,
                                                contentDescription = "Star Selector",
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clickable { userRating = star }
                                                    .testTag("star_rating_$star")
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text("${userRating.toInt()} Stars", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = userComment,
                                        onValueChange = { userComment = it },
                                        placeholder = { Text("What did you like or dislike about this app package?", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("review_comment_input"),
                                        maxLines = 2,
                                        colors = TextFieldDefaults.colors()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            if (userComment.isNotBlank()) {
                                                viewModel.submitReview(product.id, userRating, userComment) {
                                                    userComment = ""
                                                    userRating = 5.0f
                                                }
                                            }
                                        },
                                        enabled = userComment.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("review_submit_btn"),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Submit Review", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Iteration of review rows
                    if (reviews.isEmpty()) {
                        item {
                            Text(
                                "No rating comments posted yet. Be the first to add your perspective!",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            )
                        }
                    } else {
                        items(reviews.take(reviewsCountLimit)) { review ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(review.reviewerName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Row {
                                            repeat(review.rating.toInt()) {
                                                Icon(Icons.Filled.Star, contentDescription = "Stars Display", tint = Color(0xFFFFB300), modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(review.comment, fontSize = 11.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewProductDialog(
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Regular") }
    var visualType by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Release New App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Application Name", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("add_product_name_input"),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description Outline", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("add_product_desc_input"),
                    maxLines = 3
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("App Pricing (\$)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("add_product_price_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(Modifier.height(10.dp))

                Text("App Category Choice:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Regular", "Subscription", "Mod").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.testTag("add_product_cat_pills_$cat")
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val priceValue = priceStr.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                viewModel.addNewProduct(name, desc, priceValue, category, visualType)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank() && priceStr.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).testTag("add_product_submit_btn")
                    ) {
                        Text("Publish Release")
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewNotificationDialog(
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("discount") } // "discount", "new_app", "general"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Post Broadcast Alert",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("add_notif_title_input"),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = msg,
                    onValueChange = { msg = it },
                    label = { Text("Broadcast Body Message", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("add_notif_body_input"),
                    maxLines = 3
                )

                Spacer(Modifier.height(8.dp))

                Text("Alert Context Theme:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("discount", "new_app", "general").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.replace("_", " ").uppercase(), fontSize = 10.sp) },
                            modifier = Modifier.testTag("add_notif_type_$t")
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.publishNotification(title, msg, type)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).testTag("add_notif_submit_btn")
                    ) {
                        Text("Send Broadcast")
                    }
                }
            }
        }
    }
}
