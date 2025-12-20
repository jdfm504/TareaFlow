package com.campusdigitalfp.tareaflow.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.campusdigitalfp.tareaflow.R
import com.campusdigitalfp.tareaflow.ui.theme.ApplyStatusBarTheme
import com.campusdigitalfp.tareaflow.ui.theme.GreenLight
import com.campusdigitalfp.tareaflow.ui.theme.GreenPrimary

@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val heroFraction = 0.7f // controla la altura de la zona verde
    val config = LocalConfiguration.current
    val screenH = config.screenHeightDp.dp

    ApplyStatusBarTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ZONA VERDE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heroFraction)
                .clip(RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
                .background(brush = Brush.verticalGradient(listOf(GreenPrimary, GreenLight)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(top = 80.dp, bottom = 180.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Parte superior zona verde
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(top = 25.dp, bottom = 25.dp)
                    )

                    Spacer(Modifier.height(25.dp))

                    Text(
                        text = stringResource(R.string.app_name),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 25.dp)
                    )
                }

                // Subtítulo
                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.widthIn(max = 300.dp)
                )
            }
        }

        // BOTÓN CIRCULAR EN EL BORDE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = screenH * heroFraction - 36.dp), // 36 = radio del círculo
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = CircleShape,
                color = GreenPrimary,
                shadowElevation = 10.dp,
                onClick = onStart,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        //  TEXTO ZONA BLANCA
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.onboarding_button),
                color = GreenPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 35.dp)

            )

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onGoToLogin) {
                Text(
                    text = stringResource(R.string.onboarding_login_link),
                    color = GreenPrimary,
                    fontSize = 15.sp
                )
            }
        }
    }
}
