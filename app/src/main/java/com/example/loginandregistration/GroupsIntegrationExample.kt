// app/src/main/java/com/example/loginandregistration/GroupsIntegrationExample.kt
package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * Example activity showing how to integrate the new Compose Groups screen into your existing app.
 * You can call this from your MainActivity or any other activity.
 */
class GroupsIntegrationExample : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple layout with a button to launch the new Groups screen
        val button =
                Button(this).apply {
                    text = "Open New Groups Screen (Compose)"
                    setOnClickListener {
                        // Launch the new Compose-based Groups screen
                        val intent =
                                Intent(
                                        this@GroupsIntegrationExample,
                                        GroupsComposeActivity::class.java
                                )
                        startActivity(intent)
                    }
                }

        setContentView(button)
    }
}

/** Extension function to easily launch the Groups screen from any Activity */
fun android.app.Activity.launchGroupsScreen() {
    val intent = Intent(this, GroupsComposeActivity::class.java)
    startActivity(intent)
}
