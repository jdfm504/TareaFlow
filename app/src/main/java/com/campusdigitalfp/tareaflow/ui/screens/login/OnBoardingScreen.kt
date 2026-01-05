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
    val conf = LocalConfiguration.current
    val screenHeight = conf.screenHeightDp.dp
    val screenWidth = conf.screenWidthDp.dp

    // Altura de la zona verde según altura de pantalla
    val heroHeight = when {
        screenHeight < 600.dp -> screenHeight * 0.62f
        screenHeight < 750.dp -> screenHeight * 0.67f
        else -> screenHeight * 0.72f
    }

    // Icono segun tamaño de pantalla
    val iconSize = when {
        screenWidth < 350.dp -> 90.dp
        screenWidth < 400.dp -> 120.dp
        else -> 150.dp
    }

    // Tamaño del título según pantalla
    val titleSize = when {
        screenWidth < 350.dp -> 28.sp
        screenWidth < 400.dp -> 32.sp
        else -> 38.sp
    }

    ApplyStatusBarTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Zona verde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(GreenPrimary, GreenLight)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(
                            top = when {
                                screenHeight < 600.dp -> 80.dp   // móviles pequeños
                                screenHeight < 750.dp -> 120.dp   // medianos
                                else -> 140.dp                    // grandes
                            },
                            bottom = 80.dp
                        ),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // titulo
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.White,
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // subitutlo
                    Text(
                        text = stringResource(R.string.onboarding_subtitle),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.widthIn(max = 300.dp)
                    )
                }
            }

            //  botón
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = CircleShape,
                    color = GreenPrimary,
                    shadowElevation = 10.dp,
                    onClick = onStart,
                    modifier = Modifier
                        .size(72.dp)
                        .offset(y = (-36).dp) // la mitad hacia arriba para solaparse con el borde negro
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

            // zona blanca
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(R.string.onboarding_button),
                    color = GreenPrimary,
                    fontSize = if (screenWidth < 350.dp) 28.sp else 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onGoToLogin) {
                    Text(
                        text = stringResource(R.string.onboarding_login_link),
                        color = GreenPrimary,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}