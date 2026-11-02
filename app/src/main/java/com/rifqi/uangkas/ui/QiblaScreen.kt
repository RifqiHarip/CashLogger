package com.rifqi.uangkas.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    isDarkTheme: Boolean, // 👈 Parameter tema dinamis
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }

    // --- STATE LOKASI DINAMIS GPS ---
    var userLat by remember { mutableDoubleStateOf(-6.4025) }
    var userLng by remember { mutableDoubleStateOf(106.7942) }
    var isLocationLoaded by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Launcher Izin Lokasi Runtime
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Ambil Lokasi Real-Time dari GPS
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location ->
                    location?.let {
                        userLat = it.latitude
                        userLng = it.longitude
                        isLocationLoaded = true
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Perhitungan Sudut Kiblat (Dinamis berdasarkan lokasi terkini)
    val qiblaAngle = remember(userLat, userLng) { calculateQiblaDirection(userLat, userLng) }

    // Logika Sensor Kompas HP
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuthInDegrees + 360) % 360
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(sensorEventListener) }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CompassRotation"
    )

    val isFacingQibla = abs(azimuth - qiblaAngle) < 2.5f || abs(azimuth - qiblaAngle) > 357.5f

    var hasVibrated by remember { mutableStateOf(false) }
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !hasVibrated) {
            vibrateDevice(context)
            hasVibrated = true
        } else if (!isFacingQibla) {
            hasVibrated = false
        }
    }

    val statusColor by animateColorAsState(
        targetValue = if (isFacingQibla) KstcGold else (if (isDarkTheme) Color(0xFF10B981) else KstcDarkGreen),
        label = "StatusColor"
    )

    // --- COLOR PALETTE ADAPTIF LIGHT & DARK MODE ---
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF8F9FA)
    val cardBgColor = if (isDarkTheme) {
        if (isFacingQibla) KstcGold.copy(alpha = 0.15f) else Color(0xFF1E1E1E)
    } else {
        if (isFacingQibla) KstcGold.copy(alpha = 0.2f) else Color.White
    }
    val compassBgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val innerReadoutBg = if (isDarkTheme) Color(0xFF121212).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.95f)
    val mainTextColor = if (isDarkTheme) Color.White else Color.Black
    val subTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arah Kiblat", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KstcDarkGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Status
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(16.dp),
                border = if (isFacingQibla) androidx.compose.foundation.BorderStroke(1.5.dp, KstcGold) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isFacingQibla) "MENGHADAP KIBLAT (PAS)" else "CARI ARAH KIBLAT",
                        color = statusColor,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFacingQibla) "Posisi HP sudah tepat dengan Ka'bah" else "Putar HP hingga ikon Ka'bah berada di puncak panah",
                        color = subTextColor,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp
                    )
                }
            }

            // Kompas Utama
            Box(
                modifier = Modifier.size(320.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isFacingQibla) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(KstcGold.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                    )
                }

                // Panah Penunjuk Atas (Fixed)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pointerPath = Path().apply {
                        moveTo(size.width / 2, 8.dp.toPx())
                        lineTo(size.width / 2 - 10.dp.toPx(), 28.dp.toPx())
                        lineTo(size.width / 2 + 10.dp.toPx(), 28.dp.toPx())
                        close()
                    }
                    drawPath(pointerPath, color = if (isFacingQibla) KstcGold else Color.Red)
                }

                // Piringan Kompas Berputar
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(-animatedAzimuth)
                        .clip(CircleShape)
                        .background(compassBgColor)
                        .border(
                            width = 2.dp,
                            color = if (isFacingQibla) KstcGold else (if (isDarkTheme) Color(0xFF333333) else Color(0xFFE5E7EB)),
                            shape = CircleShape
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCompassWheel(
                            qiblaAngle = qiblaAngle,
                            isFacingQibla = isFacingQibla,
                            goldColor = KstcGold,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                // Inner Readout (Angka Azimuth Real-time)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(innerReadoutBg)
                        .border(
                            width = 1.dp,
                            color = if (isDarkTheme) Color(0xFF333333) else Color(0xFFE5E7EB),
                            shape = CircleShape
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        text = "${azimuth.toInt()}°",
                        color = mainTextColor,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Kiblat: ${qiblaAngle.toInt()}°",
                        color = if (isDarkTheme) KstcGold else KstcDarkGreen,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Status Koordinat GPS Real-Time
            Text(
                text = if (isLocationLoaded) {
                    "GPS: ${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)}"
                } else {
                    "Mendapatkan Lokasi GPS..."
                },
                color = Color.Gray,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp
            )
        }
    }
}

