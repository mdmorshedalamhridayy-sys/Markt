package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Queries
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    // Product Queries
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // Purchase Order Queries
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM orders WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getOrdersForUser(email: String): Flow<List<PurchaseOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: PurchaseOrder)

    @Update
    suspend fun updateOrder(order: PurchaseOrder)

    // Notification Queries
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationAlert>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: Int): NotificationAlert?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationAlert)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    // Review Queries
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY timestamp DESC")
    fun getReviewsForProduct(productId: Int): Flow<List<ProductReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ProductReview)
}
