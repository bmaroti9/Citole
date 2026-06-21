package com.marotidev.citole.presentation.onboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marotidev.citole.R
import com.marotidev.citole.presentation.utils.ArtworkCollage
import com.materialkolor.ktx.harmonize

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardScreen(
    onboardViewModel: OnboardViewModel = hiltViewModel()
) {
    var granted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
        if (isGranted) {
            onboardViewModel.onPermissionGranted()
        }
    }

    val artworkUris by onboardViewModel.artworkUris.collectAsStateWithLifecycle()
    val trackCount by onboardViewModel.trackCount.collectAsStateWithLifecycle()
    val albumCount by onboardViewModel.albumCount.collectAsStateWithLifecycle()
    val artistCount by onboardViewModel.artistCount.collectAsStateWithLifecycle()

    Scaffold(

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Access Media Permission", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Citole needs media permission to access music & audio on your device",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center)
            Box(
                modifier = Modifier.padding(vertical = 58.dp, horizontal = 30.dp)
            ) {
                ArtworkCollage(
                    hash = 19, //i liked the look of this one
                    artworkUris = artworkUris
                )
            }
            Column(
                modifier = Modifier.height(160.dp),
                horizontalAlignment = Alignment.Start
            ) {
                FilledTonalButton(
                    enabled = !granted,
                    onClick = {
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_AUDIO
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        launcher.launch(permission)
                    },
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 14.dp),
                    shapes = ButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = MaterialTheme.shapes.medium
                    ),
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (granted) R.drawable.ic_verified
                            else R.drawable.ic_music
                        ),
                        contentDescription = "Grant Permission",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (granted) "Permission Granted"
                        else "Grant Permission",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (granted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Check",
                            modifier = Modifier.padding(end = 8.dp, start = 4.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text("$trackCount tracks", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Check",
                            modifier = Modifier.padding(end = 8.dp, start = 4.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text("$albumCount albums", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Check",
                            modifier = Modifier.padding(end = 8.dp, start = 4.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text("$artistCount artists", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            FilledTonalIconButton (
                enabled = granted,
                onClick = {  },
                shapes = IconButtonDefaults.shapes(
                    shape = CircleShape,
                    pressedShape = MaterialTheme.shapes.medium
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}