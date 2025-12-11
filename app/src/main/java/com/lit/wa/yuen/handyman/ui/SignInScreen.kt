package com.lit.wa.yuen.handyman.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lit.wa.yuen.handyman.R // Assuming you have an R.drawable for Google/Facebook icons
import com.lit.wa.yuen.handyman.ui.components.AuthButton
import com.lit.wa.yuen.handyman.viewmodels.AuthViewModel

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
    onNavigateToEmailSignIn: () -> Unit, // New parameter to navigate to EmailSignInScreen
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) {
            onSignInSuccess()
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF808080)), // Gray background from the image
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Adjust width as needed
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White // Card background color
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Let's Get Started",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(32.dp))

                // Continue with Google Button
                AuthButton(
                    text = "Continue With Google",
                    iconRes = R.drawable.ic_google_logo, // Assume you have this drawable
                    onClick = { viewModel.onGoogleSignInClicked() },
                    enabled = !state.isLoading,
                    backgroundColor = Color.LightGray,
                    contentColor = Color.Black
                )
                Spacer(Modifier.height(16.dp))

                // Continue With Facebook Button
                AuthButton(
                    text = "Continue With Facebook",
                    iconRes = R.drawable.ic_facebook_logo, // Assume you have this drawable
                    onClick = { viewModel.onFacebookSignInClicked(context) },
                    enabled = !state.isLoading,
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "or",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(24.dp))

                // Continue With Email Button
                AuthButton(
                    text = "Continue With Email",
                    icon = Icons.Default.MailOutline,
                    onClick = onNavigateToEmailSignIn, // Uses the navigation parameter
                    enabled = !state.isLoading,
                    backgroundColor = Color(0xFFFD0445), // Red color from the image
                    contentColor = Color.White
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Create An Account",
                        color = MaterialTheme.colorScheme.primary, // Or a specific color like Color(0xFFFD0445)
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(onClick = onNavigateToSignUp)
                    )
                }

                state.error?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}