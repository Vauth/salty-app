package com.vauth.salty

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.salty.data.MessageEntry
import com.vauth.salty.data.MessageType
import com.vauth.salty.ui.theme.*

class MainActivity : ComponentActivity() {
    private val vm: SaltyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaltyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SaltyApp(vm)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaltyApp(vm: SaltyViewModel) {
    var currentTab by remember { mutableStateOf(0) }
    var showHistory by remember { mutableStateOf(false) }
    
    val messageCount by vm.messageCount.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        DarkSurface
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            SaltyHeader(messageCount = messageCount)
            
            if (!showHistory) {
                // Tab Row with modern Material 3 styling
                TabRow(
                    selectedTabIndex = currentTab,
                    containerColor = DarkSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[currentTab])
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                            // Dynamic indicator color: Cyan for encode (tab 0), Green for decode (tab 1)
                            color = if (currentTab == 0) PrimaryCyan else SecondaryTeal,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        modifier = Modifier.clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                        text = { 
                            Text(
                                "ENCODE", 
                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (currentTab == 0) PrimaryCyan else LightGray
                            ) 
                        }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        modifier = Modifier.clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)),
                        text = { 
                            Text(
                                "DECODE",
                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (currentTab == 1) SecondaryTeal else LightGray
                            ) 
                        }
                    )
                }
                
                // Content
                when (currentTab) {
                    0 -> EncodeTab(vm)
                    1 -> DecodeTab(vm)
                }
            } else {
                // History View
                HistoryView(vm = vm, onBack = { showHistory = false })
            }
        }
        
        // Floating Action Button for History
        if (!showHistory) {
            FloatingActionButton(
                onClick = { showHistory = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = PrimaryCyan,
                contentColor = DarkBackground
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = "View History"
                )
            }
        }
    }
}

