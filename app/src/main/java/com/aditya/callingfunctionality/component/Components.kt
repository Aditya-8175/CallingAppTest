package com.aditya.callingfunctionality.component

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.aditya.callingfunctionality.R
import com.aditya.callingfunctionality.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SearchForm(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    hint: String = "Search names, number",
    onFilterClick: () -> Unit = {},
    onSearch: (String) -> Unit = {},
) {
    var showFilterScreen by remember { mutableStateOf(false) }
    val searchQueryState = rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val valid = remember(searchQueryState.value) {
        searchQueryState.value.trim().isNotEmpty()
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Search Bar with Filter Icon
            InputField(
                valueState = searchQueryState,
                labelId = hint,
                enabled = true,
//                onAction = KeyboardActions {
//                    if (!valid) return@KeyboardActions
//                    onSearch(searchQueryState.value.trim())
//                    searchQueryState.value = ""
//                    keyboardController?.hide()
//                },
                onAction = KeyboardActions(
                    onDone = {
                        if (!valid) return@KeyboardActions
                        onSearch(searchQueryState.value.trim())
                        searchQueryState.value = ""
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            delay(100)
                            keyboardController?.hide()
                        }
                    }
                ),
                onFilterClick = onFilterClick,
//                onFilterClick = { showFilterScreen = !showFilterScreen },
                // Add the onValueChange behavior here:
                onValueChange = { newValue ->
                    searchQueryState.value = newValue
                    onSearch(newValue.trim())
                }

            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    modifier: Modifier = Modifier,
    valueState: MutableState<String>,
    labelId: String,
    enabled: Boolean = false,
    isSingleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
//    imeAction: ImeAction = ImeAction.Next,
    imeAction: ImeAction = ImeAction.Done,
    onAction: KeyboardActions = KeyboardActions.Default,
    onSearchClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,

    // Pass the new onValueChange lambda here
    onValueChange: (String) -> Unit = {}
) {


    val isDark = isSystemInDarkTheme()

    val context = LocalContext.current
    OutlinedTextField(value = valueState.value,
//        onValueChange = { valueState.value = it },
        onValueChange = { newValue ->
            // Update state and trigger callback immediately
            onValueChange(newValue)
        },

//        label = { Text(text = labelId, style = MaterialTheme.typography.labelLarge,
        placeholder = { Text(text = labelId, style = MaterialTheme.typography.labelLarge,

            color = if(isDark) Color.Black else Color(0xFFC6C6C6)

//                    color = Color(0xFFA6A6A6)

            ,
            overflow = TextOverflow.Ellipsis ) },
        singleLine = isSingleLine,
        shape =  RoundedCornerShape(12.dp),
        textStyle = TextStyle(
            color = Color.Black,
            fontSize = 19.sp
        ),
//        colors = TextFieldDefaults.outlinedTextFieldColors(
//            focusedBorderColor = Color(0xFF5864F8),
//            unfocusedBorderColor = Color(0xFFDDDDDD),
//        ),
        modifier = modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp, top = 5.dp)
            .fillMaxWidth()
//            .border(12.dp, Color(0xFFDDDDDD), RoundedCornerShape(12.dp))
        ,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = onAction,

        trailingIcon = {
            val keyboardController = LocalSoftwareKeyboardController.current
 
            Row(
                modifier = Modifier.padding(end = 6.dp),
                horizontalArrangement = Arrangement.End) {
//                IconButton(onClick = { }) {
                if (valueState.value.isNotEmpty()) {
                    Image(
                        painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Clear Text",
                        modifier = Modifier.size(22.dp).clickable {
                            valueState.value = ""
                            onValueChange("")
                            keyboardController?.hide()
                        },
                        colorFilter = ColorFilter.tint(Color.Gray)
                    )
                    Spacer(Modifier.width(12.dp))
                }
                else{
                Image(
                    painterResource(R.drawable.filter),
                    contentDescription = "Filter", modifier = Modifier.size(22.dp)
//                        .padding(end = 3.dp)
//                        .clickable {
//                            onFilterClick?.invoke()
//                        },
                        .clickable {
                            onFilterClick?.invoke()
                        }
                )
//                }
//                IconButton(onClick = { }) {
                Spacer(Modifier.width(12.dp))
                Image(
                    painterResource(R.drawable.addcontact),
                    contentDescription = "Add Contact",
                    modifier = Modifier.size(24.dp).clickable {
                        val addContactIntent = Intent(Intent.ACTION_INSERT).apply {
                            type = "vnd.android.cursor.dir/contact"
                            putExtra("phone", "")
                        }
                        context.startActivity(addContactIntent)
                    }
//                        modifier = Modifier.border(1.dp, Color.Blue, shape = CircleShape),

                )
                }
            }
        },
        leadingIcon = {
//            IconButton(onClick = { onSearchClick?.invoke() }) {
            Icon(
                painterResource(R.drawable.search),
                modifier = Modifier.size(22.dp),
                contentDescription = "Search Contact",
                tint = Color(0xFFA6A6A6)
            )
//            }

        }
    )

}

@Composable
fun FABContent( modifier: Modifier, onTap: () -> Unit) {
    FloatingActionButton(
        onClick = { onTap() },
        shape = CircleShape,
//        RoundedCornerShape(50.dp),
        containerColor = Color(0xFF5864F8),
        modifier = modifier.padding(16.dp)
    ) {

        Icon(
            painter = painterResource(R.drawable.dialpad),
            contentDescription = "DialPad",
            tint = Color.White
        )

    }

}


@Composable
fun ConnectLogo(modifier: Modifier = Modifier, navController: NavHostController) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()

//            .padding(start = 16.dp, top = 34.dp)
    ) {

        Row(modifier = Modifier
            .weight(1f)) {
            Image(
                painter = painterResource(R.drawable.contactlogoreal),
                contentDescription = "connect Logo",
                modifier = Modifier.padding(start = 16.dp, top = 34.dp)
            )
            Text(
                text = "Phone",
                fontWeight = FontWeight.Bold, fontSize = 26.sp,
                color = Color(0xFF5864F8),
                modifier = Modifier.padding(top = 34.dp)
            )
        }

        // Profile Icon Button

            IconButton(
                modifier = Modifier.padding(top = 34.dp),
                onClick = {

            }) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                            contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(R.drawable.profile), contentDescription = null, tint = Color.White)
                }
            }

    }
}

@Composable
fun DisplayMessage(
    message: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Gray
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}



