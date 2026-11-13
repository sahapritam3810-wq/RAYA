package com.raya.app
import android.Manifest
import android.app.*
import android.content.*
import android.os.*
import android.speech.*
import android.webkit.*

class MainActivity : Activity() {
  lateinit var web: WebView
  var sr: SpeechRecognizer? = null
  var cont = false
  var lang = "en-IN"
  val h = Handler(Looper.getMainLooper())

  override fun onCreate(b: Bundle?) {
    super.onCreate(b)
    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS), 1)
    web = WebView(this); setContentView(web)
    web.settings.javaScriptEnabled = true
    web.settings.domStorageEnabled = true
    @Suppress("DEPRECATION") run { web.settings.allowUniversalAccessFromFileURLs = true }
    web.webViewClient = WebViewClient()
    web.addJavascriptInterface(Bridge(), "Android")
    web.loadUrl("file:///android_asset/raya.html")
  }

  fun startSR(l: String) {
    lang = l
    sr?.destroy()
    sr = SpeechRecognizer.createSpeechRecognizer(this)
    sr!!.setRecognitionListener(object : RecognitionListener {
      override fun onResults(r: Bundle?) {
        val t = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
        if (t.isNotBlank()) web.evaluateJavascript("window.nativeResult(" + org.json.JSONObject.quote(t) + ")", null)
        again()
      }
      override fun onError(e: Int) { again() }
      override fun onReadyForSpeech(p: Bundle?) {}
      override fun onBeginningOfSpeech() {}
      override fun onRmsChanged(v: Float) {}
      override fun onBufferReceived(b: ByteArray?) {}
      override fun onEndOfSpeech() {}
      override fun onPartialResults(p: Bundle?) {}
      override fun onEvent(t: Int, p: Bundle?) {}
    })
    sr!!.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
      .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      .putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang))
  }
  fun again() { if (cont) h.postDelayed({ if (cont) startSR(lang) }, 500) }

  inner class Bridge {
    @JavascriptInterface fun listen(c: Boolean, l: String) { cont = c; runOnUiThread { startSR(l) } }
    @JavascriptInterface fun stop() {
      cont = false
      runOnUiThread { sr?.destroy(); sr = null; stopService(Intent(this@MainActivity, KeepAliveService::class.java)) }
    }
    @JavascriptInterface fun keepAlive() { startForegroundService(Intent(this@MainActivity, KeepAliveService::class.java)) }
    @JavascriptInterface fun alarm(at: Long, label: String) {
      val am = getSystemService(ALARM_SERVICE) as AlarmManager
      val pi = PendingIntent.getBroadcast(this@MainActivity, (at / 1000).toInt(),
        Intent(this@MainActivity, AlarmReceiver::class.java).putExtra("l", label),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
      try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) }
      catch (e: SecurityException) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) }
    }
  }
}
