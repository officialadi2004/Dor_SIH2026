package com.artknower.app.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.artknower.app.R
import com.artknower.app.data.MockDataSource
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.ui.components.PosterHeader
import com.artknower.app.ui.theme.*
import kotlinx.coroutines.launch

enum class AuthTab {
    LOGIN, SIGNUP
}

private val AVAILABLE_ROLES = listOf("artisan", "business", "customer")
private val AVAILABLE_LANGUAGES = listOf(
    Pair("en", "English"),
    Pair("hi", "हिन्दी (Hindi)")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Validation string resources
    val errEnterCredentials = stringResource(R.string.err_enter_credentials)
    val errEnterName = stringResource(R.string.err_enter_name)
    val errEnterEmail = stringResource(R.string.err_enter_email)
    val errEnterPhone = stringResource(R.string.err_enter_phone)
    val errEnterPassword = stringResource(R.string.err_enter_password)
    val msgRegisteredSuccess = stringResource(R.string.msg_registered_success)
    val msgAccountExists = stringResource(R.string.msg_account_exists)

    // Login Form State
    var loginEmailOrPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Sign Up Form State
    var selectedRole by remember { mutableStateOf("artisan") }
    var selectedLanguage by remember { mutableStateOf("en") }
    var artisanName by remember { mutableStateOf("") }
    var artisanEmail by remember { mutableStateOf("") }
    var artisanPhone by remember { mutableStateOf("") }
    var artisanCityState by remember { mutableStateOf("") }
    var artisanCountry by remember { mutableStateOf("India") }
    var artisanCraft by remember { mutableStateOf("") }
    var experienceYearsText by remember { mutableStateOf("") }
    var artisanPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F3))
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_bg_pattern),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Section (Poster Header + Language Switcher Pill)
            Box(modifier = Modifier.fillMaxWidth()) {
                PosterHeader()

                // Language Switcher Pill
                val currentLocales = AppCompatDelegate.getApplicationLocales()
                val isHindi = !currentLocales.isEmpty && currentLocales[0]?.language == "hi"

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .width(72.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable {
                            val newLocale = if (isHindi) "en" else "hi"
                            selectedLanguage = newLocale
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                        }
                        .padding(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("EN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isHindi) DarkText else GrayText)
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("HI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isHindi) DarkText else GrayText)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(if (isHindi) Alignment.CenterEnd else Alignment.CenterStart)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RustTerracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isHindi) "हि" else "EN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. Animated Segmented Tab Selector
                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF9EFE6)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                                .background(if (currentTab == AuthTab.LOGIN) RustTerracotta else Color.Transparent)
                                .clickable { currentTab = AuthTab.LOGIN; errorMessage = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.auth_tab_login), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (currentTab == AuthTab.LOGIN) Color.White else Color(0xFF8B7355))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                                .background(if (currentTab == AuthTab.SIGNUP) RustTerracotta else Color.Transparent)
                                .clickable { currentTab = AuthTab.SIGNUP; errorMessage = null; successMessage = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.auth_tab_signup), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (currentTab == AuthTab.SIGNUP) Color.White else Color(0xFF8B7355))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (successMessage != null) {
                    Surface(color = PillGreen, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = TextGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(successMessage!!, color = TextGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (errorMessage != null) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        when (currentTab) {
                            AuthTab.LOGIN -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.login_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkText))
                                    Text(stringResource(R.string.login_subtitle), style = MaterialTheme.typography.bodySmall, color = GrayText)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = loginEmailOrPhone,
                                        onValueChange = { loginEmailOrPhone = it },
                                        placeholder = { Text(stringResource(R.string.login_identifier_hint), color = Color(0xFF9E8A78)) },
                                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF8B7355)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = Color(0xFFE8DAC8), focusedContainerColor = Color(0xFFF9F5F0), unfocusedContainerColor = Color(0xFFF9F5F0))
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = loginPassword,
                                        onValueChange = { loginPassword = it },
                                        placeholder = { Text(stringResource(R.string.login_password_hint), color = Color(0xFF9E8A78)) },
                                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF8B7355)) },
                                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = { IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) { Icon(if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle Password", tint = Color(0xFF8B7355)) } },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = Color(0xFFE8DAC8), focusedContainerColor = Color(0xFFF9F5F0), unfocusedContainerColor = Color(0xFFF9F5F0))
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        enabled = !isLoading,
                                        onClick = {
                                            if (loginEmailOrPhone.isNotBlank() && loginPassword.isNotBlank()) {
                                                isLoading = true
                                                errorMessage = null
                                                successMessage = null
                                                coroutineScope.launch {
                                                    val result = AuthRepository.loginArtisan(loginEmailOrPhone, loginPassword)
                                                    isLoading = false
                                                    result.onSuccess { artisan ->
                                                        MockDataSource.updateProfile(artisan.displayName, artisan.craftSummary ?: "", listOfNotNull(artisan.locationCity, artisan.locationState).joinToString(", "))
                                                        onLoginSuccess()
                                                    }.onFailure { err -> errorMessage = err.message ?: "Login failed." }
                                                }
                                            } else { errorMessage = errEnterCredentials }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta)
                                    ) {
                                        Text(if (isLoading) stringResource(R.string.login_loading) else stringResource(R.string.login_button), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (!isLoading) { Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                                    }
                                }
                            }
                            AuthTab.SIGNUP -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.signup_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkText))
                                    Text(stringResource(R.string.signup_subtitle), style = MaterialTheme.typography.bodySmall, color = GrayText)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(stringResource(R.string.signup_role_label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AVAILABLE_ROLES.forEach { role ->
                                            val isSelected = selectedRole == role
                                            val roleLabel = when (role) { "artisan" -> stringResource(R.string.role_artisan); "business" -> stringResource(R.string.role_business); "customer" -> stringResource(R.string.role_customer); else -> role.replaceFirstChar { it.uppercaseChar() } }
                                            FilterChip(selected = isSelected, onClick = { selectedRole = role }, label = { Text(roleLabel, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RustTerracotta, selectedLabelColor = Color.White))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(stringResource(R.string.signup_lang_label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        AVAILABLE_LANGUAGES.forEach { (code, label) ->
                                            val isSelected = selectedLanguage == code
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedLanguage = code; if (code == "hi" || code == "en") AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code)) },
                                                label = { Text(label.substringBefore(" "), fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RustTerracotta, selectedLabelColor = Color.White)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedTextField(value = artisanName, onValueChange = { artisanName = it }, label = { Text(if (selectedRole == "business") stringResource(R.string.signup_business_name_label) else stringResource(R.string.signup_name_label)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(value = artisanEmail, onValueChange = { artisanEmail = it }, label = { Text(stringResource(R.string.signup_email_label)) }, placeholder = { Text("e.g. artisan@gmail.com") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(value = artisanPhone, onValueChange = { artisanPhone = it }, label = { Text(stringResource(R.string.signup_phone_label)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.weight(2f)) { OutlinedTextField(value = artisanCityState, onValueChange = { artisanCityState = it }, label = { Text(stringResource(R.string.signup_city_state_label)) }, placeholder = { Text("Jaipur, Rajasthan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor)) }
                                        Box(modifier = Modifier.weight(1f)) { OutlinedTextField(value = artisanCountry, onValueChange = { artisanCountry = it }, label = { Text(stringResource(R.string.signup_country_label)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor)) }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.weight(2f)) { OutlinedTextField(value = artisanCraft, onValueChange = { artisanCraft = it }, label = { Text(if (selectedRole == "business") stringResource(R.string.signup_industry_label) else stringResource(R.string.signup_craft_label)) }, placeholder = { Text(if (selectedRole == "business") "e.g. Retail" else "e.g. Blue Pottery") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor)) }
                                        Box(modifier = Modifier.weight(1f)) { OutlinedTextField(value = experienceYearsText, onValueChange = { experienceYearsText = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.signup_exp_label)) }, placeholder = { Text("5") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor)) }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(value = artisanPassword, onValueChange = { artisanPassword = it }, label = { Text(stringResource(R.string.signup_password_label)) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RustTerracotta, unfocusedBorderColor = ThinBorderColor))
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        enabled = !isLoading,
                                        onClick = {
                                            if (artisanName.isBlank()) { errorMessage = errEnterName; return@Button }
                                            if (artisanEmail.isBlank() || !artisanEmail.contains("@")) { errorMessage = errEnterEmail; return@Button }
                                            if (artisanPhone.isBlank()) { errorMessage = errEnterPhone; return@Button }
                                            if (artisanPassword.length < 6) { errorMessage = errEnterPassword; return@Button }
                                            isLoading = true; errorMessage = null; successMessage = null
                                            coroutineScope.launch {
                                                val result = AuthRepository.registerArtisan(artisanName, artisanEmail, artisanPhone, artisanCityState, artisanCraft, artisanPassword, selectedRole, selectedLanguage, experienceYearsText.toIntOrNull(), artisanCountry)
                                                isLoading = false
                                                result.onSuccess {
                                                    MockDataSource.updateProfile(artisanName, artisanCraft, artisanCityState)
                                                    loginEmailOrPhone = artisanEmail
                                                    loginPassword = artisanPassword
                                                    successMessage = msgRegisteredSuccess
                                                    currentTab = AuthTab.LOGIN
                                                }.onFailure { err ->
                                                    val msg = err.message ?: ""
                                                    if (msg.contains("already registered", ignoreCase = true) || msg.contains("already exists", ignoreCase = true)) {
                                                        loginEmailOrPhone = artisanEmail
                                                        loginPassword = artisanPassword
                                                        successMessage = msgAccountExists
                                                        currentTab = AuthTab.LOGIN
                                                    } else { errorMessage = "Registration failed: $msg" }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RustTerracotta)
                                    ) {
                                        Text(if (isLoading) stringResource(R.string.signup_loading) else stringResource(R.string.signup_button), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (!isLoading) { Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
