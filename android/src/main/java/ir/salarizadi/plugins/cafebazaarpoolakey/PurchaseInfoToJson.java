package ir.salarizadi.plugins.cafebazaarpoolakey;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.util.List;
import ir.cafebazaar.poolakey.entity.PurchaseInfo;

public class PurchaseInfoToJson {

    public static JSObject convert(PurchaseInfo purchaseInfo) {
        JSObject json = new JSObject();
        json.put("orderId", purchaseInfo.getOrderId());
        json.put("purchaseToken", purchaseInfo.getPurchaseToken());
        json.put("payload", purchaseInfo.getPayload());
        json.put("packageName", purchaseInfo.getPackageName());
        json.put("purchaseTime", purchaseInfo.getPurchaseTime());
        json.put("productId", purchaseInfo.getProductId());
        return json;
    }

    public static JSArray convertList(List<PurchaseInfo> purchaseInfoList) {
        JSArray jsonArray = new JSArray();
        for (PurchaseInfo info : purchaseInfoList) {
            jsonArray.put(convert(info));
        }
        return jsonArray;
    }
}