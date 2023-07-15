package com.unity3d.util;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.*;
import com.unity3d.network.NetworkManager;
import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InAppManager implements PurchasesUpdatedListener
{
    private static InAppManager _sharedInAppManager = null;
    private Context mContext;
    private BillingClient mBillingClient;

    private NetworkManager networkManager;

    private String skuString;
    private Boolean enableInApp;

    public ArrayList<String> inAppCodeArray;
    public HashMap<String, String> moneyMap;
    public ArrayList<String> inAppPrice;
    private List<SkuDetails> mSkuDetailsList_item;
    private ConsumeResponseListener mConsumeListener;

    public static InAppManager sharedInAppManager()
    {
        synchronized ( InAppManager.class )
        {
            if ( _sharedInAppManager == null )
                _sharedInAppManager = new InAppManager();
        }

        return _sharedInAppManager;
    }

    public void init( Context context )
    {
        mContext = context;

        networkManager = NetworkManager.sharedNetworkManager();

        enableInApp = false;
        inAppCodeArray = new ArrayList<String>();
        inAppPrice = new ArrayList<String>();
        moneyMap = new HashMap<String, String>();

        mBillingClient = BillingClient.newBuilder(mContext)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        // 상품 소모결과 리스너
        mConsumeListener = new ConsumeResponseListener()
        {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken)
            {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                {
                    DebugLog.log( "상품을 성공적으로 소모하였습니다. 소모된 상품 => " + purchaseToken);
                    checkPurchase( purchaseToken, skuString );
                    return;
                }
                else
                {
                    DebugLog.log( "상품 소모에 실패하였습니다. 오류코드 (" + billingResult.getResponseCode() + "), 대상 상품 코드: " + purchaseToken);
                    failPurchase();
                    return;
                }
            }
        };
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases)
    {
        DebugLog.log("====================== onPurchasesUpdated ========================");

        //결제에 성공한 경우
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null)
        {
            DebugLog.log( "결제에 성공했으며, 아래에 구매한 상품들이 나열됨");

            for (Purchase _pur : purchases)
            {
                DebugLog.log( "purchases: " + purchases);
                handlePurchase ( _pur );
            }
        }

        // 사용자가 결제를 취소한 경우
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED)
        {
            DebugLog.log("사용자에 의해 결제취소");
            failPurchase();
        }

        // 그 외에 다른 결제 실패 이유
        else
        {
            DebugLog.log("결제가 취소 되었습니다. 종료코드: " + billingResult.getResponseCode());
            failPurchase();
        }
    }

    void handlePurchase(Purchase purchase)
    {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
        {
            // 인앱 소비
            // TODO 인앱 구매 결과전송 함수 호출
            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            mBillingClient.consumeAsync(consumeParams, mConsumeListener);

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING)
        {
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.
            failPurchase();
        }
    }

    private void successCheckPurchase()
    {
        UnityPlayer.UnitySendMessage( "ViewInApp", "ResultCheckPurchase", "true" );
    }

    private void failCheckPurchase()
    {
        UnityPlayer.UnitySendMessage( "ViewInApp", "ResultCheckPurchase", "false" );
    }

    private void checkPurchase( String json, String sku )
    {
        skuString = sku;
        UnityPlayer.UnitySendMessage( "ViewInApp", "CheckVerifyingreceipt", json );
    }

    private void failPurchase()
    {
        UnityPlayer.UnitySendMessage( "ViewInApp", "ResultLaunchPurchase", "1" );
    }
    public void executePurchase()
    {
        mBillingClient.startConnection(new BillingClientStateListener()
        {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult)
            {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK)
                {
                    // The BillingClient is ready. You can query purchases here.
                    getSkuDetailList();
                }
                else
                {
                    failCheckPurchase();
                }
            }
            @Override
            public void onBillingServiceDisconnected()
            {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                failCheckPurchase();
            }
        });
    }

    public void launchPurchase( String msg, Activity act )
    {
        skuString = msg;

        DebugLog.log( "networkManager.appLanguage : " + networkManager.appLanguage );
        DebugLog.log( "skuString : " + skuString );

        SkuDetails skuDetails = null;

        if (null != mSkuDetailsList_item)
        {
            for (int i = 0; i < mSkuDetailsList_item.size(); i++)
            {
                SkuDetails details = mSkuDetailsList_item.get(i);

                if (details.getSku().equals( skuString ))
                {
                    skuDetails = details;
                    break;
                }
            }

            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            mBillingClient.launchBillingFlow( act, flowParams );
        }
    }

    private void getSkuDetailList()
    {
        SkuDetailsParams.Builder params_item = SkuDetailsParams.newBuilder();
        params_item.setSkusList(inAppCodeArray).setType(BillingClient.SkuType.INAPP);

        mBillingClient.querySkuDetailsAsync(params_item.build(), new SkuDetailsResponseListener()
        {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList)
            {
                // 상품 정보를 가지고 오지 못한 경우
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK)
                {
                    DebugLog.log("(인앱) 상품 정보를 가지고 오던 중 오류가 발생했습니다.\n오류코드: " + billingResult.getResponseCode());
                    failCheckPurchase();
                    return;
                }

                if (skuDetailsList == null)
                {
                    DebugLog.log( "(인앱) 상품 정보가 존재하지 않습니다.");
                    failCheckPurchase();
                    return;
                }
                //응답 받은 데이터들의 숫자를 출력
                DebugLog.log("(인앱) 응답 받은 데이터 숫자: " + skuDetailsList.size());

                inAppPrice.clear();
                mSkuDetailsList_item = null;

                for (String strCode : inAppCodeArray )
                {
                    for (int sku_idx = 0; sku_idx < skuDetailsList.size(); sku_idx++)
                    {
                        SkuDetails _skuDetail = skuDetailsList.get(sku_idx);

                        if ( _skuDetail.getSku().equals( strCode ) )
                            inAppPrice.add( _skuDetail.getPrice() );
                    }
                }


//                //받아온 상품 정보를 차례로 호출
//                for (int sku_idx = 0; sku_idx < skuDetailsList.size(); sku_idx++)
//                {
//                    //해당 인덱스의 객체를 가지고 옴
//                    SkuDetails _skuDetail = skuDetailsList.get(sku_idx);
//                    //해당 인덱스의 상품 정보를 출력
//                    DebugLog.log( _skuDetail.getSku() + ": " + _skuDetail.getTitle() + ", " + _skuDetail.getPrice());
//                    DebugLog.log( _skuDetail.getOriginalJson());
//
//                    inAppPrice.add( _skuDetail.getPrice() );
//                }

                mSkuDetailsList_item = skuDetailsList;
                successCheckPurchase();
            }
        });
    }
}
