package ro.pub.cs.systems.eim.test2proba3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ro.pub.cs.systems.eim.test2proba3.ui.theme.Test2Proba3Theme
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var resultText: TextView
    private val client = OkHttpClient()
    private val ACTION_DICTIONARY = "ro.pub.cs.systems.eim.test2_proba2.DICTIONARY"
    private val EXTRA_DEFINITION = "definiton"

    private val definitionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val definition = intent.getStringExtra(EXTRA_DEFINITION) ?: return
            resultText.text = definition
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test2_proba3)

        val inputEditText = findViewById<EditText>(R.id.input_edit_text)
        val searchButton = findViewById<Button>(R.id.search_button)
        resultText = findViewById(R.id.result_text_view)

        searchButton.setOnClickListener {
            val query = inputEditText.text.toString()
            if (query.isNotEmpty()) {
                fetchDefinition(query)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            definitionReceiver,
            IntentFilter(ACTION_DICTIONARY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    override fun onStop() {
        super.onStop()
        unregisterReceiver(definitionReceiver)
    }


    private fun fetchDefinition(query: String) {
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$query"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("DictionaryService", "Request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string() ?: return

                // 3.a (deja)
                Log.d("DictionaryService", "Response: $responseString")

                // 3b
                val jsonArray = org.json.JSONArray(responseString)
                val firstEntry = jsonArray.getJSONObject(0)

                val meanings = firstEntry.getJSONArray("meanings")
                val firstMeaning = meanings.getJSONObject(0)

                val definitions = firstMeaning.getJSONArray("definitions")
                val firstDefinitionObj = definitions.getJSONObject(0)

                val definition = firstDefinitionObj.getString("definition")

                Log.d("DictionaryService", "Definition: $definition")


                val intent = Intent(ACTION_DICTIONARY).apply {
                    setPackage(packageName)
                    putExtra(EXTRA_DEFINITION, definition)
                }
                sendBroadcast(intent)

            }
        })
    }
}
