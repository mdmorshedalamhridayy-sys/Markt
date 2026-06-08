package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val email: String,
    val name: String,
    val phone: String,
    val isAdmin: Boolean = false,
    val status: String = "active" // "active" or "blocked"
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // "Regular", "Subscription", "Mod"
    val visualType: Int = 0, // Code to select beautiful dynamic gradient representation
    val popularity: Int = 50,
    val isNewArrival: Boolean = true,
    val downloadCount: Int = 120,
    val ratingAvg: Float = 4.5f
)

@Entity(tableName = "orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val productCategory: String,
    val price: Double,
    val userEmail: String,
    val paymentMethod: String, // "bKash", "Nagad", "Rocket", "PayPal", "Stripe", "Google Pay"
    val transactionId: String,
    val status: String = "Pending", // "Pending", "Verified", "Rejected"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // "discount", "new_app", "announcement"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ProductReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val reviewerEmail: String,
    val reviewerName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
