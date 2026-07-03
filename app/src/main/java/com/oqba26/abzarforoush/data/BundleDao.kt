package com.oqba26.abzarforoush.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BundleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundle(bundle: Bundle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleItems(items: List<BundleItem>)

    @Transaction
    @Query("SELECT * FROM bundles")
    fun getAllBundles(): Flow<List<BundleWithProducts>>

    @Delete
    suspend fun deleteBundle(bundle: Bundle)
}

data class BundleWithProducts(
    @Embedded val bundle: Bundle,
    @Relation(
        parentColumn = "id",
        entityColumn = "bundleId",
        entity = BundleItem::class
    )
    val bundleItems: List<BundleItem>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BundleItem::class,
            parentColumn = "bundleId",
            entityColumn = "productId"
        )
    )
    val products: List<Product>
)
