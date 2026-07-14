package com.oqba26.abzarforoush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.util.toPersianDigits
import saman.zamani.persiandate.PersianDate

@Composable
fun ShamsiDatePicker(
    initialTimestamp: Long? = null,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val pDate = remember { PersianDate(initialTimestamp ?: System.currentTimeMillis()) }
    var displayedYear by remember { mutableIntStateOf(pDate.shYear) }
    var displayedMonth by remember { mutableIntStateOf(pDate.shMonth) }
    var selectedDay by remember { mutableIntStateOf(pDate.shDay) }

    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    if (showYearPicker) {
        YearPickerDialog(
            initialYear = displayedYear,
            onDismissRequest = { showYearPicker = false }
        ) { year ->
            displayedYear = year
            showYearPicker = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header - Exact style from the photo but with App Theme
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "انتخاب تاریخ شمسی",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        // Month & Year Selector - Centered style from photo
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (displayedMonth == 12) {
                                    displayedMonth = 1
                                    displayedYear++
                                } else {
                                    displayedMonth++
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه بعد")
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Month Picker
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showMonthPicker = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = PersianDate().apply { shMonth = displayedMonth }.monthName(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp))
                                    
                                    DropdownMenu(
                                        expanded = showMonthPicker,
                                        onDismissRequest = { showMonthPicker = false }
                                    ) {
                                        (1..12).forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text(PersianDate().apply { shMonth = m }.monthName()) },
                                                onClick = {
                                                    displayedMonth = m
                                                    showMonthPicker = false
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))

                                // Year Picker
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showYearPicker = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = displayedYear.toString().toPersianDigits(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp))
                                }
                            }

                            IconButton(onClick = {
                                if (displayedMonth == 1) {
                                    displayedMonth = 12
                                    displayedYear--
                                } else {
                                    displayedMonth--
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه قبل")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Days of Week Header - Same as photo
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val days = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                            days.forEach { d ->
                                Text(
                                    text = d,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calendar Grid
                        val monthData = remember(displayedYear, displayedMonth) {
                            getCalendarData(displayedYear, displayedMonth)
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(260.dp)
                        ) {
                            items(monthData.size) { index ->
                                val day = monthData[index]
                                if (day == 0) {
                                    Spacer(modifier = Modifier.aspectRatio(1f))
                                } else {
                                    val isSelected = day == selectedDay
                                    val isToday = isToday(displayedYear, displayedMonth, day)
                                    val isFriday = (index % 7) == 6

                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else Color.Transparent
                                            )
                                            .clickable { selectedDay = day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString().toPersianDigits(),
                                            color = if (isSelected) Color.White
                                            else if (isFriday) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Actions - Two equal buttons as requested, matching App Style
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val result = PersianDate().apply {
                                        shYear = displayedYear
                                        shMonth = displayedMonth
                                        shDay = selectedDay
                                        hour = 12
                                    }
                                    onDateSelected(result.time)
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("تایید", style = MaterialTheme.typography.labelLarge)
                            }
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("انصراف", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YearPickerDialog(
    initialYear: Int,
    onDismissRequest: () -> Unit,
    onYearSelected: (Int) -> Unit,
) {
    val years = remember { (1380..1420).toList() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(Unit) {
        val index = years.indexOf(initialYear)
        if (index != -1) listState.scrollToItem(index)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.8f).height(400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "انتخاب سال",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                androidx.compose.foundation.lazy.LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(years.size) { index ->
                        val year = years[index]
                        Text(
                            text = year.toString().toPersianDigits(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onYearSelected(year) }
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            style = if (year == initialYear) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                            color = if (year == initialYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun getCalendarData(year: Int, month: Int): List<Int> {
    val pDate = PersianDate().apply {
        shYear = year
        shMonth = month
        shDay = 1
    }
    
    // In PersianDate, 0 is Saturday, 1 is Sunday, ..., 6 is Friday
    val dayOfWeek = pDate.dayOfWeek() 
    val daysInMonth = pDate.monthDays
    
    val list = mutableListOf<Int>()
    repeat(dayOfWeek) {
        list.add(0)
    }
    for (i in 1..daysInMonth) {
        list.add(i)
    }
    return list
}

private fun isToday(year: Int, month: Int, day: Int): Boolean {
    val today = PersianDate()
    return today.shYear == year && today.shMonth == month && today.shDay == day
}
