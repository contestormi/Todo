package com.example.todolist

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolist.presentation.detail.TaskDetailFragment
import com.example.todolist.presentation.list.TaskListFragment

class MainActivity : AppCompatActivity(), MainNavigator {
    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
        requestNotificationsPermissionIfNeeded()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    TaskListFragment.newInstance()
                )
                .commit()
        }
    }

    override fun openTask(taskId: Long) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                TaskDetailFragment.newInstance(taskId)
            )
            .addToBackStack(null)
            .commit()
    }

    override fun openNewTask() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                TaskDetailFragment.newInstance(0L)
            )
            .addToBackStack(null)
            .commit()
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return
        requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

interface MainNavigator {
    fun openTask(taskId: Long)
    fun openNewTask()
}