package com.kulishov.tracker
//##################################################################################################
//##################################################################################################
//#####################                   Calendar screen                    #######################
//##################################################################################################
//####  Author:Kulishov Ilua                         ###############################################
//####  Version:0.0.0                                ###############################################
//####  Date:22.10.2024                              ###############################################
//##################################################################################################
//##################################################################################################

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

//=====================================================================================
//Calendar screen
//Input values:
//              navController:NavController - navController
//=====================================================================================
@Composable
fun calendarScreen(navController: NavController){
    var reallyTime by remember {
        mutableStateOf(System.currentTimeMillis()+10800000)
    }
    var reallyDay by remember {
     mutableStateOf(DateAndTimeS(0,0,0,0,0,0))
    }
    reallyDay.convertUnixTimeToDate1(reallyTime)
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()+10800000) }
    var currentData= DateAndTimeS(0,0,0,0,0,0)
    currentData.convertUnixTimeToDate1(currentTime)
    currentData.hour=0; currentData.minute=0; currentData.second=0
    val month = listOf("Январь","Февраль","март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь")
    val listState = rememberLazyListState()
    val kIndex = reallyDay.hour
    LaunchedEffect(Unit) {
        listState.scrollToItem(kIndex)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        LazyColumn(modifier = Modifier.padding(top=30.dp, bottom = 115.dp), state = listState) {
            items(24){
                hour->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)){
                    Box(modifier = Modifier
                        .padding(start = 30.dp)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ){

                        var calendarTask by remember {
                            mutableStateOf(emptyList<Task>())
                        }
                        var calendarTaskValue = false
                        for(x in listTask.value){
                            var dateTaskStart = DateAndTimeS(0,0,0,0,0,0)
                            var dateTaskEnd = DateAndTimeS(0,0,0,0,0,0)
                            dateTaskStart.convertUnixTimeToDate1(x.start*1000)
                            dateTaskEnd.convertUnixTimeToDate1(x.end*1000)
                            if(currentData.year>=dateTaskStart.year&&currentData.month>=dateTaskStart.month&&currentData.day>=dateTaskStart.day&&
                                currentData.year<=dateTaskEnd.year&&currentData.month<=dateTaskEnd.month&&currentData.day<=dateTaskEnd.day){
                                if(dateTaskStart.hour<=hour&&dateTaskEnd.hour>=hour){
                                    if(!calendarTask.contains(x)){
                                        calendarTask+=x
                                    }
                                    calendarTaskValue=true
                                }
                            }

                            if(calendarTaskValue){

                                var count = 0
                                LazyRow(Modifier.padding(start = 40.dp).fillMaxWidth()) {
                                    items(calendarTask) { item ->
                                        var startPadding = 0
                                        var dateTaskStart = DateAndTimeS(0, 0, 0, 0, 0, 0)
                                        var dateTaskEnd = DateAndTimeS(0, 0, 0, 0, 0, 0)
                                        dateTaskStart.convertUnixTimeToDate1(item.start * 1000)
                                        dateTaskEnd.convertUnixTimeToDate1(item.end * 1000)
                                        var topPadding = 0
                                        var bottomPadding = 0
                                        if (dateTaskStart.hour == hour) {
                                            topPadding = 50 * dateTaskStart.minute / 60
                                        }
                                        if (dateTaskEnd.hour == hour) {
                                            bottomPadding = 50 * (60 - dateTaskEnd.minute) / 60
                                        }
                                        for(x in listTask.value){
                                            if(x.start<=item.end&&x.end>=item.start&&x!=item){
                                                var xTaskEnd = DateAndTimeS(0, 0, 0, 0, 0, 0)
                                                xTaskEnd.convertUnixTimeToDate1(x.end*1000)
                                                if(xTaskEnd.hour<hour) startPadding+=100+count*100; count++

                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .padding(
                                                    top = topPadding.dp,
                                                    bottom = bottomPadding.dp,
                                                    start = startPadding.dp
                                                )
                                                .width(100.dp)
                                                .fillMaxHeight()
                                                .background(
                                                    color = parseColor(listGroup[item.groupID - 1].color),
                                                    shape = RoundedCornerShape(
                                                        if (dateTaskStart.hour == hour) 40 else 0,
                                                        if (dateTaskStart.hour == hour) 40 else 0,
                                                        if (dateTaskEnd.hour == hour) 40 else 0,
                                                        if (dateTaskEnd.hour == hour) 40 else 0
                                                    )
                                                )
                                                .clickable {
                                                    updateTaskTrun = item
                                                    islandState = 3
                                                    navController.navigate("tracker")
                                                }, contentAlignment = Alignment.Center
                                        ) {
                                            if (dateTaskStart.hour == hour && dateTaskStart.minute <= 40 || dateTaskStart.hour == hour - 1 && dateTaskStart.minute > 40) {
                                                Text(
                                                    text = item.name!!, style = TextStyle(
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.background
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                //calendarTask = emptyList()
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "$hour:00", style = TextStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ))
                                Box(modifier = Modifier
                                    .padding(start = 5.dp)
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(color = MaterialTheme.colorScheme.primary))
                            }

                        }
                        
                    }
                    if(reallyDay.hour==hour){
                        val dpTop = 50*(currentData.minute/60)
                        Row(
                            Modifier
                                .padding(end = 35.dp, top = dpTop.dp)
                                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd){
                                Box(modifier = Modifier
                                    .padding(end = 40.dp)
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(
                                        color = if (currentData.day == reallyDay.day && currentData.year == reallyDay.year
                                            && currentData.month == reallyDay.month
                                        ) Color(194, 6, 6) else Color(91, 91, 91)
                                    ))
                                Box(){
                                Text(
                                    text = "${
                                        String.format(
                                            "%d:%02d",
                                            reallyDay.hour,
                                            reallyDay.minute
                                        )
                                    }", style = TextStyle(
                                        fontSize = 16.sp,
                                        color =  if (currentData.day == reallyDay.day && currentData.year == reallyDay.year
                                            && currentData.month == reallyDay.month
                                        ) Color(194, 6, 6) else Color(91,91,91)
                                    )
                                )
                                    }
                            }
                        }
                    }

                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(115.dp), contentAlignment = Alignment.Center){
            Row(verticalAlignment = Alignment.CenterVertically){
                Icon(painter = painterResource(id = R.drawable.vectordown), contentDescription ="previous",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier
                        .padding(end = 20.dp)
                        .rotate(90f)
                        .clickable { currentTime -= 24 * 3600000 })
                Text(text = "${month[currentData.month-1]} ${currentData.day}", style = TextStyle(
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                ),modifier = Modifier.clickable {
                    currentTime=System.currentTimeMillis()+10800000
                })

                Icon(painter = painterResource(id = R.drawable.vectordown), contentDescription ="next",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier
                        .padding(start = 20.dp)
                        .rotate(270f)
                        .clickable { currentTime += 24 * 3600000 })
            }
        }
    }
}