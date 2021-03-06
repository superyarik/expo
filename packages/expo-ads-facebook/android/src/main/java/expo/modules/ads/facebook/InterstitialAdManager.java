package expo.modules.ads.facebook;

import android.content.Context;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import expo.core.ExportedModule;
import expo.core.ModuleRegistry;
import expo.core.Promise;
import expo.core.interfaces.ExpoMethod;
import expo.core.interfaces.LifecycleEventListener;
import expo.core.interfaces.ModuleRegistryConsumer;
import expo.core.interfaces.services.UIManager;

public class InterstitialAdManager extends ExportedModule implements InterstitialAdListener, LifecycleEventListener, ModuleRegistryConsumer {

  private Promise mPromise;
  private boolean mDidClick = false;
  private InterstitialAd mInterstitial;
  private UIManager mUIManager;

  public InterstitialAdManager(Context reactContext) {
    super(reactContext);
  }

  @Override
  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    if (mUIManager != null) {
      mUIManager.unregisterLifecycleEventListener(this);
    }
    mUIManager = moduleRegistry.getModule(UIManager.class);
    mUIManager.registerLifecycleEventListener(this);
  }

  @ExpoMethod
  public void showAd(String placementId, Promise p) {
    if (mPromise != null) {
      p.reject("E_FAILED_TO_SHOW", "Only one `showAd` can be called at once");
      return;
    }

    mPromise = p;
    mInterstitial = new InterstitialAd(getContext(), placementId);
    mInterstitial.setAdListener(this);
    mInterstitial.loadAd();
  }

  @Override
  public String getName() {
    return "CTKInterstitialAdManager";
  }

  @Override
  public void onError(Ad ad, AdError adError) {
    mPromise.reject("E_FAILED_TO_LOAD", adError.getErrorMessage());
    cleanUp();
  }

  @Override
  public void onAdLoaded(Ad ad) {
    if (ad == mInterstitial) {
      mInterstitial.show();
    }
  }

  @Override
  public void onAdClicked(Ad ad) {
    mDidClick = true;
  }

  @Override
  public void onInterstitialDismissed(Ad ad) {
    mPromise.resolve(mDidClick);
    cleanUp();
  }

  @Override
  public void onInterstitialDisplayed(Ad ad) {

  }

  @Override
  public void onLoggingImpression(Ad ad) {
  }

  private void cleanUp() {
    mPromise = null;
    mDidClick = false;
    mUIManager.unregisterLifecycleEventListener(this);
    mUIManager = null;

    if (mInterstitial != null) {
      mInterstitial.destroy();
      mInterstitial = null;
    }
  }

  @Override
  public void onHostResume() {

  }

  @Override
  public void onHostPause() {

  }

  @Override
  public void onHostDestroy() {
    cleanUp();
  }
}
