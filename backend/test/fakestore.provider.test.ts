import { describe, expect, it } from 'vitest';
import { ProviderError } from '../src/domain/product.js';
import { FakeStoreProvider } from '../src/providers/FakeStoreProvider.js';

const raw = {
  id: 7, title: 'Ring', price: 9.5, description: 'Nice', category: 'jewelery',
  image: 'http://img/7.png', rating: { rate: 4.1, count: 12 },
};
const respond = (body: string, status = 200): typeof fetch =>
  (async () => new Response(body, { status })) as typeof fetch;

describe('FakeStoreProvider', () => {
  it('maps the upstream shape to our Product', async () => {
    const provider = new FakeStoreProvider('http://x', respond(JSON.stringify([raw])));
    expect(await provider.listProducts()).toEqual([
      { id: '7', title: 'Ring', description: 'Nice', category: 'jewelery', price: 9.5,
        imageUrl: 'http://img/7.png', rating: { rate: 4.1, count: 12 } },
    ]);
  });

  it('returns null for an unknown product (empty upstream body) and for non-numeric ids', async () => {
    const provider = new FakeStoreProvider('http://x', respond(''));
    expect(await provider.getProduct('123')).toBeNull();
    expect(await provider.getProduct('../etc')).toBeNull();
  });

  it('wraps upstream HTTP failures and unexpected shapes in ProviderError', async () => {
    await expect(new FakeStoreProvider('http://x', respond('oops', 500)).listProducts()).rejects.toBeInstanceOf(ProviderError);
    await expect(new FakeStoreProvider('http://x', respond('[{"id":"bad"}]')).listProducts()).rejects.toBeInstanceOf(ProviderError);
  });
});
