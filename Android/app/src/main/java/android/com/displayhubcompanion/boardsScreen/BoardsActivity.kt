package android.com.displayhubcompanion.boardsScreen

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation

class BoardsActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private val viewModel: BoardsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_boards)
        val view = findViewById<View>(R.id.boards_fragment_host)
        navController = Navigation.findNavController(view)
    }


    override fun onBackPressed() {
        if (navController.currentDestination?.label == "fragment_boards_list") {
            return
        }
        super.onBackPressed()
    }

}
