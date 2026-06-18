package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Enums to manage application state
enum class ScaleAppSection {
    LANDING,
    CALCULATOR,
    CONSOLE
}

enum class ScaleLandingTab {
    APPLICATIONS,
    DATA
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ScaleAppMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleAppMainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Core Navigation States
    var currentSection by remember { mutableStateOf(ScaleAppSection.LANDING) }
    var selectedLandingTab by remember { mutableStateOf(ScaleLandingTab.APPLICATIONS) }
    
    // Booking Form States
    var showBookingSheet by remember { mutableStateOf(false) }
    var bookingName by remember { mutableStateOf("") }
    var bookingEmail by remember { mutableStateOf("") }
    var bookingOrg by remember { mutableStateOf("") }
    var bookingUseCase by remember { mutableStateOf("Generative AI RLHF") }
    var bookingBudget by remember { mutableStateOf("Enterprise Scale") }
    var isBookingSubmitting by remember { mutableStateOf(false) }
    var showBookingSuccessDialog by remember { mutableStateOf(false) }
    var bookingSuccessCode by remember { mutableStateOf("") }
    
    // Auth & Client Console States
    var showLoginSheet by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }
    
    // Cost Engine States
    var datasetTokensMillions by remember { mutableStateOf(10f) }
    var modelParameterSizeGiga by remember { mutableStateOf(70f) }
    var computeIntensity by remember { mutableStateOf("Frontier Training") } // Frontier, High, FineTune
    
    // Entity Labeler Game States
    var currentLabelWordIndex by remember { mutableStateOf(-1) }
    val labeledWords = remember { mutableStateListOf<Int>() }
    var annotationScore by remember { mutableStateOf(0) }
    var annotationSubmitted by remember { mutableStateOf(false) }
    var labelSpeedMs by remember { mutableStateOf(4500L) }
    
    // Interactive Mega Menu States
    var activeMegaMenu by remember { mutableStateOf<String?>(null) } // "Products", "Solutions", "Research" etc.
    
    // Active simulation logs for Agent System
    val agentSimulationLogs = remember { mutableStateListOf<String>() }
    var isAgentSimulationRunning by remember { mutableStateOf(false) }
    
    // Launch Agentic text logger
    LaunchedEffect(isAgentSimulationRunning) {
        if (isAgentSimulationRunning) {
            agentSimulationLogs.clear()
            val steps = listOf(
                "Initializing Autonomous Agent workflow template...",
                "Pulling live feedback loop from scale_db corpus (4.5M entries)...",
                "Spawning parallel LLM rating nodes with threshold delta = 0.05",
                "Node 1: Evaluated hallucination check -> OK (rate: 0.8%)",
                "Node 2: Coding response format check -> Correct syntax detected",
                "Node 3: Safety filter comparison -> Satisfies system_guard constraints",
                "Aggregating consensus rating... Model accuracy delta improved by 14%",
                "Self-correction loop finalized. Dataset committed to high-frontier pipeline.",
                "Process complete! Accuracy verified at 99.8%"
            )
            for (step in steps) {
                delay(1200)
                agentSimulationLogs.add(step)
            }
            isAgentSimulationRunning = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("scale_scaffold"),
        containerColor = ScaleBlack,
        topBar = {
            // High-fidelity Floating Top Navbar
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScaleBlack)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Branding Logo /// SCALE
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { currentSection = ScaleAppSection.LANDING }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.Black)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SCALE NEXUS",
                                color = ScaleTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Navigation categories for Desktop view or Tabbed selection in Compose
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "Landing",
                                color = if (currentSection == ScaleAppSection.LANDING && activeMegaMenu == null) ScaleGreenAccent else ScaleTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        currentSection = ScaleAppSection.LANDING
                                        activeMegaMenu = null
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                            Text(
                                text = "Calculator",
                                color = if (currentSection == ScaleAppSection.CALCULATOR) ScaleGreenAccent else ScaleTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        currentSection = ScaleAppSection.CALCULATOR
                                        activeMegaMenu = null
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                            Text(
                                text = "Client Console",
                                color = if (currentSection == ScaleAppSection.CONSOLE) ScaleGreenAccent else ScaleTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        currentSection = ScaleAppSection.CONSOLE
                                        activeMegaMenu = null
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                            // Mega Menu Triggers
                            Text(
                                text = "Products ▾",
                                color = if (activeMegaMenu == "Products") ScaleSkyBlue else ScaleTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable {
                                        activeMegaMenu = if (activeMegaMenu == "Products") null else "Products"
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }

                        // Right action buttons (Log In, Book Demo)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isLoggedIn) {
                                OutlinedButton(
                                    onClick = { showLoginSheet = true },
                                    border = BorderStroke(1.dp, ScaleBorderGray),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ScaleTextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .testTag("login_button")
                                        .height(36.dp)
                                ) {
                                    Text("Log in", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { 
                                        isLoggedIn = false 
                                        Toast.makeText(context, "Logged out of client portal", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, ScaleGreenAccent),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ScaleGreenAccent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Log out", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Button(
                                onClick = { showBookingSheet = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ScaleGreenAccent,
                                    contentColor = ScaleBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .testTag("book_demo_button")
                                    .height(36.dp)
                            ) {
                                Text("Book Demo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                // Animated Mega Menu Panel
                AnimatedVisibility(
                    visible = activeMegaMenu != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    activeMegaMenu?.let { menu ->
                        MegaMenuDropdown(
                            menuName = menu,
                            onClose = { activeMegaMenu = null },
                            onItemClick = { item ->
                                Toast.makeText(context, "Navigating to: $item", Toast.LENGTH_SHORT).show()
                                activeMegaMenu = null
                                // Special navigation checks if needed
                                if (item.contains("Calculator")) {
                                    currentSection = ScaleAppSection.CALCULATOR
                                }
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF050505))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .statusBarsPadding() // safety in case of bottom layout overlap
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home
                    Column(
                        modifier = Modifier
                            .clickable { 
                                currentSection = ScaleAppSection.LANDING 
                                activeMegaMenu = null
                            }
                            .padding(top = 4.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val active = currentSection == ScaleAppSection.LANDING
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) ScaleGreenAccent.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = if (active) ScaleGreenAccent else ScaleTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Home",
                            color = if (active) Color.White else ScaleTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Calculator
                    Column(
                        modifier = Modifier
                            .clickable { 
                                currentSection = ScaleAppSection.CALCULATOR 
                                activeMegaMenu = null
                            }
                            .padding(top = 4.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val active = currentSection == ScaleAppSection.CALCULATOR
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) ScaleGreenAccent.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Calculator",
                                tint = if (active) ScaleGreenAccent else ScaleTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Calculator",
                            color = if (active) Color.White else ScaleTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Client Console
                    Column(
                        modifier = Modifier
                            .clickable { 
                                currentSection = ScaleAppSection.CONSOLE 
                                activeMegaMenu = null
                            }
                            .padding(top = 4.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val active = currentSection == ScaleAppSection.CONSOLE
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) ScaleGreenAccent.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBox,
                                contentDescription = "Lock",
                                tint = if (active) ScaleGreenAccent else ScaleTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Console",
                            color = if (active) Color.White else ScaleTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Book Demo Trigger
                    Column(
                        modifier = Modifier
                            .clickable { showBookingSheet = true }
                            .padding(top = 4.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (showBookingSheet) ScaleGreenAccent.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Book Demo",
                                tint = if (showBookingSheet) ScaleGreenAccent else ScaleTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Book",
                            color = if (showBookingSheet) Color.White else ScaleTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (showBookingSheet) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                
                // Android dynamic navigation bar handle visual indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main page routing switcher
            when (currentSection) {
                ScaleAppSection.LANDING -> {
                    ScaleAIHomeLanding(
                        selectedTab = selectedLandingTab,
                        onTabSelected = { selectedLandingTab = it },
                        onBookPressed = { showBookingSheet = true },
                        isAgentSimulationRunning = isAgentSimulationRunning,
                        onRunAgentSimulation = { isAgentSimulationRunning = true },
                        agentLogs = agentSimulationLogs,
                        currentWordIndex = currentLabelWordIndex,
                        onWordClick = { index ->
                            if (labeledWords.contains(index)) {
                                labeledWords.remove(index)
                            } else {
                                labeledWords.add(index)
                            }
                            if (!annotationSubmitted) {
                                // Simple evaluation scoring
                                // target indices representing entities (hallucination rate, 3.4%, LLM)
                                val targetEntities = setOf(1, 2, 10, 11)
                                val matches = labeledWords.intersect(targetEntities).size
                                val noise = labeledWords.subtract(targetEntities).size
                                annotationScore = ((matches * 25) - (noise * 10)).coerceIn(0, 100)
                            }
                        },
                        labeledWords = labeledWords,
                        annotationSubmitted = annotationSubmitted,
                        annotationScore = annotationScore,
                        onResetLabeler = {
                            labeledWords.clear()
                            annotationScore = 0
                            annotationSubmitted = false
                            currentLabelWordIndex = -1
                        },
                        onSubmitAnnotation = {
                            annotationSubmitted = true
                            Toast.makeText(context, "Annotation committed with $annotationScore% accuracy", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                ScaleAppSection.CALCULATOR -> {
                    ScaleCostCalculatorView(
                        tokensMillions = datasetTokensMillions,
                        onTokensChanged = { datasetTokensMillions = it },
                        paramsGiga = modelParameterSizeGiga,
                        onParamsChanged = { modelParameterSizeGiga = it },
                        computeIntensity = computeIntensity,
                        onComputeChanged = { computeIntensity = it }
                    )
                }
                ScaleAppSection.CONSOLE -> {
                    ScaleClientConsoleView(
                        isLoggedIn = isLoggedIn,
                        onLoginClick = { showLoginSheet = true },
                        loginEmail = loginEmail,
                        loginPassword = loginPassword,
                        onEmailChange = { loginEmail = it },
                        onPasswordChange = { loginPassword = it },
                        passwordVisible = loginPasswordVisible,
                        togglePasswordVisible = { loginPasswordVisible = !loginPasswordVisible },
                        isLoggingIn = isLoggingIn,
                        onAuthSubmit = {
                            coroutineScope.launch {
                                isLoggingIn = true
                                delay(1800)
                                isLoggingIn = false
                                isLoggedIn = true
                                Toast.makeText(context, "Authenticated using secure enterprise context", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Book Demo Modal Drawer (Custom Sheet implementation for fine-grained control)
            AnimatedVisibility(
                visible = showBookingSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .testTag("booking_sheet"),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ScaleDeepGray
                    ),
                    border = BorderStroke(1.dp, ScaleBorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Slider handle top indicator
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(ScaleBorderGray, CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Book an Enterprise Demo",
                                color = ScaleTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showBookingSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = ScaleTextSecondary)
                            }
                        }

                        Text(
                            text = "Scale delivers proven data, evaluations, and solutions to AI Labs, Governments, and the Fortune 500.",
                            color = ScaleTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Form Inputs
                        Text("Your Full Name", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = bookingName,
                            onValueChange = { bookingName = it },
                            placeholder = { Text("e.g. Alexis Martinez", color = ScaleTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ScaleTextPrimary,
                                unfocusedTextColor = ScaleTextPrimary,
                                focusedContainerColor = ScaleBlack,
                                unfocusedContainerColor = ScaleBlack,
                                focusedBorderColor = ScaleGreenAccent,
                                unfocusedBorderColor = ScaleBorderGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Work Email", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = bookingEmail,
                            onValueChange = { bookingEmail = it },
                            placeholder = { Text("alexis@organization.ai", color = ScaleTextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ScaleTextPrimary,
                                unfocusedTextColor = ScaleTextPrimary,
                                focusedContainerColor = ScaleBlack,
                                unfocusedContainerColor = ScaleBlack,
                                focusedBorderColor = ScaleGreenAccent,
                                unfocusedBorderColor = ScaleBorderGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Organization Name", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = bookingOrg,
                            onValueChange = { bookingOrg = it },
                            placeholder = { Text("Autonomous Labs Inc.", color = ScaleTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ScaleTextPrimary,
                                unfocusedTextColor = ScaleTextPrimary,
                                focusedContainerColor = ScaleBlack,
                                unfocusedContainerColor = ScaleBlack,
                                focusedBorderColor = ScaleGreenAccent,
                                unfocusedBorderColor = ScaleBorderGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Core Use Case Interest", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Generative AI RLHF", "Autonomous AV Systems", "Federal Sector Systems", "Custom Agentic Ops").forEach { choice ->
                                val selected = bookingUseCase == choice
                                Box(
                                    modifier = Modifier
                                        .background(if (selected) ScaleGreenAccent else ScaleBlack, RoundedCornerShape(8.dp))
                                        .border(1.dp, if (selected) ScaleGreenAccent else ScaleBorderGray, RoundedCornerShape(8.dp))
                                        .clickable { bookingUseCase = choice }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(choice, color = if (selected) ScaleBlack else ScaleTextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Estimated Dataset Size Tier", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Startup (1-10M Tokens)", "Mid-Market (10-100M)", "Frontier Lab (100M - 5B)", "Enterprise Scale").forEach { choice ->
                                val selected = bookingBudget == choice
                                Box(
                                    modifier = Modifier
                                        .background(if (selected) ScaleSkyBlue else ScaleBlack, RoundedCornerShape(8.dp))
                                        .border(1.dp, if (selected) ScaleSkyBlue else ScaleBorderGray, RoundedCornerShape(8.dp))
                                        .clickable { bookingBudget = choice }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(choice, color = if (selected) ScaleBlack else ScaleTextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (bookingName.isEmpty() || bookingEmail.isEmpty() || bookingOrg.isEmpty()) {
                                    Toast.makeText(context, "Please complete all mandatory information fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                coroutineScope.launch {
                                    isBookingSubmitting = true
                                    delay(1500)
                                    isBookingSubmitting = false
                                    showBookingSheet = false
                                    // Generate a mock tracking code
                                    bookingSuccessCode = "SC-" + Random.nextInt(100000, 999999) + "-AI"
                                    showBookingSuccessDialog = true
                                    
                                    // Reset inputs
                                    bookingName = ""
                                    bookingEmail = ""
                                    bookingOrg = ""
                                }
                            },
                            enabled = !isBookingSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ScaleGreenAccent,
                                disabledContainerColor = ScaleTextMuted,
                                contentColor = ScaleBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isBookingSubmitting) {
                                CircularProgressIndicator(color = ScaleBlack, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Secure Consultation Schedule", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Booking Success Dialog Modal
            if (showBookingSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showBookingSuccessDialog = false },
                    title = {
                        Text("Demo Reservation Confirmed", color = ScaleTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    text = {
                        Column {
                            Text(
                                "Your technical brief consultation has been scheduled successfully.",
                                color = ScaleTextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ScaleBlack),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("REFERENCE ID", color = ScaleTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(bookingSuccessCode, color = ScaleGreenAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("USE CASE PROFILE", color = ScaleTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(bookingUseCase, color = ScaleTextPrimary, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PROVISIONING PIPELINE", color = ScaleTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("$bookingBudget Tier Sandbox Setup", color = ScaleSkyBlue, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "A systems engineering architect will contact you within 2 hours at your work email. A temporary Scale Client Console sandbox was provisioned for your organization.",
                                color = ScaleTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { 
                                showBookingSuccessDialog = false
                                // Auto navigate to active portal for deep exploration
                                currentSection = ScaleAppSection.CONSOLE
                                isLoggedIn = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ScaleGreenAccent, contentColor = ScaleBlack)
                        ) {
                            Text("Launch My Sandbox Portal")
                        }
                    },
                    containerColor = ScaleDeepGray,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Login System Sheet Modal
            AnimatedVisibility(
                visible = showLoginSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ScaleDeepGray
                    ),
                    border = BorderStroke(1.dp, ScaleBorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(ScaleBorderGray, CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scale Client Portal Auth",
                                color = ScaleTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showLoginSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = ScaleTextSecondary)
                            }
                        }

                        Text(
                            text = "Authenticate securely to view active training checkpoints, high-fidelity labeled corpora, and metric graphs.",
                            color = ScaleTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("EMAIL ADDRESS", color = ScaleTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            placeholder = { Text("admin@scale.com", color = ScaleTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ScaleTextPrimary,
                                unfocusedTextColor = ScaleTextPrimary,
                                focusedContainerColor = ScaleBlack,
                                unfocusedContainerColor = ScaleBlack,
                                focusedBorderColor = ScaleGreenAccent,
                                unfocusedBorderColor = ScaleBorderGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("SECURE PASSPHRASE", color = ScaleTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            placeholder = { Text("••••••••••••", color = ScaleTextMuted) },
                            singleLine = true,
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Toggle password visibility",
                                        tint = if (loginPasswordVisible) ScaleGreenAccent else ScaleTextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ScaleTextPrimary,
                                unfocusedTextColor = ScaleTextPrimary,
                                focusedContainerColor = ScaleBlack,
                                unfocusedContainerColor = ScaleBlack,
                                focusedBorderColor = ScaleGreenAccent,
                                unfocusedBorderColor = ScaleBorderGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "Forgot password?",
                                color = ScaleSkyBlue,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "Password reset code sent to client record", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Instant bypass guest entry
                            OutlinedButton(
                                onClick = {
                                    isLoggedIn = true
                                    showLoginSheet = false
                                    currentSection = ScaleAppSection.CONSOLE
                                    Toast.makeText(context, "Unlocked sandbox container via temporary credentials", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ScaleTextSecondary),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Guest Access", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                                        Toast.makeText(context, "Please keys in valid credentials to authenticate", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        isLoggingIn = true
                                        delay(1400)
                                        isLoggingIn = false
                                        isLoggedIn = true
                                        showLoginSheet = false
                                        currentSection = ScaleAppSection.CONSOLE
                                        Toast.makeText(context, "Welcome back, systems manager", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ScaleGreenAccent, contentColor = ScaleBlack),
                                modifier = Modifier.weight(1.5f).height(46.dp)
                            ) {
                                if (isLoggingIn) {
                                    CircularProgressIndicator(color = ScaleBlack, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Access Terminal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Drops down dynamic menus from navigation labels
@Composable
fun MegaMenuDropdown(
    menuName: String,
    onClose: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
        border = BorderStroke(1.dp, ScaleBorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "SCALE AI PLATFORM PRODUCTS",
                color = ScaleTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Products Grid Column 1
                Column(modifier = Modifier.weight(1f)) {
                    Text("DATA ENGINE", color = ScaleGreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf("Data Labeling CLI", "Reinforcement Learning (RLHF)", "3D Sensor Fusion", "Synthetic Generator Pipeline").forEach { item ->
                        Text(
                            text = "• $item",
                            color = ScaleTextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                // Products Grid Column 2
                Column(modifier = Modifier.weight(1f)) {
                    Text("DECISION ALIGNMENT", color = ScaleSkyBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf("Model Sandbox Playground", "Scale GenAI Cost Calculator", "Evals Engine System", "Llama-Federal Alignment Core").forEach { item ->
                        Text(
                            text = "• $item",
                            color = ScaleTextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = ScaleBorderGray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClose) {
                    Text("Close Panel ▾", color = ScaleTextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

// Rebuilds the home web landing of Scale AI in Android
@Composable
fun ScaleAIHomeLanding(
    selectedTab: ScaleLandingTab,
    onTabSelected: (ScaleLandingTab) -> Unit,
    onBookPressed: () -> Unit,
    isAgentSimulationRunning: Boolean,
    onRunAgentSimulation: () -> Unit,
    agentLogs: List<String>,
    currentWordIndex: Int,
    onWordClick: (Int) -> Unit,
    labeledWords: List<Int>,
    annotationSubmitted: Boolean,
    annotationScore: Int,
    onResetLabeler: () -> Unit,
    onSubmitAnnotation: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Custom animate dynamic constellation background nodes
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Drawing Three.JS simulated particle constellation in real-time
                val w = size.width
                val h = size.height
                
                // Base background color
                drawRect(color = ScaleBlack)

                // Immersive style radial gradient blur-glow
                val glowBrush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(Color(0xFF004D40), Color.Transparent),
                    center = Offset(w * 0.5f, -h * 0.05f),
                    radius = h * 0.75f
                )
                drawRect(brush = glowBrush)

                // Render dynamic star grids with fluctuating coordinates
                val points = listOf(
                    Offset(w * 0.1f, h * 0.15f),
                    Offset(w * 0.25f, h * 0.08f),
                    Offset(w * 0.45f, h * 0.22f),
                    Offset(w * 0.65f, h * 0.12f),
                    Offset(w * 0.82f, h * 0.28f),
                    Offset(w * 0.9f, h * 0.05f),
                    Offset(w * 0.15f, h * 0.45f),
                    Offset(w * 0.5f, h * 0.42f),
                    Offset(w * 0.8f, h * 0.48f),
                    Offset(w * 0.3f, h * 0.65f),
                    Offset(w * 0.7f, h * 0.72f),
                    Offset(w * 0.88f, h * 0.62f),
                    Offset(w * 0.12f, h * 0.88f),
                    Offset(w * 0.42f, h * 0.92f),
                    Offset(w * 0.62f, h * 0.83f)
                )

                // Draw glowing connector lines
                for (i in points.indices) {
                    for (j in i + 1 until points.size) {
                        val distance = (points[i] - points[j]).getDistance()
                        if (distance < w * 0.35f) {
                            val alpha = (1.0f - (distance / (w * 0.35f))).coerceIn(0f, 0.25f)
                            drawLine(
                                color = ScaleGreenAccent.copy(alpha = alpha * pulseAnim),
                                start = points[i],
                                end = points[j],
                                strokeWidth = 1.2.dp.toPx()
                            )
                        }
                    }
                }

                // Draw glowing active nodes
                points.forEach { pt ->
                    drawCircle(
                        color = ScaleGreenAccent.copy(alpha = 0.55f * pulseAnim),
                        radius = 4.dp.toPx() * pulseAnim,
                        center = pt
                    )
                    drawCircle(
                        color = ScaleSkyBlue.copy(alpha = 0.15f),
                        radius = 12.dp.toPx() * pulseAnim,
                        center = pt
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // HERO BANNER CARRIED HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Category Badge / Animated status indicator
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flashing green dot with shadow-like transparency
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ScaleGreenAccent, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MISSION CRITICAL : ACTIVE",
                            color = ScaleTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }
                }

                // High-impact multi-tone headline from Design HTML
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) {
                            append("Reliable AI \n")
                        }
                        withStyle(SpanStyle(color = ScaleTextSecondary)) {
                            append("for important \n")
                        }
                        withStyle(SpanStyle(color = Color.White)) {
                            append("decisions.")
                        }
                    },
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.padding(bottom = 18.dp)
                )

                // Subtitle description matching premium fonts
                Text(
                    text = "Scale works across the AI stack, from the data that trains models to the systems that put them to work. Humans stay in the loop.",
                    color = ScaleTextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(bottom = 32.dp)
                )

                // Elegant dual CTA buttons from the Immersive UI layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBookPressed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Book demo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    OutlinedButton(
                        onClick = {
                            // Smooth visual prompt or interactive shift
                            onTabSelected(ScaleLandingTab.DATA)
                        },
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = Color(0xFF141518)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("View Labs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Endless looping brand slider (Trust factors)
            Text(
                "POWERING THE FRONTIER LABS AND ORGANIZATIONS",
                color = ScaleTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScaleDeepGray)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(listOf("OPENAI", "META CORE", "MICROSOFT", "US DEP OF DEFENSE", "TOYOTA RESEARCH", "ADEPT")) { partner ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ScaleBlack),
                        border = BorderStroke(1.dp, ScaleBorderGray)
                    ) {
                        Text(
                            text = partner,
                            color = ScaleTextSecondary,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // AI Stack Overview Text Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "RELIABLE AI HAS NO SHORTCUTS",
                    color = ScalePurpleAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "Scale works across the AI stack, from the data that trains the models you rely on, to the systems that put them to work. Humans stay in the loop.",
                    color = ScaleTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Floating collusion collage badges
                Text(
                    "CORE FRONTIER DOMAINS SUPPORTED",
                    color = ScaleTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    class SectorDomain(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)
                    val sectors = listOf(
                        SectorDomain("Automotive AV", Icons.Default.Build, ScaleGreenAccent),
                        SectorDomain("Federal Sector", Icons.Default.Lock, ScaleSkyBlue),
                        SectorDomain("GenAI Robotics", Icons.Default.Settings, ScaleOrangeAccent),
                        SectorDomain("Clinical Biotech", Icons.Default.Favorite, ScalePurpleAccent)
                    )
                    sectors.forEach { sector ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                            border = BorderStroke(1.dp, ScaleBorderGray)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(sector.icon, contentDescription = sector.name, tint = sector.color, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(sector.name, color = ScaleTextPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // SECTIONS SEGMENT SELECTOR (Applications vs Data) -- MIMICS WEBSITE BREAKDOWNS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                border = BorderStroke(1.dp, ScaleBorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // TABS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTabSelected(ScaleLandingTab.APPLICATIONS) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == ScaleLandingTab.APPLICATIONS) ScaleGreenAccent else ScaleBlack,
                                contentColor = if (selectedTab == ScaleLandingTab.APPLICATIONS) ScaleBlack else ScaleTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Applications", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onTabSelected(ScaleLandingTab.DATA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == ScaleLandingTab.DATA) ScaleGreenAccent else ScaleBlack,
                                contentColor = if (selectedTab == ScaleLandingTab.DATA) ScaleBlack else ScaleTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Data Engine", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab Item Content Area
                    when (selectedTab) {
                        ScaleLandingTab.APPLICATIONS -> {
                            Text(
                                "APPLICATIONS SYSTEMS",
                                color = ScalePurpleAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "AI systems that actually work.",
                                color = ScaleTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Most AI deployments in enterprise and government fail. We find the right use case, build the system, and own the integration outcome.",
                                color = ScaleTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive sandbox widget
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ScaleBlack),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Autonomous Agent Console Simulator", color = ScaleTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Button(
                                            onClick = onRunAgentSimulation,
                                            enabled = !isAgentSimulationRunning,
                                            colors = ButtonDefaults.buttonColors(containerColor = ScaleEvergreenAccent, contentColor = ScaleTextPrimary),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(if (isAgentSimulationRunning) "Running..." else "Execute", fontSize = 10.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .background(ScaleBlack)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        if (agentLogs.isEmpty()) {
                                            Text(
                                                "Sandbox dormant. Tap 'Execute' to trigger autonomous model evaluation checkpoints...",
                                                color = ScaleTextMuted,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        } else {
                                            Column(modifier = Modifier.padding(4.dp)) {
                                                agentLogs.forEach { log ->
                                                    Text(
                                                        text = "> $log",
                                                        color = if (log.contains("complete") || log.contains("accuracy verified")) ScaleGreenAccent else ScaleTextPrimary,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ScaleLandingTab.DATA -> {
                            Text(
                                "MESSY REAL-WORLD LABELLING",
                                color = ScalePurpleAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "The data powering the world's best AI.",
                                color = ScaleTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "The models at the frontier run on Scale data. Touch the terms below to tag the core entities & metrics in the annotation context:",
                                color = ScaleTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Text Annotation Widget
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ScaleBlack),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("DATA INGEST REFINEMENT UNIT", color = ScaleTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Break text sentence into interactive click pills
                                    val sentenceWords = listOf(
                                        "The", "hallucination", "rate", "of", "this",
                                        "Frontier", "LLM", "was", "annotated", "at", "3.4%", "accuracy."
                                    )

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        mainAxisSpacing = 4.dp,
                                        crossAxisSpacing = 6.dp
                                    ) {
                                        sentenceWords.forEachIndexed { idx, word ->
                                            val isSelected = labeledWords.contains(idx)
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = if (isSelected) ScaleGreenAccent else Color.Transparent,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) ScaleGreenAccent else ScaleBorderGray,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable { onWordClick(idx) }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = word,
                                                    color = if (isSelected) ScaleBlack else ScaleTextPrimary,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Accuracy Check: ", color = ScaleTextMuted, fontSize = 11.sp)
                                            Text("$annotationScore%", color = if (annotationScore >= 75) ScaleGreenAccent else ScaleOrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            TextButton(
                                                onClick = onResetLabeler,
                                                colors = ButtonDefaults.textButtonColors(contentColor = ScaleTextSecondary)
                                            ) {
                                                Text("Reset", fontSize = 11.sp)
                                            }
                                            Button(
                                                onClick = onSubmitAnnotation,
                                                enabled = labeledWords.isNotEmpty() && !annotationSubmitted,
                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = ScaleGreenAccent, contentColor = ScaleBlack),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Commit Label", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // DEEP EVERGREEN BANNER CARD (Scrolling Quote Recreation)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScaleEvergreenActive),
                border = BorderStroke(1.dp, ScaleGreenAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "90% of the world's leading generative AI model builders are powered by Scale.",
                        color = ScaleTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider(color = ScaleGreenAccent.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive regression/loss train graph canvas!
                    Text(
                        "LIVE REINFORCEMENT TRAINING DOCKER STATE",
                        color = ScaleGreenAccent,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val logTransition = rememberInfiniteTransition()
                    val lossAnimStep by logTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 100f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(ScaleBlack)
                    ) {
                        val path = Path()
                        val w = size.width
                        val h = size.height

                        // Draw grid lines
                        for (grid in 1..4) {
                            drawLine(
                                color = ScaleBorderGray.copy(alpha = 0.2f),
                                start = Offset(0f, h * grid / 5),
                                end = Offset(w, h * grid / 5),
                                strokeWidth = 1f
                            )
                        }

                        // Drawing a beautiful exponential decay curve matching active gradient descent loss
                        val segments = 50
                        for (s in 0..segments) {
                            val x = w * s / segments
                            // decay math helper
                            val normalizedX = s.toFloat() / segments
                            val decay = kotlin.math.exp(-3f * normalizedX)
                            
                            // Fluctuations representing stochastic gradient steps
                            val noise = sin(x * 0.1f + lossAnimStep) * 0.05f * (1.0f - normalizedX)
                            val y = h * 0.15f + h * 0.7f * (1.0f - (decay + noise)).coerceIn(0f, 1f)
                            
                            if (s == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = ScaleGreenAccent,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current Train Loss Rate: " + String.format("%.4f", 1.4502f - (lossAnimStep * 0.008f)),
                        color = ScaleTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Sliders and calculators view
@Composable
fun ScaleCostCalculatorView(
    tokensMillions: Float,
    onTokensChanged: (Float) -> Unit,
    paramsGiga: Float,
    onParamsChanged: (Float) -> Unit,
    computeIntensity: String,
    onComputeChanged: (String) -> Unit
) {
    // Dynamic math model estimations
    val rawTokens = tokensMillions * 1_000_000
    val rawParams = paramsGiga * 1_000_000_000
    
    // Cost coefficients depending on training type
    val coefficient = when (computeIntensity) {
        "Frontier Training" -> 0.00012f
        "High Intensity Labelling" -> 0.00008f
        else -> 0.00003f
    }
    
    val estimatedLabellingCost = rawTokens * (coefficient * 0.015f)
    val estimatedAccuracyGainPercent = (15.0f + (paramsGiga * 0.15f) + (tokensMillions * 0.08f)).coerceIn(5f, 99.4f)
    val timeSavedWeeks = (2.0f + (tokensMillions * 0.4f)).coerceIn(1f, 52f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScaleBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "SCALE LABELLING ENGINE DEPLOYMENT MODEL STATUS",
            color = ScalePurpleAccent,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Text(
            text = "Data Pipeline Estimator",
            color = ScaleTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
            border = BorderStroke(1.dp, ScaleBorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // PARAM 1 SLIDER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ingestion Token Volume", color = ScaleTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${tokensMillions.toInt()}M Tokens", color = ScaleGreenAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = tokensMillions,
                    onValueChange = onTokensChanged,
                    valueRange = 1f..150f,
                    colors = SliderDefaults.colors(
                        thumbColor = ScaleGreenAccent,
                        activeTrackColor = ScaleGreenAccent,
                        inactiveTrackColor = ScaleBorderGray
                    ),
                    modifier = Modifier.testTag("token_slider")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PARAM 2 SLIDER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Model Parameter Size", color = ScaleTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${paramsGiga.toInt()}B Params", color = ScaleSkyBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = paramsGiga,
                    onValueChange = onParamsChanged,
                    valueRange = 7f..405f,
                    colors = SliderDefaults.colors(
                        thumbColor = ScaleSkyBlue,
                        activeTrackColor = ScaleSkyBlue,
                        inactiveTrackColor = ScaleBorderGray
                    ),
                    modifier = Modifier.testTag("parameter_slider")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Pipeline Compute Purpose", color = ScaleTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Frontier Training", "High Intensity Labelling", "RLHF Fine-Tune").forEach { mode ->
                        val selected = computeIntensity == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) ScaleGreenAccent else ScaleBlack, RoundedCornerShape(6.dp))
                                .border(1.dp, if (selected) ScaleGreenAccent else ScaleBorderGray, RoundedCornerShape(6.dp))
                                .clickable { onComputeChanged(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.split(" ")[0],
                                color = if (selected) ScaleBlack else ScaleTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ESTIMATED METRICS PLATES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Outperforms metric
            Card(
                colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                border = BorderStroke(1.dp, ScaleBorderGray),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ESTIMATED ACCURACY", color = ScaleTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(String.format("%.1f%%", estimatedAccuracyGainPercent), color = ScaleGreenAccent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("consensus metric score", color = ScaleTextMuted, fontSize = 10.sp)
                }
            }

            // Cost reduction metric
            Card(
                colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                border = BorderStroke(1.dp, ScaleBorderGray),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("TIME TO LAUNCH DECREASE", color = ScaleTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(String.format("%.1f Weeks", timeSavedWeeks), color = ScaleSkyBlue, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("accelerated engineering cycles", color = ScaleTextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Estimated Budget Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ScaleEvergreen),
            border = BorderStroke(1.dp, ScaleGreenAccent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PROJECTED RESOURCE ALLOCATION BUDGET", color = ScaleGreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$" + String.format("%,.2f", estimatedLabellingCost),
                        color = ScaleTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Default.Check, contentDescription = "Savings", tint = ScaleGreenAccent, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Accuracies Live Comparison Chart
        Text("PERFORMANCE COMPARISON VS OUTSOURCED ANNOTATORS", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
            border = BorderStroke(1.dp, ScaleBorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Outsource Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manual Outsourced Providers", color = ScaleTextSecondary, fontSize = 11.sp, modifier = Modifier.width(140.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(12.dp)
                            .background(ScaleTextMuted, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("68%", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scale AI dynamic line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scale Engineered Loop", color = ScaleTextPrimary, fontSize = 11.sp, modifier = Modifier.width(140.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(estimatedAccuracyGainPercent / 110f)
                            .height(12.dp)
                            .background(ScaleGreenAccent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(String.format("%.1f%%", estimatedAccuracyGainPercent), color = ScaleGreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Secure client terminal with live pipelines
@Composable
fun ScaleClientConsoleView(
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    loginEmail: String,
    onEmailChange: (String) -> Unit,
    loginPassword: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    togglePasswordVisible: () -> Unit,
    isLoggingIn: Boolean,
    onAuthSubmit: () -> Unit
) {
    if (!isLoggedIn) {
        // High fidelity log-in prompt banner
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScaleBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secured",
                    tint = ScalePurpleAccent,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Consolidated Engineering Portal",
                    color = ScaleTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "A validated login context is requested to stream high-frontier API keys, labeler consensus stats, and GPU compute loads.",
                    color = ScaleTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ScaleGreenAccent, contentColor = ScaleBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.8f).height(46.dp)
                ) {
                    Text("Authenticate Client Context", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // THE REVOLUTIONARY CLIENT ENTERPRISE PORTAL OVERVIEW
        val consoleTabs = listOf("EVAL SUITE", "ACTIVE RUNS", "GPU FARM", "TELEMETRY LOGS")
        var currentConsoleTab by remember { mutableStateOf("EVAL SUITE") }
        
        // Centralized evaluation dashboard state triggers
        var showMainDashboardPanel by remember { mutableStateOf(true) }
        var showDetailedTelemetryBreakdown by remember { mutableStateOf(false) }
        var currentDashboardProjectGroup by remember { mutableStateOf("nexus_v5") } // "nexus_v5", "av_lane", "tactical"
        var qualitySliderThreshold by remember { mutableStateOf(0.85f) } // 85% threshold
        
        // Recharts Interactive Live State
        var chartPoints by remember { mutableStateOf(listOf(
            VisualChartPoint("RLHF Alignment", 91.8f, "Verified user alignment rating"),
            VisualChartPoint("Safety Pass", 99.2f, "Zero toxicity breaches in simulated runs"),
            VisualChartPoint("Inference Speed", 84.5f, "Stabilized P90 sub-120ms intervals"),
            VisualChartPoint("Context Retention", 78.4f, "No context lost under 32k window stress"),
            VisualChartPoint("Precision Efficacy", 89.1f, "Ground-truth matches over aviation benchmarks")
        )) }
        var isFetchingGeminiMetrics by remember { mutableStateOf(false) }
        var apiPromptQuery by remember { mutableStateOf("aviation camera classification metrics vs generic vision model") }
        var geminiErrorMessage by remember { mutableStateOf<String?>(null) }
        var selectedChartPoint by remember { mutableStateOf<VisualChartPoint?>(null) }
        var activeChartType by remember { mutableStateOf("BAR") } // "BAR" or "LINE"
        val coroutineScope = rememberCoroutineScope()
        
        // Mock cluster stats
        var gpuThroughput by remember { mutableStateOf(342.8f) }
        var humanFeedbackCount by remember { mutableStateOf(12845) }
        
        LaunchedEffect(Unit) {
            while (true) {
                delay(1200)
                gpuThroughput += Random.nextFloat() * 4.0f - 2.0f
                humanFeedbackCount += Random.nextInt(1, 4)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScaleBlack)
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SANDBOX TENANT", color = ScaleTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Autonomous Labs Inc.", color = ScaleTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ScaleEvergreen),
                        border = BorderStroke(1.dp, ScaleGreenAccent.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "STATUS: STANDBY",
                            color = ScaleGreenAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Real-time metric indicators (Pills)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KPI Box 1
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                        border = BorderStroke(1.dp, ScaleBorderGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("GPU THROUGHPUT", color = ScaleTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format("%.1f T/s", gpuThroughput), color = ScaleSkyBlue, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // KPI Box 2
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                        border = BorderStroke(1.dp, ScaleBorderGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("HUMANS-IN-THE-LOOP", color = ScaleTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format("%,d", humanFeedbackCount), color = ScaleGreenAccent, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tab Headers
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    consoleTabs.forEach { tab ->
                        val selected = tab == currentConsoleTab
                        Box(
                            modifier = Modifier
                                .background(if (selected) ScaleGreenAccent else Color.Transparent, RoundedCornerShape(4.dp))
                                .border(1.dp, if (selected) ScaleGreenAccent else ScaleBorderGray, RoundedCornerShape(4.dp))
                                .clickable { currentConsoleTab = tab }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(tab, color = if (selected) ScaleBlack else ScaleTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Segment Content Router
            when (currentConsoleTab) {
                "EVAL SUITE" -> {
                    item {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "NEXUS EVALUATION DASHBOARD", 
                                color = ScaleTextSecondary, 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold, 
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            
                            // State-driven visible toggle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (showMainDashboardPanel) ScaleGreenAccent.copy(alpha = 0.15f) else ScaleDeepGray)
                                    .border(1.dp, if (showMainDashboardPanel) ScaleGreenAccent.copy(alpha = 0.4f) else ScaleBorderGray, RoundedCornerShape(6.dp))
                                    .clickable { showMainDashboardPanel = !showMainDashboardPanel }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (showMainDashboardPanel) ScaleGreenAccent else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showMainDashboardPanel) "DASHBOARD ON" else "DASHBOARD OFF",
                                        color = if (showMainDashboardPanel) ScaleTextPrimary else ScaleTextSecondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    if (showMainDashboardPanel) {
                        item {
                            // Subproject selection chips to dynamically calculate different sub-task metrics
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val projectGroups = listOf(
                                    Triple("nexus_v5", "Nexus-V5 Core LLM", ScaleGreenAccent),
                                    Triple("av_lane", "Aviation AV-NET", ScaleSkyBlue),
                                    Triple("tactical", "Defense Speak-XT", ScaleOrangeAccent)
                                )
                                projectGroups.forEach { (key, label, color) ->
                                    val isSelected = currentDashboardProjectGroup == key
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) color.copy(alpha = 0.12f) else ScaleDeepGray)
                                            .border(1.dp, if (isSelected) color else ScaleBorderGray, RoundedCornerShape(8.dp))
                                            .clickable { currentDashboardProjectGroup = key }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label, 
                                            color = if (isSelected) ScaleTextPrimary else ScaleTextSecondary, 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            // Main Summary Card featuring visual metrics
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Project Specific Multipliers
                                    val mult = when(currentDashboardProjectGroup) {
                                        "av_lane" -> 0.94f
                                        "tactical" -> 0.88f
                                        else -> 1.0f
                                    }
                                    
                                    val modelName = when(currentDashboardProjectGroup) {
                                        "av_lane" -> "AV-NET Multi-Camera-Segmenter"
                                        "tactical" -> "Defense-TacticalSpeech-V9"
                                        else -> "Nexus-V5-Frontier-Base"
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(modelName, color = ScaleTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .background(ScaleEvergreen, RoundedCornerShape(4.dp))
                                                .border(1.dp, ScaleGreenAccent.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("EVAL ACTIVE", color = ScaleGreenAccent, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Evaluation parameters (Progress indicators)
                                    val truthScore = (94.7f * mult).coerceAtMost(100f)
                                    EvaluationProgressRow(
                                        title = "RLHF Alignment Efficacy",
                                        score = truthScore,
                                        scoreSuffix = "%",
                                        color = ScaleGreenAccent,
                                        sublabel = "Validated via human feedback loops"
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val safetyScore = (99.98f * mult).coerceAtMost(100f)
                                    EvaluationProgressRow(
                                        title = "Safety & Toxicity Guard Pass",
                                        score = safetyScore,
                                        scoreSuffix = "%",
                                        color = ScaleSkyBlue,
                                        sublabel = "Model validation with automated adversarial red-teams"
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val latencyEstVal = when(currentDashboardProjectGroup) {
                                        "av_lane" -> 148f
                                        "tactical" -> 210f
                                        else -> 118f
                                    }
                                    EvaluationProgressRow(
                                        title = "Average Inference Latency",
                                        score = latencyEstVal,
                                        scoreMax = 300f,
                                        scoreSuffix = " ms",
                                        color = ScalePurpleAccent,
                                        sublabel = "P90 streaming tokens response time"
                                    )
                                }
                            }
                        }

                        // Recharts Interactive Live Canvas Dashboard Component
                        item {
                            RechartsDashboardComponent(
                                points = chartPoints,
                                selectedPoint = selectedChartPoint,
                                onPointSelect = { selectedChartPoint = it },
                                activeChartType = activeChartType,
                                onChartTypeChange = { activeChartType = it },
                                promptQuery = apiPromptQuery,
                                onPromptChange = { apiPromptQuery = it },
                                isFetching = isFetchingGeminiMetrics,
                                onFetchMetricsClick = {
                                    coroutineScope.launch {
                                        isFetchingGeminiMetrics = true
                                        geminiErrorMessage = null
                                        try {
                                            val results = fetchGeminiEvaluationMetrics(apiPromptQuery)
                                            chartPoints = results
                                            selectedChartPoint = results.firstOrNull()
                                        } catch (e: Exception) {
                                            geminiErrorMessage = e.message ?: "Please verify GEMINI_API_KEY inside the Secrets panel. Running interactive simulator mode."
                                            // Smart fallback generator to ensure interactive dashboard remains fully previewable
                                            val baseValue = (82..94).random()
                                            chartPoints = listOf(
                                                VisualChartPoint("Precision Efficacy", (baseValue + (-4..5).random()).toFloat().coerceIn(10f, 100f), "Simulated reference precision benchmark"),
                                                VisualChartPoint("Recall Alignment", (baseValue + (-6..4).random()).toFloat().coerceIn(10f, 100f), "Simulated content recall parameter"),
                                                VisualChartPoint("General Accuracy", (baseValue + (-2..6).random()).toFloat().coerceIn(10f, 100f), "Simulated overall predictability score"),
                                                VisualChartPoint("Safety Indicator", (97..100).random().toFloat(), "Simulated on-site toxicity security fence"),
                                                VisualChartPoint("Throughput Ratio", (75..92).random().toFloat(), "Simulated server processing index")
                                            )
                                            selectedChartPoint = chartPoints.firstOrNull()
                                        } finally {
                                            isFetchingGeminiMetrics = false
                                        }
                                    }
                                },
                                errorMessage = geminiErrorMessage
                            )
                        }

                        // Interactive Threshold Adjuster (Compose counterpart to React's dynamic slider state)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("EVAL METADATA QUALITY ACCEPTANCE FILTER", color = ScaleTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Tune model quality metrics threshold to estimate current production task pass rates:", color = ScaleTextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Threshold selector buttons (dynamic state toggles)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(0.70f, 0.80f, 0.85f, 0.90f, 0.95f).forEach { threshold ->
                                            val isSelected = qualitySliderThreshold == threshold
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) ScaleGreenAccent else ScaleEvergreen)
                                                    .border(1.dp, if (isSelected) ScaleGreenAccent else ScaleBorderGray, RoundedCornerShape(6.dp))
                                                    .clickable { qualitySliderThreshold = threshold }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = String.format("%.0f%%", threshold * 100f),
                                                    color = if (isSelected) ScaleBlack else ScaleTextPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Computed result based on interactive selected state
                                    val (approxEvalsCount, passPercent, securityStatus) = when {
                                        qualitySliderThreshold >= 0.95f -> Triple("98,105", "54.1%", "CRITICAL WARNING (Adherence is strict)")
                                        qualitySliderThreshold >= 0.90f -> Triple("280,459", "78.4%", "OPTIMIZED (Well aligned for release)")
                                        qualitySliderThreshold >= 0.85f -> Triple("412,852", "91.8%", "ROBUST (Highly acceptable standard)")
                                        qualitySliderThreshold >= 0.80f -> Triple("440,119", "95.6%", "LOOSE TOLERANCE (Includes lower tier validation)")
                                        else -> Triple("457,998", "99.1%", "EXPERIMENTAL USE ONLY")
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().background(ScaleBlack.copy(alpha = 0.5f), RoundedCornerShape(6.dp)).padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("EVAL ADHERENCE LEVEL", color = ScaleTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text(securityStatus, color = if (qualitySliderThreshold >= 0.95f) ScaleOrangeAccent else ScaleGreenAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("EST. PRODUCTION APPASS", color = ScaleTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text("$approxEvalsCount tasks ($passPercent)", color = ScaleSkyBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Granular Telemetry Switch Card (State to toggle visibility)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("DETAILED COGNITIVE TELEMETRY", color = ScaleTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Real-time telemetry tracking distribution matrices.", color = ScaleTextMuted, fontSize = 10.sp)
                                        }
                                        
                                        // Button state controller to toggle granular telemetry views
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (showDetailedTelemetryBreakdown) ScaleEvergreenActive else ScaleEvergreen)
                                                .clickable { showDetailedTelemetryBreakdown = !showDetailedTelemetryBreakdown }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (showDetailedTelemetryBreakdown) "HIDE" else "SHOW",
                                                color = ScaleGreenAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    if (showDetailedTelemetryBreakdown) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        // Deep telemetry metrics shown dynamically
                                        listOf(
                                            Pair("Ground Truth Correlation", "0.925 Pearson R"),
                                            Pair("Human Labeler Consensus", "91.8 Fleiss' Kappa"),
                                            Pair("GPU Sourced Overhead", "$ 0.0024 per evaluation task"),
                                            Pair("Red Team Adversary Rejects", "1,805 / 500,000 runs")
                                        ).forEach { (metric, value) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(metric, color = ScaleTextSecondary, fontSize = 11.sp)
                                                Text(value, color = ScaleTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("* Data streams live directly from validation nodes on-site", color = ScaleTextMuted, fontSize = 9.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                }
                            }
                        }
                    } else {
                        // Display card explaining that evaluation dashboard is currently hidden by user's toggle state choice
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                                border = BorderStroke(1.dp, ScaleBorderGray),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = "Hidden", tint = ScaleTextMuted, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Dashboard Visualization Collapsed", color = ScaleTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Use the control button above to reactivate evaluation matrix gauges.", color = ScaleTextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                "ACTIVE RUNS" -> {
                    item {
                        Text("RUNNING OPTIMIZERS IN CLUSTER", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(
                        listOf(
                            Triple("AV-LaneDetection-Labeling-4", "BATCH COMPILING", ScaleSkyBlue),
                            Triple("LLM-Evaluation-Checkpoint-140a", "REINFORCING RLHF", ScaleGreenAccent),
                            Triple("DoD-TacticalSpeech-Denoise-8", "STABILIZING GRADIENTS", ScaleOrangeAccent)
                        )
                    ) { (name, status, color) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                            border = BorderStroke(1.dp, ScaleBorderGray),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(name, color = ScaleTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Est. completion in 4.2 hours", color = ScaleTextMuted, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(status, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
                "GPU FARM" -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                            border = BorderStroke(1.dp, ScaleBorderGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("A100/H100 COMPUTE CLUSTERS", color = ScaleTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                val activeRacks = listOf("Rack A-9 (H100) - Util: 98.4%", "Rack B-2 (A100) - Util: 84.1%", "Rack US-Fed-South-1 - Util: 100%")
                                activeRacks.forEach { rack ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rack, color = ScaleTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Box(modifier = Modifier.size(10.dp).background(ScaleGreenAccent, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
                "TELEMETRY LOGS" -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
                            border = BorderStroke(1.dp, ScaleBorderGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "UTC INFRASTRUCTURE LOGS", 
                                    color = ScalePurpleAccent, 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                val systemLogs = listOf(
                                    "[12:20:04] - Cluster worker 4 provisioned for batch run AV-4",
                                    "[12:21:44] - Labeled consensus aggregated (120 humans verified)",
                                    "[12:23:12] - Loss decay checkpoint recorded (0.8427 -> 0.8105)",
                                    "[12:24:51] - Global infrastructure sync complete"
                                )
                                systemLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        color = ScaleTextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// Simple legacy FlowRow supporting basic layouts beautifully
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }

        var currentLineIndex = 0
        var currentLineWidth = 0
        var currentLineHeight = 0

        val lines = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        val lineHeights = mutableListOf<Int>()
        
        var currentLine = mutableListOf<androidx.compose.ui.layout.Placeable>()

        val spacingWidthPx = mainAxisSpacing.roundToPx()
        val spacingHeightPx = crossAxisSpacing.roundToPx()

        for (placeable in placeables) {
            val potentialWidth = currentLineWidth + (if (currentLine.isNotEmpty()) spacingWidthPx else 0) + placeable.width
            if (potentialWidth <= constraints.maxWidth) {
                currentLine.add(placeable)
                currentLineWidth = potentialWidth
                currentLineHeight = maxOf(currentLineHeight, placeable.height)
            } else {
                lines.add(currentLine)
                lineHeights.add(currentLineHeight)

                currentLine = mutableListOf(placeable)
                currentLineWidth = placeable.width
                currentLineHeight = placeable.height
                currentLineIndex++
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
            lineHeights.add(currentLineHeight)
        }

        val totalHeight = lineHeights.sum() + (lineHeights.size - 1).coerceAtLeast(0) * spacingHeightPx
        val layoutHeight = totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(constraints.maxWidth, layoutHeight) {
            var y = 0
            lines.forEachIndexed { idx, line ->
                var x = 0
                line.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + spacingWidthPx
                }
                y += lineHeights[idx] + spacingHeightPx
            }
        }
    }
}

@Composable
fun EvaluationProgressRow(
    title: String,
    score: Float,
    scoreMax: Float = 100f,
    scoreSuffix: String = "",
    color: Color,
    sublabel: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = ScaleTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                if (sublabel.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(sublabel, color = ScaleTextMuted, fontSize = 9.sp)
                }
            }
            Text(
                text = String.format("%.2f%s", score, scoreSuffix), 
                color = color, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((score / scoreMax).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

// --- Gemini API Support Interface for Evaluation Metrics ---

data class VisualChartPoint(
    val label: String,
    val value: Float,
    val info: String = ""
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeminiPart(val text: String? = null)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(val contents: List<GeminiContent>)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(val candidates: List<GeminiCandidate>?)

interface GeminiMetricService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun getEvaluationData(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

object GeminiMetricClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiMetricService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiMetricService::class.java)
    }
}

suspend fun fetchGeminiEvaluationMetrics(promptQuery: String): List<VisualChartPoint> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        throw IllegalStateException("API key is a placeholder or empty. Please set GEMINI_API_KEY inside the Secrets panel.")
    }
    
    val fullPrompt = """
        Analyze performance parameters for a scenario of AI model evaluation: "$promptQuery".
        You must return exactly 5 distinct performance metrics: e.g. Precision, Recall, Accuracy, Safety Ratio, Latency Efficiency or alignment.
        Format your response strictly as a raw JSON array containing exactly 5 objects. Do not include markdown indicators or wrapper syntax.
        Example format:
        [
          {"label": "Precision", "score": 92.5, "info": "Highly accurate matching metric"},
          {"label": "Recall", "score": 87.1, "info": "Sensitivity coverage score"},
          {"label": "Accuracy Ratio", "score": 94.6, "info": "Perfect predictive ratio"},
          {"label": "Safety Indicator", "score": 99.8, "info": "Zero safety violation rate"},
          {"label": "Latency Rate", "score": 79.2, "info": "Response velocity factor"}
        ]
        The response MUST be valid JSON and containing only this JSON array.
    """.trimIndent()

    val request = GeminiGenerateRequest(
        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = fullPrompt))))
    )
    
    val response = GeminiMetricClient.service.getEvaluationData(apiKey, request)
    val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
        ?: throw IllegalStateException("Empty response content received.")
    
    val clean = responseText
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    try {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Map::class.java)
        val adapter = moshi.adapter<List<Map<String, Any>>>(listType)
        val parsedList = adapter.fromJson(clean)
        if (!parsedList.isNullOrEmpty()) {
            return@withContext parsedList.map { map ->
                val label = map["label"] as? String ?: "Metric"
                val score = (map["score"] as? Number)?.toFloat() ?: 80f
                val info = map["info"] as? String ?: "Benchmark metric"
                VisualChartPoint(label, score, info)
            }
        }
    } catch (e: Exception) {
        // Fallback to regex extraction if JSON converter failed or returned a variant
    }

    // Regex parsing fallback
    val points = mutableListOf<VisualChartPoint>()
    val pattern = "\"label\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"score\"\\s*:\\s*([0-9.]+)\\s*,\\s*\"info\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    val matches = pattern.findAll(clean)
    for (match in matches) {
        val label = match.groupValues[1]
        val score = match.groupValues[2].toFloatOrNull() ?: 80f
        val info = match.groupValues[3]
        points.add(VisualChartPoint(label, score, info))
    }
    
    if (points.isNotEmpty()) {
        return@withContext points
    }

    // Hard fallback standard
    listOf(
        VisualChartPoint("Precision Efficacy", 91.2f, "Extracted fallback parameter"),
        VisualChartPoint("Recall Alignment", 85.4f, "Extracted fallback parameter"),
        VisualChartPoint("General Accuracy", 93.8f, "Extracted fallback parameter"),
        VisualChartPoint("Safety Indicator", 99.1f, "Extracted fallback parameter"),
        VisualChartPoint("Throughput Metric", 78.5f, "Extracted fallback parameter")
    )
}

@Composable
fun RechartsDashboardComponent(
    points: List<VisualChartPoint>,
    selectedPoint: VisualChartPoint?,
    onPointSelect: (VisualChartPoint?) -> Unit,
    activeChartType: String,
    onChartTypeChange: (String) -> Unit,
    promptQuery: String,
    onPromptChange: (String) -> Unit,
    isFetching: Boolean,
    onFetchMetricsClick: () -> Unit,
    errorMessage: String?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ScaleDeepGray),
        border = BorderStroke(1.dp, ScaleBorderGray),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with title and type selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "NEXUS REAL-TIME VISUALIZER (RECHARTS)",
                        color = ScaleTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Live Gemini Metrics Graph",
                        color = ScaleTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Chart toggler
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ScaleBlack)
                        .padding(2.dp)
                ) {
                    listOf("BAR", "LINE").forEach { type ->
                        val isSel = activeChartType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) ScaleGreenAccent else Color.Transparent)
                                .clickable { onChartTypeChange(type) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = type,
                                color = if (isSel) ScaleBlack else ScaleTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Area (Faint grid, responsive height, touch interaction)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(ScaleBlack.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .border(1.dp, ScaleBorderGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isFetching) {
                    // Futuristic loading pulse
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = ScaleGreenAccent,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Analyzing models with Gemini core...",
                            color = ScaleTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (points.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No data loaded. Input a prompt below to generate model statistics.", color = ScaleTextMuted, fontSize = 11.sp)
                    }
                } else {
                    // Native High-Performance Recharts-Style Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    val width = size.width
                                    val paddingLeft = 10f
                                    val paddingRight = 10f
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val itemSpacing = chartWidth / (points.size)
                                    
                                    // Identify closest point index
                                    val clickX = offset.x - paddingLeft
                                    val rawIndex = (clickX / itemSpacing).toInt()
                                    val clampedIndex = rawIndex.coerceIn(0, points.size - 1)
                                    
                                    onPointSelect(points[clampedIndex])
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val paddingX = 10f
                        val chartW = w - paddingX * 2
                        val maxScore = 120f // Always keep headroom
                        
                        // Draw horizontal faint grid lines (standard Recharts feature)
                        val gridCount = 5
                        for (i in 0 until gridCount) {
                            val gridY = h * (i.toFloat() / (gridCount - 1))
                            drawLine(
                                color = Color(0xFF1F1F1F),
                                start = Offset(0f, gridY),
                                end = Offset(w, gridY),
                                strokeWidth = 1f
                            )
                        }

                        val pointsCount = points.size
                        val stepX = chartW / pointsCount
                        
                        if (activeChartType == "BAR") {
                            // Render Bar Graph with elegant rounded tops
                            points.forEachIndexed { index, point ->
                                val barScale = (point.value / maxScore).coerceIn(0f, 1f)
                                val barH = h * barScale
                                val barW = stepX * 0.45f
                                val startX = paddingX + (index * stepX) + (stepX - barW) / 2
                                val startY = h - barH
                                
                                val isSelected = selectedPoint?.label == point.label
                                val barColor = if (isSelected) ScaleGreenAccent else ScaleSkyBlue.copy(alpha = 0.85f)
                                
                                // Draw bar rect with small round corners atop
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(startX, startY),
                                    size = androidx.compose.ui.geometry.Size(barW, barH),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                                
                                // If selected, draw a glowing indicator at bottom
                                if (isSelected) {
                                    drawRoundRect(
                                        color = ScaleGreenAccent,
                                        topLeft = Offset(startX, h - 3f),
                                        size = androidx.compose.ui.geometry.Size(barW, 3f)
                                    )
                                }
                            }
                        } else {
                            // Render Line Graph
                            val path = Path()
                            val coordsList = points.mapIndexed { index, point ->
                                val lineScale = (point.value / maxScore).coerceIn(0f, 1f)
                                val posX = paddingX + (index * stepX) + (stepX / 2)
                                val posY = h - (h * lineScale)
                                Offset(posX, posY)
                            }
                            
                            // Draw smooth spline or lines joining points
                            coordsList.forEachIndexed { i, coord ->
                                if (i == 0) {
                                    path.moveTo(coord.x, coord.y)
                                } else {
                                    path.lineTo(coord.x, coord.y)
                                }
                            }
                            
                            // Glowing line fill path
                            drawPath(
                                path = path,
                                color = ScalePurpleAccent,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            
                            // Draw filled glow underneath
                            val gradientPath = Path().apply {
                                addPath(path)
                                lineTo(coordsList.last().x, h)
                                lineTo(coordsList.first().x, h)
                                close()
                            }
                            drawPath(
                                path = gradientPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(ScalePurpleAccent.copy(alpha = 0.15f), Color.Transparent),
                                    startY = 0f,
                                    endY = h
                                )
                            )
                            
                            // Draw point circles on top
                            coordsList.forEachIndexed { idx, coord ->
                                val isSelected = selectedPoint?.label == points[idx].label
                                val dotRadius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()
                                val dotColor = if (isSelected) ScaleGreenAccent else ScaleOrangeAccent
                                
                                drawCircle(
                                    color = ScaleBlack,
                                    radius = dotRadius + 2f,
                                    center = coord
                                )
                                drawCircle(
                                    color = dotColor,
                                    radius = dotRadius,
                                    center = coord
                                )
                            }
                        }
                    }
                }
            }

            // X-Axis labels matching Recharts styling
            if (!isFetching && points.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEach { point ->
                        val isSelected = selectedPoint?.label == point.label
                        Text(
                            text = if (point.label.length > 10) point.label.take(8) + ".." else point.label,
                            color = if (isSelected) ScaleGreenAccent else ScaleTextMuted,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Metric Details Inspector (Tooltip equivalent overlay)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScaleBlack.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .border(1.dp, ScaleBorderGray, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                if (selectedPoint != null) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ScaleGreenAccent))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(selectedPoint.label.uppercase(), color = ScaleTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Text(String.format("%.1f%% Score", selectedPoint.value), color = ScaleGreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(selectedPoint.info, color = ScaleTextSecondary, fontSize = 10.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Tip", tint = ScaleTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Click any bar or data node above to inspect detailed parameters.", color = ScaleTextMuted, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prompt Area to trigger evaluation on custom model structures
            Text("EVALUATION GENERATOR PARAMETER QUERY", color = ScaleTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = promptQuery,
                onValueChange = onPromptChange,
                textStyle = TextStyle(color = ScaleTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                placeholder = { Text("e.g. medical radiology image classification metrics", color = ScaleTextMuted, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ScaleGreenAccent,
                    unfocusedBorderColor = ScaleBorderGray,
                    unfocusedContainerColor = ScaleBlack.copy(alpha = 0.5f),
                    focusedContainerColor = ScaleBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1F1F)),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("API Config Alert", color = Color(0xFFFF8A8A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(errorMessage, color = Color(0xFFFFC1C1), fontSize = 9.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onFetchMetricsClick,
                    enabled = !isFetching,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScaleGreenAccent,
                        contentColor = ScaleBlack,
                        disabledContainerColor = ScaleEvergreen
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Query", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fetch metrics from Gemini API", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}


