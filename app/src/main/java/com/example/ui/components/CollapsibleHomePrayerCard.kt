package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import com.example.data.model.CityLocation
import com.example.data.model.AppLanguage
import com.example.utils.PrayerCalculator
import com.example.utils.AppStrings

@Composable
fun CollapsibleHomePrayerCard(
    prayerData: PrayerTimesData?,
    nextPrayer: NextPrayerInfo?,
    liveTime: String,
    selectedCity: CityLocation,
    currentLanguage: AppLanguage,
    onNavigateToPrayerTimes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isAr = currentLanguage == AppLanguage.ARABIC
    val cityName = if (isAr) selectedCity.nameAr else selectedCity.nameEn
    val isDarkTheme = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBackground = MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkTheme) 0.75f else 0.9f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, primaryAccent.copy(alpha = if (isDarkTheme) 0.45f else 0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            // Top Row: "مواقيت الصلاة" Navigation Button & City Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "مواقيت الصلاة" Navigation button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { onNavigateToPrayerTimes() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "مواقيت الصلاة",
                            style = MaterialTheme.typography.labelLarge,
                            color = primaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, // Left arrow since it's RTL (goes to the screen)
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // City Location Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cityName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Middle Row: Next Prayer Name & Next Prayer Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name & Dot
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(primaryAccent)
                        )
                        Text(
                            text = AppStrings.nextPrayerBadge(currentLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val nextTypeName = nextPrayer?.prayerType?.let { AppStrings.prayerName(it, currentLanguage) } ?: (if (isAr) "صلاة الفجر" else "Fajr")
                    Text(
                        text = "صلاة $nextTypeName".replace("صلاة صلاة", "صلاة"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                }

                // Time
                val nextPrayerTimeStr = nextPrayer?.let { PrayerCalculator.formatTo12h(it.targetTimeStr, currentLanguage) } ?: (if (isAr) "04:35 ص" else "04:35 AM")
                Text(
                    text = nextPrayerTimeStr,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row of Header: "الصلوات الخمس" / "طي الصلوات" & Remaining Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isExpanded) "طي الصلوات" else "الصلوات الخمس",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val remainingText = nextPrayer?.remainingFormatted ?: "00:00"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryAccent.copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, primaryAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = AppStrings.countdownRemaining(currentLanguage, remainingText),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Divider progress bar
            Spacer(modifier = Modifier.height(8.dp))
            val progress = nextPrayer?.progress ?: 0.5f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.02f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(primaryAccent)
                )
            }

            // Expanded Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    val hijriDisplay = PrayerCalculator.formatFullHijriDate(prayerData?.hijriDate, currentLanguage)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hijriDisplay,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مواقيت اليوم لمدينة $cityName",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.5.sp,
                            color = primaryAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prayers Grid
                    if (prayerData != null) {
                        val allPrayers = listOf(
                            PrayerType.FAJR to prayerData.fajir,
                            PrayerType.SUNRISE to prayerData.sunrise,
                            PrayerType.DHUHR to prayerData.doher,
                            PrayerType.ASR to prayerData.asr,
                            PrayerType.SUNSET to prayerData.sunset,
                            PrayerType.MAGHRIB to prayerData.maghrib,
                            PrayerType.ISHA to prayerData.isha,
                            PrayerType.MIDNIGHT to prayerData.midnight
                        ).filter { it.second.isNotEmpty() }

                        // 2 columns grid
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (i in allPrayers.indices step 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val prayer1 = allPrayers.getOrNull(i)
                                    val prayer2 = allPrayers.getOrNull(i + 1)
                                    if (prayer1 != null) {
                                        PrayerGridItem(
                                            modifier = Modifier.weight(1f),
                                            prayerType = prayer1.first,
                                            timeStr = prayer1.second,
                                            currentLanguage = currentLanguage,
                                            isNext = nextPrayer?.prayerType == prayer1.first
                                        )
                                    }
                                    if (prayer2 != null) {
                                        PrayerGridItem(
                                            modifier = Modifier.weight(1f),
                                            prayerType = prayer2.first,
                                            timeStr = prayer2.second,
                                            currentLanguage = currentLanguage,
                                            isNext = nextPrayer?.prayerType == prayer2.first
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
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

@Composable
fun PrayerGridItem(
    modifier: Modifier = Modifier,
    prayerType: PrayerType,
    timeStr: String,
    currentLanguage: AppLanguage,
    isNext: Boolean
) {
    val isAr = currentLanguage == AppLanguage.ARABIC
    val isDarkTheme = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val bgColor = if (isNext) MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.25f else 0.15f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val textColor = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val formattedTime = PrayerCalculator.formatTo12h(timeStr, currentLanguage)
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = AppStrings.prayerName(prayerType, currentLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                )
                if (isNext) {
                    Box(modifier = Modifier.size(5.dp).clip(androidx.compose.foundation.shape.CircleShape).background(textColor))
                }
            }
        }
    }
}
