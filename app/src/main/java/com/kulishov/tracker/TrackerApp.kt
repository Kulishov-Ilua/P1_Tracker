//#####################################################################################################################
//#####################################################################################################################
//###############################                    Экран трекера                      ###############################
//#####################################################################################################################
//####   Автор: Кулишов Илья Вячеславович     #########################################################################
//####   Версия: v.0.0.1                      #########################################################################
//####   Дата: 01.09.2024                     #########################################################################
//#####################################################################################################################
//#####################################################################################################################


package com.kulishov.tracker

import android.os.Build
import android.util.Log
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import kotlin.system.measureTimeMillis

var islandState by  mutableIntStateOf(0)
var trunTime by  mutableLongStateOf(0L)
var actualGroupUID = -1

var updateGroup by mutableStateOf(false)
var updateGroupValue = GroupTask(null,"","")


var deleteGroup by mutableStateOf(false)
var deleteTask by mutableStateOf(false)
var chooseGroup by mutableStateOf(false)

var updateTask by mutableStateOf(false)
var updateListTask by mutableStateOf(false)
var updateTaskValue = Task(null,"",0L,0L,0)


//=====================================================================================
//Task card
//Input values:
//              task:TaskView - task
//=====================================================================================
@Composable
fun taskCardTracker(task: TaskView){
    Box(
        Modifier
            .fillMaxWidth()
            .height(70.dp)
        , contentAlignment = Alignment.CenterStart){
        Row(Modifier.padding(start = 15.dp, end=15.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .width(5.dp)
                .height(50.dp)
                .background(color = task.color, shape = RoundedCornerShape(4)))
            Column(Modifier.padding(start=10.dp)) {
                Text(text = task.name, style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ))
                Text(text = "${task.start.hour}:${task.start.minute} -> ${task.end.hour}:${task.end.minute}", style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                ))
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd){
                Text(text = task.time, style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ))
            }
        }
    }
}




