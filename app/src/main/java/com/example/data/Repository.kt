package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // User Operations
    val allUsers: Flow<List<UserAccount>> = appDao.getAllUsers()

    suspend fun getUserByEmail(email: String): UserAccount? {
        return appDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: UserAccount) {
        appDao.insertUser(user)
    }

    suspend fun updateUser(user: UserAccount) {
        appDao.updateUser(user)
    }

    // Product Operations
    val allProducts: Flow<List<Product>> = appDao.getAllProducts()

    suspend fun getProductById(id: Int): Product? {
        return appDao.getProductById(id)
    }

    suspend fun insertProduct(product: Product) {
        appDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        appDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        appDao.deleteProduct(product)
    }

    // Purchase Order Operations
    val allOrders: Flow<List<PurchaseOrder>> = appDao.getAllOrders()

    fun getOrdersForUser(email: String): Flow<List<PurchaseOrder>> {
        return appDao.getOrdersForUser(email)
    }

    suspend fun insertOrder(order: PurchaseOrder) {
        appDao.insertOrder(order)
    }

    suspend fun updateOrder(order: PurchaseOrder) {
        appDao.updateOrder(order)
    }

    // Notification Operations
    val allNotifications: Flow<List<NotificationAlert>> = appDao.getAllNotifications()

    suspend fun insertNotification(notification: NotificationAlert) {
        appDao.insertNotification(notification)
    }

    suspend fun deleteNotification(id: Int) {
        appDao.deleteNotificationById(id)
    }

    // Review Operations
    fun getReviewsForProduct(productId: Int): Flow<List<ProductReview>> {
        return appDao.getReviewsForProduct(productId)
    }

    suspend fun insertReview(review: ProductReview) {
        appDao.insertReview(review)
    }
}
