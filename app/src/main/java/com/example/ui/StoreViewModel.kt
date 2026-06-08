package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao())

    // UI Configuration / Search Parameters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All") // "All", "Regular", "Subscription", "Mod"
    val sortBy = MutableStateFlow("Popularity") // "Popularity", "New Arrivals", "Price Asc", "Price Desc"

    // Authentication State
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Shopping Cart State
    private val _cart = MutableStateFlow<List<Product>>(emptyList())
    val cart: StateFlow<List<Product>> = _cart.asStateFlow()

    // Database Observables
    val products = repository.allProducts
    val allUsersList = repository.allUsers
    val allNotifications = repository.allNotifications
    val allOrdersList = repository.allOrders

    init {
        seedInitialDatabase()
    }

    private fun seedInitialDatabase() {
        viewModelScope.launch {
            // Check if users empty, seed admin and user
            val firstUser = db.appDao().getUserByEmail("admin@appmarket.com")
            if (firstUser == null) {
                // Seed Admin
                repository.insertUser(
                    UserAccount(
                        email = "admin@appmarket.com",
                        name = "Admin Master",
                        phone = "+8801711223344",
                        isAdmin = true,
                        status = "active"
                    )
                )
                // Seed Normal Customer
                repository.insertUser(
                    UserAccount(
                        email = "user@appmarket.com",
                        name = "Morshed Alam",
                        phone = "+8801999888777",
                        isAdmin = false,
                        status = "active"
                    )
                )
            }

            // Seed initial apps/products if empty
            products.first().let { currentList ->
                if (currentList.isEmpty()) {
                    val initialProducts = listOf(
                        Product(
                            name = "Photo Editor Elite Pro",
                            description = "Full featured creative suite with smart presets, layers, and visual adjustments.",
                            price = 4.99,
                            category = "Regular",
                            visualType = 0,
                            popularity = 95,
                            isNewArrival = false,
                            downloadCount = 12500,
                            ratingAvg = 4.8f
                        ),
                        Product(
                            name = "Premium VPN Ultra Secure",
                            description = "Unlimited bandwidth, servers in 100+ countries, fully encrypted tunneling.",
                            price = 19.99,
                            category = "Subscription",
                            visualType = 1,
                            popularity = 90,
                            isNewArrival = true,
                            downloadCount = 4200,
                            ratingAvg = 4.6f
                        ),
                        Product(
                            name = "Infinite Runner [Speed Mod]",
                            description = "Custom modified version! Built-in trainer menu, customizable speeds, and coins unlocked.",
                            price = 2.49,
                            category = "Mod",
                            visualType = 2,
                            popularity = 88,
                            isNewArrival = false,
                            downloadCount = 8500,
                            ratingAvg = 4.3f
                        ),
                        Product(
                            name = "Calm Rain Sound Generator",
                            description = "Continuous background noise synthesizer for sound sleep, deep focus, and yoga.",
                            price = 0.99,
                            category = "Regular",
                            visualType = 3,
                            popularity = 75,
                            isNewArrival = true,
                            downloadCount = 800,
                            ratingAvg = 4.7f
                        ),
                        Product(
                            name = "AI Grammar Assistant Monthly",
                            description = "Real-time AI style and grammar suggestions directly inside your keyboard.",
                            price = 9.99,
                            category = "Subscription",
                            visualType = 4,
                            popularity = 92,
                            isNewArrival = true,
                            downloadCount = 3500,
                            ratingAvg = 4.9f
                        ),
                        Product(
                            name = "Aero Chat Unlimited [Mod Unlocked]",
                            description = "Upgraded community client with customizable dynamic interface themes and message logging.",
                            price = 3.99,
                            category = "Mod",
                            visualType = 5,
                            popularity = 84,
                            isNewArrival = false,
                            downloadCount = 6700,
                            ratingAvg = 4.4f
                        )
                    )
                    initialProducts.forEach { repository.insertProduct(it) }

                    // Seed starting notifications
                    repository.insertNotification(
                        NotificationAlert(
                            title = "Mega Eid Discount!",
                            message = "Get 50% flat off on all premium subscription packages using promo ID Eid50.",
                            type = "discount"
                        )
                    )
                    repository.insertNotification(
                        NotificationAlert(
                            title = "New Arrival alert!",
                            message = "Calm Rain Sound Generator is now live on the store. Experience nature loops.",
                            type = "new_app"
                        )
                    )

                    // Seed starting product reviews
                    val seededPid = 1
                    repository.insertReview(
                        ProductReview(
                            productId = seededPid,
                            reviewerEmail = "user@appmarket.com",
                            reviewerName = "Morshed Alam",
                            rating = 5.0f,
                            comment = "Absolutely gorgeous layout and easy editing brushes. Worth every penny!"
                        )
                    )
                    repository.insertReview(
                        ProductReview(
                            productId = seededPid,
                            reviewerEmail = "guest@appmarket.com",
                            reviewerName = "Guest Reviewer",
                            rating = 4.5f,
                            comment = "Great presets! A bit heavy on low memory devices, but works perfectly fine."
                        )
                    )
                }
            }

            // Always login default user session so review environment is not abstract
            authenticate("user@appmarket.com", "Morshed Alam", "+8801999888777", false)
        }
    }

    // AUTHENTICATION LOGIC
    fun authenticate(email: String, name: String, phone: String, isAdmin: Boolean) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _loginError.emit("Email cannot be empty")
                return@launch
            }
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                if (existing.status == "blocked") {
                    _loginError.emit("This account is currently blocked by administrator.")
                    return@launch
                }
                _currentUser.value = existing
                _loginError.value = null
            } else {
                val newUser = UserAccount(email, name, phone, isAdmin, "active")
                repository.insertUser(newUser)
                _currentUser.value = newUser
                _loginError.value = null
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _cart.value = emptyList()
    }

    // SHOPPING CART LOGIC
    fun addToCart(product: Product) {
        val current = _cart.value.toMutableList()
        if (!current.any { it.id == product.id }) {
            current.add(product)
            _cart.value = current
        }
    }

    fun removeFromCart(product: Product) {
        val current = _cart.value.toMutableList()
        current.removeAll { it.id == product.id }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // CHECKOUT / ORDER SUBMISSION
    fun submitCheckout(paymentMethod: String, transactionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val cartList = _cart.value
            if (cartList.isEmpty() || transactionId.isBlank()) return@launch

            // Submit each product in cart as a separate pending purchase order
            cartList.forEach { product ->
                val order = PurchaseOrder(
                    productId = product.id,
                    productName = product.name,
                    productCategory = product.category,
                    price = product.price,
                    userEmail = user.email,
                    paymentMethod = paymentMethod,
                    transactionId = transactionId,
                    status = "Pending"
                )
                repository.insertOrder(order)
            }
            clearCart()
            onComplete()
        }
    }

    // REVIEWS MANAGEMENT
    fun getProductReviews(productId: Int): Flow<List<ProductReview>> {
        return repository.getReviewsForProduct(productId)
    }

    fun submitReview(productId: Int, rating: Float, comment: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val review = ProductReview(
                productId = productId,
                reviewerEmail = user.email,
                reviewerName = user.name,
                rating = rating,
                comment = comment
            )
            repository.insertReview(review)
            
            // Recalculate average score for product
            val reviewsList = repository.getReviewsForProduct(productId).first()
            val totalRating = reviewsList.map { it.rating }.sum() + rating
            val totalCount = reviewsList.size + 1
            val newAvg = (totalRating / totalCount)
            
            val product = repository.getProductById(productId)
            if (product != null) {
                repository.updateProduct(
                    product.copy(ratingAvg = newAvg)
                )
            }
            onComplete()
        }
    }

    // ADMIN ONLY - PRODUCT ACTIONS
    fun addNewProduct(name: String, desc: String, price: Double, category: String, visualType: Int) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                description = desc,
                price = price,
                category = category,
                visualType = visualType,
                popularity = 50,
                isNewArrival = true,
                ratingAvg = 5.0f
            )
            repository.insertProduct(product)
            
            // Auto generate notification for new app alert
            repository.insertNotification(
                NotificationAlert(
                    title = "New App Added!",
                    message = "Awesome app '$name' is now live in '$category' section. Download now!",
                    type = "new_app"
                )
            )
        }
    }

    fun editProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // ADMIN ONLY - NOTIFICATION ACTIONS
    fun publishNotification(title: String, message: String, type: String) {
        viewModelScope.launch {
            repository.insertNotification(
                NotificationAlert(
                    title = title,
                    message = message,
                    type = type
                )
            )
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // ADMIN ONLY - ORDER VERIFICATION
    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            allOrdersList.first().find { it.id == orderId }?.let { order ->
                repository.updateOrder(order.copy(status = newStatus))
            }
        }
    }

    // ADMIN ONLY - USER CONTROLS
    fun toggleUserStatus(userEmail: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(userEmail) ?: return@launch
            val nextStatus = if (user.status == "blocked") "active" else "blocked"
            repository.updateUser(user.copy(status = nextStatus))
            
            // If currently logged user email matches, sign out.
            if (_currentUser.value?.email == userEmail && nextStatus == "blocked") {
                _currentUser.value = null
            }
        }
    }
}
