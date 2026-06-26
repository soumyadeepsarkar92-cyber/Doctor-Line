package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor: Color,
    unfocusedBorderColor: Color,
    textColor: Color
): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RazorpayCheckoutDialog(
    visible: Boolean,
    amount: Double,
    orderId: String,
    email: String,
    onSuccess: (paymentId: String, signature: String, method: String) -> Unit,
    onFailure: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val scope = rememberCoroutineScope()
    var selectedMethod by remember { mutableStateOf("upi") } // "upi", "card", "netbanking", "wallet"
    var paymentStep by remember { mutableStateOf(1) } // 1 = Instrument Details, 2 = Loading Spinner, 3 = Success, 4 = Failed
    var loadingText by remember { mutableStateOf("Initializing Secure SDK...") }
    
    // Instrument Specific Inputs
    var upiId by remember { mutableStateOf("") }
    var upiVerified by remember { mutableStateOf(false) }
    
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    
    var selectedBank by remember { mutableStateOf("SBI") }
    var selectedWallet by remember { mutableStateOf("Paytm") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Auto fill helpers for demo purposes
    fun fillDemoDetails() {
        upiId = "doctorline@okaxis"
        upiVerified = true
        cardNumber = "4312 9081 2234 5678"
        cardExpiry = "12/29"
        cardCvv = "999"
        cardName = "SaaS Pharmacy Inc"
    }

    LaunchedEffect(Unit) {
        fillDemoDetails()
    }

    val razorpayDarkTheme = Color(0xFF0F172A)
    val razorpayBlue = Color(0xFF7C5DFA)
    val razorpayGreen = Color(0xFF10B981)
    val dividerColor = Color(0xFF334155)

    Dialog(
        onDismissRequest = { 
            if (paymentStep == 1) {
                onDismiss() 
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = razorpayDarkTheme
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF7C5DFA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "Razorpay SECURE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Order: $orderId",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (paymentStep == 1) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Checkout",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 12.dp))

                if (paymentStep == 1) {
                    // Main checkout step
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Amount Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "SECURE BILLING AMOUNT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "₹${String.format("%,.2f", amount)}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "TEST MODE",
                                            color = Color(0xFF15803D),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Billed to: $email",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Choose Instrument Tab Bar
                        Text(
                            text = "SELECT PAYMENT INSTRUMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), shape = RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val tabs = listOf(
                                Triple("upi", "UPI", Icons.Default.QrCode),
                                Triple("card", "Cards", Icons.Default.CreditCard),
                                Triple("netbanking", "Net Bank", Icons.Default.AccountBalance),
                                Triple("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
                            )
                            tabs.forEach { (id, label, icon) ->
                                val selected = selectedMethod == id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) razorpayBlue else Color.Transparent)
                                        .clickable { selectedMethod = id }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (selected) Color.White else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            color = if (selected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sub-form for selected payment method
                        when (selectedMethod) {
                            "upi" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Pay via UPI App",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    
                                    OutlinedTextField(
                                        value = upiId,
                                        onValueChange = { upiId = it; upiVerified = false },
                                        placeholder = { Text("Enter UPI ID (e.g. user@okaxis)", color = Color(0xFF64748B)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = razorpayBlue,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            textColor = Color.White
                                        ),
                                        trailingIcon = {
                                            TextButton(
                                                onClick = { if (upiId.contains("@")) upiVerified = true },
                                                enabled = upiId.contains("@") && !upiVerified
                                            ) {
                                                Text(
                                                    text = if (upiVerified) "Verified ✓" else "Verify",
                                                    color = if (upiVerified) razorpayGreen else razorpayBlue,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("Google Pay", "PhonePe", "Paytm", "BHIM").forEach { app ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                                    .clickable { 
                                                        upiId = "doctorline@$app".lowercase().replace(" ", "")
                                                        upiVerified = true
                                                    }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(app, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }
                            "card" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Enter Card Details",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    
                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { cardNumber = it },
                                        placeholder = { Text("Card Number", color = Color(0xFF64748B)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = razorpayBlue,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            textColor = Color.White
                                        ),
                                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF64748B)) }
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { cardExpiry = it },
                                            placeholder = { Text("MM/YY", color = Color(0xFF64748B)) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = razorpayBlue,
                                                unfocusedBorderColor = Color(0xFF334155),
                                                textColor = Color.White
                                            )
                                        )

                                        OutlinedTextField(
                                            value = cardCvv,
                                            onValueChange = { cardCvv = it },
                                            placeholder = { Text("CVV", color = Color(0xFF64748B)) },
                                            singleLine = true,
                                            visualTransformation = PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = razorpayBlue,
                                                unfocusedBorderColor = Color(0xFF334155),
                                                textColor = Color.White
                                            )
                                        )
                                    }

                                    OutlinedTextField(
                                        value = cardName,
                                        onValueChange = { cardName = it },
                                        placeholder = { Text("Card Holder Name", color = Color(0xFF64748B)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = razorpayBlue,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            textColor = Color.White
                                        )
                                    )
                                }
                            }
                            "netbanking" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Select Net Banking Provider",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    
                                    val banks = listOf("SBI", "HDFC", "ICICI", "Axis", "Kotak", "Yes Bank")
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        mainAxisSpacing = 8.dp,
                                        crossAxisSpacing = 8.dp
                                    ) {
                                        banks.forEach { bank ->
                                            val selected = selectedBank == bank
                                            Box(
                                                modifier = Modifier
                                                    .border(
                                                        1.dp,
                                                        if (selected) razorpayBlue else Color(0xFF334155),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .background(if (selected) razorpayBlue.copy(alpha = 0.15f) else Color.Transparent)
                                                    .clickable { selectedBank = bank }
                                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = bank,
                                                    color = if (selected) razorpayBlue else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "wallet" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Select Mobile Wallet",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    
                                    val wallets = listOf("Paytm", "PhonePe", "Amazon Pay", "Mobikwik")
                                    wallets.forEach { wallet ->
                                        val selected = selectedWallet == wallet
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    if (selected) razorpayBlue else Color(0xFF334155),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedWallet = wallet }
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF64748B))
                                                Text(wallet, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            RadioButton(
                                                selected = selected,
                                                onClick = { selectedWallet = wallet },
                                                colors = RadioButtonDefaults.colors(selectedColor = razorpayBlue, unselectedColor = Color(0xFF64748B))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(errorMessage ?: "", color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Interactive Testing Bar
                        Text(
                            text = "DEVELOPER SIMULATION CONTROLS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { fillDemoDetails() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                                modifier = Modifier.weight(1f).height(36.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Fill Demo", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    paymentStep = 2
                                    scope.launch {
                                        loadingText = "Simulating Payment Interruption..."
                                        delay(1500)
                                        errorMessage = "Transaction declined by issuing bank (Code: SEC-304)."
                                        paymentStep = 1
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                modifier = Modifier.weight(1f).height(36.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Force Fail", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    paymentStep = 2
                                    scope.launch {
                                        loadingText = "Simulating User Dismissal..."
                                        delay(1000)
                                        onFailure("User Cancelled Checkout")
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                modifier = Modifier.weight(1f).height(36.dp),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Force Cancel", fontSize = 11.sp)
                            }
                        }
                    }

                    // Confirm Payment Button
                    Button(
                        onClick = {
                            errorMessage = null
                            if (selectedMethod == "upi" && !upiId.contains("@")) {
                                errorMessage = "Please enter a valid UPI ID (e.g. user@bank)."
                                return@Button
                            }
                            if (selectedMethod == "card" && (cardNumber.length < 12 || cardExpiry.length < 4 || cardCvv.length < 3)) {
                                errorMessage = "Please enter complete credit card details."
                                return@Button
                            }

                            paymentStep = 2
                            scope.launch {
                                loadingText = "Securing transaction tunnel..."
                                delay(1200)
                                loadingText = "Authorizing payment instrument via bank..."
                                delay(1200)
                                loadingText = "Verifying secure payment signature..."
                                delay(1000)
                                
                                val generatedPaymentId = "pay_" + UUID.randomUUID().toString().replace("-", "").take(14)
                                val generatedSignature = "sig_" + UUID.randomUUID().toString().replace("-", "").take(20)
                                val finalMethod = when (selectedMethod) {
                                    "upi" -> "UPI ($upiId)"
                                    "card" -> "Card (ending ${cardNumber.takeLast(4).ifBlank { "5678" }})"
                                    "netbanking" -> "Net Banking ($selectedBank)"
                                    else -> "Wallet ($selectedWallet)"
                                }
                                
                                onSuccess(generatedPaymentId, generatedSignature, finalMethod)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = razorpayBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAY ₹${String.format("%,.0f", amount)} SECURELY",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else if (paymentStep == 2) {
                    // Loading State
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = razorpayBlue, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = loadingText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Do not press Back or Close this dialog window.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Super lightweight FlowRow implementation for supporting chips
 */
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val mainSpacingPx = mainAxisSpacing.roundToPx()
        val crossSpacingPx = crossAxisSpacing.roundToPx()

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0

        placeables.forEach { placeable ->
            if (currentWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + mainSpacingPx
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        var totalHeight = 0
        rows.forEachIndexed { index, row ->
            val rowHeight = row.maxOf { it.height }
            totalHeight += rowHeight
            if (index < rows.size - 1) {
                totalHeight += crossSpacingPx
            }
        }

        layout(constraints.maxWidth, totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)) {
            var currentY = 0
            rows.forEach { row ->
                val rowHeight = row.maxOf { it.height }
                var currentX = 0
                row.forEach { placeable ->
                    placeable.place(currentX, currentY)
                    currentX += placeable.width + mainSpacingPx
                }
                currentY += rowHeight + crossSpacingPx
            }
        }
    }
}
