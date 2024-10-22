//#####################################################################################################################
//#####################################################################################################################
//###############################                        Трекер                         ###############################
//#####################################################################################################################
//####   Автор: Кулишов Илья Вячеславович     #########################################################################
//####   Версия: v.0.0.1                      #########################################################################
//####   Дата: 30.08.2024                     #########################################################################
//#####################################################################################################################
//#####################################################################################################################



package com.kulishov.tracker


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
         setContent {
             AppTheme {
                 val navController = rememberNavController()
                 //Инициализации базы данных
                 val db = Room.databaseBuilder(
                     applicationContext,
                     TrackerDatabase::class.java,
                     name = "Tracker"
                 ).build()
                 //Получить данные с Room
                 var groups=db.groupDao().getAllGroup().asLiveData().observe(this){
                      listGroup.clear()
                      it.forEach {
                          listGroup+=GroupTask(it.uid,it.name, it.color)
                      }
                  }
                 var tasks = db.trackerDao().getAllTask().asLiveData().observe(this){
                     listTask.value= emptyList()
                     it.forEach{
                         listTask.value+=Task(it.uid,it.name,it.start, it.end, it.groupID)
                 }
                 }
                 //Обновление данных
                 val coroutineScope = CoroutineScope(Dispatchers.IO)
                 if(updateGroup|| updateTask || deleteTask || deleteGroup){
                     coroutineScope.launch {
                         if(updateGroup){
                             if(updateGroupValue.uid!=null){
                                 db.groupDao().updateGroup(updateGroupValue)
                                 updateGroupValue = GroupTask(null, "", "")
                             }else {db.groupDao().insertGroup(updateGroupValue)}
                             updateGroup=false
                         }
                         if(updateTask){
                             if(updateTaskValue.uid!=null){
                                 db.trackerDao().updateTask(updateTaskValue)
                                 updateTaskTrun=Task(null,"",0L,0L,0)
                             }else {
                                 db.trackerDao().insertTask(updateTaskValue)
                             }

                             updateTask=false
                         }
                         if(deleteTask){
                             db.trackerDao().deleteTask(updateTaskValue)
                             deleteTask = false
                         }
                         if(deleteGroup){
                             db.groupDao().deleteGroup(updateGroupValue)
                             deleteGroup=false
                         }
                     }
                 }
                 Surface() {
                     Scaffold (
                         bottomBar = {
                             BottomNavigationBar(navController = navController)
                         },
                         content = {
                             padding -> NavHostContainer(navController = navController, padding = padding)
                         }
                     )
                 }
             }
        }
    }
}

//#####################################################################################################################
//###############################                    Тема приложения                    ###############################
//#####################################################################################################################
@Composable
fun AppTheme(content:@Composable () -> Unit){
    MaterialTheme (
        colorScheme = if(isSystemInDarkTheme()){
            darkColorScheme(
                background = Color(22,22,22),
                primary = Color(63,89,156),
                surface = Color(22,22,22),

            )
        }else{
            lightColorScheme(
                background = Color(255,245,225),
                primary = Color(63,89,156),
                surface = Color(255,245,225),
            )
        }, content=content

    )
}


//#####################################################################################
//класс для хранения элементов навигации                                   ############
//#####################################################################################
data class BottomNavItem(
    val label: String,
    val icon: Int,
    val route:String,
)

//#####################################################################################
//объекты навигации                                                        ############
//#####################################################################################
object Constants {
    val BottomNavItems = listOf(
        BottomNavItem(
            label = "Трекер",
            icon = R.drawable.home,
            route = "tracker"        ),
        BottomNavItem(
            label = "Календарь",
            icon = R.drawable.calendar,
            route = "calendar"
        ),
        BottomNavItem(
            label = "Счётчик",
            icon = R.drawable.counter,
            route = "counter"
        ),
        BottomNavItem(
            label = "Статистика",
            icon = R.drawable.stata,
            route = "statistic"
        )
    )
}


//=====================================================================================
//BottomBar реализация
//=====================================================================================
@Composable
fun BottomNavigationBar(navController: NavController) {

    BottomNavigation( backgroundColor = MaterialTheme.colorScheme.background
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        Constants.BottomNavItems.forEach { navItem ->

            BottomNavigationItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route)
                },
                { Image(painter = painterResource(navItem.icon), contentDescription = navItem.label,
                    modifier = Modifier.scale(if(currentRoute == navItem.route) 0.9F else 0.7F))
                },
                /*label = {
                    Text(text = navItem.label)
                },
                alwaysShowLabel = false*/
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues
) {

    NavHost(
        navController = navController,

        startDestination = "tracker",

        modifier = Modifier.padding(paddingValues = padding),

        builder = {
            composable("tracker") {
                trackerScreen()
            }


            composable("calendar") {
                //calendarScreen(navController)
                emptyScreen()
            }


            composable("counter") {
                emptyScreen()
            }
            composable("statistic") {
                emptyScreen()
            }
        })

}

//-------------------------------------

//=====================================================================================
//empty screen
//=====================================================================================
@Composable
fun emptyScreen(){
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(horizontalAlignment = Alignment.CenterHorizontally){
            Text("Тут ещё ничего нет?", style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ))
            Image(painter = painterResource(id = R.drawable.cattime), contentDescription ="Котик",
                modifier = Modifier
                    .padding(24.dp)
                    .width(200.dp)
                    .height(200.dp)
                    .clip(CircleShape))
            Text("Почему нет, есть котик", style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ))
        }
    }
}