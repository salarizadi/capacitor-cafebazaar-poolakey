package ir.salarizadi.plugins.cafebazaarpoolakey;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import ir.cafebazaar.poolakey.Connection;
import ir.cafebazaar.poolakey.ConnectionState;
import ir.cafebazaar.poolakey.Payment;
import ir.cafebazaar.poolakey.config.PaymentConfiguration;
import ir.cafebazaar.poolakey.config.SecurityCheck;
import ir.cafebazaar.poolakey.request.PurchaseRequest;
import ir.cafebazaar.poolakey.entity.SkuDetails;

import kotlin.Unit;

@CapacitorPlugin(name = "CafebazaarPoolakey")
public class CafebazaarPoolakeyPlugin extends Plugin {

    private static final String TAG = "CafebazaarPoolakey";
    private Payment payment;
    private Connection paymentConnection;

    @PluginMethod
    public void initialize(PluginCall call) {
        try {
            String rsaPublicKey = call.getString("rsaPublicKey", "");

            SecurityCheck securityCheck = rsaPublicKey.isEmpty() ?
                    SecurityCheck.Disable.INSTANCE :
                    new SecurityCheck.Enable(rsaPublicKey);

            PaymentConfiguration paymentConfig = new PaymentConfiguration(securityCheck);
            payment = new Payment(getContext(), paymentConfig);

            paymentConnection = payment.connect(connectionCallback -> {
                connectionCallback.connectionSucceed(() -> {
                    JSObject result = new JSObject();
                    result.put("connected", true);
                    result.put("state", "CONNECTED");
                    call.resolve(result);
                    return Unit.INSTANCE;
                });

                connectionCallback.connectionFailed(throwable -> {
                    call.reject("Connection failed: " + throwable.getMessage(), "CONNECTION_FAILED");
                    return Unit.INSTANCE;
                });

                connectionCallback.disconnected(() -> {
                    JSObject result = new JSObject();
                    result.put("connected", false);
                    result.put("state", "DISCONNECTED");
                    call.reject("Disconnected from Bazaar service", "DISCONNECTED");
                    return Unit.INSTANCE;
                });

                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            call.reject("Initialization failed: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void getProducts(PluginCall call) {
        if (payment == null || paymentConnection == null) {
            call.reject("Payment not initialized. Call initialize() first.");
            return;
        }

        JSArray skuArray = call.getArray("skus");
        if (skuArray == null) {
            call.reject("SKUs list is required");
            return;
        }

        List<String> skuList = new ArrayList<>();
        for (int i = 0; i < skuArray.length(); i++) {
            try {
                skuList.add(skuArray.getString(i));
            } catch (Exception e) {
                call.reject("Invalid SKU format at index " + i);
                return;
            }
        }

        payment.getInAppSkuDetails(skuList, callback -> {
            callback.getSkuDetailsSucceed(skuDetails -> {
                JSObject result = new JSObject();
                JSArray products = new JSArray();

                for (SkuDetails details : skuDetails) {
                    JSObject product = new JSObject();
                    product.put("sku", details.getSku());
                    product.put("title", details.getTitle());
                    product.put("price", details.getPrice());
                    product.put("description", details.getDescription());
                    products.put(product);
                }

                result.put("products", products);
                result.put("state", "QUERY_SUCCEEDED");
                call.resolve(result);
                return Unit.INSTANCE;
            });

            callback.getSkuDetailsFailed(throwable -> {
                call.reject("Failed to get products: " + throwable.getMessage(), "QUERY_FAILED");
                return Unit.INSTANCE;
            });

            return Unit.INSTANCE;
        });
    }

    @PluginMethod
    public void purchaseProduct(PluginCall call) {
        if (payment == null || paymentConnection == null) {
            call.reject("Payment not initialized. Call initialize() first.");
            return;
        }

        if (paymentConnection.getState() != ConnectionState.Connected.INSTANCE) {
            call.reject("Not connected to Bazaar service");
            return;
        }

        String productId = call.getString("productId");
        String payload = call.getString("payload", "");
        String dynamicPriceToken = call.getString("dynamicPriceToken");

        if (productId == null || productId.isEmpty()) {
            call.reject("Product ID is required");
            return;
        }

        call.setKeepAlive(true);

        PurchaseRequest request = new PurchaseRequest(
                productId,
                payload,
                dynamicPriceToken
        );

        payment.purchaseProduct(getActivity().getActivityResultRegistry(), request, purchaseCallback -> {
            purchaseCallback.purchaseFlowBegan(() -> {
                JSObject beganData = new JSObject();
                beganData.put("productId", productId);
                notifyPurchaseState("PURCHASE_BEGAN", beganData);
                return Unit.INSTANCE;
            });

            purchaseCallback.failedToBeginFlow(throwable -> {
                JSObject failedData = new JSObject();
                failedData.put("message", throwable.getMessage());
                notifyPurchaseState("FAILED_TO_BEGIN", failedData);
                call.reject("Failed to begin purchase: " + throwable.getMessage(), "PURCHASE_BEGIN_FAILED");
                return Unit.INSTANCE;
            });

            purchaseCallback.purchaseSucceed(purchaseInfo -> {
                JSObject purchase = new JSObject();
                purchase.put("orderId", purchaseInfo.getOrderId());
                purchase.put("purchaseToken", purchaseInfo.getPurchaseToken());
                purchase.put("payload", purchaseInfo.getPayload());
                purchase.put("packageName", purchaseInfo.getPackageName());
                purchase.put("purchaseTime", purchaseInfo.getPurchaseTime());
                purchase.put("productId", purchaseInfo.getProductId());

                JSObject successData = new JSObject();
                successData.put("purchase", purchase);
                notifyPurchaseState("PURCHASED", successData);
                call.resolve(successData);
                return Unit.INSTANCE;
            });

            purchaseCallback.purchaseCanceled(() -> {
                JSObject cancelData = new JSObject();
                cancelData.put("message", "Purchase cancelled by user");
                notifyPurchaseState("CANCELLED", cancelData);
                call.reject("Purchase cancelled by user", "PURCHASE_CANCELLED");
                return Unit.INSTANCE;
            });

            purchaseCallback.purchaseFailed(throwable -> {
                JSObject failData = new JSObject();
                failData.put("message", throwable.getMessage());
                notifyPurchaseState("FAILED", failData);
                call.reject("Purchase failed: " + throwable.getMessage(), "PURCHASE_FAILED");
                return Unit.INSTANCE;
            });

            return Unit.INSTANCE;
        });
    }

    private void notifyPurchaseState(String state, JSObject data) {
        JSObject result = new JSObject();
        result.put("state", state);
        if (data != null) {
            for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                String key = it.next();
                result.put(key, data.opt(key));
            }
        }
        notifyListeners("purchaseStateChanged", result);
    }

    @PluginMethod
    public void consumeProduct(PluginCall call) {
        if (payment == null || paymentConnection == null) {
            call.reject("Payment not initialized. Call initialize() first.");
            return;
        }

        String token = call.getString("token");
        if (token == null || token.isEmpty()) {
            call.reject("Purchase token is required");
            return;
        }

        payment.consumeProduct(token, consumeCallback -> {
            consumeCallback.consumeSucceed(() -> {
                JSObject result = new JSObject();
                result.put("state", "CONSUMED");
                result.put("consumed", true);
                call.resolve(result);
                return Unit.INSTANCE;
            });

            consumeCallback.consumeFailed(throwable -> {
                call.reject("Consume failed: " + throwable.getMessage(), "CONSUME_FAILED");
                return Unit.INSTANCE;
            });

            return Unit.INSTANCE;
        });
    }

    @PluginMethod
    public void getPurchaseInfo(PluginCall call) {
        if (payment == null || paymentConnection == null) {
            call.reject("Payment not initialized. Call initialize() first.");
            return;
        }

        payment.getPurchasedProducts(purchaseQueryCallback -> {
            purchaseQueryCallback.querySucceed(purchaseInfoList -> {
                JSObject result = new JSObject();
                result.put("state", "QUERY_SUCCEEDED");
                JSArray purchases = PurchaseInfoToJson.convertList(purchaseInfoList);
                result.put("purchases", purchases);
                call.resolve(result);
                return Unit.INSTANCE;
            });

            purchaseQueryCallback.queryFailed(throwable -> {
                call.reject("Query failed: " + throwable.getMessage(), "QUERY_FAILED");
                return Unit.INSTANCE;
            });

            return Unit.INSTANCE;
        });
    }

    @PluginMethod
    public void getConnectionState(PluginCall call) {
        if (paymentConnection == null) {
            JSObject result = new JSObject();
            result.put("state", "NOT_INITIALIZED");
            call.resolve(result);
            return;
        }

        ConnectionState state = paymentConnection.getState();
        JSObject result = new JSObject();
        result.put("state", state == ConnectionState.Connected.INSTANCE ? "CONNECTED" : "DISCONNECTED");
        call.resolve(result);
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        if (paymentConnection != null) {
            paymentConnection.disconnect();
            JSObject result = new JSObject();
            result.put("state", "DISCONNECTED");
            result.put("disconnected", true);
            call.resolve(result);
        } else {
            call.reject("Not connected to Bazaar service");
        }
    }

    @Override
    protected void handleOnDestroy() {
        if (paymentConnection != null) {
            paymentConnection.disconnect();
        }
        super.handleOnDestroy();
    }
}