//=====================================================================================
//Остров трекера
//=====================================================================================
var emptyCreateFlag by mutableStateOf(false)
var updateTaskTrun by mutableStateOf(Task(null,"",0,0,-1))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun magicIsland() {
    //транспорт для создания задачи
    var trunDate = remember { mutableStateOf(0L) }
    var trunDate2= remember { mutableStateOf(0L) }
    var trunHour= remember { mutableStateOf(0) }
    var trunHour2= remember { mutableStateOf(0) }
    var trunMin= remember { mutableStateOf(0) }
    var trunMin2= remember { mutableStateOf(0) }

    var currentTime by remember { mutableStateOf(0L) }
    var timerStartTime = remember {
        mutableStateOf(0L)
    }
    //флаг запуска трекера
    var isTrackerRunning by remember { mutableStateOf(false) }
    //анимация часов вуменьшенном острове
    val alphaMiniClock by animateFloatAsState(targetValue =
    if(isTrackerRunning&& islandState==1) 1f else 0f,
        animationSpec = tween(delayMillis = if(isTrackerRunning&& islandState==1&&currentTime>1000L) 400 else 0,
            durationMillis = 300), label = ""
    )
    //анимация выключения трекера
    val alphaTracker by animateFloatAsState(targetValue =
    if(islandState>1) 0f else 1f,
        animationSpec = tween(
            delayMillis = if(islandState<2) 400 else 0,
            durationMillis = 300), label = ""
    )
    //анимация выключения выбора в создании
    val alphaChooseCreate by animateFloatAsState(targetValue =
    if(islandState==2) 1f else 0f,
        animationSpec = tween(
            delayMillis = if(islandState==2) 300 else 0,
            durationMillis = 400), label = ""
    )

    //анимация выключения создания группы
    val alphaCreateGroup by animateFloatAsState(targetValue =
    if(islandState==4|| islandState==5) 1f else 0f,
        animationSpec = tween(
            delayMillis = if(islandState==4|| islandState==5) 400 else 0,
            durationMillis = 400), label = ""
    )
    //анимация выключения создания задачи
    val alphaCreateTask by animateFloatAsState(targetValue =
    if(islandState==3|| islandState==6) 1f else 0f,
        animationSpec = tween(
            delayMillis = if(islandState==3|| islandState ==6) 400 else 0,
            durationMillis = 400), label = ""
    )

    //анимация исчезновения кнопки создать
    val alphaCreate by animateFloatAsState(targetValue =
    if (!isTrackerRunning) 1F else 0F,
        animationSpec = tween(delayMillis = if(islandState==0&&!isTrackerRunning)300 else 0, durationMillis = 50), label = "")
    //Анимация изменения размера кнопки стоп
    val stopDp  by animateDpAsState(targetValue =
    if(islandState==0&&isTrackerRunning) 280.dp else 110.dp,
        animationSpec = tween( durationMillis = 400), label = ""
    )
    //анимация исчезновения часов, когда трекер уменьшается
    val alphaClock by animateFloatAsState(targetValue =
    if(islandState==0) 1F else 0F,
        animationSpec = tween( delayMillis = if(islandState==0) 450 else 0, durationMillis = 300), label = ""
    )
    //анимация подчеркивания в палитре цвета групп
    val animateBorderColor by animateDpAsState(targetValue =
    if(islandState==4) 300.dp else 250.dp,
        animationSpec = tween(durationMillis = 400)
    )
    val coroutineScope = rememberCoroutineScope()
    //=====================================================================================
    //Функция запуска секундомера
    //=====================================================================================
    fun start() {
        if (isTrackerRunning) return
        isTrackerRunning = true
        coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            timerStartTime.value=startTime

            while (isTrackerRunning) {
                currentTime = System.currentTimeMillis() - startTime
                delay(10L)
            }
        }
    }

    //=====================================================================================
    //Функция остановки секундомера
    //=====================================================================================
    @RequiresApi(Build.VERSION_CODES.O)
    fun stop() {
        var timerEndTime = System.currentTimeMillis()
        timerStartTime.value+=10800000L
        timerEndTime+=10800000L

        var trunsport = DateAndTimeS(0,0,0,0,0,0)
        trunsport.convertUnixTimeToDate1(timerEndTime)
        var trunsport2 = DateAndTimeS(0,0,0,0,0,0)
        trunsport2.convertUnixTimeToDate1(timerStartTime.value)


        trunDate.value=timerStartTime.value
        trunDate2.value=timerEndTime
        trunHour.value=trunsport2.hour
        trunMin.value=trunsport2.minute
        trunHour2.value=trunsport.hour
        trunMin2.value=trunsport.minute
        isTrackerRunning=false
        timerStartTime.value=0L
        currentTime=0L
        emptyCreateFlag=false
        updateTaskTrun= Task(null,"",0,0,-1)
        islandState=3


    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        //=====================================================================================
        //Остров трекера
        //=====================================================================================
        if(alphaTracker>0) {
            updateGroupValue = GroupTask(null, "", "")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (alphaClock > 0) {
                    Box(
                        Modifier
                            .padding(bottom = 30.dp)
                            .alpha(alphaClock)
                    ) {
                        timeconverter(time = currentTime)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (alphaCreate > 0) {
                        Box(
                            modifier = Modifier
                                .padding(end = 30.dp)
                                .width(110.dp)
                                .height(50.dp)
                                .alpha(alphaCreate)
                                .background(
                                    MaterialTheme.colorScheme.background, RoundedCornerShape(10)
                                )
                                .clickable {
                                    islandState = 2
                                }, contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Создать", style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    } else {
                        if (alphaMiniClock > 0) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .alpha(alphaMiniClock),
                                contentAlignment = Alignment.Center
                            ) {
                                timeconverter(time = currentTime)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = if (isTrackerRunning && islandState == 0) 0.dp else 30.dp)
                            .width(stopDp)
                            .height(50.dp)
                            .background(
                                MaterialTheme.colorScheme.background, RoundedCornerShape(10)
                            )
                            .clickable {
                                if (!isTrackerRunning) start() else stop()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (!isTrackerRunning) "Запись" else "Стоп", style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }


            }
        }
        //=====================================================================================
        //Остров выбора создания
        //=====================================================================================
        if(alphaChooseCreate>0){
            chooseCreate()
        }
        //=====================================================================================
        //Остров создания группы
        //=====================================================================================
        if(alphaCreateGroup>0){
            createGroup(alphaCreateGroup, animateBorderColor, updateGroupValue)
        }
        //=====================================================================================
        //Остров создания задачи
        //=====================================================================================
        if(alphaCreateTask>0){
            if(updateTaskTrun.uid==null) {
                if (emptyCreateFlag) createTask(alphaCreateTask, 0, 0, 0, 0, 0, 0,"", null,-1)
                else createTask(
                    alphaCreateTask,
                    trunDate.value,
                    trunHour.value,
                    trunMin.value,
                    trunDate2.value,
                    trunHour2.value,
                    trunMin2.value, "", null,-1
                )
            }else{
                var start = DateAndTimeS(0,0,0,0,0,0)
                start.convertUnixTimeToDate1(updateTaskTrun.start*1000)
                var end = DateAndTimeS(0,0,0,0,0,0)
                end.convertUnixTimeToDate1(updateTaskTrun.end*1000)
                createTask(
                    alphaCreateTask,
                    updateTaskTrun.start*1000,
                    start.hour,
                    start.minute,
                    updateTaskTrun.end*1000,
                    end.hour,
                    end.minute, updateTaskTrun.name!!, updateTaskTrun.uid, updateTaskTrun.groupID
                )
            }

        }


    }
}

//=====================================================================================
//Функция отображения экрана выбора создания
//=====================================================================================
@Composable
fun chooseCreate(){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Создать", style = TextStyle(
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.background
        ))
        Box(
            Modifier
                .padding(top = 25.dp)
                .width(300.dp)
                .height(100.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10))
                .clickable {
                    updateTaskTrun = Task(null, "", 0, 0, -1)
                    emptyCreateFlag = true
                    islandState = 3
                },
            contentAlignment = Alignment.Center){
            Text(text = "Задача", style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ))
        }
        Box(
            Modifier
                .padding(top = 25.dp)
                .width(300.dp)
                .height(100.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10))
                .clickable { islandState = 4 },
            contentAlignment = Alignment.Center){
            Text(text = "Группа", style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ))
        }
    }
}

