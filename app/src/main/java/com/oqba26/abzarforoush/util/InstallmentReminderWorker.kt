package com.oqba26.abzarforoush.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqba26.abzarforoush.data.AppDatabase
import java.util.Calendar

class InstallmentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val transactions = database.debtTransactionDao().getAllTransactionsList()
        val customers = database.customerDao().getAllCustomersList()

        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val dayAfterTomorrow = tomorrow + (24 * 60 * 60 * 1000)

        val upcomingInstallments = transactions.filter {
            !it.isPaid && it.dueDate != null && it.dueDate in tomorrow until dayAfterTomorrow
        }

        upcomingInstallments.forEach { installment ->
            val customerName = customers.find { it.id == installment.customerId }?.name ?: "مشتری نامشخص"
            NotificationHelper.showNotification(
                applicationContext,
                "یادآوری قسط فردا",
                "قسط $customerName به مبلغ ${installment.amount.toPersianPrice()} فردا سررسید می‌شود.",
                installment.id
            )
        }

        return Result.success()
    }
}
