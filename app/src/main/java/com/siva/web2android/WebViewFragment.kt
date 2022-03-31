package com.siva.web2android

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.rewarded.RewardItem


class WebViewFragment : Fragment() {

    private var webview: WebView? = null

    private var mainActivity: MainActivity? = null

    private var customView: View? = null

    private var frame: FrameLayout? = null

    private var progress: ProgressBar? = null

    private var rootView: ViewGroup? = null

    private var packageManager: PackageManager? = null

    private val downloadListener =
        DownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val downloadManager: DownloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url))
            downloadManager.enqueue(request)
        }

    private val webViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val uri = Uri.parse(url)
            if (url?.matches(Regex.fromLiteral("^https?.+")) == true) {
                val intent = Intent(Intent.ACTION_VIEW).also { it.data = uri }
                startActivity(Intent.createChooser(intent, null))
                return true
            }
            return super.shouldOverrideUrlLoading(view, url)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return this.shouldOverrideUrlLoading(view, request?.url?.toString())
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (view == webview) {
                progress?.visibility = View.VISIBLE
            }
            if (Config.interstitialAdEnabled && Config.interstitialAdUnitId != null && Config.interstitialAdOnPageLoad) {
                view?.context?.let {
                    Ads.showInterstitials(it as Activity, null)
                }
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            if (view == webview) {
                progress?.visibility = View.INVISIBLE
            }
        }

        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
            return super.shouldInterceptRequest(view, url)
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        override fun onTooManyRedirects(view: WebView?, cancelMsg: Message?, continueMsg: Message?) {
            super.onTooManyRedirects(view, cancelMsg, continueMsg)
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            if (view == webview) {
                progress?.visibility = View.INVISIBLE
            }
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (view == webview) {
                progress?.visibility = View.INVISIBLE
            }
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {

        }

        override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
            resend?.sendToTarget()
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {

        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            val ctx = view?.context
            ctx?: return
            AlertDialog.Builder(ctx)
                .setMessage(when (error?.primaryError) {
                    SslError.SSL_EXPIRED -> "SSL certificate expired"
                    SslError.SSL_DATE_INVALID -> "SSL date is invalid"
                    SslError.SSL_IDMISMATCH -> "SSL ID mismatch"
                    SslError.SSL_INVALID -> "SSL certificate is invalid"
                    SslError.SSL_NOTYETVALID -> "SSL not yet valid"
                    SslError.SSL_UNTRUSTED -> "SSL certificate is untrusted"
                    else -> "SSL error"
                }).setPositiveButton(R.string.button_proceed) { _, _ ->
                    handler?.proceed()
                }.setNegativeButton(R.string.button_abort) { _, _ ->
                    handler?.cancel()
                }.setCancelable(false)
                .show()
        }

        override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {

        }

        override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?) {
            handler?.takeIf { it.useHttpAuthUsernamePassword() }?.let {
                
            }
        }

    }

    inner class AppChromeClient : WebChromeClient() {

        override fun onReceivedTitle(view: WebView?, title: String?) {
            view?.tag?.let { if (it is AlertDialog) it.setTitle(title) }
        }

        override fun onShowFileChooser(webView: WebView?,
                                       filePathCallback: ValueCallback<Array<Uri>>?,
                                       fileChooserParams: FileChooserParams?): Boolean {
            if (!Config.fileAccessEnabled) return false
            FileChooserFragment().choose(fileChooserParams, filePathCallback, childFragmentManager)
            return true
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            GeoLocationFragment().requestPermission(origin, context, callback, childFragmentManager)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPermissionRequest(request: PermissionRequest?) {
            PermissionRequestFragment().requestPermission(request, childFragmentManager)
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest?) {
            childFragmentManager?.findFragmentByTag(PermissionRequestFragment.NAME)?.let {
                childFragmentManager?.beginTransaction()?.remove(it)?.commit()
            }
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            showCustomView(view, callback)
        }

        override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: CustomViewCallback?) {
            this.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            hideCustomView()
        }

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            if (!Config.multipleWindowsEnabled) return false
            view?.post { createWindow(resultMsg, isDialog) }
            return true
        }

        override fun onRequestFocus(view: WebView?) {
            super.onRequestFocus(view)
        }

        override fun onCloseWindow(window: WebView?) {
            window?.tag?.let { if (it is AlertDialog) it.dismiss() }
        }

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return alert(message, result, view)
        }

        override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return confirm(message, result, view)
        }

        override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
            return prompt(message, defaultValue, result, view)
        }

        override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return super.onJsBeforeUnload(view, url, message, result)
        }

    }

    private val chromeClient = AppChromeClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        packageManager = context.packageManager
        mainActivity = context as MainActivity
        mainActivity?.webViewFragment = this
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity?.webViewFragment = null
        mainActivity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_web_view, container, false) as ViewGroup?
            frame = rootView?.findViewById(R.id.frame)
            progress = rootView?.findViewById(R.id.progress_bar)
            progress?.visibility = View.INVISIBLE
            webview = createWebView()
            webview?.let {
                it.setBackgroundColor(0xffffff)
                frame?.addView(it, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                it.requestFocus()
                it.loadUrl(Config.url)
//                it.loadData("<html><body bgcolor=\"white\">test</body></html>", "text/html", null)
            }
        }
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        webview?.destroy()
    }

    private fun createWebView(): WebView? {
        val ctx = context ?: return null
        val webView = WebView(ctx)
        webView.setDownloadListener(downloadListener)
        webView.webChromeClient = chromeClient
        webView.webViewClient = webViewClient
        webView.addJavascriptInterface(Js(webView), "Web2App")

        with(webView.settings) {
            javaScriptEnabled = Config.javascriptEnabled
            loadsImagesAutomatically = true
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = Config.jsCanOpenWindow
            allowFileAccess = Config.fileAccessEnabled
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            builtInZoomControls = Config.builtInZoomControls

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            setSupportMultipleWindows(Config.multipleWindowsEnabled)
            setGeolocationEnabled(Config.locationAccessEnabled)
            setNeedInitialFocus(true)
            setSupportZoom(Config.builtInZoomEnabled)
        }

        CookieManager.getInstance().apply {
            acceptCookie()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                acceptThirdPartyCookies(webView)
            }
        }

        return webView
    }

    private fun doesAnyAppHandleUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        val resolverInfo = packageManager?.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolverInfo != null) return true
        return false
    }

    fun alert(message: String?, result: JsResult?, webview: WebView?): Boolean {
        return context?.let {
            AlertDialog.Builder(it).setMessage(message)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    result?.confirm()
                }.setOnDismissListener {
                result?.confirm()
            }.show()
            return true
        } ?: false
    }

    fun confirm(message: String?, result: JsResult?, webview: WebView?): Boolean {
        return context?.let {
            AlertDialog.Builder(it).setMessage(message).setPositiveButton(R.string.button_ok) { _, _ ->
                result?.confirm()
            }.setNegativeButton(R.string.button_cancel) { _, _ ->
                result?.cancel()
            }.setOnCancelListener {
                result?.cancel()
            }.show()
            return true
        } ?: false
    }

    fun prompt(message: String?, defaultValue: String?, result: JsPromptResult?, webView: WebView?): Boolean {
        return context?.let {
            AlertDialog.Builder(it)
                .setView(R.layout.prompt)
                .setPositiveButton(R.string.button_ok) { dialog, _ ->
                    result?.confirm((dialog as AlertDialog).findViewById<EditText>(R.id.edit_prompt)?.text?.toString())
                }.setNegativeButton(R.string.button_cancel) { _, _ ->
                    result?.cancel()
                }.setOnCancelListener {
                    result?.cancel()
                }.show().apply {
                    findViewById<TextView>(R.id.text_message)?.text = message
                    findViewById<EditText>(R.id.edit_prompt)?.setText(defaultValue)
                }
            return true
        } ?: false
    }

    inner class Js(private val webview: WebView) {

        @JavascriptInterface
        fun showInterstitials(result: String?, opened: String?, closed: String?,
                              impression: String?, clicked: String?, leftApp: String?) {
            val activity = this@WebViewFragment.activity
            var r = false
            if (activity != null) {
                r = Ads.showInterstitials(activity, object : FullScreenContentCallback() {

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                    }

                    override fun onAdShowedFullScreenContent() {
                        webview.loadUrl("javascript:$opened();")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        webview.loadUrl("javascript:$closed();")
                    }

                    override fun onAdClicked() {
                        webview.loadUrl("javascript:$clicked();")
                    }

                    override fun onAdImpression() {
                        webview.loadUrl("javascript:$impression();")
                    }

                })
            }
            webview.loadUrl("javascript:$result($r);")
        }

        @JavascriptInterface
        fun hasRewardedAd(callback: String?) {
            val r = Ads.hasRewardedAd()
            webview.loadUrl("javascript:$callback($r);")
        }

        @JavascriptInterface
        fun showRewardedAd(result: String?, opened: String?, closed: String?,
                            reward: String?, error: String?) {
            val r: Boolean = activity?.let {

                Ads.showRewardedAd(it, object: Ads.RewardedAdCallback() {

                    override fun onAdFailedToShowFullScreenContent(err: AdError) {
                        val code: String = when (err.code) {
                            AdRequest.ERROR_CODE_NO_FILL,
                            AdRequest.ERROR_CODE_MEDIATION_NO_FILL -> "no fill"
                            AdRequest.ERROR_CODE_NETWORK_ERROR -> "network error"
                            AdRequest.ERROR_CODE_INTERNAL_ERROR -> "internal error"
                            AdRequest.ERROR_CODE_INVALID_REQUEST -> "invalid request"
                            AdRequest.ERROR_CODE_APP_ID_MISSING -> "app id missing"
                            else -> "unknown error"
                        }
                        webview.loadUrl("javascript:$error($code, ${err.message});")
                    }

                    override fun onAdShowedFullScreenContent() {
                        webview.loadUrl("javascript:$opened();")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        webview.loadUrl("javascript:$closed();")
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                    }

                    override fun onUserEarnedReward(item: RewardItem) {
                        webview.loadUrl("javascript:$reward(${item.amount});")
                    }

                })

            } ?: false

            webview.loadUrl("javascript:$result($r);")
        }

        @JavascriptInterface
        fun showBanner(callback: String?) {

        }

        @JavascriptInterface
        fun hideBanner(callback: String?) {

        }

    }

    fun createWindow(message: Message?, isDialog: Boolean) {
        val webView = createWebView()
        webView ?: return
        webView.tag = AlertDialog.Builder(webView.context,
            if (isDialog) R.style.Theme_Dialog else R.style.Theme_Window).apply {
            setView(webView)
            setCancelable(true)
        }.show()
        message?.let {
            (it.obj as WebView.WebViewTransport).webView = webView
            it.sendToTarget()
        }
    }

    fun showCustomView(view: View?, customViewCallback: WebChromeClient.CustomViewCallback?) {
        val ctx = context
        ctx ?: return
        hideCustomView()
        val dialog = AlertDialog.Builder(ctx, R.style.Theme_CustomView).setView(view)
            .setCancelable(true)
            .setOnDismissListener { customViewCallback?.onCustomViewHidden() }
            .show()
        customView = view
        customView?.tag = customViewCallback
        customView?.setTag(R.id.customViewDiaog, dialog)
    }

    fun hideCustomView() {
        customView ?: return
        val customViewCallback = customView?.tag as WebChromeClient.CustomViewCallback
        val dialog = customView?.getTag(R.id.customViewDiaog) as AlertDialog
        dialog?.dismiss()
        customViewCallback?.onCustomViewHidden()
        customView = null
    }

    fun onBackPressed(): Boolean {
        webview?.let {
            if (it.canGoBack()) {
                it.goBack()
                return true
            }
        }
        return false
    }

}
