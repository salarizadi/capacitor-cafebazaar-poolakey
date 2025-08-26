import { registerPlugin } from '@capacitor/core';

import type { CafebazaarPoolakeyPlugin } from './definitions';

const CafebazaarPoolakey = registerPlugin<CafebazaarPoolakeyPlugin>('CafebazaarPoolakey');

export * from './definitions';
export { CafebazaarPoolakey };