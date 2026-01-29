package com.current.wearos.energycockpit

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.wear.watchface.editor.EditorSession
import kotlinx.coroutines.launch

class EditorActivity : ComponentActivity() {
    private var editorSession: EditorSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        lifecycleScope.launch {
            editorSession = EditorSession.createOnWatchEditorSession(
                activity = this@EditorActivity
            )

            editorSession?.let { session ->
                setupComplicationButtons(session)
            }
        }
    }

    private fun setupComplicationButtons(session: EditorSession) {
        findViewById<Button>(R.id.complication_100).setOnClickListener {
            launchComplicationChooser(session, 100)
        }
        findViewById<Button>(R.id.complication_101).setOnClickListener {
            launchComplicationChooser(session, 101)
        }
        findViewById<Button>(R.id.complication_102).setOnClickListener {
            launchComplicationChooser(session, 102)
        }
        findViewById<Button>(R.id.complication_103).setOnClickListener {
            launchComplicationChooser(session, 103)
        }
    }

    private fun launchComplicationChooser(session: EditorSession, slotId: Int) {
        lifecycleScope.launch {
            session.openComplicationDataSourceChooser(slotId)
        }
    }
}
