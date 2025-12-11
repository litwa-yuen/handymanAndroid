package com.lit.wa.yuen.handyman.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lit.wa.yuen.handyman.R // Assuming R.drawable.profile_placeholder exists
import com.lit.wa.yuen.handyman.ui.components.ImageSourceSelectionDialog
import com.lit.wa.yuen.handyman.ui.components.ProfileMenuItem
import com.lit.wa.yuen.handyman.viewmodels.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel
) {
    val state by viewModel.state.collectAsState()
    val user = state.currentUser

    // Use black background for contrast, as seen in the image
    val primaryTextColor = Color.White
    val cardColor = Color(0xFF1E1E1E) // Darker gray/black for cards
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                // TODO: Create this method in your AuthViewModel
                // viewModel.onProfilePictureChange(it)
            }
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            if (user?.profilePictureUrl != null) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    // AsyncImage for profile picture URL, fallback to local drawable
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        placeholder = painterResource(R.drawable.profile_placeholder), // Placeholder resource needed
                        error = painterResource(R.drawable.profile_placeholder),     // Error fallback
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                    )

                    // Edit Icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(cardColor)
                            .border(1.dp, Color.Gray, CircleShape)
                            .clickable { showImageSourceDialog = true }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Picture",
                            tint = primaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // User Email / Display Name
            if (user?.username != null) {
                Text(
                    text = user.username,
                    color = primaryTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))
            }


            // Edit Profile Button
            Button(
                onClick = { /* TODO: Navigate to Edit Profile Screen */ },
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cardColor),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Edit profile", color = primaryTextColor)
            }
            Spacer(Modifier.height(32.dp))

            // --- Inventories Section ---
            Text(
                text = "Inventories",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            ProfileMenuItem(
                icon = Icons.Default.List,
                title = "My tasks",
                count = 2,
                onClick = { /* TODO: Navigate to My Tasks */ }
            )
            Spacer(Modifier.height(32.dp))

            // --- Preferences Section ---
            Text(
                text = "Preferences",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            ProfileMenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Logout",
                isLogout = true,
                onClick = { viewModel.onSignOutClicked() }
            )

            Spacer(Modifier.weight(1f))

            // Note: The Bottom Navigation Bar is assumed to be handled by MainScreen
        }
        // 3. Display the dialog if the state is true
        if (showImageSourceDialog) {
            ImageSourceSelectionDialog(
                onDismiss = { showImageSourceDialog = false },
                onChooseLibrary = {
                    showImageSourceDialog = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTakePhoto = {
                    // TODO: Implement logic to launch the camera
                    showImageSourceDialog = false
                    println("Action: Take Photo")
                }
            )
        }
    }
}