//=====================================================================================
//Функция отображения экрана создания группы
//=====================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun createGroup(alphaCreateGroup:Float, animateBorderColor:Dp, group:GroupTask){
    var actualColor by remember { mutableStateOf(group.color) }
    var nameGroup by remember { mutableStateOf(group.name) }
    var id by remember { mutableStateOf(group.uid) }
    if(chooseGroup){
        actualColor= updateGroupValue.color
        nameGroup = updateGroupValue.name
        id = updateGroupValue.uid
        chooseGroup=false
    }
    //анимация открытия палитры(поворота стрелки)
    val animateRotateColorVector by animateFloatAsState(targetValue =
    if(islandState==5) 180f else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )
    Column(modifier = Modifier.alpha(alphaCreateGroup), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Редактор группы", style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.background
        )
        )
        TextField(value = nameGroup!!,
            onValueChange ={nameGroup = it},
            label = {
                Text(text = "Название", style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background
                )
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.background,
                disabledTextColor = MaterialTheme.colorScheme.background,
                unfocusedTextColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.padding(top=25.dp)
        )
        Box(
            Modifier
                .width(300.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.background))
        //анимация изменения бокса палитры
        val animateColorBox by animateDpAsState(targetValue = if (islandState == 4) 50.dp else 210.dp
            , animationSpec = tween(durationMillis = 400)
        )
        Box(modifier = Modifier
            .padding(top = 25.dp)
            .width(300.dp)
            .height(animateColorBox)
            .border(
                width = if (islandState == 5) 2.dp else 0.dp,
                color = if (islandState == 5) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4)
            ), contentAlignment = Alignment.TopCenter){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .height(50.dp)
                        .clickable {
                            if (islandState == 4) islandState = 5 else islandState = 4
                        }, contentAlignment = Alignment.Center){
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier
                            .padding(start = 25.dp, end = 25.dp)
                            .height(48.dp)
                            , verticalAlignment = Alignment.CenterVertically){
                            Text(text = "Цвет", style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.background
                            ))

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd){
                                Box(
                                    modifier = Modifier
                                        .padding(start = 15.dp)
                                        .width(25.dp)
                                        .height(25.dp)
                                        .background(
                                            if (actualColor != "" && islandState == 4) parseColor(
                                                actualColor
                                            ) else MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4)
                                        ), contentAlignment = Alignment.Center
                                ){
                                    Icon(painter = painterResource(id = R.drawable.vectordown),
                                        tint = MaterialTheme.colorScheme.background
                                        , contentDescription ="",
                                        modifier = Modifier
                                            .scale(0.7f)
                                            .rotate(animateRotateColorVector))
                                }

                            }
                        }

                        Box(
                            Modifier
                                .width(animateBorderColor)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.background))
                    }

                }
                if(islandState==5){
                    LazyColumn(Modifier.height(160.dp)) {
                        items(colorList){
                                color-> Box(
                            Modifier
                                .height(50.dp)
                                .clickable {
                                    if (color.colorS == actualColor) {
                                        actualColor = ""
                                    } else {
                                        actualColor = color.colorS
                                    }
                                    islandState = 4
                                }, contentAlignment = Alignment.Center){
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(modifier = Modifier
                                    .padding(start = 25.dp, end = 25.dp)
                                    .height(48.dp)
                                    , verticalAlignment = Alignment.CenterVertically){
                                    Text(text = color.name, style = TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.background
                                    ))
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd){
                                        Box(
                                            Modifier
                                                .width(25.dp)
                                                .height(25.dp)
                                                .background(
                                                    color.color,
                                                    shape = RoundedCornerShape(4)
                                                ),
                                            contentAlignment = Alignment.Center){
                                            if(actualColor==color.colorS){
                                                Icon(painter = painterResource(R.drawable.ok),
                                                    contentDescription = "",
                                                    tint = MaterialTheme.colorScheme.background)
                                            }
                                        }
                                    }
                                }
                                Box(
                                    Modifier
                                        .width(animateBorderColor)
                                        .height(2.dp)
                                        .background(MaterialTheme.colorScheme.background))
                            }

                        }

                        }
                    }
                }

            }
        }
        Row(Modifier.padding(top=25.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .padding(end = 10.dp)
                    .width(140.dp)
                    .height(50.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10)
                    )
                    .clickable {
                        if (id != null) {
                            deleteGroup = true
                        }
                        islandState = 0
                    },
                contentAlignment = Alignment.Center){
                Text(text = "Удалить", style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ))

            }
            Box(
                Modifier
                    .padding(start = 10.dp)
                    .width(140.dp)
                    .height(50.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10)
                    )
                    .clickable {
                        if (nameGroup != "" && actualColor != "") {
                            updateGroupValue = GroupTask(id, nameGroup, actualColor)
                            updateGroup = true
                            islandState = 0
                        }
                    },
                contentAlignment = Alignment.Center){
                Text(text = "Сохранить", style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ))

            }
        }
    }
}

