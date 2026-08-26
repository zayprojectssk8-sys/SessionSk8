package com.zayprojetcs.weeksk8.screens.progress_user


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.core.room.model.RoomDay
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zayprojetcs.weeksk8.core.room.model.StatusTrick
import com.zayprojetcs.weeksk8.screens.progress_user.ui_state.model.TypeFilterProgressUser
import com.zayprojetcs.weeksk8.ui.customs.FloatingButtonCustom
import com.zayprojetcs.weeksk8.ui.customs.LoadTitleSectionCustom
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom
import com.zayprojetcs.weeksk8.ui.customs.SubFabItem
import com.zayprojetcs.weeksk8.ui.customs.TextBodyLarge
import com.zayprojetcs.weeksk8.ui.customs.view.ItemDayCustom
import com.zayprojetcs.weeksk8.ui.customs.view.ItemTrickCustom

@Composable
fun ProgressUserScreen(
    viewModel: ProgressUserViewModel = viewModel(),
    onCreateDaySkate: () -> Unit
) {

    val daysSkate by viewModel.daysSkate.collectAsStateWithLifecycle()

    val tricksList by viewModel.tricksList.collectAsStateWithLifecycle()

    ScaffoldCustom(
        floatingActionButton = {
            CreateFabGroup(onCreateDaySkate = onCreateDaySkate)
        },
        content = {
            ProgressUserContent(daysSkate = daysSkate, tricksList = tricksList)
        }
    )
}

@Composable
fun ProgressUserContent(
    daysSkate: List<RoomDay>,
    tricksList: List<RoomTrick>
) {
    var selectedStanceType by remember { mutableStateOf(TypeFilterProgressUser.UNLOCK_TRICK) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadTitleSectionCustom("PROGRESO SKATE")

        Spacer(modifier = Modifier.height(10.dp))

        TrickStanceFilterChips(
            daysSkate = daysSkate,
            trickList = tricksList,
            selectedType = selectedStanceType,
            onTypeSelected = { itTypeSelected -> selectedStanceType = itTypeSelected },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            when (selectedStanceType) {
                TypeFilterProgressUser.UNLOCK_TRICK -> ProgressUserContentTricks(tricksList.filter { it.status == StatusTrick.UNLOCK.status })
                TypeFilterProgressUser.TRYING_TRICK -> ProgressUserContentTricks(tricksList.filter { it.status == StatusTrick.TRYING.status })
                TypeFilterProgressUser.SKATE_DAYS -> ProgressUserContentSkateDays(daysSkate)
            }
        }
    }
}

@Composable
fun ProgressUserContentSkateDays(daysSkate: List<RoomDay>) {
    Spacer(modifier = Modifier.height(20.dp))

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(daysSkate) { roomDay ->
            ItemDayCustom(roomDay = roomDay)
        }
    }
}

@Composable
fun ProgressUserContentTricks(trickList: List<RoomTrick>) {
    Spacer(modifier = Modifier.height(20.dp))

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(trickList) { trick ->
            ItemTrickCustom(trick = trick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickStanceFilterChips(
    daysSkate: List<RoomDay>,
    trickList: List<RoomTrick>,
    selectedType: TypeFilterProgressUser,
    onTypeSelected: (TypeFilterProgressUser) -> Unit,
    modifier: Modifier = Modifier
) {


    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(TypeFilterProgressUser.entries) { type ->
            val isSelected = type == selectedType

            val contItems = when (type) {
                TypeFilterProgressUser.UNLOCK_TRICK -> trickList.filter { it.status == StatusTrick.UNLOCK.status }.size
                TypeFilterProgressUser.TRYING_TRICK -> trickList.filter { it.status == StatusTrick.TRYING.status }.size
                TypeFilterProgressUser.SKATE_DAYS -> daysSkate.size
            }

            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        type.nameFilter.plus(" (").plus(contItems.toString()).plus(")")
                    )
                }, leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null, colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.background, // Tu Verde Jade
                    selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                    labelColor = MaterialTheme.colorScheme.onBackground
                ))
        }
    }
}

@Composable
fun CreateFabGroup(
    onCreateDaySkate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }


    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp), // Espacio entre botones
        modifier = Modifier.padding(16.dp)
    ) {


        if (expanded) {

            SubFabItem(
                label = "INICIAR DÍA SKATE",
                icon = Icons.Default.Skateboarding,
                color = MaterialTheme.colorScheme.primary,
                onClick = {
                    onCreateDaySkate()
                    expanded = false
                })

        }

        FloatingButtonCustom(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            onClick = { expanded = !expanded })

    }
}

