package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.model.PredefinedCities
import com.example.ui.theme.AppColors

enum class CityCategory(val titleAr: String, val titleEn: String) {
    ALL("الكل", "All"),
    HOLY_SHRINES("العتبات والمراقد", "Holy Shrines"),
    IRAQ_DISTRICTS("أقضية العراق", "Iraqi Districts"),
    IRAQ_PROVINCES("المحافظات العراقية", "Governorates"),
    WORLD_CITIES("المدن الإسلامية والعالمية", "World Cities")
}

@Composable
fun CitySelectionDialog(
    currentLanguage: AppLanguage,
    selectedCity: CityLocation,
    onCitySelect: (CityLocation) -> Unit,
    onGpsLocate: () -> Unit,
    isGpsLocating: Boolean = false,
    gpsStatusMessage: String? = null,
    onDismiss: () -> Unit
) {
    val isAr = currentLanguage == AppLanguage.ARABIC
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CityCategory.ALL) }

    val allCities = remember { PredefinedCities.list }

    val filteredCities = remember(searchQuery, selectedCategory) {
        allCities.filter { city ->
            val matchesCategory = when (selectedCategory) {
                CityCategory.ALL -> true
                CityCategory.HOLY_SHRINES -> city.isHolyCity
                CityCategory.IRAQ_DISTRICTS -> city.id.startsWith("dist_")
                CityCategory.IRAQ_PROVINCES -> city.countryAr == "العراق" && !city.id.startsWith("dist_") && !city.isHolyCity
                CityCategory.WORLD_CITIES -> city.countryAr != "العراق"
            }

            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    city.nameAr.lowercase().contains(query) ||
                    city.nameEn.lowercase().contains(query) ||
                    city.province.lowercase().contains(query) ||
                    city.countryAr.lowercase().contains(query)

            matchesCategory && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("city_selection_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = AppColors.current.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.5.dp, AppColors.current.tealGlow40)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow20),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AppColors.current.tealAccentLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isAr) "اختيار المدينة والموقع" else "Select City & Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = if (isAr) "بيانات الموقع ومواقيتها محفوظة أوفلاين" else "Offline saved & locked location",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isAr) "إغلاق" else "Close",
                            tint = AppColors.current.textTitle,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS Quick Locate Action Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGpsLocate() }
                        .testTag("dialog_gps_locate_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.current.tealGlow10),
                    border = BorderStroke(1.dp, AppColors.current.tealGlow40)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.current.tealGlow20),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGpsLocating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = AppColors.current.tealAccentLight,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = AppColors.current.tealAccentLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isAr) "تحديد الموقع الحالي تلقائياً عبر GPS" else "Auto-Locate via GPS",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.current.textTitle
                                )
                                Text(
                                    text = if (isGpsLocating) {
                                        if (isAr) "جارِ البحث عن إشارة GPS..." else "Acquiring GPS fix..."
                                    } else {
                                        if (isAr) "تحديد المدينة بناءً على إحداثيات جهازك" else "Detect city by device coordinates"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.current.tealAccentLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                if (gpsStatusMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = gpsStatusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.current.tealAccentLight,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_city_search_input"),
                    placeholder = {
                        Text(
                            text = if (isAr) "ابحث عن قضاء، ناحية، محافظة أو مرقد مقدس..." else "Search city, district, or shrine...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.current.textMuted.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AppColors.current.tealAccentLight,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = AppColors.current.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.current.tealAccentLight,
                        unfocusedBorderColor = AppColors.current.borderSubtle,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = AppColors.current.textTitle,
                        unfocusedTextColor = AppColors.current.textTitle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(CityCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = if (isAr) category.titleAr else category.titleEn,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.current.tealGlow20,
                                selectedLabelColor = AppColors.current.tealAccentLight,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = AppColors.current.textMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AppColors.current.tealAccentLight else AppColors.current.borderSubtle
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cities List
                Box(modifier = Modifier.weight(1f)) {
                    if (filteredCities.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AppColors.current.textMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isAr) "لا توجد نتائج مطابقة لبحثك" else "No matching cities found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.current.textMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(filteredCities, key = { it.id }) { city ->
                                val isSelected = city.id == selectedCity.id ||
                                        (city.nameAr == selectedCity.nameAr && city.province == selectedCity.province)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                             onCitySelect(city)
                                             onDismiss()
                                        }
                                        .testTag("dialog_city_item_${city.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) AppColors.current.tealGlow20 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) AppColors.current.tealAccentLight else AppColors.current.borderSubtle
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (city.isHolyCity) Color(0x33D4AF37) else AppColors.current.tealGlow10
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (city.isHolyCity) Icons.Default.Mosque else Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = if (city.isHolyCity) Color(0xFFF0D28B) else AppColors.current.tealAccentLight,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = if (isAr) city.nameAr else city.nameEn,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                        color = if (isSelected) AppColors.current.tealAccentLight else AppColors.current.textTitle
                                                    )
                                                    if (city.isHolyCity) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0x33D4AF37))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = if (isAr) "عتبة مقدسة" else "Holy",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color(0xFFF0D28B),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = if (city.province.isNotEmpty() && city.province != city.nameAr) {
                                                        "${city.province} • ${city.countryAr} • ${city.formattedCoordinates}"
                                                    } else {
                                                        "${city.countryAr} • ${city.formattedCoordinates}"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = AppColors.current.textMuted.copy(alpha = 0.7f),
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(AppColors.current.tealAccentLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Offline & Permanent Lock Assurance Footer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, AppColors.current.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.current.tealAccentLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAr)
                                "ملاحظة: المدينة المختارة تُحفظ في ملفات النظام وتظل ثابتة نهائياً حتى تغيرها بنفسك، مع دعم العمل أوفلاين 100%."
                            else
                                "Note: Selected city is stored in system files and remains permanently locked until changed manually, with 100% offline support.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = AppColors.current.textMuted,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
