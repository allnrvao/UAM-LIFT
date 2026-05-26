package ni.edu.uam.uamlift

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ni.edu.uam.UAM_LIFT.screens.create.CreateRideScreen
import ni.edu.uam.UAM_LIFT.screens.home.HomeScreen
import ni.edu.uam.UAM_LIFT.screens.search.SearchScreen
import ni.edu.uam.uamlift.navegation.BottomNavigationBar
import ni.edu.uam.uamlift.screens.messages.MessagesScreen
import ni.edu.uam.uamlift.screens.profile.ProfileScreen


@Composable
fun UamLiftApp() {
    var currentTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentTab) { newTab ->
                currentTab = newTab
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            "home" -> HomeScreen(Modifier.padding(paddingValues))
            "search" -> SearchScreen(Modifier.padding(paddingValues))
            "create" -> CreateRideScreen(Modifier.padding(paddingValues))
            "messages" -> MessagesScreen(Modifier.padding(paddingValues))
            "profile" -> ProfileScreen(Modifier.padding(paddingValues))
        }
    }
}