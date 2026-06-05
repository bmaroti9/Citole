/*
Copyright (C) <2026>  <Balint Maroti>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

*/

package com.marotidev.citole

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.marotidev.citole.viewmodels.PlayerViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    onDismissRequest: () -> Unit,
    playerViewModel: PlayerViewModel
) {
    val sheetState = rememberModalBottomSheetState()

    val sheetCornerRadius by animateDpAsState(
        targetValue = if (sheetState.currentValue == SheetValue.Expanded) 0.dp else 32.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp)
            ) {
                items(playerViewModel.currentQueue.size) { index ->
                    TrackItem (
                        playerViewModel.currentQueue[index],
                        playerViewModel,
                        index = index,
                        count = playerViewModel.currentQueue.size
                    ) {
                        playerViewModel.skipInQueue(index)
                    }
                }
            }
        }

    }
}