@Composable
fun SaltyHeader(messageCount: Int) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SALTY",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryCyan
                )
                Text(
                    text = "Secure Hash Encoder & Decoder",
                    fontSize = 13.sp,
                    color = LightGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            // GitHub icon link
            IconButton(
                onClick = { 
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vauth/salty-app"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Silently handle if no browser app is available
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Code,
                    contentDescription = "GitHub",
                    tint = LightGray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        if (messageCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .background(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Message,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$messageCount messages stored",
                    fontSize = 11.sp,
                    color = LightGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncodeTab(vm: SaltyViewModel) {
    var message by remember { mutableStateOf("") }
    var salt by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var encodedResult by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = PrimaryCyan,
                        focusedLabelColor = PrimaryCyan,
                        unfocusedLabelColor = LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Message input
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message to Encode *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = PrimaryCyan,
                        focusedLabelColor = PrimaryCyan,
                        unfocusedLabelColor = LightGray
                    ),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Key input
                OutlinedTextField(
                    value = salt,
                    onValueChange = { salt = it },
                    label = { Text("Key *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryPurple,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = SecondaryPurple,
                        focusedLabelColor = SecondaryPurple,
                        unfocusedLabelColor = LightGray
                    ),
                    supportingText = { 
                        Text(
                            "Share this key with the recipient",
                            fontSize = 11.sp,
                            color = LightGray
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Encode button
                Button(
                    onClick = {
                        if (message.isNotBlank() && salt.isNotBlank()) {
                            val result = vm.encodeMessage(
                                title = title.ifBlank { "Untitled" },
                                message = message,
                                salt = salt,
                                saveToHistory = true  // Always save to history
                            )
                            when (result) {
                                is OperationResult.Success -> {
                                    encodedResult = result.result
                                    showResult = true
                                }
                                is OperationResult.Error -> {
                                    // Show error to user
                                    encodedResult = ""
                                    showResult = false
                                    // TODO: Show error snackbar or dialog with result.message
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = message.isNotBlank() && salt.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ENCODE MESSAGE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        
        // Result Card
        AnimatedVisibility(
            visible = showResult && encodedResult.isNotBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Encoded Successfully",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCyan
                            )
                        }
                        IconButton(onClick = { showResult = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = LightGray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkBackground,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                text = encodedResult,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = PrimaryCyan,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Copy button uses PrimaryCyan background to match encode button styling
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(encodedResult))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCyan,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COPY TO CLIPBOARD")
                    }
                }
            }
        }
        
        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Info,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "The recipient needs the exact same key to decode your message. Share it securely through a different channel.",
                    fontSize = 12.sp,
                    color = LightGray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecodeTab(vm: SaltyViewModel) {
    var encodedMessage by remember { mutableStateOf("") }
    var salt by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var decodedResult by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal,
                        unfocusedLabelColor = LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Encoded message input
                OutlinedTextField(
                    value = encodedMessage,
                    onValueChange = { encodedMessage = it },
                    label = { Text("Encoded Message *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal,
                        unfocusedLabelColor = LightGray
                    ),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Key input
                OutlinedTextField(
                    value = salt,
                    onValueChange = { salt = it },
                    label = { Text("Key *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryPurple,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = SecondaryPurple,
                        focusedLabelColor = SecondaryPurple,
                        unfocusedLabelColor = LightGray
                    ),
                    supportingText = { 
                        Text(
                            "Enter the same key used for encoding",
                            fontSize = 11.sp,
                            color = LightGray
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Decode button
                Button(
                    onClick = {
                        if (encodedMessage.isNotBlank() && salt.isNotBlank()) {
                            val result = vm.decodeMessage(
                                title = title.ifBlank { "Untitled" },
                                encodedMessage = encodedMessage,
                                salt = salt,
                                saveToHistory = true  // Always save to history
                            )
                            when (result) {
                                is OperationResult.Success -> {
                                    decodedResult = result.result
                                    showResult = true
                                    showError = false
                                }
                                is OperationResult.Error -> {
                                    errorMessage = result.message
                                    showError = true
                                    showResult = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryTeal,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = encodedMessage.isNotBlank() && salt.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DECODE MESSAGE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        
        // Error Card
        AnimatedVisibility(
            visible = showError,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3F1F1F)),
                shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Decoding Failed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Error
                            )
                        }
                        IconButton(onClick = { showError = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = LightGray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = LightGray
                    )
                }
            }
        }
        
        // Result Card
        AnimatedVisibility(
            visible = showResult && decodedResult.isNotBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Decoded Successfully",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                        IconButton(onClick = { showResult = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = LightGray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkBackground,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                text = decodedResult,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                color = SecondaryTeal
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Copy button uses SecondaryTeal background to match decode button styling
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(decodedResult))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryTeal,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COPY TO CLIPBOARD")
                    }
                }
            }
        }
        
        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Info,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Make sure you're using the exact same key that was used to encode the message. Any difference will result in decoding failure.",
                    fontSize = 12.sp,
                    color = LightGray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryView(vm: SaltyViewModel, onBack: () -> Unit) {
    val messages by vm.messages.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    
    var showDeleteDialog by remember { mutableStateOf<MessageEntry?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with back button
        TopAppBar(
            title = { Text("Message History") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = White,
                navigationIconContentColor = PrimaryCyan
            )
        )
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { vm.updateSearchQuery(it) },
            placeholder = { Text("Search messages...", color = LightGray) },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = LightGray)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryCyan,
                unfocusedBorderColor = DarkSurfaceVariant,
                cursorColor = PrimaryCyan,
                focusedLeadingIconColor = PrimaryCyan,
                unfocusedLeadingIconColor = LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Messages list
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Message,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No messages yet",
                        fontSize = 18.sp,
                        color = LightGray
                    )
                    Text(
                        text = "Encode or decode messages to see them here",
                        fontSize = 14.sp,
                        color = DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageCard(
                        message = message,
                        onCopy = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        onDelete = { showDeleteDialog = message }
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { messageToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Message?") },
            text = { Text("Are you sure you want to delete this message from history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteMessage(messageToDelete)
                        showDeleteDialog = null
                    }
                ) {
                    Text("DELETE", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(
    message: MessageEntry,
    onCopy: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val typeColor = when (message.messageType) {
        MessageType.ENCODED -> EncodedColor
        MessageType.DECODED -> DecodedColor
        MessageType.BOTH -> ProcessingColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = typeColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = message.messageType.displayName,
                                fontSize = 11.sp,
                                color = typeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatTimestamp(message.createdAt),
                            fontSize = 11.sp,
                            color = DarkGray
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Error
                    )
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    
                    // Original Message
                    Text(
                        text = "Original Message:",
                        fontSize = 12.sp,
                        color = LightGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = message.originalMessage,
                                fontSize = 13.sp,
                                color = White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onCopy(message.originalMessage) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = PrimaryCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Encoded Message
                    Text(
                        text = "Encoded Message:",
                        fontSize = 12.sp,
                        color = LightGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = message.encodedMessage,
                                fontSize = 11.sp,
                                color = PrimaryCyan,
                                modifier = Modifier.weight(1f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            IconButton(
                                onClick = { onCopy(message.encodedMessage) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = PrimaryCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Key
                    Text(
                        text = "Key:",
                        fontSize = 12.sp,
                        color = LightGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = message.salt,
                                fontSize = 13.sp,
                                color = SecondaryPurple,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onCopy(message.salt) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = SecondaryPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    if (message.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Notes:",
                            fontSize = 12.sp,
                            color = LightGray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.notes,
                            fontSize = 13.sp,
                            color = LightGray
                        )
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> "${diff / 604800000}w ago"
    }
}