//=====================================================================================
//Функция отображения экрана создания задачи
//=====================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun createTask(alphaCreateTask:Float, date1:Long, hour1:Int, minute1:Int, date2:Long, hour2:Int,minute2:Int, name:String, id:Long?, group:Int){
    var isTimePickerVisible1 by remember { mutableStateOf(false) }
    var isDatePickerVisible1 by remember { mutableStateOf(false) }
    var selectedHour1 by remember { mutableStateOf<Int>(hour2) }
    //selectedHour1=hour1
    var selectedMinute1 by remember { mutableStateOf<Int>(minute2) }
    //selectedMinute1=minute1
    var selectedDate1 by remember { mutableStateOf<Long?>(date2) }
    //переменные для выбора даты
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(date1) }

    // Форматирование выбранной даты в читаемый формат (например, "dd/MM/yyyy")
    val formattedDate = selectedDate?.let {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it)
    } ?: "No Date Selected"
    // Форматирование выбранной даты в читаемый формат (например, "dd/MM/yyyy")
    val formattedDate1 = selectedDate1?.let {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it)
    } ?: "No Date Selected"

    //переменные для выбора времени
    var isTimePickerVisible by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf<Int>(hour1) }
    var selectedMinute by remember { mutableStateOf<Int>(minute1) }

    val formattedTime = if (selectedHour != null && selectedMinute != null) {
        String.format("%02d:%02d", selectedHour, selectedMinute)
    } else {
        "No Time Selected"
    }
    val formattedTime1 = if (selectedHour1 != null && selectedMinute1 != null) {
        String.format("%02d:%02d", selectedHour1, selectedMinute1)
    } else {
        "No Time Selected"
    }

    //Флаг открытия окна групп
    var groupFlag by remember { mutableStateOf(false) }
    //Актуальная группа
    var actualGroup = group
    var nameTask by remember { mutableStateOf(name) }
    Column(Modifier.alpha(alphaCreateTask),horizontalAlignment = Alignment.CenterHorizontally){
        Text(text = "Редактор задачи", style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.background
        ), modifier = Modifier.padding(top=5.dp,bottom = 25.dp)
        )
        LazyColumn( horizontalAlignment = Alignment.CenterHorizontally) {



            item{
                TextField(value = nameTask,
                    onValueChange ={nameTask = it},
                    label = {
                        Text(text = "Название", style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.background
                        )
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.background,
                        disabledTextColor = MaterialTheme.colorScheme.background,
                        unfocusedTextColor = MaterialTheme.colorScheme.background
                    )
                )
                Box(
                    Modifier
                        .width(300.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.background))

                Text(
                    text = "C",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.padding(top=25.dp)
                )
                Row(Modifier.padding(top=5.dp), verticalAlignment = Alignment.CenterVertically){
                    Box(
                        Modifier
                            .padding(end = 15.dp)
                            .width(135.dp)
                            .height(50.dp)
                            .clickable { isDatePickerVisible = true },
                        contentAlignment = Alignment.Center){
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedDate!! > 0L) formattedDate else "Дата",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.background
                                )
                            )
                            Box(
                                Modifier
                                    .width(135.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.background))
                        }
                    }
                    Box(
                        Modifier
                            .padding(start = 15.dp)
                            .width(135.dp)
                            .height(50.dp)
                            .clickable { isTimePickerVisible = true },
                        contentAlignment = Alignment.Center){
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedHour != 0&&selectedMinute!=0) formattedTime else "Время",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.background
                                )
                            )
                            Box(
                                Modifier
                                    .width(135.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.background))
                        }
                    }
                }
                if (isDatePickerVisible) {
                    isTimePickerVisible=false
                    isDatePickerVisible1=false
                    isTimePickerVisible1=false
                    groupFlag=false
                    DatePickerModal(
                        onDateSelected = { date ->
                            selectedDate = date
                            isDatePickerVisible = false
                        },
                        onDismiss = { isDatePickerVisible = false }
                    )
                }
                if (isTimePickerVisible) {
                    groupFlag=false
                    isDatePickerVisible=false
                    isDatePickerVisible1=false
                    isTimePickerVisible1=false
                    inputTime(
                        onConfirm = { time->
                            selectedHour=time.hour
                            selectedMinute=time.minute
                            isTimePickerVisible=false
                        },
                        onDismiss = { isTimePickerVisible = false }
                    )
                }
                Text(
                    text = "По",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.padding(top=25.dp)
                )
                //--------------------------------------------------------------------------
                Row(Modifier.padding(top=5.dp), verticalAlignment = Alignment.CenterVertically){


                    Box(
                        Modifier
                            .padding(end = 15.dp)
                            .width(135.dp)
                            .height(50.dp)
                            .clickable { isDatePickerVisible1 = true },
                        contentAlignment = Alignment.Center){
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedDate1!! > 0) formattedDate1 else "Дата",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.background
                                )
                            )
                            Box(
                                Modifier
                                    .width(135.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.background))
                        }
                    }
                    Box(
                        Modifier
                            .padding(start = 15.dp)
                            .width(135.dp)
                            .height(50.dp)
                            .clickable { isTimePickerVisible1 = true },
                        contentAlignment = Alignment.Center){
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedHour1 != 0&&selectedMinute1!=0) String.format("%02d:%02d", selectedHour1, selectedMinute1) else "Время",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.background
                                )
                            )
                            Box(
                                Modifier
                                    .width(135.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.background))
                        }
                    }
                }
                if (isDatePickerVisible1) {
                    groupFlag=false
                    isTimePickerVisible1=false
                    isDatePickerVisible=false
                    isDatePickerVisible=false
                    DatePickerModal(
                        onDateSelected = { date ->
                            selectedDate1 = date
                            isDatePickerVisible1 = false
                        },
                        onDismiss = { isDatePickerVisible1 = false }
                    )
                }
                if (isTimePickerVisible1) {
                    groupFlag=false
                    isDatePickerVisible1=false
                    isDatePickerVisible=false
                    isTimePickerVisible=false
                    inputTime(
                        onConfirm = { time1->
                            selectedHour1=time1.hour
                            selectedMinute1=time1.minute
                            isTimePickerVisible1=false
                        },
                        onDismiss = { isTimePickerVisible1 = false }
                    )
                }
                //---------------------------------------------------------------------------------
                val animateRotateColorVectorGroup by animateFloatAsState(targetValue =
                if(groupFlag==false) 0f else 180f,
                    animationSpec = tween(durationMillis = 400)
                )
                //анимация подчеркивания в палитре  групп
                val animateBorderColorGroup by animateDpAsState(targetValue =
                if(!groupFlag) 300.dp else 250.dp,
                    animationSpec = tween(durationMillis = 400)
                )
                //анимация изменения бокса групп
                val animateGroupBox by animateDpAsState(targetValue = if (islandState == 4) 50.dp else 210.dp
                    , animationSpec = tween(durationMillis = 400)
                )
                Box(modifier = Modifier
                    .padding(top = 25.dp)
                    .width(300.dp)
                    .height(animateGroupBox)
                    .border(
                        width = if (groupFlag == true) 2.dp else 0.dp,
                        color = if (groupFlag == true) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4)
                    ), contentAlignment = Alignment.TopCenter){
                    Column {
                        Box(
                            Modifier
                                .height(50.dp)
                                .clickable {
                                    if (groupFlag) groupFlag = false else groupFlag = true
                                }, contentAlignment = Alignment.Center){
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(modifier = Modifier
                                    .padding(start = 25.dp, end = 25.dp)
                                    .height(48.dp)
                                    , verticalAlignment = Alignment.CenterVertically){
                                    (if(actualGroup==-1) "Группа" else listGroup[actualGroup-1].name)?.let {
                                        Text(text = it, style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.background
                                        ))
                                    }

                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd){
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 15.dp)
                                                .width(25.dp)
                                                .height(25.dp)
                                                .background(
                                                    if (actualGroup != -1) parseColor(
                                                        listGroup[actualGroup - 1].color
                                                    ) else MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4)
                                                ), contentAlignment = Alignment.Center
                                        ){
                                            Icon(painter = painterResource(id = R.drawable.vectordown),
                                                tint = MaterialTheme.colorScheme.background
                                                , contentDescription ="",
                                                modifier = Modifier
                                                    .scale(0.7f)
                                                    .rotate(animateRotateColorVectorGroup))
                                        }

                                    }
                                }

                                Box(
                                    Modifier
                                        .width(animateBorderColorGroup)
                                        .height(2.dp)
                                        .background(MaterialTheme.colorScheme.background))
                            }

                        }
                        if(groupFlag){
                            isTimePickerVisible=false
                            isDatePickerVisible1=false
                            isTimePickerVisible1=false
                            isDatePickerVisible=false
                            LazyColumn(Modifier.height(160.dp)) {
                                items(listGroup){
                                        group-> Box(
                                    Modifier
                                        .height(50.dp)
                                        .clickable {
                                            if (actualGroup == group.uid) {
                                                actualGroup = -1
                                            } else {
                                                actualGroup = group.uid!!
                                            }
                                            groupFlag = false
                                        }, contentAlignment = Alignment.Center){
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(modifier = Modifier
                                            .padding(start = 25.dp, end = 25.dp)
                                            .height(48.dp)
                                            , verticalAlignment = Alignment.CenterVertically){
                                            group.name?.let {
                                                Text(text = it, style = TextStyle(
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.background
                                                ))
                                            }
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd){
                                                Box(
                                                    Modifier
                                                        .width(25.dp)
                                                        .height(25.dp)
                                                        .background(
                                                            parseColor(group.color),
                                                            shape = RoundedCornerShape(4)
                                                        ),
                                                    contentAlignment = Alignment.Center){
                                                    if(actualGroup==group.uid){
                                                        Icon(painter = painterResource(R.drawable.ok),
                                                            contentDescription = "",
                                                            tint = MaterialTheme.colorScheme.background)
                                                    }
                                                }
                                            }
                                        }
                                        Box(
                                            Modifier
                                                .width(animateBorderColorGroup)
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.background))
                                    }

                                }

                                }
                            }
                        }
                        Row(Modifier.padding(top=25.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .padding(end = 10.dp)
                                    .width(140.dp)
                                    .height(50.dp)
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(10)
                                    )
                                    .clickable {
                                        if (id != null) {
                                            var startValue = DateAndTimeS(0, 0, 0, 0, 0, 0)
                                            startValue.convertUnixTimeToDate1(selectedDate)
                                            startValue.minute = selectedMinute
                                            startValue.hour = selectedHour
                                            val startValueSecond =
                                                startValue.convertToSeconds(startValue)
                                            var endValue = DateAndTimeS(0, 0, 0, 0, 0, 0)
                                            endValue.convertUnixTimeToDate1(selectedDate1)
                                            endValue.hour = selectedHour1
                                            endValue.minute = selectedMinute1
                                            val endValueSecond = endValue.convertToSeconds(endValue)
                                            updateTaskValue = Task(
                                                id,
                                                nameTask,
                                                startValueSecond,
                                                endValueSecond,
                                                actualGroup
                                            )
                                            deleteTask = true
                                            islandState = 0

                                            //Log.i(startValue.toString())
                                        }
                                        islandState = 0
                                    },
                                contentAlignment = Alignment.Center){
                                Text(text = "Удалить", style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ))

                            }
                            Box(
                                Modifier
                                    .padding(start = 10.dp)
                                    .width(140.dp)
                                    .height(50.dp)
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(10)
                                    )
                                    .clickable {
                                        if (nameTask != "" && selectedHour != null && selectedHour1 != null &&
                                            selectedMinute != null && selectedMinute1 != null && selectedDate != null
                                            && selectedDate1 != null && actualGroup != null && actualGroup != -1
                                        ) {
                                            var startValue = DateAndTimeS(0, 0, 0, 0, 0, 0)
                                            startValue.convertUnixTimeToDate1(selectedDate)
                                            startValue.minute = selectedMinute
                                            startValue.hour = selectedHour
                                            val startValueSecond =
                                                startValue.convertToSeconds(startValue)
                                            var endValue = DateAndTimeS(0, 0, 0, 0, 0, 0)
                                            endValue.convertUnixTimeToDate1(selectedDate1)
                                            endValue.hour = selectedHour1
                                            endValue.minute = selectedMinute1
                                            val endValueSecond = endValue.convertToSeconds(endValue)
                                            updateTaskValue = Task(
                                                id,
                                                nameTask,
                                                startValueSecond,
                                                endValueSecond,
                                                actualGroup
                                            )
                                            updateTask = true
                                            islandState = 0

                                            //Log.i(startValue.toString())
                                        }
                                    },
                                contentAlignment = Alignment.Center){
                                Text(text = "Сохранить", style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ))

                            }
                        }

                    }
                }
                //---------------------------------------------------------------------------------
            }
        }
    }
}

