//#####################################################################################################################
//#####################################################################################################################
//###############################                    Хранение данных                    ###############################
//#####################################################################################################################
//####   Автор: Кулишов Илья Вячеславович     #########################################################################
//####   Версия: v.0.0.1                      #########################################################################
//####   Дата: 30.08.2024                     #########################################################################
//#####################################################################################################################
//#####################################################################################################################



package com.kulishov.tracker
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete

import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import java.util.Date


//------------------------------------------------------------------------------
//Класс для групп задач
//  Поля:
//      uid     - Идентификатор
//      name    - Название
//      color   - цвет
//------------------------------------------------------------------------------
@Entity
data class GroupTask(
    @PrimaryKey(autoGenerate = true) val uid: Int?=null,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "color") val color: String
    )

//------------------------------------------------------------------------------
//Класс для задач
//  Поля:
//      uid     - Идентификатор
//      name    - Название
//      start   - Начало
//      end     - Конец
//      groupID - Идентификатор группы
//------------------------------------------------------------------------------
@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Long?=null,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "start") val start: Long,
    @ColumnInfo(name = "end") val end: Long,
    @ColumnInfo(name = "groupID") val groupID: Int,

    )

//------------------------------------------------------------------------------
//Класс для счётчика
//  Поля:
//      uid     - Идентификатор
//      name    - Название
//      count   - Количество
//      groupID - Идентификатор группы
//------------------------------------------------------------------------------
@Entity
data class Counter(
    @PrimaryKey(autoGenerate = true) val uid: Long?=null,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "count") val start: Long,
    @ColumnInfo(name = "groupID") val groupID: Int,

    )


//------------------------------------------------------------------------------
//Интерфейс Dao для трекера
//  Функции:
//      insertTask  - Добавить задачу
//      getAllTask  - Получить список задач
//      deleteTask  - Удалить задачу
//      updateTask  - Обновить задачу
//------------------------------------------------------------------------------
@Dao
interface DaoTracker{
    @Insert
    fun insertTask(task: Task)
    @Query("SELECT * FROM Task")
    fun getAllTask():Flow<List<Task>>
    @Delete
    fun deleteTask(task: Task)
    @Update
    fun updateTask(vararg task: Task)

}
//------------------------------------------------------------------------------
//Интерфейс Dao для счётчика
//  Функции:
//      insertCounter  - Добавить счётчик
//      getAllCounter  - Получить список счётчиков
//      deleteCounter  - Удалить счётчик
//      updateCounter  - Обновить счётчик
//------------------------------------------------------------------------------
@Dao
interface DaoCounter{

    @Insert
    fun insertCounter(counter: Counter)
    @Query("SELECT * FROM Counter")
    fun getAllCounter():List<Counter>
    @Delete
    fun deleteCounter(counter: Counter)
    @Update
    fun updateCounter(vararg counter: Counter)
}

//------------------------------------------------------------------------------
//Интерфейс Dao для групп
//  Функции:
//      insertGroup  - Добавить группу
//      getAllGroup  - Получить список групп
//      deleteGroup  - Удалить группу
//      updateGroup  - Обновить группу
//------------------------------------------------------------------------------
@Dao
interface DaoGroup{
    @Update
    fun updateGroup(vararg group: GroupTask)
    @Delete
    fun deleteGroup(group: GroupTask)
    @Query("SELECT * FROM GroupTask ")
    fun getAllGroup(): Flow<List<GroupTask>>
    @Insert
    fun insertGroup(group: GroupTask)
}

//#####################################################################################################################
//###############################                База данных приложения                 ###############################
//#####################################################################################################################
@Database(
    entities = [
        GroupTask::class,
        Task::class,
        Counter::class
    ],
    version = 1,
)
abstract class TrackerDatabase:RoomDatabase(){
    abstract fun trackerDao():DaoTracker
    abstract fun counterDao():DaoCounter
    abstract fun groupDao(): DaoGroup

}

//------------------------------------------------------------------------------
//Класс для отображения задачи
//  Поля:
//      uid     - Идентификатор
//      name    - Название
//      start   - Начало
//      end     - Конец
//      color   - цвет
//------------------------------------------------------------------------------

data class TaskView(
    val uid:Long,
    val name: String,
    val start: DateAndTimeS,
    val end:DateAndTimeS,
    val color: Color,
    val time:String
    )

