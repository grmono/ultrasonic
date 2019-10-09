package io.chirp.messenger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.chirp.chirpsdk.ChirpSDK
import io.chirp.chirpsdk.models.ChirpSDKState
import io.chirp.chirpsdk.models.ChirpError
import io.chirp.chirpsdk.models.ChirpErrorCode
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.graphics.Typeface


private const val REQUEST_RECORD_AUDIO = 1

private const val CHIRP_APP_KEY = "b55EceC8ecE4ACcCbcAb86Fd3"
private const val CHIRP_APP_SECRET = "9eF4EE5AB7f4BB6cBC47b7cCAF8469179CfD062AA3177B19a0"
private const val CHIRP_APP_CONFIG = "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"

private const val TAG = "ChirpMessenger"

class MainActivity : AppCompatActivity() {

    private lateinit var chirpSdk: ChirpSDK

    private lateinit var messageReceived: TextView
    private lateinit var messageToSend: EditText
    private lateinit var sendMessageBtn: Button
    private lateinit var context: Context
    private var maxPayloadLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.messageToSend = findViewById(R.id.messageToSend)
        this.messageReceived = findViewById(R.id.messageRreceived)
        this.sendMessageBtn = findViewById(R.id.sendMessage)
        this.context = this

        val calibreLight = Typeface.createFromAsset(assets, "fonts/calibre_light.ttf")
        val calibreMedium = Typeface.createFromAsset(assets, "fonts/calibre_medium.ttf")
        messageToSend.typeface = calibreLight
        messageReceived.typeface = calibreLight
        sendMessageBtn.typeface = calibreMedium

        if (CHIRP_APP_KEY == "" || CHIRP_APP_SECRET == "") {
            Log.e(TAG, "CHIRP_APP_KEY or CHIRP_APP_SECRET is not set. " +
                    "Please update with your CHIRP_APP_KEY/CHIRP_APP_SECRET from developers.chirp.io")
            return
        }

        /**
         * Instantiate SDK with key secret and local config string
         */
        chirpSdk = ChirpSDK(this, CHIRP_APP_KEY, CHIRP_APP_SECRET)
        Log.v(TAG, "ChirpSDK Version: " + chirpSdk.version)

        sendMessageBtn.setOnClickListener(sendClickListener)
        messageToSend.addTextChangedListener(textChangedListener)

        val configError = chirpSdk.setConfig(CHIRP_APP_CONFIG)
        if (configError.code > 0) {
            Log.e(TAG, "ChirpError" + configError.message)
        } else {
            maxPayloadLength = chirpSdk.maxPayloadLength()
            val startError = chirpSdk.start()

            if (startError.code > 0) {
                Log.e(TAG, "ChirpError: " + startError.message)
            } else {

                chirpSdk.onSent { data: ByteArray, channel: Int ->
                    /**
                     * onSent is called when a send event has completed.
                     * The data argument contains the payload that was sent.
                     */
                    setButtonStyle("SEND", R.color.send_button_default_bg, true)
                    displayToast("Message sent.")
                }

                chirpSdk.onReceiving { channel: Int ->
                    /**
                     * onReceiving is called when a receive event begins.
                     * No data has yet been received.
                     */
                    setButtonStyle("RECEIVING", R.color.send_button_gray_bg, false)
                    Log.v(TAG, "ChirpSDKCallback: onReceiving on channel: $channel")        }

                chirpSdk.onSending { data: ByteArray, channel: Int ->
                    /**
                     * onSending is called when a send event begins.
                     * The data argument contains the payload being sent.
                     */
                    setButtonStyle("SENDING", R.color.send_button_gray_bg, false)
                    val message = String(data, Charsets.UTF_8)
                    Log.v(TAG, "ChirpSDKCallback: onSending: $message on channel: $channel")
                }

                chirpSdk.onReceived { data: ByteArray?, channel: Int ->
                    /**
                     * onReceived is called when a receive event has completed.
                     * If the payload was decoded successfully, it is passed in data.
                     * Otherwise, data is null.
                     */
                    setButtonStyle("SEND", R.color.send_button_default_bg, true)
                    if (data == null) {
                        displayToast("Receiving failed.")
                    } else {
                        displayToast("Received message.")
                        val message = String(data, Charsets.UTF_8)
                        Log.v(TAG, "ChirpSDKCallback: onReceived: $message on channel: $channel")
                        updateReceivedMessage(message)
                    }
                }

                chirpSdk.onStateChanged { oldState: ChirpSDKState, newState: ChirpSDKState ->
                    /**
                     * onStateChanged is called when the SDK changes state.
                     */
                    Log.v(TAG, "ChirpSDKCallback: onStateChanged $oldState -> $newState")
                }
            }
        }
    }

    /**
     * Fired when sent button is clicked
     */
    private val sendClickListener = fun(view: View) {
        view.hideKeyboard()
        val message = messageToSend.text.toString()
        if (message.isEmpty()) {
            displayToast("Please enter a message first.")
        } else {
            sendPayload(messageToSend.text.toString())
        }
    }

    /**
     * Fired when sent EditText input is changed
     */
    private val textChangedListener = object : TextWatcher {
        var messageToSendText = ""
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val i = s.toString().toByteArray().size
            if (i <= maxPayloadLength) {
                Log.d(TAG, "text size: $i")
                messageToSendText = s.toString()
            } else {
                displayToast("Message too long! Max size is $maxPayloadLength bytes.")
                messageToSend.setText(messageToSendText)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    /**
     * Used to hide keyboard when send button is clicked
     */
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Used to display toast popup messages
     */
    fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        } else {
            startSdk()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSdk()
                }
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        chirpSdk.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            chirpSdk.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        stopSdk()
    }

    private fun setButtonStyle(title: String, background: Int, isClickable: Boolean) {
        runOnUiThread{
            sendMessageBtn.text = title
            sendMessageBtn.setBackgroundColor(resources.getColor(background))
            sendMessageBtn.isClickable = isClickable
        }
    }

    private fun displayToast(message: String) {
        runOnUiThread{
            context.toast(message)
        }
    }

    private fun updateReceivedMessage(newPayload: String) {
        runOnUiThread { messageReceived.text = newPayload }
    }

    private fun stopSdk() {
        val error = chirpSdk.stop()
        if (error.code > 0) {
            Log.e(TAG, "ChirpSDKError: " + error.message)
            return
        }
    }

    private fun startSdk() {
        val error = chirpSdk.start()
        if (error.code > 0) {
            Log.e(TAG, "ChirpSDKError: " + error.message)
            return
        }
    }

    private fun sendPayload(payload: String) {
        /**
         * A payload is a byte array dynamic size with a maximum size defined by the config string.
         *
         * Convert String payload to  a byte array, and send it.
         */
        val payload = payload.toByteArray(Charsets.UTF_8)
        val maxPayloadLength = chirpSdk.maxPayloadLength()
        if (payload.size > maxPayloadLength) {
            Log.e("ChirpSDKError: ", "Payload too long")
            return;
        }
        val error = chirpSdk.send(payload)
        if (error.code > 0) {
            val volumeError = ChirpError(ChirpErrorCode.CHIRP_SDK_INVALID_VOLUME, "Volume too low. Please increase volume!")
            if (error.code == volumeError.code) {
                context.toast(volumeError.message)
            }
            Log.e("ChirpSDKError: ", error.message)
        }
    }
}