//=====================================================================================
//Функция отображения секундомера
//=====================================================================================
@Composable
fun timeconverter(time:Long){

    val min = (time/1000%3600)/60
    val sec = time/1000%60
    val hour = time / 3600000
    var ret =""
    ret = "%02d:%02d:%02d".format(hour,min,sec)
    Text(ret, style = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.background
    ))
}

//=====================================================================================
//Экран трекера
//=====================================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun trackerScreen(){

    //анимация размера острова
    val islandHeight by animateDpAsState(
        targetValue = if(islandState==0) 200.dp
        else if(islandState==1) 100.dp
        else if(islandState==2) 350.dp
        else if (islandState==3)465.dp
        else if (islandState==4)310.dp
        else if (islandState==5)480.dp
        else 625.dp,
        animationSpec = tween(durationMillis= 500, delayMillis = 200), label = ""
    )
    //анимация стрелки на острове
    val ratateAnimate by animateFloatAsState(targetValue = if (islandState == 1) 180F else 0F
    , animationSpec = tween(delayMillis = 300), label = ""
    )
    //анимация колонки задач
    val alphaTaskColumn = animateFloatAsState(targetValue = if (islandState != 4 && islandState != 5) 1f else 0f
    , animationSpec = tween(delayMillis = if(islandState != 4 && islandState != 5) 300 else 0, durationMillis = 300)
    )
    //анимация колонки групп
    val alphaGroupColumn = animateFloatAsState(targetValue = if (islandState != 4 && islandState != 5) 0f else 1f
        , animationSpec = tween(delayMillis = if(islandState != 4 && islandState != 5) 0 else 300, durationMillis = 300)
    )

    Box(
        Modifier
            .padding(top = 30.dp)
            .fillMaxSize(), contentAlignment = Alignment.TopCenter){
            LazyColumn(
                Modifier
                    .padding(bottom = islandHeight + 15.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
                    ) {

                if((islandState != 4 && islandState != 5)){
                    items(listTask.value){
                        task->
                            var today = System.currentTimeMillis()+10800000
                            var todayDay = DateAndTimeS(0,0,0,0,0,0)
                            todayDay.convertUnixTimeToDate1(today)
                            todayDay.hour=0; todayDay.minute=0; todayDay.second=0
                            today = todayDay.convertToSeconds(todayDay)
                            var nextday = today + (24*3600000)

                            println("${task.start} $today ${task.end}")
                            if(nextday>(task.start)&&today<=(task.end)){
                                var dateTimeStart = DateAndTimeS(0,0,0,0,0,0)
                                dateTimeStart.convertUnixTimeToDate1(task.start*1000)
                                var dateTimeEnd = DateAndTimeS(0,0,0,0,0,0)
                                dateTimeEnd.convertUnixTimeToDate1(task.end*1000)
                                var timet = DateAndTimeS(0,0,0,0,0,0)
                                timet.calculateDifference(dateTimeStart,dateTimeEnd)
                                var tstring=""
                                if (timet.day>0) tstring+=timet.day.toString() + "д."
                                tstring+= timet.hour.toString() + "ч."
                                if(timet.day==0) tstring+= timet.minute.toString() + "м."
                                Box(modifier = Modifier.clickable {
                                    updateTaskTrun=task
                                    islandState=3
                                }) {
                                    taskCardTracker(
                                        task = TaskView(
                                            task.uid!!, task.name!!, dateTimeStart, dateTimeEnd,
                                            parseColor(listGroup[task.groupID - 1].color), tstring
                                        )
                                    )
                                }
                            }

                       // }
                    }

                }else{
                    item {
                        Box(modifier = Modifier
                            .padding(bottom = 25.dp)
                            .fillMaxWidth(), contentAlignment = Alignment.CenterStart){
                            Text(text = "Группы", style = TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ))
                        }
                    }
                    items(listGroup){
                        group->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .clickable {
                                    actualGroupUID = group.uid!!
                                    updateGroupValue = group
                                    chooseGroup = true
                                    islandState = 4
                                }, contentAlignment = Alignment.CenterStart){
                            Row(Modifier.padding(start = 15.dp, end=15.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier
                                    .width(5.dp)
                                    .height(50.dp)
                                    .background(
                                        color = parseColor(group.color),
                                        shape = RoundedCornerShape(4)
                                    ))
                                Column(Modifier.padding(start=10.dp)) {
                                    group.name?.let {
                                        Text(text = it, style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        ))
                                    }

                                }
                            }
                        }
                    }
                }

            }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
            Box(
                Modifier
                    .height(islandHeight)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10, 10, 0, 0)
                    ), contentAlignment = Alignment.TopCenter
            ){
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painter = painterResource(R.drawable.vectordown), contentDescription ="",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .rotate(ratateAnimate)
                            .clickable {
                                if (islandState == 0) islandState = 1
                                else
                                    islandState = 0
                            })
                    magicIsland()
                }

            }
    }
    }
}


