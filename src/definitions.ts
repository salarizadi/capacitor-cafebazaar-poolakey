export interface CafebazaarPoolakeyPlugin {
    initialize(options: {
        rsaPublicKey?: string;
    }): Promise<{
        connected: boolean;
        state: ConnectionState;
    }>;

    getProducts(options: {
        skus: string[];
    }): Promise<{
        state: 'QUERY_SUCCEEDED';
        products: ProductDetails[];
    }>;

    purchaseProduct(options: {
        productId: string;
        payload?: string;
        dynamicPriceToken?: string;
    }): Promise<{
        state: PurchaseState;
        purchase?: PurchaseInfo;
    }>;

    consumeProduct(options: {
        token: string;
    }): Promise<{
        state: 'CONSUMED';
        consumed: boolean;
    }>;

    getPurchaseInfo(): Promise<{
        state: 'QUERY_SUCCEEDED';
        purchases: PurchaseInfo[];
    }>;

    getConnectionState(): Promise<{
        state: ConnectionState;
    }>;

    disconnect(): Promise<{
        state: 'DISCONNECTED';
        disconnected: boolean;
    }>;
}

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

export type ConnectionState =
    | 'NOT_INITIALIZED'
    | 'CONNECTED'
    | 'DISCONNECTED';

export type PurchaseState =
    | 'PURCHASE_BEGAN'
    | 'PURCHASED'
    | 'CONSUMED';