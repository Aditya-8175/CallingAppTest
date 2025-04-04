    package com.aditya.callingfunctionality.screen.tabScreens


    import android.content.Intent
    import android.net.Uri
    import android.provider.CallLog
    import android.util.Log
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import androidx.compose.foundation.interaction.PressInteraction
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
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.itemsIndexed
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.material.ripple.rememberRipple
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.HorizontalDivider
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavController
    import coil.compose.AsyncImage
    import com.aditya.callingfunctionality.R
    import com.aditya.callingfunctionality.component.CallLogItem
    import com.aditya.callingfunctionality.component.FABContent
    import com.aditya.callingfunctionality.navigation.Routes
    import com.aditya.callingfunctionality.screen.tabScreens.recent.CallLogViewModel
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import java.text.SimpleDateFormat
    import java.util.Locale

    @Composable
    fun RecentScreens(
        viewModel: CallLogViewModel = viewModel(),
        navController: NavController,
        searchQuery: String
    ){

        val groupedLogs by viewModel.groupedLogs.collectAsState()
        Log.d("CallLog", "Grouped logs updated: $groupedLogs")



        val isDark = isSystemInDarkTheme()

        val filteredGroupedLogs = if (searchQuery.isNotEmpty()) {
            groupedLogs.mapValues { (_, logs) ->
                logs.filter { log ->
                    log.number.contains(searchQuery, ignoreCase = true) ||
                            log.name.contains(searchQuery, ignoreCase = true)
                }
            }.filterValues { it.isNotEmpty() }
        } else {
            groupedLogs
        }


        val numbersList = groupedLogs.values.flatten().map { it.number }.distinct()

        val context = LocalContext.current

        fun normalizePhoneNumber(phoneNumber: String): String {
            return phoneNumber.replace("^0+|^91", "").trim()
        }

        val normalizedNumbersList = numbersList.map { normalizePhoneNumber(it) }.distinct()





        //    if (groupedLogs.isEmpty()) {
        if (filteredGroupedLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()
                .background(color =  Color(0xFFFFFFFF)),
                contentAlignment = Alignment.Center
            ) {

                if (searchQuery.isNotEmpty()) {

                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        color = Color.Blue
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()
                .background(color =  Color(0xFFFFFFFF))
            ) {

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
                ){
    //                groupedLogs.forEach { (sectionTitle, logs) ->
                    filteredGroupedLogs.forEach { (sectionTitle, logs) ->

                        itemsIndexed(logs) { index, log ->
                            if (index == 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sectionTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if(isDark) Color.Black else Color.Black

                                    )
                                    Text(
                                        text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(log.date),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if(isDark) Color.Black else  Color(0xFFA0A0A0)

//                                        color = Color(0xFFA0A0A0)
                                    )
                                }
                            }
                            CalLogItem(log, navController)

                            HorizontalDivider(
                                modifier = Modifier.padding(top = 0.dp).fillMaxWidth(),
                                color = Color(0xFFE7E7E7)
                            )

                        }
                    }
                }

                FABContent(modifier = Modifier.align(Alignment.BottomEnd)){
                    navController.navigate(Routes.Dialer.routes)
                }
            }
        }
    }


    @Composable
    fun CalLogItem(log: CallLogItem, navController: NavController) {





        val context = LocalContext.current

        val type =  when (log.type) {
            CallLog.Calls.INCOMING_TYPE -> "Incoming"
            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            CallLog.Calls.MISSED_TYPE -> "Missed"
            else -> "Incoming"
        }

        val simIconRes = when (log.simId) {
            1 -> R.drawable.sim1
            2 -> R.drawable.sim2
            else -> R.drawable.sim1
        }


        val alph = log.name.split(" ").firstOrNull()?.firstOrNull()?.toString() ?: ""
        val fChar = alph.uppercase(Locale.ROOT)

        val backColor = when (fChar) {
            "A" -> Color.Red
            "B" -> Color.Blue
            "C" -> Color.Green

            else -> {
                Color.White
            }
        }
        val interaction = remember { MutableInteractionSource() }
        val isClick = remember { mutableStateOf(false) }

        LaunchedEffect(interaction) {
            interaction.interactions.collect { interaction ->
                if (interaction is PressInteraction.Press) {
                    isClick.value = true
                } else if (interaction is PressInteraction.Release) {
                    isClick.value = false
                }
            }
        }


        val interactionSource = remember { MutableInteractionSource() }
        val isPressed = remember { mutableStateOf(false) }

        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Press) {
                    isPressed.value = true
                } else if (interaction is PressInteraction.Release) {
                    isPressed.value = false
                }
            }
        }


        val backgroundColor = if (isPressed.value) Color.Red.copy(alpha = 0.1f) else Color.Transparent // Example



        Row(
            modifier = Modifier
                .fillMaxWidth()
    //            .background(backgroundColor)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center

        ) {

            Row(modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(color =
                    when(log.type) {
                        CallLog.Calls.MISSED_TYPE -> Color.Red.copy(alpha = 0.3f)
                        CallLog.Calls.INCOMING_TYPE -> Color.Green.copy(alpha = 0.3f)
                        CallLog.Calls.OUTGOING_TYPE -> Color.Blue.copy(alpha = 0.3f)
                        else -> Color.Green.copy(alpha = 0.3f)

                            } ),
    //                if (log.type == CallLog.Calls.MISSED_TYPE) Color.Red.copy(alpha = 0.3f)
    //                else Color.LightGray),
                    onClick = {

                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:${log.number}")
                        }
                        context.startActivity(intent)

                    }
                )

            )
            {

                Column(
    //                verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (log.photoUri != null) {
                            AsyncImage(
                                model = log.photoUri,
                                contentDescription = "Contact Image",
                                modifier = Modifier
                                    .fillMaxSize()
    //                            .size(55.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        }
                        else {

                            Image(
                                painter = painterResource(id = R.drawable.personal),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.background(color = Color(0xFFD3D7FF), shape = CircleShape)
                                    .padding(14.dp).fillMaxSize()
    //                            .size(50.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Center),
                                contentScale = ContentScale.Fit
                            )

                        }
                    }

                    Text(
                        text = "Personal",
                        color = Color(0xFF283593),
                        style = MaterialTheme.typography.labelSmall
                    )

                }


                Spacer(modifier = Modifier.width(12.dp))

    //        }
                Column(modifier = Modifier
    //                .weight(1f)
                    .padding(
    //                    end = 18.dp,
    //                    start = 18.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
    //                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                    ) {


                        if (log.name == "Unknown") {

                            Text(
                                text =log.number,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC6C6C6),
    //                            modifier = Modifier.weight(1f)
                            )
                        }
                        else {
                            val displayName = if (log.name.length > 20) {
                                log.name.substring(0, 20) + "..."
                            } else {
                                log.name
                            }

                            Text(
                                text = log.name,
                                maxLines = 1,
    //                            overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC6C6C6),
    //                            modifier = Modifier.weight(0.5f)
                            )

                        }
                            Image(
                                painter = painterResource(R.drawable.verify),
                                contentDescription = "Verified Status",
                                modifier = Modifier.size(16.dp) // Adjust size as needed
                            )
                        }
                    if (log.name.isNotEmpty()) {
                        Text(
                            text = log.name,
                            //                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (log.type == CallLog.Calls.MISSED_TYPE) Color.Red else Color.Black,

                            )
                    } else if(log.name == "Unknown") {
                        Text(
                            text = log.number,
                            style = MaterialTheme.typography.bodyLarge,
                            //                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = if (log.type == CallLog.Calls.MISSED_TYPE) Color.Red else Color.Black,

                            )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {

//                        Image(
//                            painter = painterResource(R.drawable.sim1),
//                            contentDescription = "Sim",
//                            modifier = Modifier
//                                .size(12.dp)
//                        )
                        Image(
                            painter = painterResource(simIconRes),
                            contentDescription = "Sim",
                            modifier = Modifier.size(12.dp)
                        )

                        Image(
                            painter = when (log.type) {
                                CallLog.Calls.INCOMING_TYPE -> painterResource(id = R.drawable.incoming)
                                CallLog.Calls.OUTGOING_TYPE -> painterResource(id = R.drawable.outgoing)
                                CallLog.Calls.MISSED_TYPE -> painterResource(id = R.drawable.missed)
                                else -> painterResource(id = R.drawable.incoming)
                            },
                            contentDescription = "Call Type",
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .align(Alignment.CenterVertically)
                                .size(12.dp)
                        )


                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodySmall,
                            //                        fontSize = 16.sp,
                            modifier = Modifier.padding(start = 5.dp),
                            color = if (log.type == CallLog.Calls.MISSED_TYPE) Color.Red else Color.Black,
                            //                        fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = SimpleDateFormat(
                                "hh:mm a",
                                Locale.getDefault()
                            ).format(log.date),
                            modifier = Modifier.padding(start = 5.dp),
                            style = MaterialTheme.typography.bodySmall,
                            //                            fontSize = 16.sp,
                            color = if (log.type == CallLog.Calls.MISSED_TYPE) Color.Red else Color.Black,
                            //                        fontWeight = FontWeight.SemiBold
                        )

                    }
                }




                }

            Image(
                painter = painterResource(R.drawable.details),
                contentDescription = "Info Icon",
                modifier = Modifier
                    .size(24.dp)

                    .clickable(
                        interactionSource = interaction,
                        indication = rememberRipple(color = Color.Gray.copy(alpha = 0.3f)),
                        onClick = {
                            navController.navigate(Routes.Details.routes)

                        }
                    )

            )
        }




        }


