package com.marotidev.citole.presentation.onboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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

    val buttonContainer = Color(0xFF72DE6E).harmonize(other = MaterialTheme.colorScheme.primary)
    val buttonOnContainer = Color(0xFF258020).harmonize(other = MaterialTheme.colorScheme.primary)

    val artworkUris by onboardViewModel.artworkUris.collectAsStateWithLifecycle()

    Scaffold(

    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Access Media Permission", style = MaterialTheme.typography.titleLarge)
            Text("Citole needs media permission to access music & audio on your device",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Box(
                modifier = Modifier.padding(vertical = 40.dp, horizontal = 30.dp)
            ) {
                ArtworkCollage(
                    hash = 1,
                    artworkUris = artworkUris
                )
            }
            Button(
                enabled = !granted,
                onClick = {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    launcher.launch(permission)
                },
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp),
                shapes = ButtonDefaults.shapes(
                    shape = CircleShape,
                    pressedShape = MaterialTheme.shapes.medium
                ),
                colors = if (granted) ButtonDefaults.buttonColors(
                    containerColor = buttonContainer,
                    contentColor = buttonOnContainer
                ) else ButtonDefaults.filledTonalButtonColors()
            ) {
                Icon(
                    painter = painterResource(
                        id = if (granted) R.drawable.ic_check
                            else R.drawable.ic_audio_file
                    ),
                    contentDescription = "Grant Permission",
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (granted) "Permission Granted"
                        else "Grant Permission",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Spacer(modifier = Modifier.height(70.dp))
            FilledTonalIconButton (
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