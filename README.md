# @salarizadi/capacitor-cafebazaar-poolakey

[![npm version](https://badge.fury.io/js/%40salarizadi%2Fcapacitor-cafebazaar-poolakey.svg)](https://badge.fury.io/js/%40salarizadi%2Fcapacitor-cafebazaar-poolakey)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Capacitor plugin for implementing Cafebazaar (Iranian Android App Store) in-app billing using the Poolakey library.

## Features

- 🔒 Secure payment integration with Cafebazaar
- 💰 Support for in-app purchases and consumables
- 🔄 Purchase verification and consumption
- 📦 Easy integration with Capacitor projects
- ⚡ Built on Poolakey library
- 🛡️ RSA signature verification support

## Installation

```bash
npm install @salarizadi/capacitor-cafebazaar-poolakey
npx cap sync
```

## Basic Usage

```javascript
import { CafebazaarPoolakey } from '@salarizadi/capacitor-cafebazaar-poolakey';

// OR
// const { CafebazaarPoolakey } = window.Capacitor.Plugins;

// Initialize connection
await CafebazaarPoolakey.initialize({
    rsaPublicKey: 'YOUR_RSA_PUBLIC_KEY'
});

// Get products
const products = await CafebazaarPoolakey.getProducts({
    skus: ['product_id_1', 'product_id_2']
});

// Make a purchase
const result = await CafebazaarPoolakey.purchaseProduct({
    productId: 'product_id_1'
});

// Consume the purchase
if (result.state === 'PURCHASED') {
    await CafebazaarPoolakey.consumeProduct({
        token: result.purchase.purchaseToken
    });
}
```

## API Overview

- `initialize()` - Connect to Cafebazaar service
- `getProducts()` - Get product details by SKUs
- `purchaseProduct()` - Start purchase flow
- `consumeProduct()` - Consume purchased item
- `getPurchaseInfo()` - Get purchase history
- `getConnectionState()` - Check connection status
- `disconnect()` - Disconnect from service

## Documentation

For detailed documentation, examples, and best practices, please visit our [Wiki](https://github.com/salarizadi/capacitor-cafebazaar-poolakey/wiki).

## Platform Support

- ✅ Android
- ❌ iOS
- ❌ Web

## License

MIT License © 2025 [Salar Izadi](https://github.com/salarizadi)

## Acknowledgments

This plugin is built using Cafebazaar's [Poolakey](https://github.com/cafebazaar/Poolakey) library.
