package com.campusdigitalfp.tareaflow.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun DeleteButton(onClick: () -> Unit) {
    val circle = 40.dp          // diámetro del círculo
    val protrude = 20.dp        // cuánto sobresale el círculo hacia la izquierda

    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .height(circle)
            .wrapContentWidth()                 // el ancho = contenido real (círculo + chip)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Espacio “invisible” para que la parte que sobresale a la izquierda también sea clickable
        Spacer(Modifier.width(protrude))

        // Capa donde solapamos chip y círculo
        Box(
            modifier = Modifier.wrapContentWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Zona blanco
            Surface(
                modifier = Modifier
                    .height(circle * 0.6f)
                    .padding(start = circle / 2 - protrude), // Zona blanca se situa bajo el círculo
                shape = RoundedCornerShape(50),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0x33000000)),
                tonalElevation = 8.dp,
                shadowElevation = 10.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(
                        start = protrude + 8.dp, // espacio entre el borde del círculo y el texto
                        end = 16.dp
                    )
                ) {
                    Text(
                        text = "Borrar",
                        color = Color(0xFFD01111),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Circulo rojo (desplazado dentro del ancho clickable usando el Spacer)
            Box(
                modifier = Modifier
                    .size(circle)
                    .offset(x = -protrude)
                    .shadow(16.dp, CircleShape, clip = false)
                    .background(Color(0xFFD01111), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Borrar tareas seleccionadas",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}