//------------------------------------------------------------------------------
//Класс для хранения цветов
//  Поля:
//      uid     - Идентификатор
//      name    - Название
//      color   - Цвет
//      colorS  - Цвет в строке
//------------------------------------------------------------------------------
data class ColorClass(
    val uid:Short,
    val name:String,
    val color:Color,
    val colorS:String
)

val colorList = listOf(
    ColorClass(0,"Красный", Color(192,61,61), "192,61,61"),
    ColorClass(1,"Жёлтый", Color(189,192,61), "189,192,61"),
    ColorClass(2,"Лаймовый", Color(150,192,61), "150,192,61"),
    ColorClass(3,"Зелёный", Color(61,192,82), "61,192,82"),
    ColorClass(4,"Голубой", Color(61,152,192), "61,152,192"),
    ColorClass(5,"Синий", Color(63,61,192), "63,61,192"),
    ColorClass(6,"Розовый", Color(192,61,139), "192,61,139"),
    ColorClass(7,"Пурпурный", Color(169,82,170), "169,82,170"),
)

var listGroup = mutableListOf<GroupTask>(
    GroupTask(0,"1","192,61,61"),
    GroupTask(1,"2","192,61,61"),
    GroupTask(2,"3","192,61,61"),
)

var listTask = mutableStateOf(emptyList<Task>())

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//Класс для хранения информации о месяцах в календаре                      ~~~~~~~~~~~~
//Поля:                                                                    ~~~~~~~~~~~~
//      name:String - Название                                             ~~~~~~~~~~~~
//      kolday:Int  - Количество дней в месяце                             ~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
data class month(
    val name:String,
    var kolday:Int
)
//#####################################################################################
//Массив для хранения месяцев                                              ############
//#####################################################################################
val calendar = listOf<month>(
    month("error", 0),
    month("Январь", 31),
    month("Февраль", 28),
    month("Март", 31),
    month("Апрель", 30),
    month("Май", 31),
    month("Июнь", 30),
    month("Июль", 31),
    month("Август", 31),
    month("Сентябрь", 30),
    month("Октябрь", 31),
    month("Ноябрь", 30),
    month("Декабрь", 31),
)

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//класс для хранения даты                                                  ~~~~~~~~~~~~
//Поля:                                                                    ~~~~~~~~~~~~
//      day:Int     - День                                                 ~~~~~~~~~~~~
//      month:Int   - Месяц                                                ~~~~~~~~~~~~
//      year:Int    - Год                                                  ~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
data class DateM(
    val day: Int,
    val month: Int,
    var year: Int
)

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//класс для хранения времени                                               ~~~~~~~~~~~~
//Поля:                                                                    ~~~~~~~~~~~~
//      hour:Int     - Час                                                 ~~~~~~~~~~~~
//      minute:Int   - Минута                                              ~~~~~~~~~~~~
//      second:Int   - Секунда                                             ~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
data class DateAndTimeS(
    var year:Int,
    var month: Int,
    var day: Int,
    var hour:Int,
    var minute:Int,
    var second:Int) {


    fun convertUnixTimeToDate1(unixTimeMillis: Long?) {
        val secondsInMinute = 60
        val secondsInHour = 3600
        val secondsInDay = 86400
        val daysInYearFun = 365
        val daysInLeapYearFun = 366
        val secondsInYearFun = daysInYearFun * secondsInDay
        val secondsInLeapYearFun = daysInLeapYearFun * secondsInDay

        // Эпоха Unix начинается с 1 января 1970 года
        var yearFun = 1970
        var remainingSeconds = unixTimeMillis!!/1000
        var isLeapYearFun = false
        var flag = true
        while(flag){
            isLeapYearFun = (yearFun % 4 == 0 && yearFun % 100 != 0) || (yearFun % 400 == 0)
            var yearsecond = 0
            if(isLeapYearFun) yearsecond=daysInLeapYearFun*24*60*60
            else yearsecond=daysInYearFun*24*60*60
            if(remainingSeconds!! >= yearsecond){
                yearFun++
                remainingSeconds-=yearsecond
            }else flag = false
        }
        flag = true
        val daysInMonthFun = listOf(31, if (isLeapYearFun) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var monthFun = 0
        var dayOfMonthFun = 1
        while(flag){
            var monthsecond = daysInMonthFun[monthFun] * 24 * 60* 60
            if(remainingSeconds!! >monthsecond){
                monthFun++
                remainingSeconds-=monthsecond
            }else flag=false
        }
        flag = true
        val hoursecond = 24*60*60
        while(flag){
            if(remainingSeconds!!>hoursecond){
                remainingSeconds-=hoursecond
                dayOfMonthFun++
            } else flag = false
        }
        flag = true
         val hoursecond2=3600
        var hourFun = 0
        while (flag){
            if(remainingSeconds!!>hoursecond2){
                hourFun++
                remainingSeconds-=hoursecond2
            } else flag = false
        }
        flag = true
        val secondminute = 60
        var minuteFun = 0
        while(flag){
            if(remainingSeconds!!> secondminute){
                minuteFun++
                remainingSeconds-=secondminute
            } else flag = false
        }
        val secondFun = remainingSeconds
        year=yearFun
        month=monthFun+1
        day=dayOfMonthFun
        hour= hourFun
        minute=minuteFun
        second=secondFun!!.toInt()

    }
    //=====================================================================================
    //Функция сравнения дат
    //=====================================================================================
    fun dateTimeCompare(date2:DateAndTimeS):Int{
        var res = 0
        if(year==date2.year){
            if(month==date2.month){
                if(day==date2.day){
                    if(hour==date2.hour){
                        if(minute==date2.minute){
                            if(second>date2.second) res=1
                            if(second<date2.second) res=2
                        }else{
                            if(minute>date2.minute) res=1 else res = 2
                        }
                    }else{
                        if(hour>date2.hour) res=1 else res=2
                    }
                }
                else{
                    if(day>date2.day) res =1 else res = 2
                }
            }else{
                if(month>date2.month) res=1 else res=2
            }
        } else {
            if(year>date2.year) res = 1 else res=2
        }

        return res
    }

    //=====================================================================================
    //Функция проверки весокосного года
    //=====================================================================================
    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    //=====================================================================================
    //Функция возврата дней в месяце
    //=====================================================================================
    fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1 -> 31
            2 -> if (isLeapYear(year)) 29 else 28
            3 -> 31
            4 -> 30
            5 -> 31
            6 -> 30
            7 -> 31
            8 -> 31
            9 -> 30
            10 -> 31
            11 -> 30
            12 -> 31
            else -> 0
        }
    }

    //=====================================================================================
    //Функция перевода в unixtime
    //=====================================================================================
    fun convertToSeconds(date: DateAndTimeS): Long {
        val secondsInMinute = 60
        val secondsInHour = 3600
        val secondsInDay = 86400

        var totalSeconds = 0L

        // Считаем секунды за полные годы
        for (year in 1970 until date.year) {
            totalSeconds += if (isLeapYear(year)) 366 * secondsInDay else 365 * secondsInDay
        }

        // Считаем секунды за полные месяцы в текущем году
        for (month in 1 until date.month) {
            totalSeconds += daysInMonth(date.year, month) * secondsInDay
        }

        // Считаем секунды за полные дни в текущем месяце
        totalSeconds += (date.day - 1) * secondsInDay

        // Считаем секунды за часы, минуты и секунды
        totalSeconds += date.hour * secondsInHour
        totalSeconds += date.minute * secondsInMinute
        totalSeconds += date.second

        return totalSeconds
    }

    //=====================================================================================
    //Функция вычисления времени между датами
    //=====================================================================================
    fun calculateDifference(date1: DateAndTimeS, date2: DateAndTimeS){
        val secondsInMinute = 60
        val secondsInHour = 3600
        val secondsInDay = 86400

        // Преобразуем обе даты в секунды
        val time1 = convertToSeconds(date1)
        val time2 = convertToSeconds(date2)

        // Найдем разницу в секундах
        val differenceInSeconds = kotlin.math.abs(time2 - time1)

        // Преобразуем разницу в дни, часы и минуты
        val days = (differenceInSeconds / secondsInDay).toInt()
        val remainingSeconds = differenceInSeconds % secondsInDay

        val hours = (remainingSeconds / secondsInHour).toInt()
        val minutes = ((remainingSeconds % secondsInHour) / secondsInMinute).toInt()

        day=days
        hour=hours
        minute=minutes
    }

}