//=====================================================================================
//Функция парсинга строки в цвет
//=====================================================================================
fun parseColor(colorString: String): Color {
    val rgb = colorString.split(",").map { it.trim().toInt() }
    return Color(rgb[0], rgb[1], rgb[2])
}

//=====================================================================================
//Окно выбора даты
//=====================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("Ок")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    , modifier = Modifier.scale(0.8f)) {
        DatePicker(state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary,
                headlineContentColor = MaterialTheme.colorScheme.primary,
                weekdayContentColor = MaterialTheme.colorScheme.primary,
                yearContentColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.primary,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                navigationContentColor = MaterialTheme.colorScheme.primary,

            )
        )
    }
}
//=====================================================================================
//Окно выбора времени
//=====================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun inputTime(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )
    Box(modifier = Modifier
        .padding(top = 25.dp)
        .width(300.dp)
        .height(210.dp)
        .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(4)
        ), contentAlignment = Alignment.Center){
        Column {
            TimeInput(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor =  MaterialTheme.colorScheme.background,
                    selectorColor = MaterialTheme.colorScheme.background,
                    containerColor = MaterialTheme.colorScheme.background,
                    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.background,
                    periodSelectorBorderColor =  MaterialTheme.colorScheme.background,
                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.background,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.primary,
                    clockDialSelectedContentColor =  MaterialTheme.colorScheme.primary,
                    clockDialUnselectedContentColor =  MaterialTheme.colorScheme.primary,
                    periodSelectorSelectedContainerColor =  MaterialTheme.colorScheme.background,
                    periodSelectorUnselectedContainerColor =  MaterialTheme.colorScheme.background,
                    timeSelectorSelectedContainerColor =  MaterialTheme.colorScheme.background,
                    timeSelectorUnselectedContainerColor =  MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier
                    .scale(0.8f)
            )
            Row() {
                Box(
                    Modifier
                        .padding(end = 10.dp)
                        .width(100.dp)
                        .height(50.dp)
                        .clickable {
                            onDismiss
                        }
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(4)
                        ), contentAlignment = Alignment.Center){
                    Text(text = "Отмена", style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ))
                }
                Box(
                    Modifier
                        .padding(start = 10.dp)
                        .width(100.dp)
                        .height(50.dp)
                        .clickable {
                            onConfirm(timePickerState)
                        }
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(4)
                        ), contentAlignment = Alignment.Center){
                    Text(text = "Ок", style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ))
                }
            }

        }
    }

}

