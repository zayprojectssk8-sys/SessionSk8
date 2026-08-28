package com.zayprojetcs.weeksk8.ui.customs.view


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zaysk8.core.model.TFMTrick
import com.zaysk8.core.model.TypeStanceTrick
import com.zayprojetcs.weeksk8.ui.customs.LoadTitleSectionCustom
import com.zaysk8.core.model.TypeTrick

@Composable
fun SelectTrickScreen(
    trickListSelected: List<TFMTrick>,
    availableStances: List<TypeStanceTrick>,
    limitSelected: Int = 0,
    onSendSelection: (List<TFMTrick>) -> Unit
) {
    val selectedTricks = rememberSaveable { mutableStateListOf<TFMTrick>() }

    LaunchedEffect(Unit) {
        selectedTricks.clear()
        selectedTricks.addAll(trickListSelected)
    }
    val listTricksAll =
        TypeTrick.FLOOR.trickList + TypeTrick.GRIND.trickList + TypeTrick.SLIDE.trickList + TypeTrick.GRABS.trickList + TypeTrick.BALANCE.trickList

    var selectedStanceType by remember { mutableStateOf(TypeStanceTrick.NORMAL) }

    val trickList = remember( selectedStanceType) {
        listTricksAll.map { trick ->
            val prefix = when (selectedStanceType) {
                TypeStanceTrick.NOLLIE -> "Nollie "
                TypeStanceTrick.FAKIE -> "Fakie "
                TypeStanceTrick.SWITCH -> "Switch "
                TypeStanceTrick.NORMAL -> ""
            }

            trick.copy(
                realName = "$prefix${trick.realName}",
                akaName = "$prefix${trick.akaName}"
            )
        }
    }


    var disabledSelect by rememberSaveable { mutableStateOf(false) }

    if (limitSelected > 0) {
        disabledSelect = (selectedTricks.size == limitSelected)
    }

    // 1. Estado para el texto que escribe el usuario
    var searchQuery by remember { mutableStateOf("") }

    // 2. Lista filtrada reactiva (se actualiza sola cuando cambia searchQuery o trickList)
    val filteredList = remember(searchQuery, trickList) {
        if (searchQuery.isEmpty()) {
            trickList
        } else {
            trickList.filter { it.realName.contains(searchQuery, ignoreCase = true) }
                .sortedWith(compareByDescending<TFMTrick> {
                    // 1. Prioridad máxima: Coincidencia exacta
                    it.realName.equals(searchQuery, ignoreCase = true)
                }.thenByDescending {
                    // 2. Prioridad media: Empieza con la palabra
                    it.realName.startsWith(searchQuery, ignoreCase = true)
                }.thenBy {
                    // 3. Prioridad baja: Orden alfabético para el resto
                    it.realName.length
                })
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {

        LoadTitleSectionCustom("SELECCIÓN DE TRUCOS")

        Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Solo puedes seleccionar hasta $limitSelected trucos para esta sesión (1 por cada ronda calculada). Si deseas agregar más trucos, aumenta la duración de tu sesión.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar truco (ej: Kickflip)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = MaterialTheme.colorScheme.primary,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.primary,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrickStanceFilterChips(
            availableStances = availableStances,
            selectedType = selectedStanceType,
            onTypeSelected = { selectedStanceType = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { trick ->
                TrickSelectionItem(
                    trick = trick,
                    disabledSelect = false,
                    isSelected = selectedTricks.contains(trick),
                    onCheckedChange = { isChecked ->

                        if (isChecked && !disabledSelect) selectedTricks.add(trick)
                        else selectedTricks.remove(trick)

                    }
                )
            }
        }

        Button(
            onClick = { onSendSelection(selectedTricks.toList()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = selectedTricks.isNotEmpty()
        ) {
            Text("Enviar (${selectedTricks.size}) trucos")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickStanceFilterChips(
    availableStances: List<TypeStanceTrick>,
    selectedType: TypeStanceTrick,
    onTypeSelected: (TypeStanceTrick) -> Unit,
    modifier: Modifier = Modifier
) {


    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(availableStances.toTypedArray()) { type ->
            val isSelected = type == selectedType

            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        type.typeStanceName
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


@Composable
fun TrickSelectionItem(
    trick: TFMTrick,
    isSelected: Boolean,
    disabledSelect: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        border = BorderStroke(
            1.dp, if (isSelected) MaterialTheme.colorScheme.primary
            else Color.Gray
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trick.akaName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = trick.realName,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Checkbox(
                checked = isSelected,
                enabled = !disabledSelect,
                onCheckedChange = { onCheckedChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.background,
                    uncheckedColor = Color.Gray,
                    checkmarkColor = Color.Black
                )
            )
        }
    }
}