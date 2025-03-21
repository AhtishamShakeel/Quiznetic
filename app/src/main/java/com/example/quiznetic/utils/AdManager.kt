package com.example.quiznetic.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
// import com.google.android.gms.ads.AdError
// import com.google.android.gms.ads.AdListener
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.AdSize
// import com.google.android.gms.ads.AdView
// import com.google.android.gms.ads.FullScreenContentCallback
// import com.google.android.gms.ads.LoadAdError
// import com.google.android.gms.ads.MobileAds
// import com.google.android.gms.ads.interstitial.InterstitialAd
// import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import com.unity3d.services.banners.BannerErrorInfo

class AdManager {
    companion object {
        private const val TAG = "AdManager"
        
        // AdMob test IDs - Commented out temporarily
        // private const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        // private const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        
        // Unity Ads test IDs - Using proper Unity test game ID
        private const val UNITY_GAME_ID = "5816684" // Unity test Game ID
        private const val UNITY_BANNER_ID = "Banner_Android"
        private const val UNITY_INTERSTITIAL_ID = "Interstitial_Android"
        
        private const val TEST_MODE = false
        
        // private var admobInterstitialAd: InterstitialAd? = null
        private var isUnityAdsInitialized = false
        
        /**
         * Initialize ad SDKs
         */
        fun initialize(context: Context) {
            // Initialize Unity Ads first
            
            UnityAds.initialize(context, UNITY_GAME_ID, TEST_MODE, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    
                    isUnityAdsInitialized = true
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError, message: String) {
                    
                }
            })
            
            // Initialize AdMob - Commented out temporarily
            // MobileAds.initialize(context) { initializationStatus ->
            //     
            // }
        }
        
        /**
         * Load and show a banner ad in the provided container
         */
        fun loadBannerAd(activity: Activity, adContainer: ViewGroup) {
            // First try Unity Ads
            if (isUnityAdsInitialized) {
                
                val unityBanner = BannerView(activity, UNITY_BANNER_ID, UnityBannerSize(320, 50))
                adContainer.removeAllViews()
                adContainer.addView(unityBanner)
                
                // Set a listener to detect failure and load AdMob as fallback
                unityBanner.setListener(object : BannerView.IListener {
                    override fun onBannerLoaded(bannerAdView: BannerView) {
                        
                    }
                    
                    override fun onBannerFailedToLoad(bannerAdView: BannerView, errorInfo: BannerErrorInfo) {
                        
                        // Commented out AdMob fallback temporarily
                        // loadAdMobBanner(activity, adContainer)
                    }
                    
                    override fun onBannerClick(bannerAdView: BannerView) {
                        
                    }
                    
                    override fun onBannerLeftApplication(bannerAdView: BannerView) {
                        
                    }
                    
                    override fun onBannerShown(bannerAdView: BannerView) {
                        
                    }
                })
                
                unityBanner.load()
            } else {
                
                // Commented out AdMob fallback temporarily
                // loadAdMobBanner(activity, adContainer)
            }
        }
        
        // Commented out AdMob banner loading function temporarily
        /*
        private fun loadAdMobBanner(activity: Activity, adContainer: ViewGroup) {
            // Load AdMob as fallback
            
            val adView = AdView(activity).apply {
                adUnitId = ADMOB_BANNER_ID
                setAdSize(AdSize.BANNER)
            }
            
            val adRequest = AdRequest.Builder().build()
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    
                }
            }
            
            adContainer.removeAllViews()
            adContainer.addView(adView)
            adView.loadAd(adRequest)
        }
        */
        
        /**
         * Preload an interstitial ad
         */
        fun preloadInterstitialAd(context: Context) {
            // Unity Ads are loaded on-demand when showing
            
            
            // Commented out AdMob interstitial loading temporarily
            /*
            InterstitialAd.load(context, ADMOB_INTERSTITIAL_ID, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        
                        admobInterstitialAd = interstitialAd
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        
                        admobInterstitialAd = null
                    }
                })
            */
        }
        
        /**
         * Show an interstitial ad
         */
        fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
            // First try Unity Ads
            if (isUnityAdsInitialized) {
                
                showUnityInterstitial(activity) { success ->
                    if (success) {
                        onAdClosed()
                    } else {
                        // Commented out AdMob fallback temporarily
                        
                        onAdClosed()
                    }
                }
            } else {
                
                onAdClosed()
            }
        }
        
        private fun showUnityInterstitial(activity: Activity, onComplete: (Boolean) -> Unit) {
            val loadListener = object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String) {
                    
                    UnityAds.show(activity, UNITY_INTERSTITIAL_ID, object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                            
                            onComplete(false)
                        }
                        
                        override fun onUnityAdsShowStart(placementId: String) {
                            
                        }
                        
                        override fun onUnityAdsShowClick(placementId: String) {
                            
                        }
                        
                        override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                            
                            onComplete(true)
                        }
                    })
                }
                
                override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                    
                    onComplete(false)
                }
            }
            
            
            UnityAds.load(UNITY_INTERSTITIAL_ID, loadListener)
        }
        
        // Commented out AdMob interstitial showing function temporarily
        /*
        private fun showAdMobInterstitial(activity: Activity, onAdClosed: () -> Unit) {
            if (admobInterstitialAd != null) {
                
                admobInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        
                        admobInterstitialAd = null
                        // Preload next ad
                        preloadInterstitialAd(activity)
                        onAdClosed()
                    }
                    
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        
                        admobInterstitialAd = null
                        onAdClosed()
                    }
                }
                admobInterstitialAd?.show(activity)
            } else {
                
                // No ads available
                onAdClosed()
            }
        }
        */
    }
} 