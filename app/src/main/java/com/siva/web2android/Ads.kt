package com.siva.web2android

import android.app.Activity
import android.content.Context
import android.os.Handler
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object Ads {

    abstract class RewardedAdCallback : FullScreenContentCallback(), OnUserEarnedRewardListener

    private class ProxyRewardedCallback(val ctx: Context, val callback: RewardedAdCallback?) : RewardedAdCallback() {

        override fun onAdFailedToShowFullScreenContent(err: AdError) {
            callback?.onAdFailedToShowFullScreenContent(err)
            loadRewardedAd(ctx)
        }

        override fun onAdShowedFullScreenContent() {
            callback?.onAdShowedFullScreenContent()
        }

        override fun onAdDismissedFullScreenContent() {
            callback?.onAdDismissedFullScreenContent()
            loadRewardedAd(ctx)
        }

        override fun onAdImpression() {
            callback?.onAdImpression()
        }

        override fun onAdClicked() {
            callback?.onAdClicked()
        }

        override fun onUserEarnedReward(rewarditem: RewardItem) {
            callback?.onUserEarnedReward(rewarditem)
        }

    }

    private class ProxyFullscreenContentCallback(val ctx: Context, val callback: FullScreenContentCallback?) : FullScreenContentCallback() {

        override fun onAdFailedToShowFullScreenContent(err: AdError) {
            interstitial = null
            callback?.onAdFailedToShowFullScreenContent(err)
            loadInterstitialAd(ctx)
        }

        override fun onAdShowedFullScreenContent() {
            interstitial = null
            callback?.onAdShowedFullScreenContent()
        }

        override fun onAdDismissedFullScreenContent() {
            callback?.onAdDismissedFullScreenContent()
            loadInterstitialAd(ctx)
        }

        override fun onAdImpression() {
            callback?.onAdImpression()
        }

        override fun onAdClicked() {
            callback?.onAdClicked()
        }

    }

    private var interstitial: InterstitialAd? = null

    private var rewardedAd: RewardedInterstitialAd? = null

    private var handler: Handler? = null

    fun init(context: Context) {
        handler = Handler()
        loadInterstitialAd(context)
        loadRewardedAd(context)
    }

    private fun loadRewardedAd(ctx: Context) {
        rewardedAd = null
        RewardedInterstitialAd.load(ctx, Config.rewardedAdUnitId, AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                Ads.rewardedAd = rewardedAd
            }

            override fun onAdFailedToLoad(err: LoadAdError) {
                handler?.postDelayed({
                    loadRewardedAd(ctx)
                }, 3000)
            }
        })
    }

    fun hasRewardedAd(): Boolean {
        return rewardedAd != null
    }

    fun showRewardedAd(activity: Activity, callback: RewardedAdCallback?): Boolean {
        if (!Config.rewardedAdEnabled) return false

        if (hasRewardedAd()) {
            val rewardedAdCallback = ProxyRewardedCallback(activity?.applicationContext, callback)
            rewardedAd?.fullScreenContentCallback = rewardedAdCallback
            rewardedAd?.show(activity, rewardedAdCallback)
            return true
        }
        return false
    }

    private fun loadInterstitialAd(ctx: Context) {
        interstitial = null
        InterstitialAd.load(ctx, Config.interstitialAdUnitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                interstitial = interstitialAd
            }

            override fun onAdFailedToLoad(err: LoadAdError) {
                interstitial = null
                handler?.postDelayed({
                    loadInterstitialAd(ctx)
                }, 3000)
            }
        })
    }

    fun showInterstitials(activity: Activity, callback: FullScreenContentCallback?): Boolean {
        if (!Config.interstitialAdEnabled) return false

        return if (interstitial != null) {
            val fullScreenCallback = ProxyFullscreenContentCallback(activity?.applicationContext, callback)
            interstitial?.fullScreenContentCallback = fullScreenCallback
            interstitial?.show(activity)
            true
        } else {
            false
        }
    }

}
