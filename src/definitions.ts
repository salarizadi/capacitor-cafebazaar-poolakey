import {PluginListenerHandle} from "@capacitor/core";

export type PurchaseStateType =
    | 'PURCHASE_BEGAN'
    | 'FAILED_TO_BEGIN'
    | 'PURCHASED'
    | 'CANCELLED'
    | 'FAILED';

export type ConnectionStateType =
    | 'NOT_INITIALIZED'
    | 'CONNECTED'
    | 'DISCONNECTED';

export interface ProductDetails {
    sku: string;
    title: string;
    price: string;
    description: string;
}

export interface PurchaseInfo {
    orderId: string;
    purchaseToken: string;
    payload: string;
    packageName: string;
    purchaseTime: number;
    productId: string;
}

export interface PurchaseState {
    state: PurchaseStateType;
    message?: string;
    productId?: string;
    purchase?: PurchaseInfo;
}

export interface CafebazaarPoolakeyPlugin {
    /**
     * Initialize connection to Cafebazaar service
     * @param options Configuration options
     * @returns Promise with connection state
     */
    initialize(options: {
        rsaPublicKey?: string;
    }): Promise<{
        connected: boolean;
        state: ConnectionStateType;
    }>;

    /**
     * Get products information by SKUs
     * @param options List of product SKUs
     * @returns Promise with products details
     */
    getProducts(options: {
        skus: string[];
    }): Promise<{
        state: 'QUERY_SUCCEEDED';
        products: ProductDetails[];
    }>;

    /**
     * Start purchase flow for a product
     * @param options Purchase options
     * @returns Promise with purchase result
     */
    purchaseProduct(options: {
        productId: string;
        payload?: string;
        dynamicPriceToken?: string;
    }): Promise<{
        state: PurchaseStateType;
        purchase?: PurchaseInfo;
    }>;

    /**
     * Consume a purchased product
     * @param options Consume options
     * @returns Promise with consume result
     */
    consumeProduct(options: {
        token: string;
    }): Promise<{
        state: 'CONSUMED';
        consumed: boolean;
    }>;

    /**
     * Get user's purchase history
     * @returns Promise with list of purchases
     */
    getPurchaseInfo(): Promise<{
        state: 'QUERY_SUCCEEDED';
        purchases: PurchaseInfo[];
    }>;

    /**
     * Get current connection state
     * @returns Promise with connection state
     */
    getConnectionState(): Promise<{
        state: ConnectionStateType;
    }>;

    /**
     * Disconnect from Cafebazaar service
     * @returns Promise with disconnect result
     */
    disconnect(): Promise<{
        state: 'DISCONNECTED';
        disconnected: boolean;
    }>;

    /**
     * Add listener for purchase state changes
     * @param eventName Event to listen for
     * @param listenerFunc Callback function
     */
    addListener(
        eventName: 'purchaseStateChanged',
        listenerFunc: (state: PurchaseState) => void
    ): Promise<PluginListenerHandle>;

    /**
     * Remove all listeners
     */
    removeAllListeners(): Promise<void>;
}