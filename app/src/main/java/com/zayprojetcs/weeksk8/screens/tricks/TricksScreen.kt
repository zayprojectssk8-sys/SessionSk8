package com.zayprojetcs.weeksk8.screens.tricks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zayprojetcs.weeksk8.core.model.TFMTrick
import com.zayprojetcs.weeksk8.core.model.TypeTrick
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom

@Composable
fun TricksScreen() {
    ScaffoldCustom(
        content = {
            TricksContent()
        }
    )
}

@Composable
fun TricksContent() {
    var selectedType by remember { mutableStateOf(TypeTrick.FLOOR) }

    val filteredTricks = remember(selectedType) {
        selectedType.trickList
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Biblioteca de Trucos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        TrickFilterChips(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredTricks) { trick ->
                TrickItem(trick)
            }
        }
    }
}

@Composable
fun TrickItem(trick: TFMTrick) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(8f)
                    .padding(5.dp)
            ) {
                Text(
                    text = trick.realName.ifEmpty { "N/A" },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = trick.akaName.ifEmpty { "N/A" },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "PTS", style = MaterialTheme.typography.titleMedium)
                Text(text = trick.difficulty.toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickFilterChips(
    selectedType: TypeTrick,
    onTypeSelected: (TypeTrick) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(TypeTrick.entries.toTypedArray()) { type ->
            val isSelected = type == selectedType

            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        type.typeTrickName.plus(" (").plus(type.trickList.size.toString()).plus(")")
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.background, // Tu Verde Jade
                    selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                    labelColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}