private fun DrawScope.drawCompassWheel(
    qiblaAngle: Float,
    isFacingQibla: Boolean,
    goldColor: Color,
    isDarkTheme: Boolean
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 2

    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 10.sp.toPx()
        color = if (isDarkTheme) android.graphics.Color.GRAY else android.graphics.Color.DKGRAY
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    val cardinalPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 13.sp.toPx()
        color = if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    // Tick Marks & Derajat
    for (degree in 0 until 360 step 5) {
        val angleRad = Math.toRadians((degree - 90).toDouble())
        val isMajor = degree % 30 == 0

        val tickLength = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
        val startR = radius - 8.dp.toPx()
        val endR = startR - tickLength

        val startX = center.x + startR * cos(angleRad).toFloat()
        val startY = center.y + startR * sin(angleRad).toFloat()
        val endX = center.x + endR * cos(angleRad).toFloat()
        val endY = center.y + endR * sin(angleRad).toFloat()

        val tickColor = if (isMajor) {
            if (isDarkTheme) Color.LightGray else Color.Gray
        } else {
            if (isDarkTheme) Color.DarkGray else Color.LightGray
        }

        drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
        )

        if (isMajor) {
            val textR = radius - 32.dp.toPx()
            val textX = center.x + textR * cos(angleRad).toFloat()
            val textY = center.y + textR * sin(angleRad).toFloat() + 4.dp.toPx()

            val text = when (degree) {
                0 -> "N"
                90 -> "E"
                180 -> "S"
                270 -> "W"
                else -> "$degree°"
            }

            val paint = if (degree % 90 == 0) {
                cardinalPaint.apply {
                    color = if (degree == 0) android.graphics.Color.RED else (if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
                }
            } else {
                textPaint
            }

            drawIntoCanvas {
                it.nativeCanvas.drawText(text, textX, textY, paint)
            }
        }
    }

    // Badge Ka'bah pada Sudut Kiblat Dinamis
    val qiblaRad = Math.toRadians((qiblaAngle - 90).toDouble())
    val kaabaR = radius - 16.dp.toPx()
    val kaabaX = center.x + kaabaR * cos(qiblaRad).toFloat()
    val kaabaY = center.y + kaabaR * sin(qiblaRad).toFloat()

    drawCircle(
        color = if (isFacingQibla) goldColor else (if (isDarkTheme) Color.Black else Color.White),
        radius = 16.dp.toPx(),
        center = Offset(kaabaX, kaabaY)
    )
    drawCircle(
        color = goldColor,
        radius = 16.dp.toPx(),
        center = Offset(kaabaX, kaabaY),
        style = Stroke(width = 1.5.dp.toPx())
    )

    val boxSize = 14.dp.toPx()
    drawRect(
        color = Color.Black,
        topLeft = Offset(kaabaX - boxSize / 2, kaabaY - boxSize / 2),
        size = Size(boxSize, boxSize)
    )
    drawLine(
        color = goldColor,
        start = Offset(kaabaX - boxSize / 2, kaabaY - boxSize / 6),
        end = Offset(kaabaX + boxSize / 2, kaabaY - boxSize / 6),
        strokeWidth = 2.dp.toPx()
    )
}

fun calculateQiblaDirection(latitude: Double, longitude: Double): Float {
    val kaabaLat = Math.toRadians(21.422487)
    val kaabaLng = Math.toRadians(39.826206)
    val myLat = Math.toRadians(latitude)
    val myLng = Math.toRadians(longitude)

    val y = sin(kaabaLng - myLng)
    val x = cos(myLat) * tan(kaabaLat) - sin(myLat) * cos(kaabaLng - myLng)
    var qibla = Math.toDegrees(atan2(y, x))
    if (qibla < 0) qibla += 360.0
    return qibla.toFloat()
}

fun vibrateDevice(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}